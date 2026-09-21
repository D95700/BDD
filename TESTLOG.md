# BDD Mod Test Log

## 1.3.0-alpha.1 audience-only localized distortion runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - localized audience distortion, shared pulse, and player-view isolation
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary local WebSocket 5 endpoint at `127.0.0.1:4455`
- Result: PASS - compilation, packaging, SemVer metadata loading, audience shader execution, shared-context presentation, and OBS disconnect fallback

### Verified behaviors

- The client loaded mod version `1.3.0-alpha.1`, entered the single-player world, and rendered on AMD Radeon RX 6750 GRE OpenGL 4.6 / LWJGL 3.3.1.
- The local OBS protocol endpoint reported `RECORDING`; the route created the `854x480` audience target and opened `BDD Audience Output`, proving the revised fragment shader compiled, linked, and presented without a new shader or OpenGL error.
- Stopping the temporary endpoint released the audience target and stopped synchronized audio and pulse effects without crashing the client.
- The temporary diagnostic setting was restored to `renderRouteTestEnabled = false` after the run.

### Not verified

- Direct player/audience screenshots and pixel comparison were unavailable because the local Windows capture interface returned no bindable application windows. The localized distortion's subjective visibility and intensity therefore remain for manual review.
- The Gradle development task was interrupted after the world had auto-saved and the audience route had released; a normal menu-driven client shutdown was not covered.

### Warnings and observations

- The run emitted the pre-existing vanilla warning that `rendertype_entity_translucent_emissive` could not find `Sampler2`; no new warning named the audience shader or output route.

### Evidence

- Runtime log: `run/logs/latest.log` and `run/logs/debug.log`, including audience target/window creation at `13:00:43` and disconnect release at `13:01:40`.
- Installable artifact: `build/libs/bddmod-1.3.0-alpha.1-all.jar`, SHA-256 `E835B495FC66D1E6CC52B4446278CF3B7ED543E76B5CBD9480E1D6E6B6F8A992`.

## 1.3.0.dev3 audience window orientation runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - audience output texture orientation and recording fallback
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary local WebSocket 5 endpoint at `127.0.0.1:4455`
- Result: PASS - client startup, dev3 metadata loading, corrected audience output orientation path, shared-context presentation, and normal shutdown

### Verified behaviors

- The fullscreen audience quad now maps RenderTarget V coordinates from 0.0 at the lower edge to 1.0 at the upper edge, correcting the previous vertical inversion.
- The client loaded mod version `1.3.0.dev3`, entered the single-player world, and rendered on AMD Radeon RX 6750 GRE OpenGL 4.6 / LWJGL 3.3.1.
- The local OBS protocol endpoint reported `RECORDING`; the route created the `854x480` audience target and opened the `BDD Audience Output` window without shader or OpenGL errors.
- The temporary endpoint was stopped; the run saved the world and shut down normally. The diagnostic setting was restored to `renderRouteTestEnabled = false`.

### Not verified

- A pixel-by-pixel comparison against an OBS window-source capture was not performed; the runtime log verifies the corrected coordinate path and successful presentation, while direct visual confirmation remains a manual OBS capture step.

### Evidence

- Runtime log: `run/logs/latest.log` and `run/logs/debug.log` after the dev3 client run.
- Installable artifact: `build/libs/bddmod-1.3.0.dev3-all.jar`, SHA-256 `B5D6C00B2A117F3A37F3CE223A71D2E72DA73FFA888B1F1539C49919CF46B0DA`.

## 1.3.0.dev2 audience-only grain runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - shared audience shader grain and recording pulse handoff
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary local WebSocket 5 endpoint at `127.0.0.1:4455`
- Result: PASS - client startup, dev2 metadata loading, audience shader execution, shared-context output, OBS disconnect fallback, and normal shutdown

### Verified behaviors

