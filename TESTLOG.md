# BDD Mod Test Log

## 1.4.0-alpha.3 loader-neutral final framebuffer capture build verification

- Test date: 2026-09-22
- Test type: Java compilation, resource processing, packaged-artifact verification, and source-level OpenGL state review
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17 toolchain
- Mod ID: `bddmod`
- Gradle environment: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'`; Java compilation uses `build\tmp\codex-direct-javac.init.gradle` to select the Java 17 toolchain compiler.
- Commands: `.\gradlew.bat build --no-daemon --stacktrace --init-script build\tmp\codex-direct-javac.init.gradle`; `python scripts\verify_build.py`; `python scripts\verify_release_assets.py --root . --artifact 1.4.0-alpha.1=build/libs/bddmod-1.4.0-alpha.1-all.jar --artifact 1.4.0-alpha.2=build/libs/bddmod-1.4.0-alpha.2-all.jar --artifact 1.4.0-alpha.3=build/libs/bddmod-1.4.0-alpha.3-all.jar`; `git diff --check`
- Result: PASS - the alpha.3 capture abstraction compiles, resource templates expand, the installable artifact embeds matching SemVer metadata, and all retained alpha.1-alpha.3 artifacts pass the multi-asset verifier

### Verified behaviors

- `AudienceFrameCapture.CurrentFramebuffer` reads the current read framebuffer and copies it to the audience target without assuming Minecraft's vanilla main target.
- Framebuffer bindings, viewport, scissor rectangle, and scissor enablement are restored after every capture attempt.
- Iris/Oculus detection is diagnostic only; loader presence no longer disables the audience route before capture.
- Capture failures disable only the audience target and preserve the player renderer, with the failure reason visible in the diagnostic panel.

### Not covered

- No live Vanilla or Oculus Shader Pack client run was executed for alpha.3.
- No OBS recording, runtime screenshot, or audio acceptance was produced for alpha.3.

### Evidence

- Source: `src/main/java/com/example/bddmod/client/AudienceFrameCapture.java`, `AudienceRenderTargetManager.java`, and `ShaderCompatibility.java`.
- The alpha.3 installable artifact is `build/libs/bddmod-1.4.0-alpha.3-all.jar`, SHA-256 `FDCE5DB232768BFA2E59E8DE49A5EB9F20D5F8C28D8A7BDF7314DA430CC8CA87`.
- The retained alpha.1, alpha.2, and alpha.3 assets report matching embedded versions, WTFPL metadata, bundled WebSocket dependency, required resources, and SHA-256 values.
- Negative multi-asset checks passed: missing-artifact and filename/version-mismatch fixtures both returned exit code `1`.
- Workflow YAML parsing and Python syntax checks passed for `.github/workflows/ci.yml`, `.github/workflows/release.yml`, `scripts/verify_build.py`, and `scripts/verify_release_assets.py`.

## 1.4.0-alpha.2 Iris/Oculus compatibility fallback source and build verification

- Test date: 2026-09-22
- Test type: Java compilation, resource processing, packaged-artifact verification, and source-level compatibility check
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17 toolchain
- Mod ID: `bddmod`
- Gradle environment: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'`; Java compilation uses `build\tmp\codex-direct-javac.init.gradle` to select the Java 17 toolchain compiler.
- Commands: `.\gradlew.bat compileJava --no-daemon --stacktrace --init-script build\tmp\codex-direct-javac.init.gradle`; `.\gradlew.bat processResources --no-daemon --stacktrace --init-script build\tmp\codex-direct-javac.init.gradle`; `.\gradlew.bat build --no-daemon --stacktrace --init-script build\tmp\codex-direct-javac.init.gradle`; `python scripts\verify_build.py`; `git diff --check`
- Result: PASS - the alpha.2 source compiles, resources expand, the installable artifact embeds the matching SemVer metadata, and the Iris/Oculus fallback source path is present for both supported mod IDs

### Verified behaviors

- `ShaderCompatibility` checks Forge mod IDs `iris` and `oculus`, caches the result, and logs whether the audience FBO route remains enabled or enters fallback.
- `RenderRouteTestRenderer` releases the audience route whenever a detected shader loader makes the FBO path unsupported; the player-visible renderer remains active.
- The optional route diagnostic reports `SUPPORTED` or `FALLBACK (iris, oculus)` without changing the default configuration.
- The installable artifact is `build/libs/bddmod-1.4.0-alpha.2-all.jar`, SHA-256 `201C2723FB4492C165B6E439345BDE6E0AFF47DF120BD33F32F7A991100945FD`; the alpha.1 `-all.jar` is retained alongside it for the eventual section release.

