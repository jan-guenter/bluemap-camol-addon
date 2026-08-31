# BlueMap Camol Add-on

[![CI](https://github.com/jan-guenter/bluemap-camol-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-camol-addon/actions/workflows/ci.yml)

A small exact-profile BlueMap 5.23 feature-backport add-on for Camol's
persisted camouflage.

## Status and compatibility

Version `0.1.0-alpha.3` is an unpublished BlueMap 5.23 migration candidate.
It keeps the `0.1.0-alpha.2` renderer and post-bake wrapping behavior while
moving only the internal adapter boundary and shared bootstrap helpers.
Compatibility outside the exact inputs below is not asserted. Version
`0.1.0-alpha.1` remains the latest owner-accepted release.

## Visual scope

The add-on targets only:

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java 21;
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46` at commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` with API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`;
- Camol `1.21.1-0.3.1`, exact JAR SHA-256
  `aafdbe962a4bbab97207f747ec52561ea34be9c49a4b044a835da82ff7d45609`.

Camol saves camouflage as a NeoForge chunk attachment rather than a block
entity. The add-on reads that attachment directly from the same Anvil region
file BlueMap is rendering, preserves the ordinary host model, then overlays
the saved camouflage block model at Camol's `1.005` scale. `normal` and
`solid` affect collision only and intentionally render identically.

BlueMap does not define an order for resource-pack extension `bake()` calls.
Camol therefore waits for the first block-state or block-property lookup,
after the bake phase, before it wraps variants. This preserves custom renderer
choices made by the other add-ons while keeping the Camol overlay outermost.

Missing Camol, a different artifact, malformed data, AIR tombstones, or a
region read race leaves stock BlueMap rendering unchanged. The add-on writes
nothing to the world.

## Build and verification

Clone with submodules so the exact reviewed build convention is available:

```bash
git clone --recurse-submodules \
  https://github.com/jan-guenter/bluemap-camol-addon.git
```

For an existing checkout, run `git submodule update --init --recursive`. The
build rejects an uninitialized, dirty, incorrectly pinned, or source-tree
mismatched toolkit or Adapter API checkout.

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/path/to/exact/bluemap-backport \
  -PcamolJar=/path/to/camol-1.21.1-0.3.1.jar \
  clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

`check` validates the production and sources archive boundaries.
`prototypeCheck` also verifies the exact Camol artifact and gallery. The two
archives compile the four pinned Adapter API sources directly and never nest
its standalone JAR. Tagged releases publish production/source JARs, POM,
Gradle module metadata, and checksums on GitHub Releases and Maven coordinates
`io.github.jan-guenter:bluemap-camol-addon:<version>` on GitHub Packages.

## Installation

Place `build/libs/bluemap-camol-addon-0.1.0-alpha.3.jar` in
`config/bluemap/packs` and restart BlueMap. Keep the exact Camol JAR available
to BlueMap's resource-pack scan. Do not place this add-on in `mods`.

The minimal gallery datapack builds host controls and gives a player tuned
Camol items. A player must right-click the indicated hosts because Minecraft
commands cannot write NeoForge chunk attachments.

## License and provenance

Independent code is released under the [MIT License](LICENSE). Camol is All
Rights Reserved and is not bundled. Models and textures always come from
operator-installed packs. See [NOTICE.md](NOTICE.md),
[THIRD_PARTY.md](THIRD_PARTY.md), and
[provenance/upstreams.json](provenance/upstreams.json).