- The client loaded mod version `1.3.0.dev2`, entered the single-player world, and rendered on AMD Radeon RX 6750 GRE OpenGL 4.6 / LWJGL 3.3.1.
- The local OBS protocol endpoint reported `RECORDING`; the route created the `854x480` audience target and `BDD Audience Output` window, then ran the audience fragment shader with pulse and phase uniforms without a new shader/OpenGL error.
- The audience-only grain and dark-red edge tint were applied in the separate output path; the player window remained responsive and unchanged by the shader pass.
- Stopping the temporary endpoint released the audience target, returned the Minecraft window, stopped the synchronized audio/pulse, and allowed normal world save and shutdown.
- The temporary diagnostic setting was restored to `renderRouteTestEnabled = false` after shutdown.

### Not verified

- A real OBS Studio window-source capture and pixel-by-pixel audience/player comparison were not performed; the endpoint only supplied the WebSocket state needed to exercise the client.
- Audience-only model distortion and hidden text remain outside this micro-step.

### Warnings and observations

- The run emitted the pre-existing vanilla warning that `rendertype_entity_translucent_emissive` could not find `Sampler2`; no new warning named the audience shader or output route.

## 1.3.0.dev1 shared-context audience window runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - temporary local OBS WebSocket recording state, audience RenderTarget, and independent GLFW output window
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary local WebSocket 5 endpoint at `127.0.0.1:4455`
- Result: PASS - client startup, recording-state activation, shared-context audience window creation, texture presentation path, OBS disconnect fallback, and normal process shutdown

### Verified behaviors

- The Forge client entered the existing single-player world and reached the render thread on AMD Radeon RX 6750 GRE OpenGL 4.6 / LWJGL 3.3.1.
- The local OBS protocol endpoint completed identification and reported `RECORDING`; the mod logged `Route-test GUI renderer active`, `Audience render target ready at 854x480`, and `Audience output window opened at 854x480` with title `BDD Audience Output`.
- The audience window used the shared OpenGL context and presented the copied main color buffer without a crash or an OpenGL error. The Java process exposed the audience window title while it was open.
- Stopping the temporary endpoint changed the monitor back to standby; the audience RenderTarget was released, the Minecraft window title returned, and the client remained responsive.
- The temporary diagnostic setting was restored to `renderRouteTestEnabled = false` after shutdown.

### Not verified

- A real OBS Studio window-source capture was not performed in this run; the endpoint only supplied the same WebSocket state messages needed to exercise the client.
- Audience-only model distortion, noise, hidden text, native swap-buffer interception, and capture output pixel comparison remain outside this step.

### Evidence

- Runtime log: `run/logs/latest.log` and `run/logs/debug.log`.
- Render-route messages: `run/logs/latest.log` entries at `05:04:29` and fallback at `05:05:03`.
- Development artifact: `build/libs/bddmod-1.3.0.dev2-all.jar`, SHA-256 `31C32C0D8160CAAF30392C34EFFC68F907AAC80682F011FE6A4CAB298715566F`.
- Temporary protocol driver used only during the test: `build/tmp/mock_obs_route_test.py`.

## 1.3.0.dev1 audience RenderTarget runtime attempt

- Test date: 2026-09-21
- Test type: Forge development-client startup attempt for audience RenderTarget lifecycle
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - the checked-in Gradle Wrapper could not start because its distribution lock was denied; Minecraft did not launch and no FBO runtime behavior is claimed

### Verified behaviors

- `compileJava`, `processResources`, and the full `build` passed before this runtime attempt.
- The resulting artifact is `build/libs/bddmod-1.3.0.dev1-all.jar`, SHA-256 `6A6800479915AF719538CC6DF8C0F6B407384F3E8F0E8A58ED2C7E4F6A96FF23`.
- The ignored diagnostic setting was restored to `renderRouteTestEnabled = false` after the attempt.

### Failure and observations

- Gradle stopped at `ExclusiveFileAccessManager` with `FileNotFoundException` for `C:UsersAdministrator/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8-bin.zip.lck` and did not create a Minecraft process.
- Directly invoking the cached Gradle distribution was also unavailable because its Windows native service could not load `native-platform.dll`.

### Evidence

- Wrapper output: `build/tmp/runclient-1.3.0.dev1.err.log`.
- Previous successful package output: `build/libs/bddmod-1.3.0.dev1-all.jar`.

## 1.2.1 stable release packaging

- Test date: 2026-09-21
- Test type: Full Forge verification and release packaging
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium compiler used by the host-only init script)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - stable compile, resource processing, tests (`NO-SOURCE`), reobfuscation, Jar-in-Jar packaging, and release artifact inspection

