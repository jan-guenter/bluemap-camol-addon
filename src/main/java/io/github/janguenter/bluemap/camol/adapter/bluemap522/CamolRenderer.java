/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.IdentityHashMap;
import java.util.Map;

/** Preserves the host renderer and appends Camol's persisted saved-state model. */
final class CamolRenderer implements BlockRenderer {

    private static final float SCALE = 1.005F;
    private static final float OFFSET = (1F - SCALE) / 2F;
    private static final ThreadLocal<Boolean> DELEGATING =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Boolean> HOST_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final CamolRuntime runtime;
    private final VariantRendererCatalog catalog;
    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final Map<BlockRendererType, BlockRenderer> originalRenderers = new IdentityHashMap<>();
    private final ThreadLocal<Visit> visits = ThreadLocal.withInitial(Visit::new);
    private volatile CamolAttachmentReader attachments;
    private volatile BlockStateModelRenderer stateRenderer;

    CamolRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            CamolRuntime runtime,
            VariantRendererCatalog catalog
    ) {
        this.runtime = runtime;
        this.catalog = catalog;
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int safeStart = -1;
        try {
            safeStart = target.getStart();
            renderCamol(block, variant, target, mapColor);
        } catch (Error error) {
            CamolRuntime.throwIfFatal(error);
            broadFailSoft(block, variant, target, mapColor, safeStart, error);
        }
    }

    private void renderCamol(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int hostStart = target.getStart();
        try {
            runtime.trace("host-render-enter");
            BlockRendererType originalType = catalog == null
                    ? BlockRendererType.DEFAULT
                    : catalog.original(variant);
            originalRenderers.computeIfAbsent(
                    originalType,
                    type -> type.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
            runtime.trace("host-render-complete");
        } catch (StackOverflowError error) {
            failHost(block, variant, target, mapColor, hostStart, "host-render-stack", error);
            return;
        } catch (LinkageError error) {
            failHost(block, variant, target, mapColor, hostStart, "host-render-linkage", error);
            return;
        }

        if (DELEGATING.get() || !runtime.active()) {
            return;
        }
        int overlayStart = -1;
        try {
            runtime.trace("attachment-read-enter");
            BlockState camouflage = attachmentReader().camouflageAt(block);
            runtime.trace("attachment-read-complete");
            if (camouflage == null || camouflage.isAir()
                    || !visits.get().first(block, target.getTileModel())) {
                return;
            }
            runtime.trace("camouflage-found");

            overlayStart = target.getTileModel().size();
            TileModelView overlay = new TileModelView(target.getTileModel()).initialize(overlayStart);
            DELEGATING.set(Boolean.TRUE);
            runtime.trace("overlay-state-render-enter");
            stateRenderer().render(block, camouflage, overlay, mapColor);
            runtime.trace("overlay-state-render-complete");
            overlay.initialize(overlayStart)
                    .scale(SCALE, SCALE, SCALE)
                    .translate(OFFSET, OFFSET, OFFSET);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (StackOverflowError error) {
            resetOverlay(target, overlayStart);
            runtime.failSoft("overlay-stack", error);
        } catch (LinkageError error) {
            resetOverlay(target, overlayStart);
            runtime.failSoft("overlay-linkage", error);
        } catch (RuntimeException exception) {
            resetOverlay(target, overlayStart);
            runtime.report("overlay-render-failed-" + exception.getClass().getSimpleName());
        } finally {
            DELEGATING.set(Boolean.FALSE);
        }
    }

    private void broadFailSoft(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int safeStart,
            Error error
    ) {
        runtime.failSoftMinimal("renderer-outer", error);
        if (safeStart < 0 || !resetAfterError(target, safeStart)
                || HOST_FALLBACK.get()) {
            return;
        }
        HOST_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType.DEFAULT.create(resourcePack, textures, settings)
                    .render(block, variant, target, mapColor);
        } catch (Error fallbackError) {
            CamolRuntime.throwIfFatal(fallbackError);
            runtime.failSoftMinimal("renderer-outer-default-fallback", fallbackError);
            resetAfterError(target, safeStart);
        } finally {
            HOST_FALLBACK.set(Boolean.FALSE);
            DELEGATING.set(Boolean.FALSE);
        }
    }

    private boolean resetAfterError(TileModelView target, int start) {
        try {
            target.getTileModel().reset(start);
            target.initialize(start);
            return true;
        } catch (Error resetError) {
            CamolRuntime.throwIfFatal(resetError);
            runtime.failSoftMinimal("renderer-outer-reset", resetError);
            return false;
        }
    }

    private void failHost(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int hostStart,
            String stage,
            Error error
    ) {
        target.getTileModel().reset(hostStart);
        target.initialize(hostStart);
        runtime.failSoft(stage, error);
        if (HOST_FALLBACK.get()) {
            return;
        }
        HOST_FALLBACK.set(Boolean.TRUE);
        try {
            runtime.trace("default-host-fallback-enter");
            BlockRendererType.DEFAULT.create(resourcePack, textures, settings)
                    .render(block, variant, target, mapColor);
            runtime.trace("default-host-fallback-complete");
        } catch (StackOverflowError fallbackError) {
            runtime.failSoft("default-host-fallback-stack", fallbackError);
        } catch (LinkageError fallbackError) {
            runtime.failSoft("default-host-fallback-linkage", fallbackError);
        } finally {
            HOST_FALLBACK.set(Boolean.FALSE);
        }
    }

    private CamolAttachmentReader attachmentReader() {
        CamolAttachmentReader found = attachments;
        if (found == null) {
            synchronized (this) {
                found = attachments;
                if (found == null) {
                    found = new CamolAttachmentReader(runtime);
                    attachments = found;
                }
            }
        }
        return found;
    }

    private BlockStateModelRenderer stateRenderer() {
        BlockStateModelRenderer found = stateRenderer;
        if (found == null) {
            synchronized (this) {
                found = stateRenderer;
                if (found == null) {
                    found = new BlockStateModelRenderer(resourcePack, textures, settings);
                    stateRenderer = found;
                }
            }
        }
        return found;
    }

    private static void resetOverlay(TileModelView target, int overlayStart) {
        if (overlayStart >= 0) {
            target.getTileModel().reset(overlayStart);
            target.initialize(overlayStart);
        }
    }

    /** Suppresses duplicate overlay emission for multipart host blockstates. */
    private static final class Visit {

        private TileModel tile;
        private int x = Integer.MIN_VALUE;
        private int y = Integer.MIN_VALUE;
        private int z = Integer.MIN_VALUE;

        boolean first(BlockNeighborhood block, TileModel currentTile) {
            if (tile == currentTile && x == block.getX() && y == block.getY() && z == block.getZ()) {
                return false;
            }
            tile = currentTile;
            x = block.getX();
            y = block.getY();
            z = block.getZ();
            return true;
        }
    }
}
