# AGENTS.md — PIT Idea Plugin

**After every code change, update this file to reflect the new state of the project.**

## Project Overview

IntelliJ IDEA plugin for [PIT Mutation Testing](http://pitest.org). Adds a run configuration and context menu actions to execute PIT directly within the IDE. Current version: **1.4.13-SNAPSHOT**, bundling PIT **1.25.8** and JUnit5 plugin **1.2.3**.

## Build & Run Commands

```bash
./gradlew build          # Full build (compile + test + format check)
./gradlew test           # Run unit tests only
./gradlew integrationTest # Run integration tests (starts real IDE with plugin)
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
├── gui/              # Settings editor form + populators
│   └── populator/    # Form <-> CLI argument mapping
└── PitOutputReader.java  # Reads PIT process info via reflection for integration tests
└── PitTestHelper.java  # E2E test helper (runs PIT in-forked-JVM for integration tests)

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

src/test/kotlin/                   # All tests are Kotlin (8 test files)
src/integrationTest/
├── kotlin/                        # Integration tests using IntelliJ Starter framework
│   └── PitPluginIntegrationTest.kt  # 4 tests: plugin load, actions, config type, E2E PIT execution
└── resources/testProject/         # Test project with Calculator.java, CalculatorTest.java
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

- Unit tests in `src/test/kotlin/` — run with: `./gradlew test`
- Integration tests in `src/integrationTest/kotlin/` — run with: `./gradlew integrationTest`
- `ClassPathPopulatorTest` is a meta-test: parses `build.gradle.kts` to verify `ClassPathPopulator.kt` references correct PIT versions (prevents version drift)
- **Integration test approach**: Uses IntelliJ Starter framework (`testIdeUi`) to start a real separate IDE process with the plugin installed. Communication via `@Remote` stubs over JMX. `PitTestHelper.java` (in plugin main source) is invoked remotely — it builds the classpath and PIT command directly, then starts PIT as a forked JVM process. This bypasses IntelliJ's unreliable module root resolution in test environments.
- **Integration test setup**: `setupTestProject` copies test project sources + Gradle wrapper to `build/testProject/`. `compileTestProject` then runs `./gradlew classes testClasses` to pre-compile sources (with JUnit resolved from Maven Central). The `integrationTest` task depends on `buildPlugin`, `setupTestProject`, and `compileTestProject`.

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

`PitVersionConsistencyTest` enforces consistency between `build.gradle.kts`, `ClassPathPopulator.kt`, and `META-INF/plugin.xml` — it parses the build file and asserts the versions match in all locations.

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
4. Run `./gradlew test` — `PitVersionConsistencyTest` will catch build.gradle/classpath mismatches

## Key Gotchas

- **`ClassPathPopulator.kt`** hardcodes JAR filenames including version numbers — changing `build.gradle.kts` without updating this file will cause runtime classpath errors
- **`PitConfigurationForm.resetEditorFrom()`/`applyEditorTo()` are empty** — form fields are read directly at execution time, not through the standard SettingsEditor contract

## Integration Test Troubleshooting

### Test Project Model
- Test project (`src/integrationTest/resources/testProject/`) is a Gradle project. The test IDE imports it via Gradle wrapper (not manual `.idea/` XML files).
- The `build.gradle.kts` JUnit version MUST match the plugin's bundled `junit-platform-launcher` version. Currently both are `6.1.1`. A mismatch (e.g., `5.11.1` in test project + `6.1.1` launcher) causes PIT to fail silently with `"Pitest could not run any tests"` because the launcher and engine versions are incompatible.

### Debugging PIT failures
- PIT runs as a forked JVM via `PitRunConfiguration.startProcess()`. If the process exits before a `ProcessAdapter` is registered (because `super.startProcess()` starts the process), the listener misses `onTextAvailable` events. **Fix**: create the `ColoredProcessHandler` directly, attach listeners, then call `startNotify()`:
  ```java
  ColoredProcessHandler handler = new ColoredProcessHandler(commandLine);
  handler.addProcessListener(new ProcessAdapter() { ... });
  handler.startNotify();
  ```
- To capture PIT output for debugging, run the exact command line from the test report directly in a terminal (extract from `PIT output: Command line:` in the HTML report). This bypasses IntelliJ and shows PIT's actual error messages.

### Key Integration Test Files
- `PitOutputReader.java` — reads PIT process info from `RunContentManager` via reflection (exit code, command line, report dir contents, and optionally `pit-output.log` written by `ProcessAdapter`).
- `PitPluginIntegrationTest.kt` — `@Remote` stubs call `PitTestHelper`/`PitOutputReader` inside the test IDE over JMX.
- `PitTestHelper.java` Creates `PitRunConfiguration` and calls `executeConfiguration()` with `waitForProcessCompletion=true`.

### PIT HTML Report Structure
PIT creates a timestamped subdirectory for each run (e.g., `report/20260728.../index.html`). The `DirectoryReader` resolves the latest directory. Tests should walk the report tree to find `index.html`, not assume it's directly in the report dir.`


