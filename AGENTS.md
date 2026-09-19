# Repository Guide

## Project shape

- This is a single-project Minecraft Forge MDK, pinned to Minecraft `1.20.1`, Forge `47.4.10`, official mappings, and Java 17 bytecode/toolchain settings.
- The mod entrypoint is `src/main/java/com/example/bddmod/BDDMod.java`; client configuration is defined in `src/main/java/com/example/bddmod/Config.java`.
- `src/generated/resources` is a main resource directory even when it does not exist yet; it is owned by the Forge data generator.

## Change documentation

- After every code or resource change, update `CHANGELOG.md` in the same change set.
- Write entries for the README/design team in user-facing language: describe visible behavior, configuration changes, installation or compatibility impact, and verification status.
- Keep technical implementation details concise and separate from the user-facing summary so the file can be reused when preparing the README and release notes.

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
