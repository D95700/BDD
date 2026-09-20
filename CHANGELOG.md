# BDD Local Client — Change Log

This file records user-facing changes for the README, visual-design, and release-note teams.

## 1.2.0-test6 — repeatable OBS settings

### User-facing changes

- Added a persistent OBS settings entry to the Minecraft main menu so the OBS guide can be reopened after first launch.
- Refined that entry from a large side button into a compact red recording-dot control beside the vanilla accessibility button. Hovering it shows `OBS 设置`, keeping the action discoverable without competing with the primary Play buttons.
- Reopening the page loads the current port and password, keeps the password masked, and applies saved changes through the existing asynchronous reconnect path.
- The first-run page still offers `稍后设置`; when opened from the main menu, the secondary action becomes `返回主菜单` and does not rewrite the current settings.

### Compatibility and verification

- Compatible with Minecraft 1.20.1, Forge 47.4.10, and Java 17.
- The revised `ClientEventHandler` passes a focused Java 17 compilation against the resolved Minecraft/Forge dependencies.
- `./gradlew.bat build --init-script build/tmp/codex-direct-javac.init.gradle`: passed; the ignored, host-only init script invokes Java 17 `javac` directly to avoid this machine's JDK loopback-selector failure. Artifact: `build/libs/bddmod-1.2.0-test6.jar`.
- `./gradlew.bat runClient`: passed; the client reached the title-screen runtime, saved a client-config change, immediately reconnected the OBS monitor, and exited normally.
- The runtime verifies the settings save/reconnect path. The revised compact button placement at normal and narrow window sizes and return-without-saving behavior still require visual confirmation.

## 1.2.0-test5 — OBS first-launch setup guide

### User-facing changes

- Added a first-launch OBS setup screen that appears when the Minecraft title screen opens for the first time.
- The guide shows the localhost connection address, accepts an OBS WebSocket port, and provides a masked password field.
- Saving the guide writes the client configuration and requests a background OBS reconnect immediately; choosing `稍后设置` keeps the game usable without OBS.
- OBS monitoring remains restricted to `127.0.0.1`; no password is written to source code or logs.
- The reconnect path ignores delayed close/error callbacks from an obsolete socket so a settings change cannot clear a newer connection.

### Compatibility and verification

- Compatible with Minecraft 1.20.1, Forge 47.4.10, and Java 17.
- `./gradlew.bat compileJava`: passed.
- `./gradlew.bat build`: passed; artifact: `build/libs/bddmod-1.2.0-test5.jar`.
- `./gradlew.bat runClient`: passed for client launch, single-player lifecycle, route-test renderer execution, and persistence of the setup-completed flag.
- The setup and in-game diagnostic visuals were manually confirmed by the user as visible and satisfactory; no repository screenshot is attached yet.
- OBS connected to `127.0.0.1:4455` without a subsequent authentication rejection in the latest run, but recording-state transitions, visual guide capture, and OBS Game Capture frame separation remain unverified.
- The user confirmed that the current OBS-visible picture is not separated from the player's display; this build remains a diagnostic lifecycle test rather than a capture-separation implementation.

## 1.2.0-test1 — FBO split-route diagnostic build

### User-facing changes

- Added an opt-in-by-default recording-time test panel with clearly different green `PLAYER VIEW`
  and red `OBS TEST BUFFER` markers.
- Added an independent Forge `TextureTarget` probe that is resized with the window, bound, cleared,
  written to, and restored without changing the normal path when the test is disabled.
- Added `renderRouteTestEnabled` to the client configuration. Set it to `false` to hide the panel.

### Compatibility and verification

- The test panel is intended to make the render-route experiment immediately visible during OBS
  recording-state tests.
- This build does **not** install a native Windows `wglSwapBuffers` hook and therefore does not yet
  prove that OBS Game Capture receives a different frame from the player's display.
- FBO allocation or rendering failures disable only the probe and restore the main render target.
- `./gradlew.bat compileJava`: passed.
- `./gradlew.bat build`: passed; artifact: `build/libs/bddmod-1.2.0-test1.jar`.
- `.\gradlew.bat runClient`: passed on Minecraft 1.20.1, Forge 47.4.10, and Java 17; the client loaded, entered a single-player world, connected to the integrated server, saved, and exited normally.
- OBS recording was not active during the runtime test, so the FBO recording branch and actual OBS capture separation remain unverified.

## Diagnostic panel visibility correction

