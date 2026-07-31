package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project

class GradleProjectDeterminer {
    fun isGradleProject(project: Project): Boolean =
        project.baseDir?.let { baseDir ->
            baseDir.findChild(BUILD_GRADLE_FILE) != null || baseDir.findChild(BUILD_GRADLE_KTS_FILE) != null
        } ?: false

    companion object {
        const val BUILD_GRADLE_FILE = "build.gradle"
        const val BUILD_GRADLE_KTS_FILE = "build.gradle.kts"
    }
}
