# BDD Local Client — Change Log

This file records user-facing changes for the README, visual-design, and release-note teams.

## Unreleased — post-release README synchronization policy

### Project workflow changes

- README updates now occur only after a stable-version commit has been pushed and its GitHub Release and installable assets have been published and verified.
- Development builds, prereleases, and roadmap micro-steps no longer change the README; it continues to describe the latest published stable release.
- Each stable release now receives a separate post-release README documentation commit so its badge, download filename, guidance, summary, configuration notes, and roadmap status can be checked against the release that actually exists on GitHub.

## Unreleased — SemVer 2.0.0 version naming

### Project workflow changes

- Replaced the project-specific development suffix rules with the SemVer 2.0.0 format effective immediately for the next version change.
- Roadmap micro-steps now use valid prereleases such as `1.4.0-alpha.1`, stable releases omit the prerelease suffix, and compatible bug fixes use PATCH increments.
- Added SemVer rules for `MAJOR.MINOR.PATCH`, hyphenated prereleases, optional plus-prefixed build metadata, numeric identifiers without leading zeroes, and version precedence.
- Kept all existing `1.3.0.dev*` and earlier versions as immutable historical artifacts; no existing tag, release, or JAR is renamed.

## 1.4.0-alpha.1 — automated build and artifact verification

### User-facing changes

- Added continuous build verification for pull requests and pushes to `main`.
- Installable `-all.jar` artifacts are checked automatically before they are retained as CI artifacts.

### Configuration and compatibility

- No gameplay or configuration behavior changed in this prerelease.
- `README.md` remains unchanged because this is a development prerelease.

### Technical behavior and verification

- GitHub Actions now runs `compileJava`, `processResources`, `build`, and the standard-library artifact verifier on Java 17.
- The verifier checks SemVer metadata, embedded mod version, WTFPL license, bundled `nv-websocket-client-2.14`, required resources, and SHA-256 output.
- Runtime screenshots remain a manual client-test responsibility and must be recorded in `TESTLOG.md`; CI does not require a graphical desktop or live OBS instance.

## 1.4.0-alpha.2 — Iris/Oculus audience-route fallback

### User-facing changes

- Added a compatibility fallback for Iris and Oculus shader loaders.
- When either loader is present, the mod keeps the normal player renderer active and disables only the independently capturable audience FBO/window route instead of risking a conflicting OpenGL pipeline.

### Configuration and compatibility

- No new configuration key or installation step is required.
- The fallback is detected automatically from the loaded Forge mod list for `iris` and `oculus`.
- `README.md` remains unchanged because this is a development prerelease.

### Technical behavior and verification

- Detection is cached after the first client render and reported in the log; the optional route diagnostic panel identifies the fallback loader.
- Build and source-level compatibility checks are recorded in `TESTLOG.md`; a live Iris/Oculus runtime session remains unverified in this environment.

## 1.4.0-alpha.3 — loader-neutral final framebuffer capture

### User-facing changes

- The audience output now captures the framebuffer that is actually presenting the final Minecraft frame, including a shader-loader-managed framebuffer when available.
- Iris/Oculus detection no longer disables the audience window by itself; the player view remains unchanged while the route reports its capability state in the diagnostic panel.

### Configuration and compatibility

- No new configuration key or installation step is required.
- The implementation has no compile-time Iris or Oculus dependency. Unsupported loader APIs or OpenGL failures enter a safe audience-only fallback.
- `README.md` remains unchanged because this is a development prerelease.

### Technical behavior and verification

- The capture path preserves and restores framebuffer bindings, viewport, and scissor state around the blit.
- The diagnostic panel now reports loader status, the current capture source, and the last audience FBO failure.
- Added a manually gated stable-release workflow and a multi-artifact verifier; prerelease CI artifacts are retained for the eventual `1.4.0` asset bundle.
- Vanilla and active Oculus shader-pack runtime verification remain beta acceptance gates; this alpha records build and source-level verification only.