### Verified behaviors

- The full `build` completed successfully in 23 seconds without running `clean`, preserving all development artifacts from this roadmap section.
- `bddmod-1.2.1-all.jar` reports embedded mod version `1.2.1` and contains the synchronized audio player, pulse controller, vein overlay, sound registrations, both OGG assets, the WTFPL license, and third-party notices.
- The retained `1.2.1.dev1`, `1.2.1.dev2`, and `1.2.1.dev3` installable JARs each report the development version matching their filenames.
- Stable artifact: `build/libs/bddmod-1.2.1-all.jar`, SHA-256 `0FB5B673E51945690D6D03FAD77C2F08A337A63BF4B712CA2A09DF2E0AB2798F`.

### Retained release assets

- `bddmod-1.2.1.dev1-all.jar`: `3CDC45E46E62025AE40655A921FD2CF29956F0A25D2C70B986486671A44F1EE8`
- `bddmod-1.2.1.dev2-all.jar`: `82175391D4EB6370BBAD48E4D401DDF8E9EE20E249F787112A9575F547FD35D2`
- `bddmod-1.2.1.dev3-all.jar`: `B40FDE6CAE63C913A6C6862DDD3B533AB4A54FC8252B2A6A21F2EB06A43F4098`
- `bddmod-1.2.1-all.jar`: `0FB5B673E51945690D6D03FAD77C2F08A337A63BF4B712CA2A09DF2E0AB2798F`

### Runtime verification boundary

- The stable artifact differs from the accepted `1.2.1.dev3` build only in release metadata/versioning. The immediately preceding `1.2.1.dev3` runtime test verified real OBS synchronization, in-game audio playback, user listening acceptance, world save, and normal shutdown.
- No separate stable-version client runtime was performed after the metadata-only promotion.

## 1.2.1.dev3 in-game recording audio acceptance

- Test date: 2026-09-21
- Test type: Forge development client runtime test - real OBS recording and player-audible synchronized audio
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client lifecycle, real OBS recording state, Minecraft sound-engine playback, user listening acceptance, and normal shutdown

### Verified behaviors

- OBS Studio started recording and exposed its WebSocket service on port `4455`; the client identified it and observed `RECORDING`.
- Minecraft initialized OpenAL on `Speakers (USBAudio2.0)`, started the normal sound engine, and logged activation of both synchronized in-game audio and the shared visual pulse.
- The user reported that the heartbeat level was appropriate, the breathing was very quiet through speakers without headphones, and the combined effect was satisfactory. The current mix was therefore retained.
- Minecraft stopped normally, saved the world, and Gradle reported `BUILD SUCCESSFUL in 2m 8s`.
- OBS stopped recording and finalized a 6m22s MP4 containing H.264 video and a 48 kHz stereo AAC audio stream.

### Not verified

- Headphone listening and the breathing level on other playback devices were not tested.
- The recording contains an audio track, but this run did not isolate or measure the breathing/heartbeat waveform inside the mixed OBS track.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- Gradle output: `build/tmp/runclient-dev3-listen.out.log` and `build/tmp/runclient-dev3-listen.err.log`.
- OBS log: `C:/Users/Administrator/AppData/Roaming/obs-studio/logs/2026-09-21 01-34-29.txt`.
- OBS recording: `E:/OBS/2026-09-21 01-34-50.mp4`.

## 1.2.1.dev2 synchronized recording pulse runtime

- Test date: 2026-09-21
- Test type: Forge development client runtime test - shared visual pulse and virtual-device breathing/heartbeat audio
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client lifecycle, OBS transitions, shared pulse, virtual audio output lifecycle, and reduced-obstruction visual

### Verified behaviors