### Not covered

- No live client session with Iris or Oculus was run, so shader-pack-specific visual output and a real fallback transition remain unverified.
- No OBS recording or runtime screenshot was produced for alpha.2.

### Evidence

- Source: `src/main/java/com/example/bddmod/client/ShaderCompatibility.java` and `src/main/java/com/example/bddmod/client/RenderRouteTestRenderer.java`.
- Artifact verifier output: `build/tmp/alpha2-verify-build.txt`.

### Warnings and observations

- A plain wrapper invocation hit this machine's known Gradle loopback-selector error; the environment prefix and Java compiler init script above completed the same checks successfully.

## 1.3.0-alpha.5 repair runtime and audience-frame verification

- Test date: 2026-09-22
- Test type: Forge development-client runtime test - repaired breathing cadence, held randomized head effects, and direct audience-frame capture
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Runtime command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable -Dbddmod.testThirdPerson=true -Dbddmod.testAudienceCaptureDirectory=E:/MOD/forge-1.20.1-47.4.10-mdk/build/tmp/alpha5-capture'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle` with an isolated mock OBS endpoint at `127.0.0.1:4456`
- Build command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - repaired runtime branches, all three audience head modes, direct OpenGL audience-frame inspection, resource validation, final compilation, packaging, and normal client shutdown

### Verified behaviors

- The client loaded `1.3.0-alpha.5`, connected to the isolated mock OBS endpoint, created the `854x480` audience target, and routed the mock capture source to `BDD Audience Output:GLFW30:java.exe`.
- The new breathing asset is a 2.18-second, 48 kHz mono Vorbis phrase with distinct inhale, pause, exhale, and silence segments; the runtime starts it once per three-second pulse cycle instead of looping a continuous noise bed.
- Head modes now hold for two seconds in a shuffled sequence. Runtime evidence recorded `MOSAIC` at `01:57:05`, `DISTORTION` at `01:57:07`, and `DEFORMATION` at `01:57:09`.
- Direct PNG reads from the audience OpenGL back buffer confirmed visible results: large block mosaic, clearly visible radial swirl, and strong horizontal deformation localized to the player's head. The hidden `WE SEE YOU` message was also visible in the deformation capture.
- The final client exited normally. No new crash report, audience OpenGL failure, or shader exception was produced.
- The final source after removing the temporary third-person and frame-dump hooks completed `build`; Gradle reported `test NO-SOURCE`.

### Test-only instrumentation

- The runtime command temporarily enabled a JVM-gated third-person switch and a frame-dump directory. Both hooks were removed before the final build and are absent from the packaged source.
- The mock OBS endpoint used port `4456` so the real OBS process on `4455` remained untouched; the repository test configuration was restored to `4455` afterwards.

### Not covered

- The revised breathing sound was structurally validated and included in the build, but the user did not perform a second listening acceptance after the alpha.5 asset replacement in this run.
- A long real-OBS recording of the repaired build was not produced; direct audience back-buffer PNGs verified the effect pixels independently of OBS encoding.

### Evidence

- Runtime log: `run/logs/latest.log`, including audience route and mode timestamps around `01:57:04-01:57:09`.
- Direct audience frames: `build/tmp/alpha5-capture/head-mode-0.png`, `head-mode-1.png`, and `head-mode-2.png`.
- Breath asset inspection: `src/main/resources/assets/bddmod/sounds/recording_breath.ogg`, 2.18 seconds, 48 kHz mono Vorbis.
- Final installable artifact: `build/libs/bddmod-1.3.0-alpha.5-all.jar`, SHA-256 `687C8C9C8DADB7C5E2E4876153062561399A50154C745E09FF840F3DB6548715`.

## 1.3.0-alpha.4 user visual acceptance

- Test date: 2026-09-22
- Test type: Real OBS recording and user visual/audio acceptance test
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: FAIL - runtime branches executed without a crash, but the user reported that the breathing sounded like wind and that the expected visual effects did not appear correctly

### Runtime evidence