## 1.3.0 — stable release

### User-facing changes

- Promoted the recording experience to the `1.3.0` stable release after the alpha verification sequence.
- Recording mode keeps synchronized heartbeat audio, visual pulse and dark-red vein effects, audience-window rendering, and randomized audience head distortion effects.
- Breathing audio remains removed; the recording audio volume setting controls heartbeat audio only.

### Configuration and compatibility

- No new configuration key or installation step is required for this release.
- The release is built for Minecraft `1.20.1` on Forge `47.4.10` with Java 17 bytecode compatibility.
- `README.md` is updated only after this stable commit and GitHub Release are published, following the repository release workflow.

### Technical behavior and verification

- Stable version metadata, generated resources, changelog heading, test record, tag, release title, and installable JAR name are synchronized to `1.3.0`.
- The full Gradle build and packaged metadata verification are recorded in `TESTLOG.md`.

## 1.3.0-alpha.6 — remove breathing audio

### User-facing changes

- Removed the in-game breathing sound from recording mode.
- Recording mode now keeps only the heartbeat audio while preserving the synchronized visual pulse, dark-red veins, and audience effects.

### Configuration and compatibility

- `recordingAudioVolume` now controls the heartbeat sound only; the existing configuration key remains compatible.
- Removed the unused breathing sound asset and registry entry. No new installation step is required.
- Version advanced to `1.3.0-alpha.6`; `README.md` remains unchanged under the post-release synchronization policy.

### Technical behavior and verification

- Removed breathing playback and cleanup from the client sound controller while retaining heartbeat phase detection and visual breath envelopes.
- Build and resource verification are recorded in `TESTLOG.md`.

## 1.3.0-alpha.5 — visible audience effects and distinct breathing cadence

### User-facing changes

- Fixed the audience head treatments being effectively invisible in recorded footage: each randomly ordered mode now remains stable for two seconds, uses a larger head mask, and applies visibly stronger mosaic, radial distortion, or deformation.
- Kept close third-person head views eligible for processing instead of rejecting them when the projected head occupies a large part of the frame.
- Replaced the continuous wind-like breathing bed with a single inhale, pause, exhale, and silent-rest phrase played once per shared three-second pulse cycle.
- Increased the audience-only localized distortion, grain, red edge tint, and hidden-message visibility enough to support visual acceptance without adding them to the player's Minecraft window.

### Configuration and compatibility

- Existing OBS, terror-mode, and recording-audio settings remain compatible; no new configuration key is required.
- Version advanced to `1.3.0-alpha.5`; `README.md` remains unchanged under the post-release synchronization policy.

### Technical behavior and verification

- Random head modes are shuffled in groups of three, guaranteeing every treatment appears during each six-second sequence while avoiding frame-by-frame flicker.
- Real OBS footage from the failed alpha.4 acceptance run was decoded and inspected to confirm the route contained valid game frames but did not show a usable head treatment.
- Build and runtime verification are recorded in `TESTLOG.md`.

## 1.3.0-alpha.4 — randomized audience-only player-head effects

### User-facing changes

- When the local player's head is rendered in third-person view during recording, the audience output now randomly applies one of three head-localized treatments on every render: mosaic, radial distortion, or deformation.
- The effect follows the projected head position and apparent size as the camera moves instead of using a fixed screen location.
- Head treatments remain exclusive to `BDD Audience Output`; the player's Minecraft window is unchanged.
- Effects are skipped in first-person view, where Minecraft does not render the local player's head, and whenever the projected head is behind the camera or outside a safe visible range.

### Technical behavior and verification

- Projects the interpolated local-player eye position and camera-relative head extents into audience texture coordinates from Forge's player render event.
- Keeps the three effects inside a feathered elliptical head mask and guards inactive uniforms against invalid shader coordinates.
- Version advanced to `1.3.0-alpha.4`; build and runtime verification are recorded in `TESTLOG.md`.
- `README.md` remains unchanged under the post-release synchronization policy.

