/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class CamolResourceExtensionTest {

    private static final CamolRuntime RUNTIME = CamolRuntime.INSTANCE;

    @Test
    void firstBlockStateLookupCapturesRendererAssignedByLaterBake() throws Exception {
        Fixture fixture = fixture("block-state");

        fixture.extension().bake();
        fixture.variant().setRenderer(fixture.lateRenderer());
        fixture.extension().getBlockStateKey(Key.parse("test:block"));

        assertWrappedAroundLateRenderer(fixture);
        VariantRendererCatalog catalog = RUNTIME.catalog(fixture.pack());

        fixture.extension().getBlockStateKey(Key.parse("test:block"));
        assertSame(catalog, RUNTIME.catalog(fixture.pack()));
        assertWrappedAroundLateRenderer(fixture);
    }

    @Test
    void firstPropertyLookupCapturesRendererAssignedByLaterBake() throws Exception {
        Fixture fixture = fixture("properties");

        fixture.extension().bake();
        fixture.variant().setRenderer(fixture.lateRenderer());
        fixture.extension().getBlockProperties(
                de.bluecolored.bluemap.core.world.BlockState.AIR,
                de.bluecolored.bluemap.core.world.BlockProperties.builder()
        );

        assertWrappedAroundLateRenderer(fixture);
    }

    private static void assertWrappedAroundLateRenderer(Fixture fixture) {
        assertSame(fixture.wrapper(), fixture.variant().getRenderer());
        assertSame(
                fixture.lateRenderer(),
                RUNTIME.catalog(fixture.pack()).original(fixture.variant())
        );
    }

    private static Fixture fixture(String suffix) {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        Variant variant = new Variant(new ResourcePath<Model>("test", "block/model"));
        pack.getBlockStates().put(
                Key.parse("test:block"),
                new BlockState(new Variants(new VariantSet[0], new VariantSet(variant)))
        );

        BlockRendererType wrapper = renderer("wrapper-" + suffix);
        BlockRendererType lateRenderer = renderer("late-" + suffix);
        RUNTIME.activate();
        CamolResourceExtension extension = new CamolResourceExtension(pack, wrapper, RUNTIME);
        return new Fixture(pack, variant, wrapper, lateRenderer, extension);
    }

    private static BlockRendererType renderer(String path) {
        return new BlockRendererType.Impl(
                Key.parse("test:" + path),
                (pack, textures, settings) -> null
        );
    }

    private record Fixture(
            ResourcePack pack,
            Variant variant,
            BlockRendererType wrapper,
            BlockRendererType lateRenderer,
            CamolResourceExtension extension
    ) {
    }
}
