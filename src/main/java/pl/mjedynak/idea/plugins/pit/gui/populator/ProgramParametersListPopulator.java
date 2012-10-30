package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.execution.configurations.ParametersList;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public class ProgramParametersListPopulator {

    public void populateProgramParametersList(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        addReportDir(programParametersList, pitConfigurationForm);
        addSourceDir(programParametersList, pitConfigurationForm);
        addTargetClasses(programParametersList, pitConfigurationForm);
        addOtherParams(programParametersList, pitConfigurationForm);

    }

    private void addReportDir(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(PitCommandLineArgument.REPORT_DIR.getName());
        programParametersList.add(pitConfigurationForm.getReportDir());
    }

    private void addSourceDir(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(PitCommandLineArgument.SOURCE_DIRS.getName());
        programParametersList.add(pitConfigurationForm.getSourceDir());
    }

    private void addTargetClasses(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(PitCommandLineArgument.TARGET_CLASSES.getName());
        programParametersList.add(pitConfigurationForm.getTargetClasses());
    }

    private void addOtherParams(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        String otherParams = pitConfigurationForm.getOtherParams();
        String[] namesAndValues = otherParams.split(" ");
        for (String param : namesAndValues) {
            programParametersList.add(param);
        }
    }
}
