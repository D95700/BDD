# BDD Mod Test Log

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