- The client entered the existing single-player world, connected to the real OBS WebSocket service, opened the `854x480` off-screen audience window, and routed the active OBS capture input to `BDD Audience Output:GLFW30:java.exe`.
- Three real recording intervals were detected: `01:23:05-01:23:13`, `01:23:18-01:24:21`, and `01:24:23-01:24:45`.
- The recording vein, breathing/heartbeat audio, and shared pulse branches started and stopped with the OBS recording state.
- During third-person rendering, the log recorded first-hit execution for `MOSAIC`, `DEFORMATION`, and `DISTORTION`, with finite projected center and radius values.
- The client shut down normally and Gradle reported `BUILD SUCCESSFUL in 2m 48s`. No new crash report, audience OpenGL failure, or audience-shader exception was produced.

### User-observed failures

- The breathing track sounded like wind rather than recognizable breathing.
- The expected audience visual effects did not appear correctly during the user's inspection. Log messages proving that a branch executed are therefore not treated as visual acceptance.
- This result supersedes the earlier alpha.4 runtime-only PASS for subjective audio and visual quality; that earlier run did not include direct user inspection of the audience pixels.

### Follow-up observations

- The breathing implementation loops one OGG sample continuously and changes only its volume envelope, which may preserve a wind-like noise character even when synchronization is correct.
- The head mode is reselected on every successful player render, while the distortion and deformation offsets are small at the observed head radius. Excessively rapid switching and low pixel displacement are plausible contributors, but this run did not isolate the root cause.
- The OBS capture routing log confirms that settings were updated, but does not prove which final pixels OBS encoded; the capture path still requires frame-level verification during the repair.

### Evidence

- Runtime log: `run/logs/latest.log`.
- OBS recordings: `E:/OBS/2026-09-22 01-23-04.mp4`, `E:/OBS/2026-09-22 01-23-18.mp4`, and `E:/OBS/2026-09-22 01-24-23.mp4`.

## 1.3.0-alpha.4 randomized audience player-head effects runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - third-person head projection, randomized audience shader modes, and mock OBS routing
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Runtime command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable -Dbddmod.testThirdPerson=true'; .\gradlew.bat runClient --args="--quickPlaySingleplayer 新的世界" --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary OBS WebSocket 5 endpoint at `127.0.0.1:4456`
- Build command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - development-client launch, third-person local-head projection, all three randomized modes, audience shader execution, final packaging, and embedded metadata verification

### Verified behaviors

- The client loaded `1.3.0-alpha.4`, entered the existing single-player world, connected to the isolated mock OBS endpoint, and opened the `854x480` off-screen audience target and window.
- A temporary JVM-gated test hook selected third-person view after entering the world. It was removed before the final build and is not present in the packaged source or JAR.
- The projected head center and radii were finite and plausible. The runtime log recorded `DISTORTION`, `DEFORMATION`, and `MOSAIC` at center approximately `(0.5, 0.4975)` with radius approximately `(0.0386, 0.0735)`.
- The mock capture source was routed to `BDD Audience Output:GLFW30:java.exe`, exercising the audience-only render path while the three head modes were selected.
- No new crash report, audience-shader failure, OpenGL exception, or render-thread error was produced during the run.
- The final source without the temporary test hook completed the full `build`; Gradle reported `test NO-SOURCE`, and the installable JAR embeds version `1.3.0-alpha.4` and license `WTFPL`.

### Not verified

- The off-screen audience window's pixels were not visually inspected because the local computer-control interface could not enumerate Windows application windows. Mode execution and projected coordinates are runtime-log verified, but the subjective mosaic, distortion, deformation, masking, and transition appearance still requires manual recording review.
- First-person suppression was source-reviewed but was not independently exercised during this third-person runtime test.
- The real OBS process listening on `4455` was deliberately left untouched; this run used the mock endpoint on `4456` and did not inspect a recorded video.

### Warnings and observations

- The existing vanilla warning that `rendertype_entity_translucent_emissive` could not find `Sampler2` remained; no warning named the audience shader or any of its new uniforms.
- The development task was interrupted after the required evidence was collected, so a menu-driven client shutdown was not covered. The mock endpoint's expected connection-reset traceback occurred when the client and endpoint were stopped.

### Evidence

- Runtime log: `run/logs/latest.log`, especially the audience creation and three first-hit mode records at `21:49:18`.
- Final installable artifact: `build/libs/bddmod-1.3.0-alpha.4-all.jar` (220892 bytes), SHA-256 `013D559FCB81C814F6A31F2EB3124349772E67C092136A7F3D024E31A7BF1A0D`.

## 1.3.0-alpha.3 automatic OBS audience capture routing runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime and OBS WebSocket 5 routing protocol test
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Runtime command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary OBS WebSocket 5 endpoint at `127.0.0.1:4455`
- Build command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS - development-client launch, mock and real OBS source routing, resource cleanup, compilation, packaging, and embedded metadata verification

