package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project

class GradleProjectDeterminer {
    fun isGradleProject(project: Project): Boolean = project.baseDir?.findChild(BUILD_GRADLE_FILE) != null

    companion object {
        const val BUILD_GRADLE_FILE = "build.gradle"
    }
}
