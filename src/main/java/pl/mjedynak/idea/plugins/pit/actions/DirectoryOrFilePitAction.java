package pl.mjedynak.idea.plugins.pit.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration;

public abstract class DirectoryOrFilePitAction extends PitAction {

    abstract String getTitleForItem(final String item);

    abstract boolean isEnabled(
            @NotNull final Project project, @NotNull final Module module, @NotNull final VirtualFile vfile);

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        final Module module = e.getData(DataKeys.MODULE);
        final VirtualFile vfile = e.getData(DataKeys.VIRTUAL_FILE);

        final boolean enabled =
                (project != null) && (module != null) && (vfile != null) && isEnabled(project, module, vfile);

        e.getPresentation().setEnabledAndVisible(enabled);
        if (enabled) {
            e.getPresentation().setText(getTitleForItem(vfile.getPresentableName()));
        }
    }

    abstract PitRunConfiguration makeConfigurationForClassList(
            @NotNull final String classList, @NotNull final Project project, @NotNull final String title);

    @Override
    protected PitRunConfiguration getConfigurationForActionEvent(final AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        final Module module = e.getData(DataKeys.MODULE);
        final VirtualFile vfile = e.getData(DataKeys.VIRTUAL_FILE);

        if ((project == null) || (module == null) || (vfile == null) || !isEnabled(project, module, vfile)) {
            return null;
        }

        final List<String> classNames;
        if (vfile.isDirectory()) {
            final PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(vfile);
            classNames = PitActionUtils.getClassNamesInDirectory(psiDirectory);
        } else {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(vfile);
            classNames = PitActionUtils.getClassNamesForFile(psiFile);
        }

        if (classNames.isEmpty()) {
            return null;
        }

        final String joinedString = String.join(",", classNames);

        return makeConfigurationForClassList(joinedString, project, vfile.getPresentableName());
    }
}
