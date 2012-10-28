package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.execution.configurations.ParametersList;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public class ProgramParametersListPopulator {

    public void populateProgramParametersList(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm) {
        programParametersList.add(PitCommandLineArgument.REPORT_DIR.getName());
        programParametersList.add(pitConfigurationForm.getReportDir());
        programParametersList.add(PitCommandLineArgument.SOURCE_DIRS.getName());
        programParametersList.add(pitConfigurationForm.getSourceDir());
        programParametersList.add(PitCommandLineArgument.TARGET_CLASSES.getName());
        programParametersList.add(pitConfigurationForm.getTargetClasses());
    }
}
