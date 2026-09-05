package pl.mjedynak.idea.plugins.pit

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.ActionManager
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getPlugin
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.BackgroundRun
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes

private const val PLUGIN_ID = "PIT mutation testing Idea plugin"

private const val TIMESTAMPED_DIR_NAME_LENGTH = 14

@Remote("pl.mjedynak.idea.plugins.pit.PitTestHelper", plugin = PLUGIN_ID)
interface PitTestHelperStub {
    fun executePitForTest(project: Project): String
}

@Remote("pl.mjedynak.idea.plugins.pit.PitActionTestHelper", plugin = PLUGIN_ID)
interface PitActionTestHelperStub {
    fun verifyActionUpdateForSourceClass(project: Project): String

    fun performActionForSourceClass(project: Project): String
}

@Remote("pl.mjedynak.idea.plugins.pit.PitOutputReader", plugin = PLUGIN_ID)
interface PitOutputReaderStub {
    fun getLastProcessInfo(project: Project): String

    fun getDefaultReportDir(project: Project): String
}

@Remote("pl.mjedynak.idea.plugins.pit.PitCoverageTestHelper", plugin = PLUGIN_ID)
interface PitCoverageTestHelperStub {
    fun getCoveredLinesInEditor(project: Project): String
}

@Remote("com.intellij.execution.impl.RunManagerImpl")
interface RunManagerImplStub {
    fun getInstanceImpl(project: Project): RunManagerImplStub
}

class PitPluginIntegrationTest {
    companion object {
        private lateinit var bgRun: BackgroundRun
        private lateinit var driver: Driver
        private lateinit var project: Project

        @BeforeAll
        @JvmStatic
        fun startIde() {
            val pluginPath = Path.of(System.getProperty("path.to.build.plugin"))
            val projectPath = Path.of(System.getProperty("test.project.path", "build/testProject"))

            bgRun =
                Starter
                    .newContext(
                        testName = "pitIntegrationSuite",
                        TestCase(
                            IdeInfo.IdeaUltimate.copy(buildNumber = "262.10315.19"),
                            LocalProjectInfo(projectPath),
                        ),
                    ).apply {
                        pluginConfigurator.installPluginFromPath(pluginPath)
                        internalMode(true)
                        disableUltimateModule()
                        removeMigrateConfigAndCreateStubFile()
                    }.runIdeWithDriver()

            driver = bgRun.driver
            driver.waitForIndicators(3.minutes)

            project = driver.singleProject()
        }

        @AfterAll
        @JvmStatic
        fun stopIde() {
            if (::bgRun.isInitialized) {
                bgRun.closeIdeAndWait()
            }
        }
    }

    @Test
    fun `should load plugin, register actions and config, and execute PIT`() {
        with(driver) {
            assertNotNull(project, "Project should be open")
            assertTrue(project.isInitialized(), "Project should be initialized")

            assertPluginLoaded()
            assertActionsRegistered()
            assertRunManagerAvailable(project)

            val reportDirFile = File(utility(PitOutputReaderStub::class).getDefaultReportDir(project))
            cleanReportDir(reportDirFile)
            val preExistingReportDirs = reportDirNames(reportDirFile)

            val result = utility(PitTestHelperStub::class).executePitForTest(project)
            assertTrue(result.startsWith("SUCCESS"), "PIT execution should succeed.\n$result")

            waitForHtmlReport(reportDirFile, preExistingReportDirs, project)
            assertReportFiles(reportDirFile)
        }
    }

    @Test
    fun `should execute PIT action on source class in project view context`() {
        with(driver) {
            assertNotNull(project, "Project should be open")
            assertTrue(project.isInitialized(), "Project should be initialized")

            assertPluginLoaded()
            assertActionsRegistered()

            val updateResult = utility(PitActionTestHelperStub::class).verifyActionUpdateForSourceClass(project)
            assertTrue(updateResult.startsWith("SUCCESS"), "action.update() should succeed for source class context.\n$updateResult")

            val reportDirFile = File(utility(PitOutputReaderStub::class).getDefaultReportDir(project))
            cleanReportDir(reportDirFile)
            val preExistingReportDirs = reportDirNames(reportDirFile)

            val actionResult = utility(PitActionTestHelperStub::class).performActionForSourceClass(project)
            assertTrue(actionResult.startsWith("SUCCESS"), "action.actionPerformed() should succeed.\n$actionResult")

            waitForHtmlReport(reportDirFile, preExistingReportDirs, project)
            assertReportFiles(reportDirFile)
        }
    }

