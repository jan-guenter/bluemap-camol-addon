# Changelog

## 0.1.0-alpha.3 - 2026-08-31

- Target only the pinned BlueMap 5.23 feature backport and API commits.
- Move the local adapter boundary from `bluemap522` to `bluemap523`.
- Compile the four pinned Adapter API `0.1.0-alpha.2` sources and remove the
  duplicate compatibility, extension-factory, and registry helpers.
- Restore the original renderer identity on delegated variants so Camol stays
  compatible with identity-sensitive renderers from other add-ons.
- Keep persisted attachment decoding, post-bake wrapping, Camol overlay
  geometry, gallery sources, and stock fallback unchanged.

## 0.1.0-alpha.2 - 2026-08-28

- Defer the global variant wrap until BlueMap's first post-load block-state or
  block-property lookup.
- Preserve renderer identities assigned by resource-pack extensions that bake
  after Camol, including aggregate PneumaticCraft, Extreme Reactors, More Red,
  and Crystalix routes.
- Keep the Camol renderer outermost with synchronized, one-time catalog
  initialization.
- Add regression tests for both post-load callback paths and repeated lookup.

## 0.1.0-alpha.1 - 2026-08-18

- Preserve each host block's ordinary model and overlay its saved Camol block
  state at Camol's `1.005` scale.
- Read modern and legacy persisted camouflage directly from chunk data.
- Render `normal` and `solid` camouflage identically while leaving collision
  behavior out of BlueMap's static view.
- Fall back to stock BlueMap rendering for absent, malformed, unsupported, or
  AIR camouflage data.
- Pass disposable full-pack staging and owner visual acceptance.
