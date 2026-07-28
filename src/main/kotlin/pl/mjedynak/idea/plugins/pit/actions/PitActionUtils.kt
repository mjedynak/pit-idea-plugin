package pl.mjedynak.idea.plugins.pit.actions

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

object PitActionUtils {
    fun getClassNamesForFile(psiFile: PsiFile?): List<String> {
        if (psiFile !is PsiJavaFile) return emptyList()
        return psiFile.classes.mapNotNull { it.qualifiedName }
    }

    fun getClassNamesInDirectory(psiDirectory: PsiDirectory?): List<String> {
        if (psiDirectory == null) return emptyList()
        val classNames = mutableListOf<String>()
        for (d in psiDirectory.subdirectories) {
            classNames.addAll(getClassNamesInDirectory(d))
        }
        for (f in psiDirectory.files) {
            classNames.addAll(getClassNamesForFile(f))
        }
        return classNames
    }
}
