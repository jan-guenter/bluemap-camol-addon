# Agent guide for BlueMap Camol Add-on

Read `/root/work/allthemons/AGENTS.md` before changing this standalone project.
It is a BlueMap add-on, not a NeoForge mod.

The only supported profile is All the Mons `1.2.0`, Minecraft `1.21.1`,
NeoForge `21.1.248`, BlueMap feature backport
`5.22-feature.backport-5.23-stateless-java-web-server-46` at commit
`7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` with API commit
`285c9a60eff3ac2b0cab308ce1058d1565be0971`, and the exact 62,188-byte Camol
`1.21.1-0.3.1` JAR whose SHA-256 is
`aafdbe962a4bbab97207f747ec52561ea34be9c49a4b044a835da82ff7d45609`.
Release `0.1.0-alpha.1` is 32,265 bytes with SHA-256
`b107291040c1816e209929db37bc2871e25b9b7ab128ec2cc58f1cceaf47db53`.
The owner-accepted `0.1.0-alpha.3` release candidate is 72,737 bytes with
SHA-256
`afd10b6b7f072fb4194782f4274dfad78732cb4230772f64620c1bb4a46bceba`.
It migrates the BlueMap adapter boundary and restores original renderer
identity during host delegation. It compiles the four Adapter API
`0.1.0-alpha.2` sources from commit
`e81f08bc4bfbf02d810ec8949a019130e2e61634` and source tree
`2f974c9bb2ba13888d69682f86f30f58922d30eb`.

Preserve stock host rendering and append only the saved camouflage model.
`normal` and `solid` have the same appearance. Unknown, malformed, absent, or
AIR attachment values must retain stock BlueMap rendering. Never bundle Camol,
Minecraft, models, textures, source, worlds, or private fixtures.

Camol must install its global renderer wrapper only after all resource-pack
extensions have baked. Use the first block-state or block-property callback as
that boundary, keep initialization synchronized and idempotent, and preserve
the final renderer assigned by another add-on as the original renderer. Pass a
variant carrying that original renderer identity to the delegated renderer.

Use Java 21 and initialize both pinned submodules. Run Gradle 9.6.1 with the
exact `camolJar` and `bluemapSourcePath` properties through `prototypeCheck`,
`build`, POM generation, and module-metadata generation.
Version `0.1.0-alpha.3` was accepted on 2026-08-31 in the combined BlueMap
5.23 staging view and passed the final 51-gallery aggregate runtime suite.
Release still requires the sealed `verifyReleaseCandidate` gate and a reviewed
pull request.
