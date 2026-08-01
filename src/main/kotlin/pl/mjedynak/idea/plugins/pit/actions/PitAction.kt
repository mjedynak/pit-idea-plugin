package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration

abstract class PitAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        val module = e.getData(PlatformCoreDataKeys.MODULE)
        val available = project != null && module != null
        e.presentation.isEnabledAndVisible = available
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val module = e.getData(PlatformCoreDataKeys.MODULE) ?: return
        val pitRunConfiguration = getConfigurationForActionEvent(e) ?: return

        val builder =
            ExecutionEnvironmentBuilder.create(
                DefaultRunExecutor.getRunExecutorInstance(),
                pitRunConfiguration,
            )
        ProgramRunnerUtil.executeConfiguration(
            builder
                .contentToReuse(null)
                .dataContext(null)
                .activeTarget()
                .build(),
            true,
            true,
        )
    }

    protected abstract fun getConfigurationForActionEvent(e: AnActionEvent): PitRunConfiguration?
}
