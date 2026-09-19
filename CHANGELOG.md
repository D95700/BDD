# BDD Local Client — Change Log

This file records user-facing changes for the README, visual-design, and release-note teams.

## Documentation sync — v1.1.0 README

### User-facing changes

- Updated the README to describe the released OBS WebSocket 5 monitoring behavior instead of the retired HTTP availability check.
- Added current installation, password configuration, reconnect behavior, privacy notes, and v1.1.0 verification guidance.

### Verification

- Documentation updated against the existing v1.1.0 release entry below.

## v1.1.0 — OBS WebSocket detection

### User-facing changes

- Replaced the temporary HTTP availability check with a real OBS WebSocket 5 connection on `127.0.0.1:<obsPort>`.
- The HUD now receives recording start/stop changes from OBS events instead of inferring state from an HTTP response body.
- The mod authenticates with the configured OBS WebSocket password when OBS requires one.
- On connection, the mod requests the current recording state so the HUD can recover correctly even when Minecraft starts after OBS is already recording.
- If OBS is closed, authentication fails, or the connection drops, the mod returns to `OBS STANDBY` and retries in the background.

### Compatibility and security

- OBS monitoring remains localhost-only and does not send data to external services.
- The implementation uses Java 17's built-in WebSocket client, so the distributable JAR does not require extra OBS/Jetty runtime libraries.
- The password is used only to calculate the OBS WebSocket authentication response and is not logged.

### Verification

- `./gradlew compileJava`: passed.
- `./gradlew build`: passed.
- Release artifact: `build/libs/bddmod-1.1.0.jar`.

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

### Verification

- `./gradlew compileJava`: passed.
- `./gradlew build`: passed.
- Release artifact: `build/libs/bddmod-1.0.0.jar`.

## Documentation workflow

For every future code or resource change, add a new dated or versioned section above this line. Include the visible player-facing result, configuration/install impact, limitations, and the verification command used. This file is intended to be reused directly when preparing the project README and release notes.