## 1.3.0-alpha.3 — automatic OBS audience capture routing

### User-facing changes

- The OBS recording source should now capture `BDD Audience Output`, not the player's Minecraft window, so audience-only distortion, grain, tint, and hidden text reach the final video.
- While OBS is connected in a world, the audience window stays rendered off-screen and does not request focus, while remaining available to OBS Game Capture or Window Capture.
- Once OBS is connected and the audience window is ready, the mod finds the enabled game/window capture source in the current program scene and switches its window target to `BDD Audience Output` automatically.
- The source remains unchanged while terror mode is disabled or the audience window cannot be opened, avoiding an automatic switch to a missing target.
- If no capture source exists, or multiple sources cannot be narrowed to exactly one Minecraft source, the mod leaves the scene unchanged and records a warning instead of modifying an arbitrary source.

### Configuration and compatibility

- Added `autoRouteAudienceCapture`, enabled by default, to allow the automatic OBS source update to be disabled in `bddmod-client.toml`.
- `renderRouteTestEnabled` now controls only the player-visible diagnostic panel; the audience output itself runs automatically while OBS is connected and terror mode is enabled.
- The routing workflow updates only the selected source's `window` setting and preserves its other Game Capture or Window Capture settings.

### Technical behavior and verification

- Uses OBS WebSocket 5 requests for the current program scene, scene items, input settings, and the final input-settings overlay update.
- Version advanced to `1.3.0-alpha.3`; build and runtime/protocol verification are recorded in `TESTLOG.md`.
- `README.md` remains unchanged under the post-release synchronization policy.

## 1.3.0-alpha.2 — hidden audience message

### User-facing changes

- Added the audience-only message `WE SEE YOU`, which briefly fades into the lower-right area of `BDD Audience Output` during recording.
- The message follows the synchronized recording pulse and remains absent from the player's Minecraft window.
- Kept the message small, dark red, and time-limited so it reads as a discoverable audience detail rather than a persistent obstruction.

### Technical behavior and verification

- Generates a compact 5x7 glyph texture at runtime and samples it only in the separate audience-window fragment shader; no external image or font dependency is added.
- Version advanced to the SemVer prerelease `1.3.0-alpha.2`; build and runtime verification are recorded in `TESTLOG.md`.
- `README.md` remains unchanged under the post-release synchronization policy.

## 1.3.0-alpha.1 — audience-only localized distortion

### User-facing changes

- Added a small, slowly drifting distortion region to the `BDD Audience Output` window while OBS recording is active.
- The distortion follows the shared breathing and heartbeat pulse, remains restrained to a localized area, and does not affect the player's Minecraft window.
- Retained the existing audience-only grain and dark-red edge tint; hidden audience text remains a later independent roadmap step.

### Technical behavior and verification

- The audience fragment shader now warps texture coordinates inside a feathered elliptical mask before applying the existing grain and tint pass.
- Versioning switches immediately from the historical `1.3.0.dev*` format to the SemVer prerelease `1.3.0-alpha.1`.
- Build and runtime verification are recorded in `TESTLOG.md`; `README.md` remains unchanged under the post-release synchronization policy.

## 1.3.0.dev3 — audience window orientation fix

### User-facing changes

- Fixed the `BDD Audience Output` window being displayed upside down; its image orientation now matches the player's Minecraft window.
- Retained the dev2 audience-only grain and dark-red edge tint without changing the player's main view.

### Technical behavior and verification

- Corrected the fullscreen quad's vertical texture coordinates for Minecraft's RenderTarget texture orientation.
- Development version advanced to `1.3.0.dev3`; build and runtime verification are recorded in `TESTLOG.md`.

## 1.3.0.dev2 — audience-only grain pass

### User-facing changes

- Added a subtle animated grain layer to the `BDD Audience Output` window; it is visible to OBS viewers while the player's Minecraft window remains unchanged.
- The grain strength follows the existing recording pulse and adds a restrained dark-red edge tint without covering the center view.
- The effect is part of the diagnostic audience route and automatically disappears when recording stops or the audience window is released.

