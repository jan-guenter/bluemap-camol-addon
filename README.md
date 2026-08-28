# BlueMap Camol Add-on

[![CI](https://github.com/jan-guenter/bluemap-camol-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-camol-addon/actions/workflows/ci.yml)

A small exact-profile BlueMap 5.22 add-on for Camol's persisted camouflage.

## Status and compatibility

Version `0.1.0-alpha.2` is the aggregate-test release candidate for this exact
environment. Its production JAR is 32,587 bytes with SHA-256
`2c707f0cb1e8ebbef735f3afeae818e9154029a4d3892c58a737ae75891d197b`.
Compatibility outside these inputs is not asserted. Version `0.1.0-alpha.1`
remains the latest owner-accepted release until aggregate testing finishes.

## Visual scope

The add-on targets only:

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java 21;
- BlueMap backport `5.22-agent.backport-5.22-mc1.21.1-2` at commit
  `9be321df995a1103808621d529eb72773e719d4d`;
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

```bash
gradle --no-daemon clean check build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

`check` rejects any production JAR that differs from the recorded candidate
size or SHA-256. Tagged releases publish production/source JARs, POM, Gradle module
metadata, and checksums on GitHub Releases and Maven coordinates
`io.github.jan-guenter:bluemap-camol-addon:<version>` on GitHub Packages.

## Installation

Place `build/libs/bluemap-camol-addon-0.1.0-alpha.2.jar` in
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