### Verified behaviors

- The client loaded `1.3.0-alpha.3`, entered the single-player world, and kept `renderRouteTestEnabled = false` while `autoRouteAudienceCapture = true`.
- With two enabled mock sources, the WebSocket request sequence inspected both settings, selected only the source targeting Minecraft, and sent `SetInputSettings` with `overlay = true` and `window = BDD Audience Output:GLFW30:java.exe`.
- The `854x480` audience target and borderless audience window opened beyond the virtual desktop while OBS reported recording, then both released after the mock endpoint disconnected.
- After reconnecting to the running OBS instance, the mod changed the current scene's enabled `game_capture` source to `BDD Audience Output:GLFW30:java.exe` without creating a new source. The final scheduling guard that waits for the audience window was subsequently build-verified.
- The full `build` completed successfully; Gradle reported `test NO-SOURCE`, and the installable JAR contains version `1.3.0-alpha.3`, license `WTFPL`, and the bundled WebSocket dependency.

### Not verified

- The final recorded video pixels were not inspected, so long-duration off-screen capture continuity still requires a manual recording review.
- The no-candidate and ambiguous-candidate safety branches were reviewed in source but were not exercised in this runtime session.

### Warnings and observations

- A plain `.\gradlew.bat build` attempt stopped before compilation with the environment-specific `Unable to establish loopback connection` error; the documented local workaround command above completed the same `build` task successfully.
- Existing Forge/vanilla warnings about MDK language-provider metadata, Goat Horn sounds, `Sampler2`, performance counters, and Realms authorization were unrelated to this change.

### Evidence

- Runtime log: `run/logs/latest.log` and `run/logs/debug.log`.
- Protocol transcript: `build/tmp/mock_obs_route_test.out`.
- Installable artifact: `build/libs/bddmod-1.3.0-alpha.3-all.jar`, SHA-256 `BA9E47920D4227381E6F5FD175EC46754BD6BC87BA713434A17EE83576E1B6FF`.

## 1.3.0-alpha.2 hidden audience message runtime

- Test date: 2026-09-21
- Test type: Forge development-client runtime test - audience-only glyph texture, timed reveal, and disconnect cleanup
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17.0.15 (Eclipse Adoptium)
- Mod ID: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat runClient --args='--quickPlaySingleplayer "新的世界"' --init-script build\tmp\codex-direct-javac.init.gradle` with a temporary local WebSocket 5 endpoint at `127.0.0.1:4455`
- Result: PASS - compilation, packaging, SemVer metadata loading, two-texture audience shader execution, and disconnect cleanup

### Verified behaviors

- The client loaded mod version `1.3.0-alpha.2`, entered the single-player world, and rendered on AMD Radeon RX 6750 GRE OpenGL 4.6 / LWJGL 3.3.1.
- The local OBS protocol endpoint reported `RECORDING`; the route created the `854x480` audience target and opened `BDD Audience Output`, proving the shader linked and presented with both the frame and runtime glyph texture samplers.
- Stopping the temporary endpoint logged `Audience output window resources released`, then released the audience target and stopped synchronized audio and pulse effects without crashing the client.
- The temporary diagnostic setting was restored to `renderRouteTestEnabled = false` after the run.

### Not verified

- Direct screenshot confirmation of the timed `WE SEE YOU` reveal was unavailable because the local Windows capture interface returned no bindable application windows. Its subjective size, placement, and opacity remain for manual review.
- The Gradle development task was interrupted after the world had auto-saved and the audience resources had released; a normal menu-driven client shutdown was not covered.

### Warnings and observations

- The run emitted the pre-existing vanilla warning that `rendertype_entity_translucent_emissive` could not find `Sampler2`; no new warning named the audience shader, hidden-text sampler, or output route.

### Evidence

- Runtime log: `run/logs/latest.log` and `run/logs/debug.log`, including audience creation at `13:13:57` and resource release at `13:14:40`.
- Installable artifact: `build/libs/bddmod-1.3.0-alpha.2-all.jar`, SHA-256 `588069DAF55A1BE7FC439A94E92FD44F6E5F29AB6DE393605C76CE3B17EAE353`.

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

## 1.3.0-alpha.6 verification result

- Test date: 2026-09-22
- Test type: Build and resource verification after removing recording breathing audio
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java runtime: 11 (`JAVA_HOME=C:\Program Files\Java\jdk-11`); Java 17 remains the configured bytecode target
- Mod: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS — `BUILD SUCCESSFUL` (18s)

### Verified behaviors

- Java compilation, resource processing, reobfuscation, shaded JAR packaging, and the full Gradle `build` task completed successfully.
- The distributable artifact was produced at `build/libs/bddmod-1.3.0-alpha.6-all.jar`.
- The packaged metadata reports version `1.3.0-alpha.6` and license `WTFPL`.
- The packaged sound registry contains only `recording_heartbeat`; no breathing sound entry or asset is present.
- SHA-256: `A14F20A826AA99BB273D4B3F8F624D92F95456443436107CBADDFE8F29197864`.

### Not covered

- No development-client runtime session was run for this change, so in-game heartbeat playback, visual pulse synchronization, and the absence of breathing audio were not independently verified in a live world.

### Evidence

- Gradle output: `BUILD SUCCESSFUL in 18s`
- Artifact: `build/libs/bddmod-1.3.0-alpha.6-all.jar`
- Runtime test evidence: not generated for this change.

## 1.3.0 stable release verification result

- Test date: 2026-09-22
- Test type: Stable release build and packaged-resource verification
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java runtime: 11 (`JAVA_HOME=C:\Program Files\Java\jdk-11`); Java 17 remains the configured bytecode target
- Mod: `bddmod`
- Command: `$env:GRADLE_USER_HOME='C:\Users\Administrator\.gradle'; $env:JAVA_HOME='C:\Program Files\Java\jdk-11'; $env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=Z:\bddmod-unavailable'; .\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`
- Result: PASS — `BUILD SUCCESSFUL` (19s)

