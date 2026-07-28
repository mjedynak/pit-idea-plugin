package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration

abstract class PitAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.getData(DataKeys.PROJECT)
        val module = e.getData(DataKeys.MODULE)
        val available = project != null && module != null
        e.presentation.isEnabledAndVisible = available
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(DataKeys.PROJECT) ?: return
        val module = e.getData(DataKeys.MODULE) ?: return
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
