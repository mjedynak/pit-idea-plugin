package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
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
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactoryImpl;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator;

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
        populateFormIfNeeded();
        SettingsEditorGroup<PitRunConfiguration> group = new SettingsEditorGroup<PitRunConfiguration>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), pitConfigurationForm);
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }


    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        JavaCommandLineState javaCommandLineState = new JavaCommandLineState(env) {
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
            protected OSProcessHandler startProcess() throws ExecutionException {
                final OSProcessHandler handler = super.startProcess();
                handler.addProcessListener(new ProcessAdapter() {
                    public void processTerminated(ProcessEvent event) {
                        // TODO: parse result and highlight lines
                    }
                });
                return handler;
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
        DefaultArgumentsContainerFactoryImpl defaultArgumentsContainerFactory
                = new DefaultArgumentsContainerFactoryImpl(ProjectRootManager.getInstance(getProject()), PsiManager.getInstance(getProject()));
        return new PitRunConfiguration("Pit Run Configuration", getProject(), PitConfigurationType.getInstance().getConfigurationFactories()[0], new PitConfigurationForm(),
                defaultArgumentsContainerFactory, new PitConfigurationFormPopulator(), new ProgramParametersListPopulator());
    }

    private void populateFormIfNeeded() {
        if (formIsEmpty()) { // if form wasn't used before
            pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, defaultArgumentsContainerFactory, getProject());
        }
    }

    private boolean formIsEmpty() {
        return pitConfigurationForm.getReportDir().equals("");
    }
}
