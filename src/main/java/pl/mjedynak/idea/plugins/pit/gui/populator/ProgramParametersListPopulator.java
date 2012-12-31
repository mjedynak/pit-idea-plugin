package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.execution.configurations.ParametersList;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;

public class ProgramParametersListPopulator {

    public void populateProgramParametersList(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        addReportDir(programParametersList, pitConfigurationForm);
        addSourceDir(programParametersList, pitConfigurationForm);
        addTargetClasses(programParametersList, pitConfigurationForm);
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

    private void addOtherParams(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        String otherParams = pitConfigurationForm.getOtherParams();
        String[] namesAndValues = otherParams.split(" ");
        for (String param : namesAndValues) {
            programParametersList.add(param);
        }
    }
}
