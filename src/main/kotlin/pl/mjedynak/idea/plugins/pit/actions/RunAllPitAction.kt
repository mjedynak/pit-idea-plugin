package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfigurationFactory

class RunAllPitAction : PitAction() {
    override fun getConfigurationForActionEvent(e: AnActionEvent): PitRunConfiguration {
        val project = e.getData(CommonDataKeys.PROJECT)!!
        val pitRunConfigurationFactory = PitRunConfigurationFactory()
        return pitRunConfigurationFactory.createConfiguration(project)
    }
}
