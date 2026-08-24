# AGENTS.md — PIT Idea Plugin

**After every code change, update this file to reflect the new state of the project.**

## Project Overview

IntelliJ IDEA plugin for [PIT Mutation Testing](http://pitest.org). Adds a run configuration and context menu actions to execute PIT directly within the IDE. After a run finishes, mutation lines are marked in the editor with color-coded gutter bands mirroring the PIT HTML report: light green for covered lines (mutations `KILLED`/`NON_VIABLE`), light red for uncovered lines (any `SURVIVED`/`NO_COVERAGE`), blue-grey for other statuses; each band carries the mutation descriptions as a hover tooltip (gutter icon + scrollbar stripe) and a right-click context menu to clear the markings. Current version: **1.4.13-SNAPSHOT**, bundling PIT **1.25.9** and JUnit5 plugin **1.2.3**.

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
├── editor/             # Editor coverage annotation after PIT run
│   ├── MutationStatus.kt            # PIT mutation status enum (KILLED/SURVIVED/NO_COVERAGE/...) + fromXml
│   ├── MutationReportParser.kt      # Parses mutations.xml; keeps ALL mutations incl. NO_COVERAGE
│   ├── CoverageLineMarkerRenderer.kt # Status-colored gutter band (LineMarkerRendererEx, Position.LEFT)
│   └── PitCoverageAnnotator.kt       # Project service: parses report + annotates open editors (band + gutter icon w/ hover tooltip + right-click clear menu)
├── console/DirectoryReader.kt     # Finds latest report directory
├── gradle/GradleProjectDeterminer.kt           # Detects Gradle projects
└── maven/                        # Maven project detection + pom.xml parsing
    ├── MavenProjectDeterminer.kt
    └── MavenPomReader.kt

src/testSupport/kotlin/pl/mjedynak/idea/plugins/pit/
├── PitTestHelper.kt           # E2E test helper (creates PitRunConfiguration and executes it)
├── PitActionTestHelper.kt     # Verifies action visibility/enablement and simulates user clicking the action
├── PitOutputReader.kt         # Reads PIT process info via reflection
└── PitCoverageTestHelper.kt   # Opens Calculator.java, polls editors for CoverageLineMarkerRenderer, returns line:STATUS:HAS_TOOLTIP triplets
src/test/kotlin/                   # All tests are Kotlin (9 test files)
src/integrationTest/
├── kotlin/                        # Integration tests using IntelliJ Starter framework
│   └── PitPluginIntegrationTest.kt  # 3 test scenarios: (1) plugin load + config + PIT via helper, (2) action visibility check + simulated click + PIT report, (3) coverage markers mirror report (6/10 COVERED, 14 UNCOVERED)
└── resources/testProject/         # Test project with Calculator.java, CalculatorTest.java
META-INF/plugin.xml                # Plugin descriptor (actions, extensions)
```

## Technology Stack

- **Languages**: Kotlin (all plugin source code)
- **Build**: Gradle 9.6.1, Kotlin DSL, IntelliJ Platform Gradle Plugin 2.18.1
- **Target**: IntelliJ IDEA 2026.2, Java 25 toolchain
- **PIT**: 1.25.9 (bundled as non-transitive dependencies)
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
- **Integration test approach**: Uses IntelliJ Starter framework (`testIdeUi`) to start a single IDE process shared across all test methods via `@BeforeAll`/`@AfterAll` in a `companion object`. Communication via `@Remote` stubs over JMX. `PitTestHelper.kt`, `PitActionTestHelper.kt`, `PitOutputReader.kt`, and `PitCoverageTestHelper.kt` (in `src/testSupport/`, bundled in the plugin JAR) are invoked remotely. **Important**: `@Remote` stub calls run off-EDT without a read action — any PSI/model/editor access from them must be wrapped in `ApplicationManager.getApplication().runReadAction { ... }` or dispatched to the EDT, or `ThreadingAssertions` throws.
- **Integration test setup**: `setupTestProject` (a Gradle `Sync` task) copies test project sources + committed `.idea` module config + Gradle wrapper to `build/testProject/`, deleting stale state (old reports, leftover `.idea`). `compileTestProject` runs `./gradlew copyTestLib classes testClasses` to pre-compile sources AND copy the test runtime classpath (JUnit jars) to `build/testLib/`. The `integrationTest` task depends on `buildPlugin`, `setupTestProject`, and `compileTestProject`.
- **Test project module config**: The test IDE does NOT import Gradle — it opens `build/testProject` as a plain project. The committed `src/integrationTest/resources/testProject/.idea/testProject.iml` provides the module with output dirs (`build/classes/java/main|test`) and a `junit5` module-library pointing at `build/testLib/*.jar`. Without this, `JavaParametersCreator` produces a PIT classpath with no compiled classes or JUnit → PIT fails with `No mutations found` and no report. The JUnit version in the `.iml` must match `build.gradle.kts` (`junit-jupiter:6.1.1` → `copyTestLib` task).
- **Report dir resolution**: the tests do NOT hardcode a `report/` path. `PitOutputReader.getDefaultReportDir(project)` computes the plugin's own default via `DefaultArgumentsContainerPopulator` + `DefaultArgumentsContainerFactory` (inside `runReadAction`) and reads the `REPORT_DIR` argument — for the Gradle test project this yields `<testProject>/build/reports/pit` (NOT `report/`). Each test cleans that dir (with retry, `cleanReportDir`), then `waitForHtmlReport` waits for the top-level `index.html` (or a new 14-digit timestamped subdir as a fallback) up to 30s. `GradleProjectDeterminer` accepts `build.gradle` OR `build.gradle.kts`, so the test project is treated as Gradle. The report is NOT deleted after a run to avoid racing with the plugin's `processTerminated` handler that writes `pit-output.log` — the next test cleans it instead.
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

- `createJavaParameters()` calls `populateFormIfNeeded()` **unconditionally** (not only when the module is missing — stored run configs restore a non-null module from XML and used to skip population). `PitConfigurationFormPopulator.populateTextFieldsInForm` fills **only blank fields**, so values stored in the run-config XML survive while empty fields (e.g. `reportDir` in configs saved before the Gradle default existed) get the computed defaults. Action-created configs (custom `DefaultArgumentsContainerFactory` injecting `TARGET_CLASSES`/`TARGET_TESTS`) still work because a fresh form is all-blank.
- `PitRunConfiguration.resolveReportDir()` resolves the report directory the same way the PIT CLI does: absolute values as-is, relative values against `project.basePath`, blank values via `DefaultArgumentsContainerFactory` (inside `runReadAction`). It is used for `pit-output.log`, the console report link, AND the annotator call — otherwise a relative/blank stored `reportDir` made `File("")`/`File("report")` resolve against the IDE JVM CWD while PIT wrote to the project base path → report generated but no editor coverage.

### Main class executed in forked JVM
`org.pitest.mutationtest.commandline.MutationCoverageReport`

### Editor Coverage Annotation

```
PitRunConfiguration.startProcess (executor thread)
    ↓ invokeLater (EDT)
PitCoverageAnnotator.clearAnnotations()                 # clears stale markings BEFORE the run
                                                        #   (bands + gutter icons + cached report maps)

PitRunConfiguration.processTerminated (executor thread)
    ↓ invokeLater (EDT)
PitCoverageAnnotator.updateFromReport(File(reportDir))   # project service
    ├── MutationReportParser.parse(mutations.xml)        # keeps ALL mutations incl. NO_COVERAGE
    ├── resolve target files by sourceFile name → PsiFile
    └── Editor.markupModel → per annotated line:
        ├── CoverageLineMarkerRenderer (status-colored band, Position.LEFT)
        ├── errorStripeMarkColor + errorStripeTooltip (report-style descriptions)
        └── RangeHighlighter.setGutterIconRenderer(GutterIconRenderer)   # status-colored square icon w/ hover tooltip + right-click clear menu
```

- **Markings are cleared before each run**: `PitRunConfiguration.startProcess()` calls `clearPreviousCoverage()` first, which dispatches `PitCoverageAnnotator.clearAnnotations()` to the EDT via `invokeLater` (same required try/catch pattern as `processTerminated`). `clearAnnotations()` removes the coverage highlighters from all open editors **and** resets the cached `mutationsByClassAndLine`/`sourceFilesByClass` maps — the map reset matters because the `editorCreated` listener re-annotates editors on open, and without it a file opened *during* the run would be re-marked with stale data. So the editor is clean while PIT runs, and markings appear only after the new report is parsed.

- Registered as a `projectService` in `META-INF/plugin.xml`; only annotates already-open editors.
- `processTerminated` runs on the executor thread, so the annotator call is dispatched via `invokeLater`. The inner try/catch is **required**: an uncaught exception inside the `invokeLater` lambda runs on the EDT, pops an error dialog in internal test mode, and hangs subsequent `invokeAndWait` calls. Keep the try/catch but LOG via `LOG.warn` — silent `catch (_: Exception) {}` makes annotation failures undiagnosable in real IDEs.
- `processTerminated` first resolves the report dir via `resolveReportDir()` on the executor thread and passes the resulting `File` into the `invokeLater` lambda — it is NOT re-resolved on the EDT.
- `PitCoverageAnnotator.updateFromReport(reportDir)` returns a one-line summary (`"PIT coverage: N mutations, M classes, K covered + U uncovered line(s) marked in E open editor(s). Classes: <class> -> <path>|NOT FOUND, ..."`) that `processTerminated` prints to the run console — the per-class resolution and the *actually marked* line count (not just open editors) are the first place to look when a user reports "no coverage in the editor". It also `LOG.warn`s when: no `mutations.xml` is found, parsing fails, the report contains NO mutations at all (usually means PIT ran 0 tests, e.g. a JUnit launcher/engine version mismatch), or a class cannot be resolved to an editor file.
- `resolveFileForClass` accepts only a real source file from `findClass` (`.java`/`.kt`); a class-file PSI from compiled output or a dependency would never match an open editor, so it falls back to `FilenameIndex` by the `sourceFile` name from `mutations.xml`, preferring a file that is currently open in an editor.
- The console "Open report in browser" link prefers the top-level `reportDir/index.html` (PIT 1.25.9 layout) and falls back to the latest timestamped subdirectory for older layouts. `DirectoryReader.getLatestDirectoryFrom` only returns subdirectories, so it must NOT be used directly to build the link for top-level reports.
- PIT's `mutations.xml` contains one `<mutation>` per mutation with a `status` attribute. All mutations are parsed and kept, then aggregated per source line: a line is `COVERED` (green) if it has KILLED/NON_VIABLE mutations, `UNCOVERED` (red) if any mutation is SURVIVED/NO_COVERAGE (red takes precedence), else `UNKNOWN` (blue-grey, e.g. TIMED_OUT). Report colors are mirrored: covered `#aaffaa`, uncovered `#ffaaaa`, other `#dde7ef`. Each highlighter sets `errorStripeMarkColor` + `errorStripeTooltip` carrying the report-style description list (`1. add : Replaced integer addition with subtraction → KILLED`), so hovering the scrollbar stripe shows the mutation descriptions.
- **Tooltips are visible on the band itself, not just the scrollbar stripe**: each annotated line's `RangeHighlighter` also gets a `GutterIconRenderer` via `RangeHighlighter.setGutterIconRenderer(...)`. The renderer implements `DumbAware`, uses `GutterIconRenderer.Alignment.LEFT`, and returns a small status-colored square `Icon` (dark band colors: covered `0x2F6B2F`, uncovered `0x8B3333`, other `0x3A4A5C`; drawn as an 8x8 rounded rect) plus `getTooltipText()` = the SAME report-style description text as the error-stripe tooltip (built once per line by `buildTooltip`, shared by both). Hovering the gutter icon shows the descriptions directly in the editor. **Right-clicking the gutter icon opens a context menu with "Clear PIT coverage markings"** (via `getPopupMenuActions()` returning a `DefaultActionGroup` with a `DumbAware` `AnAction`); the action calls `PitCoverageAnnotator.clearAnnotations()` directly — actions run on the EDT, so no `invokeLater` is needed, and the renderer receives the `Project` at construction to resolve the service. The renderer is skipped when a line's tooltip is blank. Removing the highlighter releases the renderer — no manual cleanup.
- **`CoverageLineMarkerRenderer` must implement `LineMarkerRendererEx` with `Position.LEFT`.** A plain `LineMarkerRenderer` defaults to the RIGHT free-painters area, which has zero width on the New UI (2022.1+) — the renderer is attached to the markup model (integration test polls for it and passes) but `paint()` draws into a 0-width rect, so nothing is ever visible. This cost three rounds of "no coverage in the editor" debugging before being identified. The LEFT area always has a guaranteed minimum width.

## Version Management

**⚠️ PIT version is declared in 3 places** — all must be updated together:

| Location | What | Current        |
|----------|------|----------------|
| `build.gradle.kts` | `val pitVersion` / `val pitJunit5PluginVersion` | 1.25.9 / 1.2.3 |
| `ClassPathPopulator.kt` | JAR filename strings | 1.25.9 / 1.2.3 |
| `META-INF/plugin.xml` | Description text ("Bundled with PIT ...") | 1.25.9         |

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
- **`processTerminated` annotation dispatch**: keep the outer AND inner try/catch around the `invokeLater` block in `PitRunConfiguration.kt`. The inner one swallows annotator exceptions on the EDT; removing it turns any annotation failure into a modal error dialog that freezes the integration test IDE.

## Integration Test Troubleshooting

### Test Project Model
- Test project (`src/integrationTest/resources/testProject/`) is a Gradle project used ONLY for pre-compilation (via `compileTestProject`). The test IDE does NOT import Gradle — it opens `build/testProject` as a plain project using the committed `.idea/` XML files. The module's compiled output dirs and `junit5` module-library (pointing at `build/testLib/`) are what make PIT find the compiled classes and JUnit engine. If these are missing, PIT exits with `No mutations found` and no report.
- The `build.gradle.kts` JUnit version MUST match both the committed `.iml` library (jar filenames in `build/testLib/`) and the plugin's bundled `junit-platform-launcher` version. Currently all are `6.1.1`. A mismatch (e.g., `5.11.1` in test project + `6.1.1` launcher) causes PIT to fail silently with `"Pitest could not run any tests"` because the launcher and engine versions are incompatible.

### Debugging PIT failures
- PIT runs as a forked JVM via `PitRunConfiguration.startProcess()`. If the process exits before a process listener is registered (because `super.startProcess()` starts the process), the listener misses `onTextAvailable` events. **Fix**: create the `ColoredProcessHandler` directly, attach listeners, then call `startNotify()`:
  ```kotlin
  val handler = ColoredProcessHandler(commandLine)
  handler.addProcessListener(object : ProcessListener { ... })
  handler.startNotify()
  ```
  (`ProcessAdapter` is deprecated in current platform versions — implement `ProcessListener` directly; all its methods have default implementations.)
- To capture PIT output for debugging, run the exact command line from the test report directly in a terminal (extract from `PIT output: Command line:` in the HTML report). This bypasses IntelliJ and shows PIT's actual error messages.

### Key Integration Test Files
- `PitTestHelper.kt` (in `src/testSupport/`) — Creates `PitRunConfiguration` and calls `executeConfiguration()` with `waitForProcessCompletion=true`.
- `PitActionTestHelper.kt` (in `src/testSupport/`) — Provides `verifyActionUpdateForSourceClass()` (checks right-click action is visible/enabled for a source class) and `performActionForSourceClass()` (simulates clicking the action, triggering PIT execution).
- `PitOutputReader.kt` (in `src/testSupport/`) — reads PIT process info from `RunContentManager` via reflection (exit code, command line, report dir contents, and optionally `pit-output.log` written by the plugin's process listener in `PitRunConfiguration`); also exposes `getDefaultReportDir(project)` which resolves the plugin's own report-dir default (see Report dir resolution above).
- `PitCoverageTestHelper.kt` (in `src/testSupport/`) — opens `Calculator.java` in an editor, polls the markup model for `CoverageLineMarkerRenderer` instances, and returns `line:STATUS:HAS_TOOLTIP:HAS_MENU` quadruplets (e.g. `SUCCESS\n6:COVERED:1:1,10:COVERED:1:1,14:UNCOVERED:1:1`), where `HAS_TOOLTIP` is `1` when the line's `RangeHighlighter` has a `GutterIconRenderer` with a non-blank tooltip and `HAS_MENU` is `1` when that renderer exposes a popup menu (`getPopupMenuActions()`).

- `PitPluginIntegrationTest.kt` — `@Remote` stubs call `PitTestHelper`/`PitActionTestHelper`/`PitOutputReader`/`PitCoverageTestHelper` inside the test IDE over JMX. All test methods share a single IDE process (started once in `@BeforeAll`, closed in `@AfterAll`). Each test cleans the resolved report dir, runs PIT, waits for a NEW report (top-level `index.html` or a fresh timestamped subdir), asserts via `assertReportFiles()`, and leaves the report for the next test to clean. `waitForHtmlReport` never matches pre-existing/stale reports (they'd cause flaky false-passes or partial-report assertions). The coverage test asserts the exact map `{6: COVERED:1:1, 10: COVERED:1:1, 14: UNCOVERED:1:1}` — line 14 (`multiply`) is marked red because its mutations are `NO_COVERAGE`, and every annotated line must have a gutter icon with a non-blank tooltip and a popup menu, mirroring the report.
### PIT HTML Report Structure
PIT 1.25.9 writes the report directly into the configured `--reportDir` (top-level `index.html`, `mutations.xml`, `calculator/` package pages). Older PIT created a timestamped subdirectory (e.g., `report/20260728.../index.html`); `DirectoryReader` and the test's `waitForHtmlReport` still support both layouts. The editor coverage annotation is driven by `mutations.xml` — `MutationReportParser` reads every `<mutation>`'s `lineNumber`/`sourceFile`/`mutatedClass`/`status` and keeps ALL of them; the annotator mirrors the report's `style.css` colors (covered `#aaffaa`, uncovered `#ffaaaa`, other `#dde7ef`) and puts the mutation descriptions (`<mutatedMethod> : <description> → <STATUS>`) into the error-stripe tooltip.


