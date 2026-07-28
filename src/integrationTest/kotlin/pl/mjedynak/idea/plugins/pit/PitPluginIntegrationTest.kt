package pl.mjedynak.idea.plugins.pit

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.ActionManager
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getPlugin
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes

private const val PLUGIN_ID = "PIT mutation testing Idea plugin"

@Remote("pl.mjedynak.idea.plugins.pit.PitTestHelper", plugin = PLUGIN_ID)
interface PitTestHelperStub {
    fun executePitForTest(project: Project): String
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
    @Test
    fun `should load plugin, register actions and config, and execute PIT`() {
        val pluginPath = Path.of(System.getProperty("path.to.build.plugin"))
        val projectPath = Path.of(System.getProperty("test.project.path", "build/testProject"))

        Starter
            .newContext(
                testName = "pitIntegrationTest",
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
            .useDriverAndCloseIde {
                waitForIndicators(3.minutes)

                val project = singleProject()
                assertNotNull(project, "Project should be open")
                assertTrue(project.isInitialized(), "Project should be initialized")

                assertPluginLoaded()
                assertActionsRegistered()
                assertRunManagerAvailable(project)
                assertPitReportGenerated(project)
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

    private fun Driver.assertPitReportGenerated(project: Project) {
        val projectPath = project.getBasePath()
        val result = utility(PitTestHelperStub::class).executePitForTest(project)
        assertTrue(result.startsWith("SUCCESS"), "PIT execution should succeed.\n$result")

        val reportDirFile = File("$projectPath/report")
        waitForHtmlReport(reportDirFile, project)

        val htmlFiles = reportDirFile.walkTopDown().filter { it.extension == "html" }.toList()
        assertReportContent(htmlFiles, reportDirFile)
        assertCalculatorReportExists(htmlFiles)
    }

    private fun Driver.waitForHtmlReport(
        reportDirFile: File,
        project: Project,
        timeoutMs: Long = 60_000L,
    ) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (reportDirFile.exists() && reportDirFile.walkTopDown().any { it.name == "index.html" }) {
                return
            }
            Thread.sleep(3000)
        }
        val output = utility(PitOutputReaderStub::class).getLastProcessInfo(project)
        assertTrue(
            false,
            "PIT should generate HTML report within ${timeoutMs / 1000}s in ${reportDirFile.absolutePath}\nPIT output:\n$output",
        )
    }

    private fun assertReportContent(
        htmlFiles: List<File>,
        reportDirFile: File,
    ) {
        val indexFile = htmlFiles.find { it.name == "index.html" }
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
