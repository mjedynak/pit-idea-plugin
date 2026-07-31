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

CI runs `./gradlew integrationTest build` on push/PR to `master` (Java 25 temurin, Ubuntu).

**After every significant code change, run `./gradlew integrationTest` to verify the plugin loads and runs PIT end-to-end in a real IDE process.** `build` alone does not cover this.

## Project Structure

```
src/main/kotlin/pl/mjedynak/idea/plugins/pit/
├── actions/          # IntelliJ context menu actions (PitAction hierarchy)
│   ├── PitAction.kt
│   ├── PitActionUtils.kt
│   ├── DirectoryOrFilePitAction.kt
│   ├── RunAllPitAction.kt
│   ├── RunSomeTestsPitAction.kt
│   └── PitTestSomeClassesAction.kt
├── cli/              # CLI argument model and container
│   ├── PitCommandLineArgumentsContainer.kt
│   ├── PitCommandLineArgumentsContainerImpl.kt
│   ├── model/
│   │   └── PitCommandLineArgument.kt
│   └── factory/
│       ├── DefaultArgumentsContainerFactory.kt
│       └── DefaultArgumentsContainerPopulator.kt
├── configuration/    # Run configuration (PitRunConfiguration, PitConfigurationType)
│   ├── PitRunConfiguration.kt
│   ├── PitRunConfigurationFactory.kt
│   ├── PitConfigurationType.kt
│   └── PitRunConfigurationStorer.kt
├── gui/              # Settings editor form + populators
│   ├── PitConfigurationForm.kt
│   └── populator/
│       ├── PitConfigurationFormPopulator.kt
│       └── ProgramParametersListPopulator.kt
├── JavaParametersCreator.kt       # Builds JavaParameters for PIT execution
├── ClassPathPopulator.kt          # Assembles PIT classpath from plugin dir
├── console/DirectoryReader.kt     # Finds latest report directory
├── gradle/GradleProjectDeterminer.kt           # Detects Gradle projects
└── maven/                        # Maven project detection + pom.xml parsing
    ├── MavenProjectDeterminer.kt
    └── MavenPomReader.kt

src/testSupport/kotlin/pl/mjedynak/idea/plugins/pit/
├── PitTestHelper.kt           # E2E test helper (creates PitRunConfiguration and executes it)
├── PitActionTestHelper.kt     # Verifies action visibility/enablement and simulates user clicking the action
└── PitOutputReader.kt         # Reads PIT process info via reflection
src/test/kotlin/                   # All tests are Kotlin (8 test files)
src/integrationTest/
├── kotlin/                        # Integration tests using IntelliJ Starter framework
│   └── PitPluginIntegrationTest.kt  # 2 test scenarios: (1) plugin load + config + PIT via helper, (2) action visibility check + simulated click + PIT report
└── resources/testProject/         # Test project with Calculator.java, CalculatorTest.java
META-INF/plugin.xml                # Plugin descriptor (actions, extensions)
```

## Technology Stack

- **Languages**: Kotlin (all plugin source code)
- **Build**: Gradle 9.6.1, Kotlin DSL, IntelliJ Platform Gradle Plugin 2.18.1
- **Target**: IntelliJ IDEA 2026.2, Java 25 toolchain
- **PIT**: 1.25.8 (bundled as non-transitive dependencies)
- **Testing**: JUnit 5 (Jupiter 6.1.1) + Mockito-Kotlin 6.3.0
- **Formatting**: Spotless — ktlint (Kotlin)

## Code Conventions

- **Formatting is enforced** — run `./gradlew spotlessApply` before committing
- **Test method names** use backtick-quoted descriptive names: `` `should return absent for empty directory` ``
- **Nullable IntelliJ APIs** handled with `?.` safe calls in Kotlin
- **Commit messages**: Conventional commits format (`feat:`, `fix:`, `chore:`)

## Testing

