package pl.mjedynak.idea.plugins.pit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiManager;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator;
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator;
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader;
import pl.mjedynak.idea.plugins.pit.maven.ProjectDeterminer;

public class PitRunConfigurationFactory {

    public PitRunConfiguration createConfiguration(Project project) {
        DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator = new DefaultArgumentsContainerPopulator(
                ProjectRootManager.getInstance(project), PsiManager.getInstance(project), new ProjectDeterminer(), new MavenPomReader());
        DefaultArgumentsContainerFactory defaultArgumentsContainerFactory
                = new DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator);
        return new PitRunConfiguration("PIT Run Configuration", project, PitConfigurationType.getInstance().getConfigurationFactories()[0],
                new PitConfigurationForm(), defaultArgumentsContainerFactory, new PitConfigurationFormPopulator(), new ProgramParametersListPopulator(), new DirectoryReader());
    }
}
