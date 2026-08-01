package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Paths

class GradleProjectDeterminer {
    fun isGradleProject(project: Project): Boolean =
        project.basePath?.let { basePath ->
            Files.exists(Paths.get(basePath, BUILD_GRADLE_FILE)) ||
                Files.exists(Paths.get(basePath, BUILD_GRADLE_KTS_FILE))
        } ?: false

    companion object {
        const val BUILD_GRADLE_FILE = "build.gradle"
        const val BUILD_GRADLE_KTS_FILE = "build.gradle.kts"
    }
}