- Unit tests in `src/test/kotlin/` — run with: `./gradlew test`
- Integration tests in `src/integrationTest/kotlin/` — run with: `./gradlew integrationTest`
- `ClassPathPopulatorTest` is a meta-test: parses `build.gradle.kts` to verify `ClassPathPopulator.kt` references correct PIT versions (prevents version drift)
- **Integration test approach**: Uses IntelliJ Starter framework (`testIdeUi`) to start a single IDE process shared across all test methods via `@BeforeAll`/`@AfterAll` in a `companion object`. Communication via `@Remote` stubs over JMX. `PitTestHelper.kt` and `PitActionTestHelper.kt` (in `src/testSupport/`, bundled in the plugin JAR) are invoked remotely.
- **Integration test setup**: `setupTestProject` (a Gradle `Sync` task) copies test project sources + committed `.idea` module config + Gradle wrapper to `build/testProject/`, deleting stale state (old reports, leftover `.idea`). `compileTestProject` runs `./gradlew copyTestLib classes testClasses` to pre-compile sources AND copy the test runtime classpath (JUnit jars) to `build/testLib/`. The `integrationTest` task depends on `buildPlugin`, `setupTestProject`, and `compileTestProject`.
- **Test project module config**: The test IDE does NOT import Gradle — it opens `build/testProject` as a plain project. The committed `src/integrationTest/resources/testProject/.idea/testProject.iml` provides the module with output dirs (`build/classes/java/main|test`) and a `junit5` module-library pointing at `build/testLib/*.jar`. Without this, `JavaParametersCreator` produces a PIT classpath with no compiled classes or JUnit → PIT fails with `No mutations found` and no report. The JUnit version in the `.iml` must match `build.gradle.kts` (`junit-jupiter:6.1.1` → `copyTestLib` task).
- **Report wait logic**: each test cleans `report/` (with retry, `cleanReportDir`) before running PIT, then `waitForHtmlReport` waits for the top-level `report/index.html` (or a new 14-digit timestamped subdir as a fallback) up to 120s. The report is NOT deleted after a run to avoid racing with the plugin's `processTerminated` handler that writes `pit-output.log` — the next test cleans it instead.
- **Action integration test flow**: test method calls `verifyActionUpdateForSourceClass()` to check action visibility/enablement, then `performActionForSourceClass()` to simulate user clicking the action (calls `PitAction.actionPerformed()` which triggers `ProgramRunnerUtil.executeConfiguration`). After that the test polls the filesystem for the HTML report and asserts report content via `assertReportFiles()`.

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
1. Add enum value to `PitCommandLineArgument.kt` (e.g., `MUTATORS("--mutators")`)
2. Add the field to `PitConfigurationForm.kt` + `PitConfigurationForm.form`
3. Wire it in `ProgramParametersListPopulator.kt` (form → ParametersList)
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
- Test project (`src/integrationTest/resources/testProject/`) is a Gradle project used ONLY for pre-compilation (via `compileTestProject`). The test IDE does NOT import Gradle — it opens `build/testProject` as a plain project using the committed `.idea/` XML files. The module's compiled output dirs and `junit5` module-library (pointing at `build/testLib/`) are what make PIT find the compiled classes and JUnit engine. If these are missing, PIT exits with `No mutations found` and no report.
- The `build.gradle.kts` JUnit version MUST match both the committed `.iml` library (jar filenames in `build/testLib/`) and the plugin's bundled `junit-platform-launcher` version. Currently all are `6.1.1`. A mismatch (e.g., `5.11.1` in test project + `6.1.1` launcher) causes PIT to fail silently with `"Pitest could not run any tests"` because the launcher and engine versions are incompatible.

### Debugging PIT failures
- PIT runs as a forked JVM via `PitRunConfiguration.startProcess()`. If the process exits before a `ProcessAdapter` is registered (because `super.startProcess()` starts the process), the listener misses `onTextAvailable` events. **Fix**: create the `ColoredProcessHandler` directly, attach listeners, then call `startNotify()`:
  ```kotlin
  val handler = ColoredProcessHandler(commandLine)
  handler.addProcessListener(object : ProcessAdapter() { ... })
  handler.startNotify()
  ```
- To capture PIT output for debugging, run the exact command line from the test report directly in a terminal (extract from `PIT output: Command line:` in the HTML report). This bypasses IntelliJ and shows PIT's actual error messages.

### Key Integration Test Files
- `PitTestHelper.kt` (in `src/testSupport/`) — Creates `PitRunConfiguration` and calls `executeConfiguration()` with `waitForProcessCompletion=true`.
- `PitActionTestHelper.kt` (in `src/testSupport/`) — Provides `verifyActionUpdateForSourceClass()` (checks right-click action is visible/enabled for a source class) and `performActionForSourceClass()` (simulates clicking the action, triggering PIT execution).
- `PitOutputReader.kt` (in `src/testSupport/`) — reads PIT process info from `RunContentManager` via reflection (exit code, command line, report dir contents, and optionally `pit-output.log` written by `ProcessAdapter`).
- `PitPluginIntegrationTest.kt` — `@Remote` stubs call `PitTestHelper`/`PitActionTestHelper`/`PitOutputReader` inside the test IDE over JMX. Both test methods share a single IDE process (started once in `@BeforeAll`, closed in `@AfterAll`). Each test cleans `report/`, runs PIT, waits for a NEW report (top-level `index.html` or a fresh timestamped subdir), asserts via `assertReportFiles()`, and leaves the report for the next test to clean. `waitForHtmlReport` never matches pre-existing/stale reports (they'd cause flaky false-passes or partial-report assertions).

### PIT HTML Report Structure
PIT 1.25.8 writes the report directly into the configured `--reportDir` (top-level `index.html`, `mutations.xml`, `calculator/` package pages). Older PIT created a timestamped subdirectory (e.g., `report/20260728.../index.html`); `DirectoryReader` and the test's `waitForHtmlReport` still support both layouts.


