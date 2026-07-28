package pl.mjedynak.idea.plugins.pit.configuration

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator

class PitRunConfigurationFactory {
    fun createConfiguration(project: Project): PitRunConfiguration {
        val defaultArgumentsContainerPopulator =
            DefaultArgumentsContainerPopulator(
                ProjectRootManager.getInstance(project),
                PsiManager.getInstance(project),
            )
        val defaultArgumentsContainerFactory = DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator)
        return PitRunConfiguration(
            "PIT Run Configuration",
            project,
            PitConfigurationType.getInstance()!!.configurationFactories[0],
            defaultArgumentsContainerFactory,
        )
    }
}