- The `1.2.1.dev2` client loaded, entered the single-player world, and identified the local OBS Studio 32.2.2 WebSocket service.
- Real recording changed the monitor to `RECORDING`, activated the 20-path vein overlay, and started the shared three-second pulse clock.
- Java Sound matched and opened `VoiceMeeter Aux Input (VB-Audio VoiceMeeter AUX VAIO)` as a compatible `44.1 kHz`, 16-bit mono output. The synthesized breathing and heartbeat stream remained active without an audio exception.
- Stopping recording changed the monitor to `STANDBY`; the audio stream and shared pulse both stopped in the same update window.
- The FBO diagnostic panel remained disabled. OBS screenshots show shorter, thinner veins confined to the edges with an unobstructed center view.
- Seven screenshots across one pulse cycle measured edge red-excess values from `60,768` to `103,923`, a roughly 71% peak-to-trough change that confirms visible intensity modulation.
- The client stopped normally, saved all dimensions, and Gradle reported `BUILD SUCCESSFUL in 3m 43s`.

### Not verified

- OBS currently captures the default desktop and microphone devices. The VoiceMeeter line was verified as open and streaming, but inclusion of that virtual input in the final OBS audio track requires corresponding OBS/VoiceMeeter routing and was not claimed.
- Subjective breathing and heartbeat loudness still requires listening through the configured VoiceMeeter/OBS monitoring path.
- Native OBS/player frame separation remains outside this test.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- Pulse screenshots: `build/tmp/pulse-frame-0.png` through `build/tmp/pulse-frame-6.png`; representative peak and low frames are `pulse-frame-4.png` and `pulse-frame-6.png`.
- OBS recording: `E:/OBS/2026-09-21 01-02-38.mp4`.
- OBS log: `C:/Users/Administrator/AppData/Roaming/obs-studio/logs/2026-09-21 01-01-25.txt`.
- Full build artifact: `build/libs/bddmod-1.2.1.dev2-all.jar`, SHA-256 `82175391D4EB6370BBAD48E4D401DDF8E9EE20E249F787112A9575F547FD35D2`.

## 1.2.1.dev1 recording vein visual acceptance

- Test date: 2026-09-21
- Test type: Forge development client runtime test - real OBS recording and dark-red branching vein visual
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client lifecycle, real OBS synchronization, and visible recording vein overlay

### Verified behaviors

- The `1.2.1.dev1` client loaded, entered the existing single-player world, and connected to OBS Studio 32.2.2 on localhost.
- Starting real OBS recording changed the monitor state to `RECORDING`; the renderer then activated all 30 vein paths at a `427x240` GUI resolution and initialized the FBO at `854x480`.
- An OBS program-output screenshot visibly shows the dark-red branching pattern around the corners and edges while leaving the center view available. The HUD remains legible above the effect.
- Stopping OBS recording changed the monitor state back to `STANDBY`; the client then stopped normally, saved all dimensions, and Gradle reported `BUILD SUCCESSFUL in 1m 21s`.
- The FBO readiness diagnostic appeared once when the target was created rather than once per frame.

### Not verified

- The static screenshot verifies visibility and placement but does not independently prove the full temporal breathing cycle.
- The route-test panel still states `HOOK: NOT INSTALLED`; this test does not claim native OBS/player frame separation.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- OBS screenshot: `build/tmp/obs-current.png` at `1280x720`.
- OBS recording: `E:/OBS/2026-09-21 00-38-16.mp4`.
- Full build artifact: `build/libs/bddmod-1.2.1.dev1-all.jar`, SHA-256 `3CDC45E46E62025AE40655A921FD2CF29956F0A25D2C70B986486671A44F1EE8`.

## 1.2.1.dev1 initial recording vein runtime

- Test date: 2026-09-21
- Test type: Forge development client runtime test - initial real OBS recording branch verification
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client launch, recording transition, vein-renderer activation, and normal exit

### Verified behaviors

- The client entered the world, observed real OBS `RECORDING` and `STANDBY` transitions, activated the new 30-path vein renderer, and exited normally with `BUILD SUCCESSFUL in 2m 3s`.
- The run exposed per-frame FBO-ready DEBUG output in the development console. Logging was restricted to FBO creation or resize before the final visual acceptance run.

### Evidence and observations

- OBS recording: `E:/OBS/2026-09-21 00-33-06.mp4`.
- This run established the recording branch behavior; the following acceptance run archived the static visual evidence.

## 1.2.0 recording visual acceptance retry

