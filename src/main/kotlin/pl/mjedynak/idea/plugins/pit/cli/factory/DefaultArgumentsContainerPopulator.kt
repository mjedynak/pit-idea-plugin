package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.gradle.GradleProjectDeterminer
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader
import pl.mjedynak.idea.plugins.pit.maven.MavenProjectDeterminer

class DefaultArgumentsContainerPopulator(
    private val projectRootManager: ProjectRootManager,
    private val psiManager: PsiManager,
) {
    private val mavenProjectDeterminer = MavenProjectDeterminer()
    private val gradleProjectDeterminer = GradleProjectDeterminer()
    private val mavenPomReader = MavenPomReader()

    fun addReportDir(
        project: Project,
        container: PitCommandLineArgumentsContainer,
    ) {
        val baseDir = project.baseDir ?: return
        var suffix = DEFAULT_REPORT_DIR
        if (mavenProjectDeterminer.isMavenProject(project)) {
            suffix = MAVEN_REPORT_DIR
        } else if (gradleProjectDeterminer.isGradleProject(project)) {
            suffix = GRADLE_REPORT_DIR
        }
        val reportDir = "${baseDir.path}/$suffix"
        container.put(REPORT_DIR, reportDir)
    }

    fun addSourceDir(container: PitCommandLineArgumentsContainer) {
        val sourceRoots = projectRootManager.contentSourceRoots
        val javaSrcFolder = sourceRoots.find { it.path.contains("java") }
        if (javaSrcFolder != null) {
            container.put(SOURCE_DIRS, javaSrcFolder.path)
        } else if (sourceRoots.isNotEmpty()) {
            container.put(SOURCE_DIRS, sourceRoots[0].path)
        }
    }

    fun addTargetClasses(
        project: Project,
        container: PitCommandLineArgumentsContainer,
    ) {
        if (mavenProjectDeterminer.isMavenProject(project)) {
            addTargetClassesForMavenProject(project, container)
        } else {
            addTargetClassesForNonMavenProject(container)
        }
    }

    private fun addTargetClassesForMavenProject(
        project: Project,
        container: PitCommandLineArgumentsContainer,
    ) {
        val baseDir = project.baseDir
        val pomVirtualFile = baseDir.findChild(MavenProjectDeterminer.POM_FILE)
        val groupId = mavenPomReader.getGroupId(pomVirtualFile!!.inputStream)
        container.put(TARGET_CLASSES, "$groupId$ALL_CLASSES_SUFFIX")
    }

    private fun addTargetClassesForNonMavenProject(container: PitCommandLineArgumentsContainer) {
        val sourceRoots = projectRootManager.contentSourceRoots
        if (sourceRoots.isNotEmpty()) {
            val directory = psiManager.findDirectory(sourceRoots[0])
            if (directory != null) {
                val subdirectories = directory.subdirectories
                if (subdirectories.isNotEmpty()) {
                    container.put(TARGET_CLASSES, "${subdirectories[0].name}$ALL_CLASSES_SUFFIX")
                }
            }
        }
    }

    companion object {
        const val DEFAULT_REPORT_DIR = "report"
        const val MAVEN_REPORT_DIR = "target/report"
        const val GRADLE_REPORT_DIR = "build/reports/pit"
        const val ALL_CLASSES_SUFFIX = ".*"
    }
}
