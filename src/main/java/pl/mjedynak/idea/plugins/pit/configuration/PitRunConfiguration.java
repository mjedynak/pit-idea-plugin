package pl.mjedynak.idea.plugins.pit.configuration;

import com.google.common.base.Optional;
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
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.JavaParametersCreator;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;

public class PitRunConfiguration extends ModuleBasedConfiguration implements RunConfiguration {

    private PitConfigurationForm pitConfigurationForm = new PitConfigurationForm();
    private PitConfigurationFormPopulator pitConfigurationFormPopulator = new PitConfigurationFormPopulator();
    private DirectoryReader directoryReader = new DirectoryReader();
    private JavaParametersCreator javaParametersCreator = new JavaParametersCreator();
    private DefaultArgumentsContainerFactory defaultArgumentsContainerFactory;
    private PitRunConfigurationStorer pitRunConfigurationStorer = new PitRunConfigurationStorer();

    public PitRunConfiguration(String name, Project project, ConfigurationFactory configurationFactory,
                               DefaultArgumentsContainerFactory defaultArgumentsContainerFactory) {
        super(name, new JavaRunConfigurationModule(project, false), configurationFactory);
        this.defaultArgumentsContainerFactory = defaultArgumentsContainerFactory;
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
                RunConfigurationModule runConfigurationModule = getConfigurationModule();
                if (runConfigurationModule.getModule() == null) { // on IDEA fresh start, module can't be found in previously saved configuration
                    Module module = ModuleUtil.findModuleForFile(getProject().getProjectFile(), getProject());
                    runConfigurationModule.setModule(module);
                    populateFormIfNeeded();
                }
                return javaParametersCreator.createJavaParameters(runConfigurationModule, pitConfigurationForm);
            }

            @NotNull
            @Override
            protected OSProcessHandler startProcess() throws ExecutionException {
                OSProcessHandler handler = super.startProcess();
                handler.addProcessListener(new ProcessAdapter() {
                    public void processTerminated(ProcessEvent event) {
                        // TODO: parse result and highlight lines
                        Optional<File> reportDirectory = directoryReader.getLatestDirectoryFrom(new File(pitConfigurationForm.getReportDir()));
                        if (reportDirectory.isPresent()) {
                            String reportLink = "file:///" + reportDirectory.get().getAbsolutePath() + "/index.html";
                            consoleView.printHyperlink("Open report in browser", new OpenUrlHyperlinkInfo(reportLink));
                        }
                    }
                });
                return handler;
            }

            @NotNull
            @Override
            public ExecutionResult execute(@NotNull final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
                ProcessHandler processHandler = startProcess();
                ConsoleView console = createConsole(executor);
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
            PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(getProject());
            pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, container);
        }
    }

    private boolean formIsEmpty() {
        return isEmpty(pitConfigurationForm.getReportDir());
    }

    @Override
    public void readExternal(final Element element) throws InvalidDataException {
        super.readExternal(element);
        pitRunConfigurationStorer.readExternal(pitConfigurationForm, element);
    }

    @Override
    public void writeExternal(final Element element) throws WriteExternalException {
        pitRunConfigurationStorer.writeExternal(pitConfigurationForm, element);
        super.writeExternal(element);
    }
}
