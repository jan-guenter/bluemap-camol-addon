/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import io.github.janguenter.bluemap.camol.profile.ExactCamolArtifactDetector;

import java.nio.file.Path;

/** Activates the exact Camol profile and wraps loaded variants without replacing them. */
final class CamolResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final CamolRuntime runtime;

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
    public void bake() {
        if (!runtime.active()) {
            return;
        }
        VariantRendererCatalog catalog = VariantRendererCatalog.wrap(resourcePack, renderer);
        runtime.catalog(resourcePack, catalog);
        System.out.println("BlueMap Camol add-on active: wrapped "
                + catalog.size() + " resource variants for persisted camouflage overlays.");
    }
}
