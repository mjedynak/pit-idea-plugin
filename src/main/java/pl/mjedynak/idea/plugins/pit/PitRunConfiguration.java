package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactoryImpl;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulatorImpl;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulatorImpl;

import java.util.Arrays;
import java.util.Collection;

public class PitRunConfiguration extends ModuleBasedConfiguration implements RunConfiguration {

    private static final String PIT_MAIN_CLASS = "org.pitest.mutationtest.MutationCoverageReport";

    private PitConfigurationForm pitConfigurationForm;
    private DefaultArgumentsContainerFactory defaultArgumentsContainerFactory;
    private PitConfigurationFormPopulator pitConfigurationFormPopulator;
    private ProgramParametersListPopulator programParametersListPopulator;


    public PitRunConfiguration(final String name, final Project project, ConfigurationFactory configurationFactory, PitConfigurationForm pitConfigurationForm,
                               DefaultArgumentsContainerFactory defaultArgumentsContainerFactory, PitConfigurationFormPopulator pitConfigurationFormPopulator,
                               ProgramParametersListPopulator programParametersListPopulator) {
        super(name, new JavaRunConfigurationModule(project, false), configurationFactory);
        this.pitConfigurationForm = pitConfigurationForm;
        this.defaultArgumentsContainerFactory = defaultArgumentsContainerFactory;
        this.pitConfigurationFormPopulator = pitConfigurationFormPopulator;
        this.programParametersListPopulator = programParametersListPopulator;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, defaultArgumentsContainerFactory, getProject());
        SettingsEditorGroup<PitRunConfiguration> group = new SettingsEditorGroup<PitRunConfiguration>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), pitConfigurationForm);
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }

    @Override
    public JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
        System.out.println("createRunnerSettings " + provider);
        return null;
    }

    @Override
    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
        System.out.println("getRunnerSettingsEditor " + runner);
        return null;
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        System.out.println("getState " + executor + ", " + env);
        JavaCommandLineState javaCommandLineState = new JavaCommandLineState(env) {
            @Override
            protected JavaParameters createJavaParameters() throws ExecutionException {
                JavaParameters javaParameters = new JavaParameters();
                final RunConfigurationModule runConfigurationModule = getConfigurationModule();
                final int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS;
                JavaParametersUtil.configureModule(runConfigurationModule, javaParameters, classPathType, null);
                javaParameters.setMainClass(PIT_MAIN_CLASS);
                programParametersListPopulator.populateProgramParametersList(javaParameters.getProgramParametersList(), pitConfigurationForm);
                javaParameters.getProgramParametersList().add("--outputFormats");
                javaParameters.getProgramParametersList().add("HTML,XML,CSV");
                return javaParameters;
            }

            @NotNull
            protected OSProcessHandler startProcess() throws ExecutionException {
                final OSProcessHandler handler = super.startProcess();
                handler.addProcessListener(new ProcessAdapter() {
                    public void processTerminated(ProcessEvent event) {
                        System.out.println("terminated");
                    }

                    public void onTextAvailable(final ProcessEvent event, final Key outputType) {
                        System.out.println(event.getText());
                    }
                });
                return handler;
            }
        };
        return javaCommandLineState;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        System.out.println("checkConfiguration");
    }

    @Override
    public Collection<Module> getValidModules() {
        return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        DefaultArgumentsContainerFactoryImpl defaultArgumentsContainerFactory
                = new DefaultArgumentsContainerFactoryImpl(ProjectRootManager.getInstance(getProject()), PsiManager.getInstance(getProject()));
        return new PitRunConfiguration("Pit Run Configuration", getProject(), PitConfigurationType.getInstance().getConfigurationFactories()[0], new PitConfigurationForm(),
                defaultArgumentsContainerFactory, new PitConfigurationFormPopulatorImpl(), new ProgramParametersListPopulatorImpl());
    }
}
