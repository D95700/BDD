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

- Completing a roadmap section is a formal release boundary: remove any development or prerelease suffix, advance the project version to the next appropriate stable version, and synchronize all user-facing version references except `README.md`, which follows the post-release workflow below.
- For every completed roadmap section, run the full `build`, commit the completed section, push it to GitHub, and publish a GitHub Release with the installable `-all.jar` artifact.
- Preserve every installable `-all.jar` produced during that roadmap section until the stable release is published. Upload the retained development or prerelease `-all.jar` files and the final stable `-all.jar` together as assets of the same GitHub Release so the complete section history remains downloadable.
- Do not upload thin JARs without bundled runtime dependencies. List every uploaded version and SHA-256 digest in the release notes, and verify each retained artifact reports the matching embedded mod version before publishing.
- Do not run `clean` or otherwise remove retained development artifacts between the final development build and the section release. If a required artifact is missing, reproduce it from its original source revision rather than relabeling current code with an older version.
- Use only the SemVer prerelease formats defined below while a roadmap section is still under development; do not publish a completed section as a test release.

## README release synchronization

- `README.md` MUST describe the latest published stable release. Do not update it for development builds, prereleases, roadmap micro-steps, or an unpushed stable-version commit.
- The stable code/version commit MUST be built, committed, and pushed first, without modifying `README.md`. Publish and verify the corresponding GitHub Release and its downloadable `-all.jar` assets before editing the README.
- Only after the stable commit and GitHub Release have been pushed may `README.md` be updated. Make the README update as a separate documentation commit, then push that commit immediately; the release workflow is incomplete until this follow-up push succeeds.
- At minimum, synchronize the version badge, introductory current-version text, installable JAR filename, installation/download guidance, release summary, relevant configuration documentation, and completed roadmap status against the release that was actually published.
- After the README follow-up push, verify that its stable version, tag, and asset names exactly match the published GitHub Release. Commit and push any required correction immediately rather than deferring it to the next development cycle.

## Version naming (SemVer 2.0.0)

- This section supersedes all earlier project-specific version naming rules effective immediately. Existing published and development versions such as `1.3.0.dev1`, `1.3.0.dev2`, and `1.3.0.dev3` are historical, immutable records; do not rename or rewrite them. The next version change MUST use this SemVer policy.
- The version core MUST use `MAJOR.MINOR.PATCH` (`X.Y.Z`). `X`, `Y`, and `Z` MUST be non-negative integers with no leading zeroes. Compare them numerically from left to right, so `1.9.0 < 1.10.0 < 1.11.0`.
- Increment `MAJOR` for incompatible public API, module, or architectural changes. Reset `MINOR` and `PATCH` to `0` when `MAJOR` increments.
- Increment `MINOR` for backward-compatible features, meaningful compatible behavior changes, or deprecations. Reset `PATCH` to `0` when `MINOR` increments.
- Increment `PATCH` for backward-compatible bug fixes and other compatible corrections. A severe compatible bug fix is still a PATCH release.
- A `0.y.z` version denotes an initial-development API whose compatibility is not guaranteed. Version `1.0.0` and later represent the established public API, subject to prerelease status.
- A stable release has no suffix, for example `1.4.0` or `1.4.1`.
- A prerelease MUST follow the core after a hyphen and contain one or more dot-separated ASCII identifiers using only `[0-9A-Za-z-]`, for example `1.4.0-alpha.1`, `1.4.0-beta.1`, or `1.4.0-rc.1`. Numeric identifiers MUST NOT have leading zeroes. Project stage identifiers are `alpha`, `beta`, and `rc`; use a monotonically increasing numeric component for successive builds of the same stage.
- Roadmap development builds MUST use SemVer prereleases such as `1.4.0-alpha.1` and `1.4.0-alpha.2`. Do not create new `.devN`, `.aN`, `.bN`, `.cN`, `-testN`, or underscore date-stage versions after this policy takes effect.
- Build metadata MAY follow a release or prerelease after `+`, using dot-separated ASCII identifiers, for example `1.4.0+build.1` or `1.4.0-rc.1+sha.abc123`. Build metadata MUST NOT change version precedence and MUST NOT replace a required prerelease identifier.
- Version precedence compares `MAJOR`, `MINOR`, and `PATCH` numerically; a prerelease has lower precedence than its corresponding stable release. When prerelease cores match, compare identifiers left to right: numeric identifiers numerically, non-numeric identifiers by ASCII order, numeric identifiers lower than non-numeric identifiers, and a longer equal prefix has higher precedence. Ignore build metadata for precedence.
- Published versions are immutable. After a tag or release is published, never replace its code, resources, metadata, or artifact; issue a new version for every subsequent published change.
- Keep `gradle.properties`, generated mod metadata, changelog headings, test records, Git tags, GitHub Release titles, and JAR filenames synchronized to the same SemVer value (excluding an optional `v` prefix used only in Git tag names). Synchronize README badges and download names only through the post-release workflow above.

## Roadmap step versioning under SemVer

- Give every independently verifiable roadmap micro-step its own monotonically increasing SemVer prerelease, even when multiple micro-steps belong to one larger roadmap section. For example, use `1.4.0-alpha.1`, then `1.4.0-alpha.2`.
- Bump `mod_version` to the next valid SemVer prerelease before implementing the next micro-step and synchronize that value in the changelog heading, generated metadata, test records, and installable `-all.jar` filename. Leave `README.md` unchanged until a stable release has been pushed and published.
- When the roadmap section is complete, promote the accepted prerelease line to the next stable core version without a prerelease suffix, such as `1.4.0`; preserve each prerelease `-all.jar` so the completed section can publish the full sequence together.
- Do not combine separately verifiable micro-steps under one prerelease, and do not relabel an existing artifact as another version.

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
