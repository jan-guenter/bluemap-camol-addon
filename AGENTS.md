# Agent guide for BlueMap Camol Add-on

Read `/root/work/allthemons/AGENTS.md` before changing this standalone project.
It is a BlueMap add-on, not a NeoForge mod.

The only supported profile is All the Mons `1.2.0`, Minecraft `1.21.1`,
NeoForge `21.1.248`, BlueMap backport
`5.22-agent.backport-5.22-mc1.21.1-2`, and the exact 62,188-byte Camol
`1.21.1-0.3.1` JAR whose SHA-256 is
`aafdbe962a4bbab97207f747ec52561ea34be9c49a4b044a835da82ff7d45609`.
Release `0.1.0-alpha.1` is 32,265 bytes with SHA-256
`b107291040c1816e209929db37bc2871e25b9b7ab128ec2cc58f1cceaf47db53`.
The `0.1.0-alpha.2` release candidate is 32,587 bytes with SHA-256
`2c707f0cb1e8ebbef735f3afeae818e9154029a4d3892c58a737ae75891d197b`.

Preserve stock host rendering and append only the saved camouflage model.
`normal` and `solid` have the same appearance. Unknown, malformed, absent, or
AIR attachment values must retain stock BlueMap rendering. Never bundle Camol,
Minecraft, models, textures, source, worlds, or private fixtures.

Camol must install its global renderer wrapper only after all resource-pack
extensions have baked. Use the first block-state or block-property callback as
that boundary, keep initialization synchronized and idempotent, and preserve
the final renderer assigned by another add-on as the original renderer.

Use Java 21 and run `gradle --no-daemon clean check build
generatePomFileForAddonPublication generateMetadataFileForAddonPublication`.
Owner visual acceptance is required before release. Version `0.1.0-alpha.1`
was accepted on 2026-08-18; `0.1.0-alpha.2` still requires aggregate runtime
acceptance.
