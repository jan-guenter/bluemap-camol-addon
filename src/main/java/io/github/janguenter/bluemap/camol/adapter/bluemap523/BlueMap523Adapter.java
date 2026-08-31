/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final CamolRuntime RUNTIME = CamolRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            de.bluecolored.bluemap.core.util.Key.parse("bluemap_camol:overlay"),
            BlueMap523Adapter::createRenderer
    );
    private static final Key EXTENSION_KEY = Key.parse("bluemap_camol:prototype");
    private static final ResourcePack.Extension<CamolResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    EXTENSION_KEY,
                    pack -> new CamolResourceExtension(pack, RENDERER, RUNTIME)
            );

    private BlueMap523Adapter() {
    }

    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.inactive("registry-collision");
            return false;
        }
        boolean installed = RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                && RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION);
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

}
