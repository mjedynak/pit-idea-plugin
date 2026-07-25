package pl.mjedynak.idea.plugins.pit.maven

import com.intellij.openapi.project.Project

class MavenProjectDeterminer {
    fun isMavenProject(project: Project): Boolean = project.baseDir?.findChild(POM_FILE) != null

    companion object {
        const val POM_FILE = "pom.xml"
    }
}
