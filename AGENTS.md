# AGENTS.md — PIT Idea Plugin

**After every code change, update this file to reflect the new state of the project.**

## Project Overview

IntelliJ IDEA plugin for [PIT Mutation Testing](http://pitest.org). Adds a run configuration and context menu actions to execute PIT directly within the IDE. Current version: **1.4.13-SNAPSHOT**, bundling PIT **1.25.8** and JUnit5 plugin **1.2.3**.

## Build & Run Commands

```bash
./gradlew build          # Full build (compile + test + format check)
./gradlew test           # Run unit tests only
./gradlew spotlessApply  # Auto-format code (Java + Kotlin)
./gradlew spotlessCheck  # Check formatting without modifying
```

CI runs `./gradlew build` on push/PR to `master` (Java 25 temurin, Ubuntu).

## Project Structure

```
src/main/java/pl/mjedynak/idea/plugins/pit/
├── actions/          # IntelliJ context menu actions (PitAction hierarchy)
├── cli/              # CLI argument model and container
│   ├── model/        # PitCommandLineArgument enum
│   └── factory/      # DefaultArgumentsContainerFactory
├── configuration/    # Run configuration (PitRunConfiguration, PitConfigurationType)
└── gui/              # Settings editor form + populators
    └── populator/    # Form <-> CLI argument mapping

src/main/kotlin/pl/mjedynak/idea/plugins/pit/
├── JavaParametersCreator.kt       # Builds JavaParameters for PIT execution
├── ClassPathPopulator.kt          # Assembles PIT classpath from plugin dir
├── console/DirectoryReader.kt     # Finds latest report directory
├── configuration/PitRunConfigurationStorer.kt  # XML persistence
├── gradle/GradleProjectDeterminer.kt           # Detects Gradle projects
├── maven/                        # Maven project detection + pom.xml parsing
│   ├── MavenProjectDeterminer.kt
│   └── MavenPomReader.kt
└── cli/factory/DefaultArgumentsContainerPopulator.kt  # Default arg population

src/test/kotlin/                   # All tests are Kotlin (9 test files)
META-INF/plugin.xml                # Plugin descriptor (actions, extensions)
```

## Technology Stack

- **Languages**: Java (actions, config, GUI, CLI model) + Kotlin (utilities, project detection, tests)
- **Build**: Gradle 9.6.1, Kotlin DSL, IntelliJ Platform Gradle Plugin 2.18.1
- **Target**: IntelliJ IDEA 2026.2, Java 25 toolchain
- **PIT**: 1.25.8 (bundled as non-transitive dependencies)
- **Testing**: JUnit 5 (Jupiter 6.1.1) + Mockito-Kotlin 6.3.0
- **Formatting**: Spotless — Palantir Java Format (Java), ktlint (Kotlin)

## Code Conventions

- **Formatting is enforced** — run `./gradlew spotlessApply` before committing
- **Test method names** use backtick-quoted descriptive names: `` `should return absent for empty directory` ``
- **Nullable IntelliJ APIs** handled with `?.` safe calls in Kotlin, `@Nullable`/`@NotNull` in Java
- **Commit messages**: Conventional commits format (`feat:`, `fix:`, `chore:`)

## Testing

- All tests in `src/test/kotlin/`
- Run with: `./gradlew test`
- `ClassPathPopulatorTest` is a meta-test: parses `build.gradle.kts` to verify `ClassPathPopulator.kt` references correct PIT versions (prevents version drift)

## Architecture

### Action Hierarchy (Command Pattern)

```
AnAction (IntelliJ SDK)
└── PitAction (abstract) — enables/disables based on Project+Module availability
    ├── RunAllPitAction — runs all tests in module
    └── DirectoryOrFilePitAction (abstract) — adds file/directory resolution
        ├── RunSomeTestsPitAction — targets selected TEST directory
        └── PitTestSomeClassesAction — targets selected SOURCE directory
```

### Run Configuration Flow

```
Context Menu Action / Run Config UI
    ↓
PitRunConfiguration (ModuleBasedConfiguration)
    ├── PitConfigurationForm — 5 text fields (target classes, tests, source dir, report dir, other)
    ├── PitRunConfigurationStorer — XML persistence (readExternal/writeExternal)
    └── getState() → JavaCommandLineState
        ├── JavaParametersCreator
        │   ├── ProgramParametersListPopulator — form fields → CLI args
        │   └── ClassPathPopulator — adds PIT jars from plugin install dir
        └── ConsoleView → DirectoryReader → clickable report link
```

### Main class executed in forked JVM
`org.pitest.mutationtest.commandline.MutationCoverageReport`

## Version Management

**⚠️ PIT version is declared in 3 places** — all must be updated together:

| Location | What | Current        |
|----------|------|----------------|
| `build.gradle.kts` | `val pitVersion` / `val pitJunit5PluginVersion` | 1.25.8 / 1.2.3 |
| `ClassPathPopulator.kt` | JAR filename strings | 1.25.8 / 1.2.3 |
| `META-INF/plugin.xml` | Description text ("Bundled with PIT ...") | 1.25.8         |

`ClassPathPopulatorTest` enforces consistency between `build.gradle.kts` and `ClassPathPopulator.kt` — it parses the build file and asserts the versions match. It does NOT check `plugin.xml`.

## Common Tasks

### Adding a new CLI argument
1. Add enum value to `PitCommandLineArgument.java` (e.g., `MUTATORS("--mutators")`)
2. Add the field to `PitConfigurationForm.java` + `PitConfigurationForm.form`
3. Wire it in `ProgramParametersListPopulator.java` (form → ParametersList)
4. Add default value logic in `DefaultArgumentsContainerPopulator.kt`
5. Add persistence in `PitRunConfigurationStorer.kt`
6. Add test in `PitCommandLineArgumentTest.kt`

### Adding a new context menu action
1. Create class extending `PitAction` or `DirectoryOrFilePitAction`
2. Register in `META-INF/plugin.xml` under `<actions>`
3. Choose menu group: `ProjectViewPopupMenuRunGroup` (project view) or `EditorPopupMenu.Run` (editor)

### Updating PIT version
1. Update `val pitVersion` in `build.gradle.kts`
2. Update JAR filenames in `ClassPathPopulator.kt`
3. Update `META-INF/plugin.xml` description
4. Run `./gradlew test` — `ClassPathPopulatorTest` will catch build.gradle/classpath mismatches

## Key Gotchas

- **`ClassPathPopulator.kt`** hardcodes JAR filenames including version numbers — changing `build.gradle.kts` without updating this file will cause runtime classpath errors
- **`PitConfigurationForm.resetEditorFrom()`/`applyEditorTo()` are empty** — form fields are read directly at execution time, not through the standard SettingsEditor contract

