/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CamolRendererTest {

    @Test
    void delegatedVariantRestoresTheOriginalRendererIdentity() {
        BlockRendererType wrapper = renderer("wrapper");
        BlockRendererType original = renderer("original");
        ResourcePath<Model> model = new ResourcePath<>("test", "block/contextual");
        Variant wrapped = new Variant(model, 90, 180, 270, true, 3.5);
        wrapped.setRenderer(wrapper);

        Variant delegated = CamolRenderer.withRenderer(wrapped, original);

        assertNotSame(wrapped, delegated);
        assertSame(wrapper, wrapped.getRenderer());
        assertSame(original, delegated.getRenderer());
        assertEquals(model, delegated.getModel());
        assertEquals(90, delegated.getX());
        assertEquals(180, delegated.getY());
        assertEquals(270, delegated.getZ());
        assertTrue(delegated.isUvlock());
        assertEquals(3.5, delegated.getWeight());
    }

    private static BlockRendererType renderer(String path) {
        return new BlockRendererType.Impl(
                Key.parse("test:" + path),
                (pack, textures, settings) -> null
        );
    }
}