- Test date: 2026-09-20
- Test type: Forge development client runtime test - real OBS recording-state and dark-red edge feedback
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - Minecraft and OBS synchronization passed, but the expected dark-red recording visual was not visible

### Verified behaviors

- The client loaded successfully, entered the single-player world, started the integrated server, and activated the route-test GUI renderer at `427x240` GUI pixels.
- The monitor identified the real OBS WebSocket service and observed `RECORDING` at 21:43:19, followed by `STANDBY` at 21:44:08.
- The recording-time FBO probe reported ready at `854x480`, so the recording branch was executing.
- The client and integrated server shut down normally; Gradle reported `BUILD SUCCESSFUL in 1m 44s`.

### Failure and observations

- The user did not see the expected dark-red recording effect while OBS was recording, so the visual acceptance criterion failed.
- Source inspection after the run found that the current effect is only a four-sided, two-GUI-pixel border with alpha varying from 28 to 40. It does not implement a vein pattern and is faint enough to be effectively invisible at the tested scale.
- Forge and Minecraft source inspection confirmed that the shared `GuiGraphics` batch is flushed at the end of the GUI frame; a missing explicit `flush()` in the handler is not the cause.
- This result does not verify OBS/player frame separation; the diagnostic still reports that the native capture hook is not installed.

### Evidence

- Runtime log: `run/logs/latest.log`.
- Gradle output: `build/tmp/runclient-visual-retry.out.log` and `build/tmp/runclient-visual-retry.err.log`.

## 1.2.0 recording visual initial attempt

- Test date: 2026-09-20
- Test type: Forge development client runtime test - initial dark-red recording visual attempt
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - the client crashed before recording visual verification

### Failure and observations

- World startup reached integrated-server initialization, then Java failed to open the selector required by the local connection with `Unable to establish loopback connection`.
- No recording visual behavior was verified in this attempt. The retry used `-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable` to force the affected selector path to fall back to TCP loopback.

### Evidence

- Crash report: `run/crash-reports/crash-2026-09-20_21.38.51-client.txt`.
- Gradle output: `build/tmp/runclient-visual.out.log` and `build/tmp/runclient-visual.err.log`.

## 1.2.0-test7 real OBS identification follow-up

- Test date: 2026-09-20
- Test type: Forge development client runtime test - real OBS identification and authenticated connection status
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - real OBS identification, client launch, and normal exit

### Verified behaviors

- The monitor established its blocking-socket transport connection to the running OBS Studio 32.2.2 service on `127.0.0.1:4455`.
- OBS returned `Identified`; the log recorded `OBS WebSocket identified on 127.0.0.1:4455`, confirming that `CONNECTED` is now set after protocol identification and authentication rather than at the earlier transport handshake.
- `bddmod` completed loading, reached the title-screen runtime, and created no new crash report.
- The client closed normally after an explicit window-close request and Gradle reported `BUILD SUCCESSFUL in 59s`.

### Not verified

- Real OBS recording was not started or stopped in this short follow-up; recording transitions were covered by the immediately preceding simulator-backed run.
- OBS/player visual frame separation remains outside this transport test.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- Forge's version-check thread still reports the host Java NIO selector error independently of `bddmod`; it does not prevent mod loading or the blocking-socket OBS connection.

## 1.2.0-test7 resilient OBS transport runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test - OBS handshake, recording events, fallback, and reconnect
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client launch, OBS protocol flow, reconnect, and normal exit

### Verified behaviors

- `bddmod` loaded successfully and reached the Minecraft title-screen runtime without a mod-loading failure.
- Against a localhost OBS WebSocket 5 simulator, the monitor completed Hello/Identify/Identified, sent `GetRecordStatus`, and processed both `RecordStateChanged` transitions. Evidence: `OBS recording state changed: RECORDING` followed by `STANDBY` in `run/logs/latest.log`.
- After the simulator was stopped, the monitor detected the reset and automatically connected to the real OBS Studio 32.2.2 service on `127.0.0.1:4455` within the next retry interval.
- The Java NIO selector failure still occurred in Forge's unrelated version-check thread, but it did not affect the new blocking-socket OBS transport or stop the game.
- The client closed normally after an explicit window-close request and Gradle reported `BUILD SUCCESSFUL in 2m 3s`.