### Technical behavior and verification

- Applied the audience-only effect in the shared-window fragment shader after the main color buffer is copied, keeping the player framebuffer untouched.
- Development version advanced to `1.3.0.dev2`; build and runtime verification are recorded in `TESTLOG.md`.

## 1.3.0.dev1 — audience render-target lifecycle

### User-facing changes

- Added a shared-context `BDD Audience Output` window while the route-test diagnostic is enabled; OBS can capture this window separately from the player window.
- The audience window currently mirrors the already-rendered player frame, leaving the player view unchanged while preparing a dedicated audience-only render pass.
- Closing the audience window, losing its OpenGL context, failing window creation, or disconnecting OBS disables only the audience route and returns to normal player rendering.
- Began the next roadmap section with a dedicated audience RenderTarget manager; default gameplay remains unchanged because audience-only effects are not connected yet.
- The diagnostic panel now reports the managed audience framebuffer state and pixel dimensions.
- Stopping OBS recording or disabling the diagnostic releases the audience framebuffer so a later recording can initialize a fresh target.

### Technical behavior and verification

- Centralized framebuffer creation, resize, render-thread binding, main-target restoration, cleanup, and failure isolation outside the diagnostic renderer.
- Added a shared OpenGL-context presentation path with an isolated shader/VAO pipeline; the main Minecraft context is restored after every audience frame.
- The audience target is seeded from the main color buffer before future audience-only passes run, so the current window is a useful capture surface even before model distortion is added.
- A failed audience pass is disabled without taking down Minecraft's main render target; releasing the manager clears the failure state for a future retry.
- Development version advanced to `1.3.0.dev1`; `compileJava`, `processResources`, and the full `build` passed. The installable artifact is `build/libs/bddmod-1.3.0.dev1-all.jar`.

## 1.2.1 — synchronized recording pulse

### User-facing changes

- Added a clearly visible dark-red branching vein effect around the screen edges while OBS is recording, with shorter paths, reduced opacity, and a clear central view.
- Added subtle player-audible breathing and heartbeat effects through Minecraft's ambient sound channel.
- Synchronized vein intensity, breathing volume, and heartbeat accents to one three-second phase clock; all effects start and stop with OBS recording.
- Disabled the large FBO route-test panel by default while keeping it available through `renderRouteTestEnabled` for diagnostics.
- Added `recordingAudioVolume` for the recording effects. Minecraft's master and ambient volume controls also apply.

### Compatibility and verification

- Stable version `1.2.1` targets Minecraft 1.20.1, Forge 47.4.10, and Java 17 with no new external runtime dependency.
- The final development-client run passed real OBS recording detection, synchronized visual/audio activation, user listening acceptance, world save, and normal shutdown.
- The full stable `build` passed. The installable artifact is `build/libs/bddmod-1.2.1-all.jar` with SHA-256 `0FB5B673E51945690D6D03FAD77C2F08A337A63BF4B712CA2A09DF2E0AB2798F`.
- Package inspection confirmed the stable mod version, synchronized audio/visual classes, OGG sound assets, project license, and third-party notices. Full artifact verification is recorded in `TESTLOG.md`.

### Release

- Promoted the accepted `1.2.1.dev3` implementation to stable `1.2.1` without changing its gameplay mix.
- GitHub Release `v1.2.1` retains the installable `1.2.1.dev1`, `1.2.1.dev2`, and `1.2.1.dev3` builds alongside the final stable JAR.
- Verified release asset SHA-256 digests:
  - `1.2.1.dev1`: `3CDC45E46E62025AE40655A921FD2CF29956F0A25D2C70B986486671A44F1EE8`
  - `1.2.1.dev2`: `82175391D4EB6370BBAD48E4D401DDF8E9EE20E249F787112A9575F547FD35D2`
  - `1.2.1.dev3`: `B40FDE6CAE63C913A6C6862DDD3B533AB4A54FC8252B2A6A21F2EB06A43F4098`
  - `1.2.1`: `0FB5B673E51945690D6D03FAD77C2F08A337A63BF4B712CA2A09DF2E0AB2798F`

