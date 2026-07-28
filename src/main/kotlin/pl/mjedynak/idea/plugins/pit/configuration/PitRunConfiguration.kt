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
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo
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
import org.apache.commons.lang3.StringUtils.isEmpty
import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.JavaParametersCreator
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader
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
                        this@PitRunConfiguration.populateFormIfNeeded()
                    }
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
                                try {
                                    val reportDir = pitConfigurationForm.reportDir
                                    val outputFile = File(reportDir, "pit-output.log")
                                    outputFile.parentFile.mkdirs()
                                    Files.writeString(outputFile.toPath(), outputBuilder.toString())
                                } catch (_: Exception) {
                                }
                                val reportDirectory =
                                    directoryReader.getLatestDirectoryFrom(
                                        File(pitConfigurationForm.reportDir),
                                    )
                                if (reportDirectory.isPresent) {
                                    val reportLink =
                                        reportDirectory
                                            .get()
                                            .toURI()
                                            .resolve("index.html")
                                            .toString()
                                    consoleView?.printHyperlink(
                                        "Open report in browser",
                                        OpenUrlHyperlinkInfo(reportLink),
                                    )
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
                    if (console != null) {
                        console.attachToProcess(processHandler)
                    }
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
        if (formIsEmpty()) {
            val container =
                defaultArgumentsContainerFactory
                    .createDefaultPitCommandLineArgumentsContainer(project)
            pitConfigurationFormPopulator.populateTextFieldsInForm(
                pitConfigurationForm,
                container,
            )
        }
    }

    private fun formIsEmpty(): Boolean =
        isEmpty(pitConfigurationForm.reportDir) &&
            isEmpty(pitConfigurationForm.sourceDir) &&
            isEmpty(pitConfigurationForm.targetClasses) &&
            isEmpty(pitConfigurationForm.otherParams)

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