### Not verified

- A real OBS recording start/stop was not triggered; recording transitions were protocol-tested with the local simulator while real OBS verified live connection and reconnect behavior.
- Authentication rejection status still requires a dedicated run with deliberately invalid credentials.
- Player/OBS visual frame separation remains outside this transport milestone.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- No new crash report was created by the successful run. The newest report remains the earlier dependency-classpath failure at `run/crash-reports/crash-2026-09-20_20.37.33-fml.txt`.
- The simulator was a host-only ignored test helper under `build/tmp/`; it is not included in source control or the distributable JAR.

## 1.2.0-test7 initial transport runtime attempt

- Test date: 2026-09-20
- Test type: Forge development client runtime test - bundled blocking WebSocket transport
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - the development runtime omitted the new WebSocket library from its classpath

### Verified behaviors

- Forge reached `bddmod` construction with version `1.2.0-test7`.
- The preceding full build completed successfully and the distributable `build/libs/bddmod-1.2.0-test7-all.jar` contained both the nested library and valid Jar-in-Jar metadata.

### Failure and evidence

- Mod construction failed with `NoClassDefFoundError: com/neovisionaries/ws/client/WebSocketListener` because the dependency was initially declared on Gradle's generic `implementation` configuration rather than ForgeGradle's development-runtime `minecraftLibrary` configuration.
- Runtime log: `run/logs/latest.log`.
- Crash report: `run/crash-reports/crash-2026-09-20_20.37.33-fml.txt`.
- OBS protocol behavior was not reached in this attempt. The dependency configuration was corrected before the next run.

## 1.2.0-test6 OBS initialization recovery result

- Test date: 2026-09-20
- Test type: Forge development client runtime regression test - OBS HTTP client initialization failure containment
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - client launch and mod loading; OBS monitoring remained unavailable on this host

### Verified behaviors

- `bddmod` completed construction and Forge finished the client loading lifecycle without a mod-loading error or new crash report.
- The client completed resource loading and reached the Minecraft runtime; the integrated server also started and initialized its overworld.
- The host's existing Java NIO error remained reproducible, but it was contained in the daemon `bddmod-obs-monitor` thread. The monitor stayed disconnected and retried without stopping Minecraft.
- Full packaging passed before the runtime test with `BUILD SUCCESSFUL in 18s`; artifact: `build/libs/bddmod-1.2.0-test6.jar`.

### Not verified

- OBS WebSocket connectivity and recording-state updates could not be verified because this host could not create the Java HTTP client's internal loopback connection.
- The compact OBS button's exact visual placement and click behavior were not independently inspected during this run.
- Gameplay features were not exhaustively tested; the run verified startup and integrated-server initialization only.

### Evidence and observations

- Runtime logs: `run/logs/latest.log` and `run/logs/debug.log`.
- The most recent crash report remains the pre-fix `run/crash-reports/crash-2026-09-20_20.03.08-fml.txt`; this run created no new crash report.
- The main window closed after a close request, but the development process did not finish on its own and was interrupted from the Gradle terminal. No normal-exit claim is made for this run.
- The runtime reported a saved-world version difference from `1.2.0-test5` to `1.2.0-test6`; this did not prevent integrated-server startup.

## 1.2.0-test6 compact OBS button runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test - compact OBS settings button
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; .\gradlew.bat runClient --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - `bddmod` crashed during mod construction before the title screen became usable

### Verified behaviors

- Forge 47.4.10 launched the Minecraft 1.20.1 development client with Java 17.0.15.
- The failure is reproducible during `bddmod` construction: static initialization of `OBSMonitor` attempted to create a Java HTTP client and failed with `Unable to establish loopback connection`.
- The client stopped after Forge displayed the mod-loading failure. Gradle reported `BUILD SUCCESSFUL in 1m 15s`, but this does not represent a successful game launch.

### Not verified

- The compact OBS settings button's placement, tooltip, click behavior, and settings-screen navigation were not reached or visually verified.
- Gameplay, OBS connection, recording-state synchronization, and render routing were not exercised in this run.

### Evidence and observations