## 1.2.1.dev3 — audible in-game recording pulse

### User-facing changes

- Moved the synchronized breathing and heartbeat from a configured virtual audio device into Minecraft's normal audio output, so the player can hear both effects directly during OBS recording.
- Added dedicated breathing and heartbeat sound assets. The breathing volume envelope, heartbeat triggers, and vein intensity still use the same three-second phase clock.
- Stopping recording now immediately stops both the breathing loop and any heartbeat currently playing.
- Assigned both effects to Minecraft's ambient sound category, so the master volume, ambient volume, and new `recordingAudioVolume` setting all control their loudness.
- Removed the obsolete `hiddenAudioVolume` and `virtualAudioDeviceName` settings; VB-CABLE or Voicemeeter is no longer required for the effect.

### Compatibility and verification

- Development version advanced to `1.2.1.dev3`; Minecraft 1.20.1, Forge 47.4.10, and Java 17 requirements are unchanged.
- `compileJava`, `processResources`, and the full `build` passed. Installable artifact: `build/libs/bddmod-1.2.1.dev3-all.jar`.
- The bundled breathing and heartbeat files were verified as 44.1 kHz OGG Vorbis audio.
- A real OBS development-client run passed recording detection, Minecraft sound-engine playback, world save, and normal shutdown. The user accepted the mix: heartbeat level was appropriate, while breathing remained intentionally very quiet through speakers without headphones.

## 1.2.1.dev2 — synchronized recording pulse

### User-facing changes

- Shortened the vein paths, reduced them from 30 to 20, removed the thick outer layer, and lowered resting opacity so the recording effect covers less of the game view.
- Added subtle synthesized breathing and low heartbeat audio that starts and stops with OBS recording.
- Visual intensity, breathing volume, and paired heartbeat accents now use one shared three-second phase clock, so their changes remain synchronized.
- Hidden audio continues to target only configured VB-CABLE or Voicemeeter-style virtual outputs and never falls back to the player's default speakers.
- The large FBO route-test panel now defaults to disabled so diagnostic UI does not cover normal gameplay; it remains available through `renderRouteTestEnabled`.

### Compatibility and verification

- Development version advanced to `1.2.1.dev2`; Minecraft 1.20.1, Forge 47.4.10, and Java 17 requirements are unchanged.
- `compileJava` and the full `build` passed. Installable artifact: `build/libs/bddmod-1.2.1.dev2-all.jar`.
- A real OBS run passed `RECORDING` and `STANDBY` transitions, opened the compatible `VoiceMeeter Aux Input` line, streamed synthesized audio without error, and stopped both the audio and shared pulse cleanly.
- Seven OBS screenshots across one three-second cycle measured about 71% peak-to-trough edge-red variation and confirm that the shorter veins leave the center view unobstructed.
- The current OBS scene uses default desktop and microphone sources, so inclusion of the VoiceMeeter stream in the final recorded audio track remains dependent on external OBS/VoiceMeeter routing.

## 1.2.1.dev1 — recording vein visibility correction

### User-facing changes

- Replaced the nearly invisible two-pixel recording border with a clearly recognizable dark-red branching vein pattern around all four edges and corners.
- The veins use layered glow, body, and core colors with a slow breathing pulse, while leaving the center of the game view unobstructed.
- HUD text now renders above the vein effect so recording status and session statistics remain readable.
- The recording-time FBO diagnostic now logs readiness only when its target is created or resized instead of once per rendered frame.

### Compatibility and verification

