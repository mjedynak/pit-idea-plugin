package pl.mjedynak.idea.plugins.pit.gui.populator;

import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS;

public class PitConfigurationFormPopulator {

    public static final String OTHER_PARAMS = "--outputFormats XML,HTML";

    public void populateTextFieldsInForm(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        setReportDir(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setSourceDir(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setTargetClasses(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setTargetTests(pitConfigurationForm, pitCommandLineArgumentsContainer);
        setOtherParams(pitConfigurationForm);
    }

    private void setTargetClasses(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String targetClasses = pitCommandLineArgumentsContainer.get(TARGET_CLASSES);
        pitConfigurationForm.setTargetClasses(targetClasses);
    }

    private void setTargetTests(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String targetTests = pitCommandLineArgumentsContainer.get(TARGET_TESTS);
        pitConfigurationForm.setTargetTests(targetTests);
    }

    private void setSourceDir(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String sourceDir = pitCommandLineArgumentsContainer.get(SOURCE_DIRS);
        pitConfigurationForm.setSourceDir(sourceDir);
    }

    private void setReportDir(PitConfigurationForm pitConfigurationForm, PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer) {
        String reportDir = pitCommandLineArgumentsContainer.get(REPORT_DIR);
        pitConfigurationForm.setReportDir(reportDir);
    }

    private void setOtherParams(PitConfigurationForm pitConfigurationForm) {
        pitConfigurationForm.setOtherParams(OTHER_PARAMS);
    }
}
