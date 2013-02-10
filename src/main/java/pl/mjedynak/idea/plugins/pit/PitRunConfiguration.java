package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class PitRunConfiguration extends ModuleBasedConfiguration implements RunConfiguration {

    private static final String PIT_MAIN_CLASS = "org.pitest.mutationtest.MutationCoverageReport";

    private PitConfigurationForm pitConfigurationForm;
    private DefaultArgumentsContainerFactory defaultArgumentsContainerFactory;
    private PitConfigurationFormPopulator pitConfigurationFormPopulator;
    private ProgramParametersListPopulator programParametersListPopulator;
    private DirectoryReader directoryReader;

    public PitRunConfiguration(String name, Project project, ConfigurationFactory configurationFactory, PitConfigurationForm pitConfigurationForm,
                               DefaultArgumentsContainerFactory defaultArgumentsContainerFactory, PitConfigurationFormPopulator pitConfigurationFormPopulator,
                               ProgramParametersListPopulator programParametersListPopulator, DirectoryReader directoryReader) {
        super(name, new JavaRunConfigurationModule(project, false), configurationFactory);
        this.pitConfigurationForm = pitConfigurationForm;
        this.defaultArgumentsContainerFactory = defaultArgumentsContainerFactory;
        this.pitConfigurationFormPopulator = pitConfigurationFormPopulator;
        this.programParametersListPopulator = programParametersListPopulator;
        this.directoryReader = directoryReader;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        populateFormIfNeeded();
        SettingsEditorGroup<PitRunConfiguration> group = new SettingsEditorGroup<PitRunConfiguration>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), pitConfigurationForm);
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }


    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        JavaCommandLineState javaCommandLineState = new JavaCommandLineState(env) {
            private ConsoleView consoleView;

            @Override
            protected JavaParameters createJavaParameters() throws ExecutionException {
                JavaParameters javaParameters = new JavaParameters();
                RunConfigurationModule runConfigurationModule = getConfigurationModule();
                if (runConfigurationModule.getModule() == null) { // on IDEA fresh start, module can't be found in previously saved configuration
                    Module module = ModuleUtil.findModuleForFile(getProject().getProjectFile(), getProject());
                    runConfigurationModule.setModule(module);
                    populateFormIfNeeded();
                }
                int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS;
                JavaParametersUtil.configureModule(runConfigurationModule, javaParameters, classPathType, null);
                javaParameters.setMainClass(PIT_MAIN_CLASS);
                programParametersListPopulator.populateProgramParametersList(javaParameters.getProgramParametersList(), pitConfigurationForm);
                return javaParameters;
            }

            @NotNull
            @Override
            protected OSProcessHandler startProcess() throws ExecutionException {
                final OSProcessHandler handler = super.startProcess();
                handler.addProcessListener(new ProcessAdapter() {
                    public void processTerminated(ProcessEvent event) {
                        // TODO: parse result and highlight lines
                        File reportDirectory = directoryReader.getLatestDirectoryFrom(new File(pitConfigurationForm.getReportDir()));
                        String reportLink = "file:///" + reportDirectory.getAbsolutePath() + "/index.html";
                        consoleView.printHyperlink("Open report in browser", new OpenUrlHyperlinkInfo(reportLink));
                    }
                });
                return handler;
            }

            @NotNull
            @Override
            public ExecutionResult execute(@NotNull final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
                final ProcessHandler processHandler = startProcess();
                final ConsoleView console = createConsole(executor);
                if (console != null) {
                    console.attachToProcess(processHandler);
                }
                this.consoleView = console;
                return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler, executor));
            }
        };
        javaCommandLineState.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return javaCommandLineState;
    }

    @Override
    public Collection<Module> getValidModules() {
        return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        PitRunConfigurationFactory pitRunConfigurationFactory = new PitRunConfigurationFactory();
        return pitRunConfigurationFactory.createConfiguration(getProject());
    }

    private void populateFormIfNeeded() {
        if (formIsEmpty()) {
            pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, defaultArgumentsContainerFactory, getProject());
        }
    }

    private boolean formIsEmpty() {
        return isEmpty(pitConfigurationForm.getReportDir());
    }
}
