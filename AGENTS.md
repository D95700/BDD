# Repository Guide

## Project shape

- This is a single-project Minecraft Forge MDK, pinned to Minecraft `1.20.1`, Forge `47.4.10`, official mappings, and Java 17 bytecode/toolchain settings.
- The mod entrypoint is `src/main/java/com/example/bddmod/BDDMod.java`; client configuration is defined in `src/main/java/com/example/bddmod/Config.java`.
- `src/generated/resources` is a main resource directory even when it does not exist yet; it is owned by the Forge data generator.

## Change documentation

- After every code or resource change, update `CHANGELOG.md` in the same change set.
- Write entries for the README/design team in user-facing language: describe visible behavior, configuration changes, installation or compatibility impact, and verification status.
- Keep technical implementation details concise and separate from the user-facing summary so the file can be reused when preparing the README and release notes.
- Set `mod_authors` directly from the contributors' GitHub names. When there are multiple contributors, list the names with English commas and do not substitute a project or organization name.

## Roadmap release policy

- Completing a roadmap section is a formal release boundary: remove any `-test` prerelease suffix, advance the project version to the next appropriate stable version, and synchronize all user-facing version references.
- For every completed roadmap section, run the full `build`, commit the completed section, push it to GitHub, and publish a GitHub Release with the installable `-all.jar` artifact.
- Use prerelease/test versions only while a roadmap section is still under development; do not publish a completed section as a test release.

## Test logging

- Store runtime test results in the repository-root file `TESTLOG.md`; the filename must remain fully uppercase.
- Update `TESTLOG.md` after each development-client or development-server runtime test so other teams can read the latest verification status.
- Record the test date, test type, Minecraft/Forge/Java versions, Mod ID, exact Gradle command, and an explicit `PASS` or `FAIL` result.
- Record verified behaviors separately from observations, warnings, and features not covered by the test.
- Include evidence paths such as the relevant runtime log, test world, or Gradle output when available.
- Runtime test results must distinguish successful game launching from exhaustive feature verification; do not claim untested gameplay features were verified.
- Keep `TESTLOG.md` user-readable and concise. Do not paste the full game log into it; summarize relevant warnings and link or point to the source log instead.

## Commands

- Use the checked-in wrapper (`.\gradlew.bat` on Windows, `./gradlew` elsewhere), not a system Gradle installation.
- Full verification/package: `.\gradlew.bat build`. This compiles, expands resource templates, and runs `reobfJar`; the distributable JAR is under `build/libs/`.
- Fast Java-only check: `.\gradlew.bat compileJava`. Resource-template check: `.\gradlew.bat processResources`.
- Launch development environments with `runClient` or `runServer`; both use `run/` as their working directory. Generate IDE launch configurations with `genIntellijRuns`, `genEclipseRuns`, or `genVSCodeRuns`.
- Regenerate data with `.\gradlew.bat runData`; it writes `src/generated/resources/`, reads existing resources from `src/main/resources/`, and uses `run-data/` as its working directory.
- Do not use `runGameTestServer` as routine verification: no GameTests currently exist, and the configured task is expected to fail when none are registered.
- There are currently no test sources, lint/formatter plugins, or CI checks; `build` reports `test NO-SOURCE` rather than exercising the mod in-game.

## Forge-specific synchronization

- Mod metadata/version values come from `gradle.properties` and are expanded into `META-INF/mods.toml` and `pack.mcmeta` by `processResources`; keep the `${...}` placeholders in those resource templates.
- If renaming the mod, update `mod_id` and `mod_group_id` in `gradle.properties`, the Java package, and `BDDMod.MODID` together. The mod ID also selects the namespace loaded by all configured GameTest runs.
- Keep registrations on the mod event bus (`context.getModEventBus()`), while runtime Forge events use `MinecraftForge.EVENT_BUS`; the current entrypoint deliberately uses both.
- `publish` targets the repository-local `mcmodsrepo/` directory, not a remote artifact repository.
