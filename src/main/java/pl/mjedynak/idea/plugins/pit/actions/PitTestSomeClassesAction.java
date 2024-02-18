package pl.mjedynak.idea.plugins.pit.actions;

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator;
import pl.mjedynak.idea.plugins.pit.configuration.PitConfigurationType;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration;

/**
 * Runs pitest filtered to a directory of code
 */
public class PitTestSomeClassesAction extends DirectoryOrFilePitAction {

    @Override
    String getTitleForItem(final String item) {
        return "Pitest classes in '" + item + "'";
    }

    @Override
    boolean isEnabled(@NotNull final Project project, @NotNull final Module module, @NotNull final VirtualFile vfile) {
        return (vfile.isDirectory() || (vfile.getFileType() == StdFileTypes.JAVA))
                && module.getModuleContentScope().contains(vfile)
                && !module.getModuleTestsWithDependentsScope().contains(vfile);
    }

    @Override
    PitRunConfiguration makeConfigurationForClassList(
            @NotNull final String classList, @NotNull final Project project, @NotNull final String title) {
        final DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator =
                new DefaultArgumentsContainerPopulator(
                        ProjectRootManager.getInstance(project), PsiManager.getInstance(project));

        final DefaultArgumentsContainerFactory defaultArgumentsContainerFactory =
                new DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator) {
                    @Override
                    public PitCommandLineArgumentsContainer createDefaultPitCommandLineArgumentsContainer(
                            final Project proj) {
                        final PitCommandLineArgumentsContainer container =
                                super.createDefaultPitCommandLineArgumentsContainer(proj);
                        container.put(TARGET_CLASSES, classList);
                        container.put(TARGET_TESTS, "*");
                        return container;
                    }
                };

        return new PitRunConfiguration(
                "PIT for classes in " + title,
                project,
                PitConfigurationType.getInstance().getConfigurationFactories()[0],
                defaultArgumentsContainerFactory);
    }
}
