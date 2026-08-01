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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import java.util.Date
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
                            IdeInfo.IdeaUltimate,
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

        private const val EXCERPT_LENGTH = 3000
    }

    @Test
    fun `should load plugin, register actions and config, and execute PIT`() {
        with(driver) {
            assertNotNull(project, "Project should be open")
            assertTrue(project.isInitialized(), "Project should be initialized")

            assertPluginLoaded()
            assertActionsRegistered()
            assertRunManagerAvailable(project)

            val projectPath = project.getBasePath()
            val reportDirFile = expectedReportDir(projectPath)
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

            val projectPath = project.getBasePath()
            val reportDirFile = expectedReportDir(projectPath)
            cleanReportDir(reportDirFile)
            val preExistingReportDirs = reportDirNames(reportDirFile)

            val actionResult = utility(PitActionTestHelperStub::class).performActionForSourceClass(project)
            assertTrue(actionResult.startsWith("SUCCESS"), "action.actionPerformed() should succeed.\n$actionResult")

            waitForHtmlReport(reportDirFile, preExistingReportDirs, project)
            assertReportFiles(reportDirFile)
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
            "PIT should generate HTML report within ${timeoutMs / 1000}s.\n" +
                reportDiagnostics(reportDirFile, project) +
                "\nPIT output:\n$output",
        )
    }

    /**
     * Resolves the directory where the plugin writes the PIT report by default,
     * mirroring [pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.addReportDir].
     * The test project contains a Gradle build file, so it is detected as a Gradle project
     * and the report is written to `build/reports/pit` rather than `report`.
     */
    private fun expectedReportDir(projectPath: String): File {
        val baseDir = File(projectPath)
        val suffix =
            when {
                File(baseDir, "pom.xml").exists() -> "target/report"
                isGradleProject(baseDir) -> "build/reports/pit"
                else -> "report"
            }
        return File(baseDir, suffix)
    }

    private fun isGradleProject(baseDir: File): Boolean =
        File(baseDir, "build.gradle").exists() || File(baseDir, "build.gradle.kts").exists()

    private fun reportDiagnostics(
        reportDirFile: File,
        project: Project,
    ): String {
        val basePath = project.getBasePath()
        val sb = StringBuilder()
        sb
            .append("Expected report dir: ")
            .append(reportDirFile.absolutePath)
            .append("\n")
        sb.append("  exists: ").append(reportDirFile.exists()).append("\n")
        if (reportDirFile.exists()) {
            sb.append("  contents:\n")
            appendDirContents(reportDirFile, sb)
        }
        sb
            .append("Project type detection inputs:\n")
            .append("  pom.xml: ")
            .append(File(basePath, "pom.xml").exists())
            .append("\n")
            .append("  build.gradle: ")
            .append(File(basePath, "build.gradle").exists())
            .append("\n")
            .append("  build.gradle.kts: ")
            .append(File(basePath, "build.gradle.kts").exists())
            .append("\n")
        sb.append("index.html files found under project base path:\n")
        File(basePath)
            .walkTopDown()
            .filter { it.isFile && it.name == "index.html" }
            .sortedByDescending { it.lastModified() }
            .take(20)
            .forEach {
                sb
                    .append("  ")
                    .append(it.absolutePath)
                    .append(" (")
                    .append(Date(it.lastModified()))
                    .append(")\n")
            }
        return sb.toString()
    }

    private fun appendDirContents(
        dir: File,
        sb: StringBuilder,
    ) {
        dir.listFiles()?.sortedBy { it.name }?.forEach {
            val suffix = if (it.isDirectory) "/" else ""
            sb
                .append("    ")
                .append(it.name)
                .append(suffix)
                .append(" (")
                .append(it.length())
                .append(" bytes)\n")
            if (it.isDirectory) {
                appendDirContents(it, sb)
            }
        }
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
        assertNotNull(
            indexFile,
            "index.html should exist in report directory.\nFound HTML files:\n${formatHtmlFiles(htmlFiles)}",
        )
        val indexContent = indexFile!!.readText()
        assertTrue(
            indexContent.contains("Mutation Coverage"),
            "Report should contain Mutation Coverage column.\n" +
                "Report file: ${indexFile.absolutePath}\nContent excerpt:\n${contentExcerpt(indexContent)}",
        )
        assertTrue(
            indexContent.contains("Test Strength"),
            "Report should contain Test Strength column.\n" +
                "Report file: ${indexFile.absolutePath}\nContent excerpt:\n${contentExcerpt(indexContent)}",
        )
    }

    private fun assertCalculatorReportExists(htmlFiles: List<File>) {
        val calculatorFile = htmlFiles.find { it.name == "Calculator.java.html" }
        assertNotNull(
            calculatorFile,
            "Calculator.java.html should exist in report.\nFound HTML files:\n${formatHtmlFiles(htmlFiles)}",
        )
        val calculatorContent = calculatorFile!!.readText()
        assertTrue(
            calculatorContent.contains("Calculator"),
            "Calculator report should mention Calculator.\n" +
                "Report file: ${calculatorFile.absolutePath}\nContent excerpt:\n${contentExcerpt(calculatorContent)}",
        )
    }

    private fun formatHtmlFiles(htmlFiles: List<File>): String =
        htmlFiles.joinToString("\n") { "  ${it.absolutePath} (${it.length()} bytes)" }

    private fun contentExcerpt(content: String): String =
        content.take(EXCERPT_LENGTH).let { if (content.length > EXCERPT_LENGTH) "$it..." else it }
}
