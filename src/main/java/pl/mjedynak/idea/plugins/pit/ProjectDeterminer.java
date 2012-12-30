package pl.mjedynak.idea.plugins.pit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ProjectDeterminer {

    static final String POM_FILE = "pom.xml";

    public boolean isMavenProject(@NotNull Project project) {
        boolean result = false;
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
            result = baseDir.findChild(POM_FILE) != null;
        }
        return result;
    }
}
