# Add-on execution

Initialize the two pinned source dependencies before any Gradle gate:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
```

The settings preflight pins Adapter API `0.1.0-alpha.2` at commit
`e81f08bc4bfbf02d810ec8949a019130e2e61634` and Java source tree
`2f974c9bb2ba13888d69682f86f30f58922d30eb`. Gradle compiles its four Java
sources directly. The standalone Adapter API JAR is not a runtime dependency.

## Prototype

Acquire the exact 62,188-byte Camol `1.21.1-0.3.1` JAR outside Git and verify
SHA-256
`aafdbe962a4bbab97207f747ec52561ea34be9c49a4b044a835da82ff7d45609`.
Then run with Gradle 9.6.1:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/absolute/path/to/exact/bluemap-backport \
  -PcamolJar=/absolute/path/to/camol-1.21.1-0.3.1.jar \
  clean prototypeCheck build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

Deploy only the candidate JAR and unchanged Camol gallery to disposable
combined staging. Compare the saved camouflage blocks with the client before
accepting the candidate.

## Acceptance and release

After owner acceptance, replace every `PENDING` artifact identity in
`gradle.properties` and `provenance/release.json` with a reproducible Gradle
9.6.1 promotion build. Record the accepted staging commit, tree, JAR, and
visual acceptance. Then run:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/absolute/path/to/exact/bluemap-backport \
  -PcamolJar=/absolute/path/to/camol-1.21.1-0.3.1.jar \
  -PreleaseTag=v0.1.0-alpha.3 \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge through a pull request and create an annotated tag at the reviewed
commit. Publication does not deploy to a Minecraft server.
