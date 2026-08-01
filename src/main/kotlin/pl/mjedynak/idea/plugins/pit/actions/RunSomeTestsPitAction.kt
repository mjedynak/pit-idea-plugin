package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS
import pl.mjedynak.idea.plugins.pit.configuration.PitConfigurationType
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration

class RunSomeTestsPitAction : DirectoryOrFilePitAction() {
    override fun isEnabled(
        project: Project,
        module: Module,
        vfile: VirtualFile,
    ): Boolean =
        (vfile.isDirectory || vfile.fileType == JavaFileType.INSTANCE) &&
            module.moduleTestsWithDependentsScope.contains(vfile)

    override fun getTitleForItem(item: String): String = "Pitest using tests in '$item'"

    override fun makeConfigurationForClassList(
        classList: String,
        project: Project,
        title: String,
    ): PitRunConfiguration {
        val defaultArgumentsContainerPopulator =
            DefaultArgumentsContainerPopulator(
                ProjectRootManager.getInstance(project),
                PsiManager.getInstance(project),
            )
        val defaultArgumentsContainerFactory =
            object : DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator) {
                override fun createDefaultPitCommandLineArgumentsContainer(`project`: Project): PitCommandLineArgumentsContainer {
                    val container = super.createDefaultPitCommandLineArgumentsContainer(project)
                    container.put(TARGET_TESTS, classList)
                    return container
                }
            }
        return PitRunConfiguration(
            "PIT using tests in $title",
            project,
            PitConfigurationType.getInstance()!!.configurationFactories[0],
            defaultArgumentsContainerFactory,
        )
    }
}
