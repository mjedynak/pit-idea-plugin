package pl.mjedynak.idea.plugins.pit.configuration

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaCommandLineState
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.WriteExternalException
import com.intellij.psi.search.GlobalSearchScope
import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.JavaParametersCreator
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.Companion.DEFAULT_REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader
import pl.mjedynak.idea.plugins.pit.editor.PitCoverageAnnotator
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator
import java.io.File
import java.nio.file.Files

class PitRunConfiguration(
    name: String,
    project: Project,
    configurationFactory: ConfigurationFactory,
    private val defaultArgumentsContainerFactory: DefaultArgumentsContainerFactory,
) : ModuleBasedConfiguration<RunConfigurationModule, PitRunConfiguration>(
        name,
        JavaRunConfigurationModule(project, false),
        configurationFactory,
    ) {

    companion object {
        private val LOG = Logger.getInstance(PitRunConfiguration::class.java)
    }

    private val pitConfigurationForm = PitConfigurationForm()
    private val pitConfigurationFormPopulator = PitConfigurationFormPopulator()
    private val directoryReader = DirectoryReader()
    private val javaParametersCreator = JavaParametersCreator()

    private val pitRunConfigurationStorer = PitRunConfigurationStorer()

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        populateFormIfNeeded()
        val group = SettingsEditorGroup<PitRunConfiguration>()
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), pitConfigurationForm)
        // Extension editors are added automatically by the platform
        return group
    }

    @Throws(ExecutionException::class)
    override fun getState(
        executor: Executor,
        env: ExecutionEnvironment,
    ): RunProfileState {
        val javaCommandLineState =
            object : JavaCommandLineState(env) {
                private var consoleView: ConsoleView? = null

                @Throws(ExecutionException::class)
                override fun createJavaParameters(): JavaParameters {
                    val runConfigurationModule = configurationModule
                    if (runConfigurationModule.module == null) {
                        val projectFile = this@PitRunConfiguration.project.projectFile
                        if (projectFile != null) {
                            runConfigurationModule.module =
                                ModuleUtil.findModuleForFile(projectFile, this@PitRunConfiguration.project)
                        }
                    }
                    this@PitRunConfiguration.populateFormIfNeeded()
                    return javaParametersCreator.createJavaParameters(
                        runConfigurationModule,
                        pitConfigurationForm,
                    )
                }

                @Throws(ExecutionException::class)
                override fun startProcess(): OSProcessHandler {
                    val commandLine = createCommandLine()
                    val handler = ColoredProcessHandler(commandLine)
                    val outputBuilder = StringBuilder()
                    handler.addProcessListener(
                        object : ProcessAdapter() {
                            override fun onTextAvailable(
                                event: ProcessEvent,
                                outputType: Key<*>,
                            ) {
                                val text = event.text
                                if (text != null) {
                                    outputBuilder.append(text)
                                }
                            }

                            override fun processTerminated(event: ProcessEvent) {
                                val reportDir = resolveReportDir()
                                try {
                                    val outputFile = File(reportDir, "pit-output.log")
                                    outputFile.parentFile.mkdirs()
                                    Files.writeString(outputFile.toPath(), outputBuilder.toString())
                                } catch (e: Exception) {
                                    LOG.warn("PIT: failed to write pit-output.log", e)
                                }
                                val reportLink = resolveReportLink(reportDir)
                                if (reportLink != null) {
                                    consoleView?.printHyperlink(
                                        "Open report in browser",
                                        OpenUrlHyperlinkInfo(reportLink),
                                    )
                                }
                                try {
                                    ApplicationManager.getApplication().invokeLater {
                                        try {
                                            val annotator = this@PitRunConfiguration.project.getService(PitCoverageAnnotator::class.java)
                                            val summary = annotator.updateFromReport(reportDir)
                                            consoleView?.print(summary + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
                                        } catch (e: Exception) {
                                            LOG.warn("PIT coverage annotation failed", e)
                                        }
                                    }
                                } catch (e: Exception) {
                                    LOG.warn("PIT coverage annotation dispatch failed", e)
                                }
                            }
                        },
                    )
                    ProcessTerminatedListener.attach(handler)
                    handler.startNotify()
                    return handler
                }

                @Throws(ExecutionException::class)
                override fun execute(
                    executor: Executor,
                    runner: ProgramRunner<*>,
                ): ExecutionResult {
                    val processHandler = startProcess()
                    val console = createConsole(executor)
                    console?.attachToProcess(processHandler)
                    consoleView = console
                    return DefaultExecutionResult(
                        console,
                        processHandler,
                        *createActions(console, processHandler, executor),
                    )
                }
            }
        javaCommandLineState.consoleBuilder =
            TextConsoleBuilderFactory
                .getInstance()
                .createBuilder(project)
        return javaCommandLineState
    }

    override fun getValidModules(): Collection<Module> = listOf(*ModuleManager.getInstance(project).modules)

    override fun createInstance(): ModuleBasedConfiguration<RunConfigurationModule, PitRunConfiguration> {
        val pitRunConfigurationFactory = PitRunConfigurationFactory()
        return pitRunConfigurationFactory.createConfiguration(project)
    }

    internal fun populateFormIfNeeded() {
        val container =
            defaultArgumentsContainerFactory
                .createDefaultPitCommandLineArgumentsContainer(project)
        pitConfigurationFormPopulator.populateTextFieldsInForm(
            pitConfigurationForm,
            container,
        )
    }

    /**
     * Resolves the report directory the same way the PIT CLI does, so the annotator,
     * the console link and the log file look in the place where PIT actually wrote the report:
     * absolute values are used as-is, relative values are resolved against the project base path,
     * and a blank value falls back to the default computed from the project type.
     */
    private fun resolveReportDir(): File {
        val configuredReportDir = pitConfigurationForm.reportDir
        if (configuredReportDir.isNotBlank()) {
            val configuredFile = File(configuredReportDir)
            if (configuredFile.isAbsolute) {
                return configuredFile
            }
            val basePath = project.basePath
            if (basePath != null) {
                return File(basePath, configuredReportDir)
            }
        }
        return File(defaultReportDir())
    }

    private fun defaultReportDir(): String {
        val container =
            ApplicationManager.getApplication().runReadAction<PitCommandLineArgumentsContainer> {
                defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)
            }
        return container.get(REPORT_DIR) ?: DEFAULT_REPORT_DIR
    }

    /**
     * Builds the console report link: prefers the top-level `index.html` (PIT 1.25.8 layout)
     * and falls back to the latest timestamped subdirectory for older report layouts.
     */
    private fun resolveReportLink(reportDir: File): String? {
        val topLevelIndex = File(reportDir, "index.html")
        if (topLevelIndex.exists()) {
            return topLevelIndex.toURI().toString()
        }
        val latestDirectory = directoryReader.getLatestDirectoryFrom(reportDir)
        return if (latestDirectory.isPresent) {
            latestDirectory
                .get()
                .toURI()
                .resolve("index.html")
                .toString()
        } else {
            null
        }
    }

    override fun getSearchScope(): GlobalSearchScope? = null

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super.readExternal(element)
        pitRunConfigurationStorer.readExternal(pitConfigurationForm, element)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        pitRunConfigurationStorer.writeExternal(pitConfigurationForm, element)
        super.writeExternal(element)
    }
}