    @Test
    fun `should mark mutation-covered lines in the editor after PIT run`() {
        with(driver) {
            assertNotNull(project, "Project should be open")
            assertTrue(project.isInitialized(), "Project should be initialized")
            assertPluginLoaded()

            val reportDirFile = File(utility(PitOutputReaderStub::class).getDefaultReportDir(project))
            cleanReportDir(reportDirFile)
            val preExistingReportDirs = reportDirNames(reportDirFile)

            val result = utility(PitTestHelperStub::class).executePitForTest(project)
            assertTrue(result.startsWith("SUCCESS"), "PIT execution should succeed.\n$result")

            waitForHtmlReport(reportDirFile, preExistingReportDirs, project)

            val mutationsFile = File(reportDirFile, "mutations.xml")
            assertTrue(mutationsFile.exists(), "mutations.xml should be generated in the report dir")

            val coverage = utility(PitCoverageTestHelperStub::class).getCoveredLinesInEditor(project)
            assertTrue(coverage.startsWith("SUCCESS"), "Editor should be annotated with covered lines.\n$coverage")

            val annotatedLines =
                coverage
                    .removePrefix("SUCCESS\n")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .associate { pair ->
                        val (line, status, hasTooltip, hasMenu) = pair.split(":")
                        line.toInt() to "$status:$hasTooltip:$hasMenu"
                    }
            assertTrue(
                annotatedLines.isNotEmpty(),
                "At least one line should be marked in the editor.\nRaw helper output:\n$coverage",
            )
            assertEquals(
                mapOf(6 to "COVERED:1:1", 10 to "COVERED:1:1", 14 to "UNCOVERED:1:1"),
                annotatedLines,
                "Lines 6 and 10 should be COVERED, line 14 should be UNCOVERED (NO_COVERAGE mutations), " +
                    "and every annotated line must have a gutter icon with a non-blank tooltip and a " +
                    "popup menu.\n" +
                    "Raw helper output:\n$coverage",
            )
        }
    }

    private fun Driver.assertPluginLoaded() {
        val pluginDescriptor = getPlugin(PLUGIN_ID)
        assertNotNull(pluginDescriptor, "PIT plugin should be loaded")
        assertTrue(pluginDescriptor!!.isEnabled(), "PIT plugin should be enabled")
    }

    private fun Driver.assertActionsRegistered() {
        val actionManager = service(ActionManager::class)
        assertNotNull(actionManager.getAction("pitest.RunAllPit"), "pitest.RunAllPit action should be registered")
        assertNotNull(actionManager.getAction("pitest.RunSomePit"), "pitest.RunSomePit action should be registered")
        assertNotNull(actionManager.getAction("pitest.TestSomePit"), "pitest.TestSomePit action should be registered")
    }

    private fun Driver.assertRunManagerAvailable(project: Project) {
        val runManager = utility(RunManagerImplStub::class).getInstanceImpl(project)
        assertNotNull(runManager, "RunManager should be available")
    }

    private fun assertReportFiles(reportDirFile: File) {
        val htmlFiles = reportDirFile.walkTopDown().filter { it.extension == "html" }.toList()
        assertReportContent(htmlFiles, File(reportDirFile, "index.html"))
        assertCalculatorReportExists(htmlFiles)
    }

    private fun Driver.waitForHtmlReport(
        reportDirFile: File,
        preExistingReportDirs: Set<String>,
        project: Project,
        timeoutMs: Long = 30_000L,
    ) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (File(reportDirFile, "index.html").exists()) {
                return
            }
            val newReportDir = newReportDir(reportDirFile, preExistingReportDirs)
            if (newReportDir != null && File(newReportDir, "index.html").exists()) {
                return
            }
            Thread.sleep(1000)
        }
        val output = utility(PitOutputReaderStub::class).getLastProcessInfo(project)
        assertTrue(
            false,
            "PIT should generate HTML report within ${timeoutMs / 1000}s in ${reportDirFile.absolutePath}\nPIT output:\n$output",
        )
    }

    private fun cleanReportDir(reportDirFile: File) {
        if (!reportDirFile.exists()) return
        var attempts = 0
        while (reportDirFile.exists() && attempts < 10) {
            reportDirFile.deleteRecursively()
            if (!reportDirFile.exists()) return
            attempts++
            Thread.sleep(500)
        }
        assertTrue(
            !reportDirFile.exists(),
            "Report directory could not be cleaned before the test: ${reportDirFile.absolutePath}",
        )
    }

    private fun reportDirNames(reportDirFile: File): Set<String> =
        if (reportDirFile.isDirectory) {
            reportDirFile
                .listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.toSet()
                .orEmpty()
        } else {
            emptySet()
        }

    private fun newReportDir(
        reportDirFile: File,
        preExistingReportDirs: Set<String>,
    ): File? {
        if (!reportDirFile.isDirectory) return null
        return reportDirFile
            .listFiles()
            ?.filter {
                it.isDirectory &&
                    it.name !in preExistingReportDirs &&
                    it.name.length == TIMESTAMPED_DIR_NAME_LENGTH &&
                    it.name.all(Char::isDigit)
            }?.maxByOrNull { it.lastModified() }
    }

    private fun assertReportContent(
        htmlFiles: List<File>,
        mainIndexFile: File,
    ) {
        val indexFile = mainIndexFile.takeIf { it.exists() } ?: htmlFiles.find { it.name == "index.html" }
        assertNotNull(indexFile, "index.html should exist in report directory (found: ${htmlFiles.joinToString { it.name }})")
        val indexContent = indexFile!!.readText()
        assertTrue(indexContent.contains("Mutation Coverage"), "Report should contain Mutation Coverage column")
        assertTrue(indexContent.contains("Test Strength"), "Report should contain Test Strength column")
    }

    private fun assertCalculatorReportExists(htmlFiles: List<File>) {
        val calculatorFile = htmlFiles.find { it.name == "Calculator.java.html" }
        assertNotNull(calculatorFile, "Calculator.java.html should exist in report")
        val calculatorContent = calculatorFile!!.readText()
        assertTrue(calculatorContent.contains("Calculator"), "Calculator report should mention Calculator")
    }
}
