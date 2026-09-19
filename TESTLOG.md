# BDD Mod Test Log

## Test Information

- Test date: 2026-09-20
- Test type: Forge development client runtime test
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `./gradlew.bat runClient`
- Result: PASS

## Verified Behaviors

- Forge client started successfully.
- `bddmod` loaded successfully with Minecraft and Forge.
- Client resources and mod resources completed their reload without a crash.
- A single-player world was created and loaded successfully.
- The integrated server started successfully.
- The client connected to the integrated server with the mod list `minecraft`, `forge`, and `bddmod`.
- World generation completed and the player entered the world.
- The integrated server and world data shut down and saved successfully when the client exited.
- Gradle completed with `BUILD SUCCESSFUL`.

## Observations

- First-run configuration files were created or corrected automatically in `run/config/` and the test world's server configuration directory.
- Forge reported the client configuration defaults for `bddmod`, including terror mode, hidden audio volume, OBS WebSocket settings, and virtual audio device settings.
- The game log reported missing vanilla Goat Horn sound events.
- The game log reported a missing `Sampler2` sampler in the vanilla `rendertype_entity_translucent_emissive` shader.
- Realms authorization was unavailable in the development environment; this did not prevent local play.
- The console displayed garbled Chinese characters for some world and game status messages due to the current console encoding. This did not affect runtime behavior.

## Not Covered

- Individual BDD gameplay features were not exhaustively tested.
- OBS WebSocket recording-state transitions were not tested against a live OBS instance in this run.
- Hidden audio playback and virtual audio-device output were not independently verified.
- Multiplayer or dedicated-server compatibility was not tested.

## Evidence

- Gradle task output: `BUILD SUCCESSFUL in 5m 20s`
- Runtime log: `run/logs/latest.log`
- Test world created during the run: `run/saves/新的世界/`
