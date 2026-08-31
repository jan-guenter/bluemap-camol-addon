/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.camol.profile.ExactCamolArtifactDetector;

import java.nio.file.Path;

/** Activates the exact Camol profile and wraps variants after every extension has baked. */
final class CamolResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final CamolRuntime runtime;
    private volatile VariantRendererCatalog catalog;

    CamolResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            CamolRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.camol.disabled")) {
            runtime.inactive("operator-disabled");
        } else if (!ExactCamolArtifactDetector.matches(roots)) {
            runtime.inactive("exact-camol-artifact-not-found");
        } else {
            runtime.activate();
        }
    }

    @Override
    public Key getBlockStateKey(Key key) {
        ensureWrapped();
        return key;
    }

    @Override
    public void getBlockProperties(BlockState blockState, BlockProperties.Builder propertiesBuilder) {
        ensureWrapped();
    }

    private VariantRendererCatalog ensureWrapped() {
        if (!runtime.active()) {
            return null;
        }
        VariantRendererCatalog found = catalog;
        if (found == null) {
            synchronized (this) {
                found = catalog;
                if (found == null) {
                    found = VariantRendererCatalog.wrap(resourcePack, renderer);
                    runtime.catalog(resourcePack, found);
                    catalog = found;
                    System.out.println("BlueMap Camol add-on active: wrapped "
                            + found.size()
                            + " resource variants for persisted camouflage overlays.");
                }
            }
        }
        return found;
    }
}