- The split-route diagnostic panel now renders whenever `renderRouteTestEnabled=true`, independent of the monitor's current recording value or the terror HUD setting.
- The panel uses an opaque high-contrast layout and reports the mod-observed OBS state and WebSocket connection state, making monitor synchronization failures visible instead of hiding the entire test.
- The FBO probe still runs only when the mod observes `OBS RECORDING`; the panel explicitly states that the native capture hook is not installed.
- If FBO initialization fails, the panel remains visible and reports the failure instead of disappearing with the probe.
- Added password-safe one-time diagnostics for GUI event execution, FBO initialization, and OBS recording-state transitions.
- Added a HOTBAR overlay fallback for the route-test panel, with per-HUD-pass de-duplication, to avoid relying on only one Forge GUI event path without making the panel disappear between frames.

## 1.2.0-test2 — OBS state synchronization correction

### User-facing changes

- The route-test panel is now rendered independently of the player/world and terror-HUD conditions, so it is visible whenever `renderRouteTestEnabled=true`.
- Corrected the OBS WebSocket 5 `GetRecordStatus` request envelope. The monitor now sends it as the required `op: 6` Request message.
- The test artifact version is now `1.2.0-test2` so it can be distinguished from the previous test build.

### Verification

- `compileJava` must be rerun after this correction.
- The previous client run did not exercise live OBS recording; this correction still requires a new run with OBS WebSocket enabled and recording.

## 1.2.0-test3 — diagnostic draw submission correction

### User-facing changes

- The high-contrast route-test panel now explicitly flushes its `GuiGraphics` batch immediately after drawing.
- This removes dependence on the vanilla GUI tail flush and is intended to make the panel visibly appear even when another overlay changes render state later in the frame.
- The artifact version is now `1.2.0-test3` to prevent confusion with earlier test JARs.

### Verification

- Requires a new client runtime test; the previous run only proved that the renderer method was invoked.

## 1.2.0-test4 — dedicated topmost diagnostic overlay

### User-facing changes

- Moved the route-test panel to a Forge `IGuiOverlay` registered above all vanilla overlays, rather than relying on the HOTBAR and generic GUI event ordering.
- The panel remains controlled by `renderRouteTestEnabled` and still reports the observed OBS state, WebSocket state, FBO state, and native-hook limitation.
- Added password-safe diagnostics for OBS protocol message errors, rejected `GetRecordStatus` responses, and socket close codes/reasons.
- The test artifact version is now `1.2.0-test4`.
- Corrected event-bus registration so the dedicated overlay is actually attached to the Forge mod event bus during client initialization.

### Verification

- `1.2.0-test3` runtime: client launched and entered a world; the renderer invocation was logged and OBS later accepted a WebSocket connection, but visible panel output and a recording-state transition were not confirmed.
- `1.2.0-test4` requires a fresh build and client run.

## OBS authentication diagnostics

### User-facing changes

- OBS WebSocket authentication failures (`4009`) are now reported as `AUTH FAILED` in the diagnostic panel instead of appearing identical to an unavailable OBS server.
- Authentication failures retry in the background every 30 seconds rather than reconnecting every 5 seconds and flooding the log.
- The log reports only that authentication failed and points to `obsWebSocketPassword`; the configured password is never logged.

### Verification

- The latest development-client run reached OBS on `127.0.0.1:4455` but was rejected with `4009 Authentication failed`, confirming a password/configuration mismatch rather than a port failure.

## Repository workflow — GitHub author attribution rule

### Documentation changes

- Added a repository rule requiring Forge `mod_authors` values to use contributors' GitHub names directly.
- Multiple contributors must be separated with English commas, for example `D95700,AnotherAuthor`.

## Metadata update — GitHub author attribution

### User-facing changes

- Updated the Forge mod metadata author field to `D95700`, matching the GitHub account shown in the repository's commit history.
- Future contributors will be listed using their GitHub names, separated by commas when there are multiple contributors.

### Verification

- Repository runtime test record: `./gradlew.bat runClient` passed on Minecraft 1.20.1, Forge 47.4.10, and Java 17.
- OBS live recording transitions, hidden audio output, and multiplayer compatibility remain outside the recorded test scope.

## Runtime test record — 2026-09-20

### Verification

- Added `TESTLOG.md` with the Forge client runtime test result and verification scope.
- `./gradlew.bat runClient`: passed; the client loaded `bddmod`, entered a single-player world, connected to the integrated server, and exited with world data saved.
- No crash or fatal mod-loading error was observed during the run.
- The test log records remaining warnings and features not covered by this run.

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
