package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration

abstract class DirectoryOrFilePitAction : PitAction() {
    protected abstract fun getTitleForItem(item: String): String

    protected abstract fun isEnabled(
        project: Project,
        module: Module,
        vfile: VirtualFile,
    ): Boolean

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        val module = e.getData(PlatformCoreDataKeys.MODULE)
        val vfile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = project != null && module != null && vfile != null && isEnabled(project, module, vfile)
        e.presentation.isEnabledAndVisible = enabled
        if (project != null && module != null && vfile != null && enabled) {
            e.presentation.text = getTitleForItem(vfile.presentableName)
        }
    }

    protected abstract fun makeConfigurationForClassList(
        classList: String,
        project: Project,
        title: String,
    ): PitRunConfiguration

    override fun getConfigurationForActionEvent(e: AnActionEvent): PitRunConfiguration? {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return null
        val module = e.getData(PlatformCoreDataKeys.MODULE) ?: return null
        val vfile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (!isEnabled(project, module, vfile)) return null

        val classNames =
            if (vfile.isDirectory) {
                val psiDirectory = PsiManager.getInstance(project).findDirectory(vfile)
                PitActionUtils.getClassNamesInDirectory(psiDirectory)
            } else {
                val psiFile = PsiManager.getInstance(project).findFile(vfile)
                PitActionUtils.getClassNamesForFile(psiFile)
            }

        if (classNames.isEmpty()) return null
        val joinedString = classNames.joinToString(",")
        return makeConfigurationForClassList(joinedString, project, vfile.presentableName)
    }
}
