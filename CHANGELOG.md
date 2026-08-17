# Changelog

## 0.1.0-alpha.1 - 2026-08-18

- Preserve each host block's ordinary model and overlay its saved Camol block
  state at Camol's `1.005` scale.
- Read modern and legacy persisted camouflage directly from chunk data.
- Render `normal` and `solid` camouflage identically while leaving collision
  behavior out of BlueMap's static view.
- Fall back to stock BlueMap rendering for absent, malformed, unsupported, or
  AIR camouflage data.
- Pass disposable full-pack staging and owner visual acceptance.
