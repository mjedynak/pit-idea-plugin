package pl.mjedynak.idea.plugins.pit.gui.populator;

import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;

public interface PitConfigurationFormPopulator {

    void populateTextFieldsInForm(PitConfigurationForm pitConfigurationForm,
                                  DefaultArgumentsContainerFactory defaultArgumentsContainerFactory, Project project);
}
