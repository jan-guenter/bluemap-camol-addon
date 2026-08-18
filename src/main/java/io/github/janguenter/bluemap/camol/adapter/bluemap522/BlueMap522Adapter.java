/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;

/** BlueMap 5.22 internal ABI registration boundary. */
public final class BlueMap522Adapter {

    private static final CamolRuntime RUNTIME = CamolRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            de.bluecolored.bluemap.core.util.Key.parse("bluemap_camol:overlay"),
            BlueMap522Adapter::createRenderer
    );
    private static final ResourcePack.Extension<CamolResourceExtension> EXTENSION =
            new CamolResourceExtensionType(RENDERER, RUNTIME);

    private BlueMap522Adapter() {
    }

    public static synchronized boolean install() {
        if (!canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.inactive("registry-collision");
            return false;
        }
        boolean installed = register(BlockRendererType.REGISTRY, RENDERER)
                && register(ResourcePack.Extension.REGISTRY, EXTENSION);
        if (!installed) {
            RUNTIME.inactive("registry-collision");
        }
        return installed;
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery gallery,
            RenderSettings settings
    ) {
        RUNTIME.trace("renderer-construction-enter");
        try {
            BlockRenderer renderer = new CamolRenderer(
                    pack,
                    gallery,
                    settings,
                    RUNTIME,
                    RUNTIME.catalog(pack)
            );
            RUNTIME.trace("renderer-construction-complete");
            return renderer;
        } catch (Error error) {
            CamolRuntime.throwIfFatal(error);
            RUNTIME.failSoftMinimal("renderer-construction", error);
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        }
    }

    private static <T extends Keyed> boolean canRegister(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        return existing == null || existing == candidate;
    }

    private static <T extends Keyed> boolean register(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        if (existing == null) {
            registry.register(candidate);
            existing = registry.get(candidate.getKey());
        }
        return existing == candidate;
    }
}