- Runtime log: `run/logs/latest.log`.
- Crash report: `run/crash-reports/crash-2026-09-20_20.03.08-fml.txt`.
- Root cause location: `src/main/java/com/example/bddmod/client/OBSMonitor.java:45`.
- Underlying environment error: `java.net.SocketException: Invalid argument: connect` while Java NIO attempted to establish its internal loopback connection.

## 1.2.0-test6 runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test — repeatable OBS settings
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `.\gradlew.bat runClient`
- Result: PASS — client launch, settings save/reconnect path, and normal exit

### Verified behaviors

- The client loaded `bddmod` successfully and reached the title-screen runtime without a fatal error.
- The client configuration changed during the run, after which the OBS monitor immediately established a new connection to `127.0.0.1:4455`. This verifies the runtime save-and-reconnect path used by the settings screen.
- The client exited normally and Gradle reported `BUILD SUCCESSFUL in 3m 1s`.
- `compileJava` and `build` passed; artifact: `build/libs/bddmod-1.2.0-test6.jar`.

### Not verified

- The exact main-menu button position at normal and narrow window sizes was not captured independently in a screenshot.
- Return-to-menu behavior without saving was not separately evidenced in the runtime log.
- OBS recording-state transitions and player/OBS frame separation remain unverified; the current build does not implement the native capture hook.

### Evidence and observations

- Runtime evidence: `run/logs/latest.log`.
- The log records the client-config change and immediate OBS reconnect at `19:17:09`, followed by normal client shutdown at `19:17:10`.
- Known unrelated warnings remained: missing Goat Horn sounds, missing vanilla `Sampler2`, and unavailable Realms authorization.

## 1.2.0-test5 runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test — OBS first-launch setup guide and route diagnostics
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `.\gradlew.bat runClient`
- Result: PASS — client launch, single-player lifecycle, and setup completion persistence; OBS recording verification remains incomplete

### Verified behaviors

- The client loaded successfully, entered a single-player world, connected to the integrated server, and saved the world on exit.
- The route-test renderer executed; evidence: `Route-test GUI renderer active: 427x255, recording=false, testEnabled=true` in `run/logs/latest.log`.
- The client configuration now contains `obsSetupCompleted = true`, confirming that the first-launch setup flow reached a completion action and persisted its state. The configured password is not reproduced here.
- The user visually confirmed that the setup and in-game diagnostic presentation render correctly and that the visual result is satisfactory. No screenshot has been archived in the repository yet.
- OBS accepted a WebSocket connection on `127.0.0.1:4455`; after the latest connection, no further `4009 Authentication failed` line appeared in the captured log.
- Gradle completed with `BUILD SUCCESSFUL in 3m 45s`.

### Not verified

- The guide and diagnostic visuals were confirmed by the user, but their exact layout and masked-password rendering have not been independently archived in a screenshot.
- No `OBS recording state changed: RECORDING` or `STANDBY` line appeared, so live recording-state synchronization and the recording-time FBO branch remain unverified.
- The user confirmed that the OBS-visible picture and player display are not separated in this build. The native swap-buffer hook is not installed, so capture separation must not be claimed.

### Observations and evidence

- Runtime evidence: `run/logs/latest.log`; configuration evidence: `run/config/bddmod-client.toml` with the password value redacted from this record.
- The development runtime reported an intermediate `bddmod (version 1.2.0-test4 -> 1.2.0-test5)` difference during integrated-server connection. The client still completed the world lifecycle successfully.
- Known unrelated warnings remained: missing Goat Horn sounds, missing vanilla `Sampler2`, unavailable Realms authorization, and console encoding issues.

## Test Information

- Test date: 2026-09-20
- Test type: Forge development client runtime test — FBO split-route diagnostic build
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `.\gradlew.bat runClient`
- Result: PASS

## Pending test after 1.2.0-test2 correction

- The recorded PASS below applies to the previous `1.2.0-test1` build.
- `1.2.0-test2` adds the OBS WebSocket request-envelope fix and an always-visible route-test panel; no runtime result is claimed for these new changes until the next client run.
- The latest unexecuted source revision also adds a HOTBAR overlay fallback and one-time diagnostic logging; it requires a fresh client run.
- `1.2.0-test3` adds an explicit `GuiGraphics` flush after the panel draw; no runtime result is claimed until the next run.

