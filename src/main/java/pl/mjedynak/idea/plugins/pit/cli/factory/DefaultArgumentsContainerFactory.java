package pl.mjedynak.idea.plugins.pit.cli.factory;

import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;

public interface DefaultArgumentsContainerFactory {

    PitCommandLineArgumentsContainer createDefaultPitCommandLineArgumentsContainer(Project project);
}
