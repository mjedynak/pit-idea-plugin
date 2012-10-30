package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public class PitConfigurationFormPopulator {

    static final String OTHER_PARAMS = "--outputFormats XML,HTML";

    public void populateTextFieldsInForm(PitConfigurationForm pitConfigurationForm,
                                         DefaultArgumentsContainerFactory defaultArgumentsContainerFactory, Project project) {
        PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project);
        setReportDir(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setSourceDir(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setTargetClasses(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setOtherParams(pitConfigurationForm);
    }

    private void setTargetClasses(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String targetClasses = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.TARGET_CLASSES);
        pitConfigurationForm.setTargetClasses(targetClasses);
    }

    private void setSourceDir(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String sourceDir = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.SOURCE_DIRS);
        pitConfigurationForm.setSourceDir(sourceDir);
    }

    private void setReportDir(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String reportDir = pitCommandLineArgumentsContainer.get(PitCommandLineArgument.REPORT_DIR);
        pitConfigurationForm.setReportDir(reportDir);
    }

    private void setOtherParams(PitConfigurationForm pitConfigurationForm) {
        pitConfigurationForm.setOtherParams(OTHER_PARAMS);
    }
}
