package pl.mjedynak.idea.plugins.pit.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfigurationFactory;

public class RunAllPitAction extends PitAction {

    protected PitRunConfiguration getConfigurationForActionEvent(final AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);

        final PitRunConfigurationFactory pitRunConfigurationFactory = new PitRunConfigurationFactory();
        return pitRunConfigurationFactory.createConfiguration(project);
    }
}
