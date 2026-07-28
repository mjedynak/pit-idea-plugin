package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl

open class DefaultArgumentsContainerFactory(
    private val defaultArgumentsContainerPopulator: DefaultArgumentsContainerPopulator,
) {
    open fun createDefaultPitCommandLineArgumentsContainer(project: Project): PitCommandLineArgumentsContainer {
        val container = PitCommandLineArgumentsContainerImpl()
        defaultArgumentsContainerPopulator.addReportDir(project, container)
        defaultArgumentsContainerPopulator.addSourceDir(container)
        defaultArgumentsContainerPopulator.addTargetClasses(project, container)
        return container
    }
}
