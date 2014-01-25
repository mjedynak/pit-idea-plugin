package pl.mjedynak.idea.plugins.pit.cli.factory;

import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl;

public class DefaultArgumentsContainerFactory {

    private DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator;

    public DefaultArgumentsContainerFactory(DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator) {
        this.defaultArgumentsContainerPopulator = defaultArgumentsContainerPopulator;
    }

    public PitCommandLineArgumentsContainer createDefaultPitCommandLineArgumentsContainer(Project project) {
        PitCommandLineArgumentsContainer container = new PitCommandLineArgumentsContainerImpl();
        defaultArgumentsContainerPopulator.addReportDir(project, container);
        defaultArgumentsContainerPopulator.addSourceDir(container);
        defaultArgumentsContainerPopulator.addTargetClasses(project, container);
        return container;
    }
}