- Targets Minecraft 1.20.1, Forge 47.4.10, and Java 17 with no new runtime dependency or configuration option.
- `compileJava` and the full `build` passed. Installable artifact: `build/libs/bddmod-1.2.1.dev1-all.jar`.
- A real OBS recording run passed the `RECORDING` and `STANDBY` transitions, activated all 30 vein paths, and exited normally.
- An OBS program-output screenshot at `build/tmp/obs-current.png` confirms that the branching veins are clearly visible around the edges while the center view remains usable.
- Native OBS/player frame separation remains outside this correction; the diagnostic still reports that the capture hook is not installed.

## Historical workflow note — legacy version naming policy (superseded immediately)

### Project workflow changes

- Added a self-contained version naming standard to `AGENTS.md`, covering stable, development, Alpha, Beta, RC, and Release versions without requiring an external reference.
- Defined numeric increment and reset rules for major, minor, and patch releases, plus ordering, optional date components, and the meaning of each development stage.
- Replaced the previous ad hoc `-testN` convention for future work with `.devN`, `.aN`, `.bN`, and `.cN` identifiers.
- Declared published tags, release metadata, and artifacts immutable; any later published change must use a new version.

### Verification

- Documentation-only change; verified that the local policy contains no external source URL and can be followed offline.

## Unreleased — README version sync

### User-facing changes

- Corrected the README header badge and introduction from the obsolete `1.2.0-test6` label to the released `1.2.0` version.
- Rechecked the README installation filename and release summary against GitHub Release `v1.2.0`; both identify `bddmod-1.2.0-all.jar` as the installable artifact.

### Verification

- Confirmed that README current-version text no longer contains test-version or older-release references.

## Unreleased — project license

### User-facing changes

- Relicensed the project's original code and resources from `All Rights Reserved` to the WTFPL v2.
- Added a standard root `LICENSE` so GitHub and local tooling can identify the project license as `WTFPL`.
- Preserved the existing Forge/LGPL and other third-party terms in `THIRD_PARTY_LICENSES.txt`; third-party components are not relicensed.
- Packaged both the project license and third-party notices under the distributable JAR's `META-INF/` directory.

### Compatibility and verification

- Updated the generated mod metadata license value to `WTFPL`; gameplay behavior and compatibility are unchanged.
- The full `build` passed; the generated metadata reports `WTFPL`, and both license files are present in the installable `-all.jar`.

## 1.2.0 — resilient OBS transport

### User-facing changes

- Replaced the Java HTTP WebSocket transport with a bundled blocking-socket client so OBS monitoring no longer depends on the host's failing Java NIO selector initialization.
- OBS remains restricted to `127.0.0.1`; connection, authentication, recording-state events, and reconnect behavior keep the same user-facing protocol.
- Connection generations now isolate callbacks from obsolete sockets after settings changes, preventing an old disconnect from clearing a newer OBS connection.
- OBS networking initializes only in the background. Connection failures leave OBS disconnected and retrying instead of preventing Minecraft from loading the mod.
- The status changes to `CONNECTED` only after OBS accepts identification and authentication, avoiding a brief false-positive connection state when credentials are rejected.

### Compatibility and verification

- Targets Minecraft 1.20.1, Forge 47.4.10, and Java 17.
- The Apache-2.0 `nv-websocket-client` 2.14 library is bundled with Forge Jar-in-Jar; users do not install it separately.
- The formal `1.2.0` full `build` passed; the installable JAR contains the nested WebSocket client and valid Jar-in-Jar metadata.
- A simulator-backed runtime completed the OBS Hello/Identify flow, current-state request, `RECORDING` and `STANDBY` events, connection loss, and automatic reconnect.
- A follow-up run connected to OBS Studio 32.2.2, reached `Identified`, loaded the Minecraft title-screen runtime, and exited normally with `BUILD SUCCESSFUL in 59s`.

### Release

- Promoted the completed OBS reliability roadmap section from `1.2.0-test7` to the stable `1.2.0` release.
- Installable artifact: `build/libs/bddmod-1.2.0-all.jar`.
- Release artifact SHA-256: `AC0EF2E7024AC3844818E18E4DF64B38A0EE4D13A82FA84D97353FCC0B7E2917`.

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
