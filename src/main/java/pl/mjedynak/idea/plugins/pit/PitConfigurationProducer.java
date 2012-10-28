package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.JavaRuntimeConfigurationProducerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public class PitConfigurationProducer extends JavaRuntimeConfigurationProducerBase {

    private PsiElement psiElement;

    public PitConfigurationProducer() {
        super(PitConfigurationType.getInstance());
    }

    @Override
    public PsiElement getSourceElement() {
        return psiElement;
    }

    @Override
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext configurationContext) {
        Project project = location.getProject();
        psiElement = location.getPsiElement();

        RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(project, configurationContext);
        PitRunConfiguration configuration = (PitRunConfiguration) settings.getConfiguration();
        configuration.setPsiElement(psiElement.getContainingFile());
        configuration.setName("Pit mutation testing");
        setupConfigurationModule(configurationContext, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);
        JavaRunConfigurationExtensionManager.getInstance().extendCreatedConfiguration(configuration, location);

        return settings;
    }

    @Override
    public int compareTo(Object o) {
        System.out.println("comparing " + o);
        return 1;
    }

}
