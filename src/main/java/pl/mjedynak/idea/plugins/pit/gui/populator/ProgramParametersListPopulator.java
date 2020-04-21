package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.execution.configurations.ParametersList;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS;

public class ProgramParametersListPopulator {

    public void populateProgramParametersList(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        addReportDir(programParametersList, pitConfigurationForm);
        addSourceDir(programParametersList, pitConfigurationForm);
        addTargetClasses(programParametersList, pitConfigurationForm);
        addTargetTests(programParametersList, pitConfigurationForm);
        addOtherParams(programParametersList, pitConfigurationForm);
    }

    private void addReportDir(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(REPORT_DIR.getName());
        programParametersList.add(pitConfigurationForm.getReportDir());
    }

    private void addSourceDir(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(SOURCE_DIRS.getName());
        programParametersList.add(pitConfigurationForm.getSourceDir());
    }

    private void addTargetClasses(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(TARGET_CLASSES.getName());
        programParametersList.add(pitConfigurationForm.getTargetClasses());
    }

    private void addTargetTests(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        /* Only add this parameter if it is set - otherwise default is the target classes */
        if (!pitConfigurationForm.getTargetTests().isEmpty()) {
            programParametersList.add(TARGET_TESTS.getName());
            programParametersList.add(pitConfigurationForm.getTargetTests());
        }
    }

    private void addOtherParams(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        String otherParams = pitConfigurationForm.getOtherParams();
        String[] namesAndValues = otherParams.split(" ");
        for (String param : namesAndValues) {
            programParametersList.add(param);
        }
    }
}
