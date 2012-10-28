package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public class PitConfigurationFormPopulator {

    public void populateTextFieldsInForm(PitConfigurationForm pitConfigurationForm,
                                         DefaultArgumentsContainerFactory defaultArgumentsContainerFactory, Project project) {
        PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project);
        String reportDir = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.REPORT_DIR);
        pitConfigurationForm.setReportDir(reportDir);
        String sourceDir = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.SOURCE_DIRS);
        pitConfigurationForm.setSourceDir(sourceDir);
        String targetClasses = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.TARGET_CLASSES);
        pitConfigurationForm.setTargetClasses(targetClasses);
    }
}
