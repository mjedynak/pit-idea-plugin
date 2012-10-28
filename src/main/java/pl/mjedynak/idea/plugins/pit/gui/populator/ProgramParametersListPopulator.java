package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.execution.configurations.ParametersList;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public interface ProgramParametersListPopulator {

    void populateProgramParametersList(ParametersList programParametersList, PitConfigurationForm pitConfigurationForm);
}
