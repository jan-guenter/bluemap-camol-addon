# Changelog

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
