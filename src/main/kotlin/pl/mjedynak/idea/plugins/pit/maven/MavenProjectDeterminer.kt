package pl.mjedynak.idea.plugins.pit.maven

import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Paths

class MavenProjectDeterminer {
    fun isMavenProject(project: Project): Boolean =
        project.basePath?.let { basePath -> Files.exists(Paths.get(basePath, POM_FILE)) } ?: false

    companion object {
        const val POM_FILE = "pom.xml"
    }
}
