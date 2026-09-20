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

## Conversation completion

- If any source code or resource file is changed during a conversation, complete the relevant verification, commit the intended change set, and push the current branch before sending the final response for that conversation. This applies to development work as well as stable releases; do not leave code or resource changes only in the local working tree between conversations.
- Before committing, inspect the working tree and staged diff, exclude unrelated user changes and secrets, and keep the required `CHANGELOG.md` and test records in the same commit.
- If the user explicitly asks to pause, not commit, or not push, follow that instruction and report the remaining local state. If authentication, connectivity, or a remote rejection prevents pushing, preserve the local commit and report the exact blocker instead of claiming completion.

## Roadmap release policy

- Completing a roadmap section is a formal release boundary: remove any development or prerelease suffix, advance the project version to the next appropriate stable version, and synchronize all user-facing version references.
- For every completed roadmap section, run the full `build`, commit the completed section, push it to GitHub, and publish a GitHub Release with the installable `-all.jar` artifact.
- Preserve every installable `-all.jar` produced during that roadmap section until the stable release is published. Upload the retained development or prerelease `-all.jar` files and the final stable `-all.jar` together as assets of the same GitHub Release so the complete section history remains downloadable.
- Do not upload thin JARs without bundled runtime dependencies. List every uploaded version and SHA-256 digest in the release notes, and verify each retained artifact reports the matching embedded mod version before publishing.
- Do not run `clean` or otherwise remove retained development artifacts between the final development build and the section release. If a required artifact is missing, reproduce it from its original source revision rather than relabeling current code with an older version.
- Use only the development and prerelease formats defined below while a roadmap section is still under development; do not publish a completed section as a test release.

## Version naming

- Stable versions use `MAJOR.MINOR.PATCH`, for example `1.2.0`. All three parts are non-negative integers without leading zeroes. Compare them numerically from left to right, so `1.9.0 < 1.10.0 < 1.11.0`.
- Increment `MAJOR` for incompatible API changes, major module changes, or architectural changes. Reset `MINOR` and `PATCH` to `0` at the same time.
- Increment `MINOR` for compatible feature additions, meaningful behavior changes, or deprecations. Reset `PATCH` to `0` at the same time.
- Increment `PATCH` for compatible bug fixes and small changes. A severe bug fix is sufficient reason for a patch release.
- A `0.y.z` version denotes initial development and potentially unstable APIs. `1.0.0` and later denote an established stable API unless a development or prerelease suffix is present.
- Development builds use `MAJOR.MINOR.PATCH.devN`, where `N` is a positive, monotonically increasing integer, for example `1.3.0.dev4`.
- Prereleases use `MAJOR.MINOR.PATCH.aN`, `.bN`, or `.cN`, where `N` is a positive, monotonically increasing integer. `a` means Alpha, `b` means Beta, and `c` means Release Candidate. Do not create new `-testN` versions.
- Stage meanings are: Base for an incomplete foundation, Alpha for internal feature implementation with known defects, Beta for public testing after severe defects are removed but features may still change, RC for a near-final release candidate, and Release for the stable user-facing build. Stable Release versions omit the stage suffix.
- Development and prerelease versions sort before their matching stable release. For matching numeric parts, letter stages sort by ASCII order, for example `1.3.0.a1 < 1.3.0.b1 < 1.3.0.c1 < 1.3.0`.
- If a date-qualified build is explicitly required, use `MAJOR.MINOR.PATCH.YYYYMMDD_STAGE`, for example `1.3.0.20260920_beta`. The date is eight digits and must change on each calendar day that changes are made; `STAGE` is `base`, `alpha`, `beta`, `rc`, or `release`. Normal project releases omit both the date and textual stage and use `MAJOR.MINOR.PATCH`.
- Published versions are immutable. After a tag or release is published, never replace its code, resources, metadata, or artifact; make every subsequent published change under a new version.
- Keep `gradle.properties`, generated mod metadata, README badges and download names, changelog headings, Git tags, GitHub Release titles, and JAR filenames synchronized to the same version.

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