## 1.2.0-test3 runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test — diagnostic panel and OBS connection
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `.\\gradlew.bat runClient`
- Result: PASS — client launch and single-player world lifecycle

### Verified behaviors

- The client loaded successfully and entered a single-player world.
- The route-test renderer executed; evidence: `Route-test GUI renderer active: 427x240, recording=false, testEnabled=true` in `run/logs/latest.log`.
- OBS WebSocket eventually accepted connections on `127.0.0.1:4455`; evidence: `OBS WebSocket connected to 127.0.0.1:4455` in `run/logs/latest.log`.
- The client exited normally and Gradle reported `BUILD SUCCESSFUL`.

### Not verified

- The visible panel was not independently confirmed from the runtime log or a screenshot.
- No `OBS recording state changed` line appeared, so live recording-state synchronization and the recording-time FBO branch remain unverified.
- OBS Game Capture frame separation remains unverified; the native swap-buffer hook is not installed.

### Observations and evidence

- The log reported an intermediate `bddmod (version 1.2.0-test2 -> 1.2.0-test3)` difference, indicating an older test artifact was also visible to the development environment during this run.
- Known unrelated warnings remained: missing Goat Horn sounds, missing vanilla `Sampler2`, and unavailable Realms authorization.
- Evidence: `run/logs/latest.log`; Gradle output completed with `BUILD SUCCESSFUL in 2m 22s`.

## Pending correction after test4 review

- Source review found that `RegisterGuiOverlaysEvent` requires the mod event bus; the `1.2.0-test4` source now registers it explicitly from `BDDMod`.
- No runtime result is claimed for this correction until a fresh `runClient` test.

## 1.2.0-test4 runtime result

- Test date: 2026-09-20
- Test type: Forge development client runtime test — topmost diagnostic overlay and OBS authentication
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java target: 17
- Mod: `bddmod`
- Command: `.\\gradlew.bat runClient`
- Result: PASS for client launch; FAIL for OBS authentication verification

### Verified behaviors

- The client loaded, entered a single-player world, connected to the integrated server, saved, and exited normally.
- The diagnostic renderer executed; evidence: `Route-test GUI renderer active: 427x240, recording=false, testEnabled=true`.
- OBS WebSocket port `127.0.0.1:4455` was reachable.
- Gradle completed with `BUILD SUCCESSFUL in 10m 3s`.

### Failed or unverified behaviors

- OBS rejected the configured credentials with close code `4009 Authentication failed`; no recording state was received.
- Recording-time FBO execution, visible panel output, and OBS Game Capture frame separation remain unverified.

### Evidence and observations

- Runtime evidence: `run/logs/latest.log`.
- The development runtime reported a `1.2.0-test3 -> 1.2.0-test4` version difference during resource/world reload; no external JAR was present in `run/mods`.
- Known unrelated warnings remained: missing Goat Horn sounds, missing vanilla `Sampler2`, and unavailable Realms authorization.

## Verified Behaviors

- Forge client started successfully with the `1.2.0-test1` development resources.
- `bddmod` loaded successfully and the new `renderRouteTestEnabled` configuration entry was created with its default value.
- A single-player world was created and loaded successfully.
- The integrated server started, the client connected with `bddmod`, and the world saved successfully on exit.
- No render-route exception or mod crash was found in `run/logs/latest.log`.

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

- The OBS-recording branch was not exercised because OBS was not recording during this run; FBO creation, resize, write, restore, and the visible route-test panel therefore remain unverified at runtime.
- OBS Game Capture receiving a different frame from the player's display remains unverified; this build does not install a native swap-buffer hook.

- Individual BDD gameplay features were not exhaustively tested.
- OBS WebSocket recording-state transitions were not tested against a live OBS instance in this run.
- Hidden audio playback and virtual audio-device output were not independently verified.
- Multiplayer or dedicated-server compatibility was not tested.

## Evidence

- Gradle task output: `BUILD SUCCESSFUL in 5m 20s`
- Runtime log: `run/logs/latest.log`
- Test world created during the run: `run/saves/新的世界/`
- Gradle output: `BUILD SUCCESSFUL in 4m 24s`
