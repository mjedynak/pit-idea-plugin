package pl.mjedynak.idea.plugins.pit.actions;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration;

public abstract class PitAction extends AnAction {

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        final Module module = e.getData(DataKeys.MODULE);
        final boolean available = (project != null) && (module != null);
        e.getPresentation().setEnabledAndVisible(available);
    }

    @Override
    public final void actionPerformed(final AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        final Module module = e.getData(DataKeys.MODULE);
        if (project == null || module == null) {
            return;
        }

        final PitRunConfiguration pitRunConfiguration = getConfigurationForActionEvent(e);

        if (pitRunConfiguration == null) {
            return;
        }

        final ExecutionEnvironmentBuilder builder =
                ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), pitRunConfiguration);

        ProgramRunnerUtil.executeConfiguration(
                builder.contentToReuse(null).dataContext(null).activeTarget().build(), true, true);
    }

    @Nullable
    abstract PitRunConfiguration getConfigurationForActionEvent(final AnActionEvent e);
}
