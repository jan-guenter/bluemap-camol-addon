/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;

/** Resource-pack extension factory registered before resource loading. */
final class CamolResourceExtensionType implements ResourcePack.Extension<CamolResourceExtension> {

    private static final Key KEY = Key.parse("bluemap_camol:prototype");

    private final BlockRendererType renderer;
    private final CamolRuntime runtime;

    CamolResourceExtensionType(BlockRendererType renderer, CamolRuntime runtime) {
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public CamolResourceExtension create(ResourcePack pack) {
        return new CamolResourceExtension(pack, renderer, runtime);
    }
}
