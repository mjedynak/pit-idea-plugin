package pl.mjedynak.idea.plugins.pit.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiManager;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator;

public class PitRunConfigurationFactory {

    public PitRunConfiguration createConfiguration(Project project) {
        DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator = new DefaultArgumentsContainerPopulator(
                ProjectRootManager.getInstance(project), PsiManager.getInstance(project));
        DefaultArgumentsContainerFactory defaultArgumentsContainerFactory
                = new DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator);
        return new PitRunConfiguration("PIT Run Configuration", project, PitConfigurationType.getInstance().getConfigurationFactories()[0],
                defaultArgumentsContainerFactory);
    }
}