### Verified behaviors

- The stable version compiled, processed resources, reobfuscated, packaged, and completed the full Gradle `build` task successfully.
- The distributable artifact was produced at `build/libs/bddmod-1.3.0-all.jar`.
- The packaged mod metadata reports version `1.3.0`; the packaged license remains `WTFPL`.
- The packaged sound registry contains only `recording_heartbeat`; no breathing sound entry or asset is present.
- SHA-256: `AAF56AEE9DA2AAA9B60B5397C760552B6ED1B684A2DE4319C0A59F53B4CCEE57`.

### Not covered

- No development-client runtime session was run for the stable promotion, so live heartbeat playback, visual pulse synchronization, and OBS behavior were not independently re-tested in this build.

### Evidence

- Gradle output: `BUILD SUCCESSFUL in 19s`
- Artifact: `build/libs/bddmod-1.3.0-all.jar`
- Runtime test evidence: not generated for the stable promotion.

## 1.4.0-alpha.1 local build and artifact verification

- Test date: 2026-09-22
- Test type: Local prerelease build and installable-artifact verification
- Minecraft: 1.20.1
- Forge: 47.4.10
- Java runtime: 11 (`JAVA_HOME=C:\Program Files\Java\jdk-11`); Java 17 remains the configured bytecode target and CI runtime
- Mod: `bddmod`
- Commands: `.\gradlew.bat compileJava`; `.\gradlew.bat processResources`; `.\gradlew.bat build --init-script build\tmp\codex-direct-javac.init.gradle`; `python scripts\verify_build.py`
- Result: PASS — local build and artifact verification completed

### Verified behaviors

- The version is `1.4.0-alpha.1` and the generated installable artifact is `build/libs/bddmod-1.4.0-alpha.1-all.jar`.
- The verifier checks embedded version, WTFPL license, bundled `nv-websocket-client-2.14`, required resources, and SHA-256.
- Artifact SHA-256: `12050E0FD9C7E36B86A4E949E981FE85F9A92E7B7F90A73FA28C1CB2EDBDF7D1`.
- The new GitHub Actions workflow is configured for pull requests and pushes to `main`, using Java 17 and no Release or tag mutation.

### Manual runtime evidence format

- Development-client tests remain manual and must record the exact `runClient` command, result, runtime log path, screenshot path when available, and verified versus unverified behavior.
- No `runClient` session or runtime screenshot was executed for this alpha.1 implementation; gameplay, OBS, rendering, and audio behavior remain unverified for this change.

### Evidence

- Local Gradle output: `BUILD SUCCESSFUL`
- Artifact verifier output: `verify_build: PASS`
- Negative checks: PASS — missing artifact and version mismatch fixtures both returned exit code `1`.
- CI workflow: `.github/workflows/ci.yml`
- Verifier: `scripts/verify_build.py`
