# BDD Local Client — Change Log

This file records user-facing changes for the README, visual-design, and release-note teams.

## v1.0.0 — Initial playable release

### User-facing changes

- Added the first playable client-side BDD experience for Minecraft Forge 1.20.1.
- Added a compact in-game HUD showing:
  - `BDD // OBS STANDBY` when OBS recording is not detected.
  - `BDD // OBS RECORDING` when a local OBS recording endpoint is detected.
  - Session coverage time and basic client session statistics.
- Added a subtle pulsing red border around the game view while OBS recording is detected.
- Added a client configuration option to disable the basic visual/HUD layer when desired.
- Added safe fallback behavior when OBS is not running, OBS cannot be reached, or the configured endpoint does not respond.
- Added virtual-audio-device output support as a foundation for future hidden-audio content. The mod does not switch to the default speakers when no matching virtual device is available.

### Runtime and compatibility

- Target versions: Minecraft 1.20.1, Forge 47.4.10, Java 17.
- Client-side only; no server installation is required.
- Install `bddmod-1.0.0.jar` in the Minecraft `mods` directory.
- OBS monitoring uses localhost only; no external service or personal data upload is performed.

### Current limitations

- FBO-based OBS/player view separation is not included yet.
- Custom Mixin player distortion, shaders, hidden comments, and bundled audio assets are reserved for later releases.
- The current OBS monitor uses a lightweight localhost HTTP availability/status check rather than the full OBS WebSocket event integration described in the design document.

### Verification

- `./gradlew compileJava`: passed.
- `./gradlew build`: passed.
- Release artifact: `build/libs/bddmod-1.0.0.jar`.

## Documentation workflow

For every future code or resource change, add a new dated or versioned section above this line. Include the visible player-facing result, configuration/install impact, limitations, and the verification command used. This file is intended to be reused directly when preparing the project README and release notes.
