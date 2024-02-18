package pl.mjedynak.idea.plugins.pit.actions;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PitActionUtils {

    private PitActionUtils() {}

    @NotNull
    static List<String> getClassNamesForFile(final PsiFile psiFile) {
        final List<String> classNames = new ArrayList<String>();
        if (!(psiFile instanceof PsiJavaFile)) {
            return classNames;
        }

        final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        final PsiClass[] classes = psiJavaFile.getClasses();
        for (final PsiClass pc : classes) {
            classNames.add(pc.getQualifiedName());
        }
        return classNames;
    }

    @NotNull
    static List<String> getClassNamesInDirectory(final PsiDirectory psiDirectory) {
        final List<String> classNames = new ArrayList<String>();
        if (psiDirectory == null) {
            return classNames;
        }

        final PsiDirectory[] directories = psiDirectory.getSubdirectories();
        for (final PsiDirectory d : directories) {
            classNames.addAll(getClassNamesInDirectory(d));
        }

        final PsiFile[] files = psiDirectory.getFiles();
        for (final PsiFile f : files) {
            classNames.addAll(getClassNamesForFile(f));
        }
        return classNames;
    }
}
