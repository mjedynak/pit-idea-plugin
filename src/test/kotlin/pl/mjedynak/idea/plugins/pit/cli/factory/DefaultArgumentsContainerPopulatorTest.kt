package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.Companion.ALL_CLASSES_SUFFIX
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.Companion.DEFAULT_REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.Companion.GRADLE_REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.Companion.MAVEN_REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.gradle.GradleProjectDeterminer
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader
import pl.mjedynak.idea.plugins.pit.maven.MavenProjectDeterminer
import java.nio.file.Files
import java.nio.file.Path

class DefaultArgumentsContainerPopulatorTest {
    private val project: Project = mock()
    private val projectRootManager: ProjectRootManager = mock()
    private val psiManager: PsiManager = mock()
    private val mavenProjectDeterminer: MavenProjectDeterminer = mock()
    private val gradleProjectDeterminer: GradleProjectDeterminer = mock()
    private val mavenPomReader: MavenPomReader = mock()
    private val defaultArgumentsContainerPopulator =
        DefaultArgumentsContainerPopulator(projectRootManager, psiManager)
    private val container: PitCommandLineArgumentsContainer = PitCommandLineArgumentsContainerImpl()

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        // Use reflection or make the fields accessible
        val mavenField = DefaultArgumentsContainerPopulator::class.java.getDeclaredField("mavenProjectDeterminer")
        mavenField.isAccessible = true
        mavenField.set(defaultArgumentsContainerPopulator, mavenProjectDeterminer)

        val gradleField = DefaultArgumentsContainerPopulator::class.java.getDeclaredField("gradleProjectDeterminer")
        gradleField.isAccessible = true
        gradleField.set(defaultArgumentsContainerPopulator, gradleProjectDeterminer)

        val pomReaderField = DefaultArgumentsContainerPopulator::class.java.getDeclaredField("mavenPomReader")
        pomReaderField.isAccessible = true
        pomReaderField.set(defaultArgumentsContainerPopulator, mavenPomReader)
    }

    @Test
    fun `should create container with default report dir`() {
        val baseDirPath = "app"
        whenever(project.basePath).thenReturn(baseDirPath)

        defaultArgumentsContainerPopulator.addReportDir(project, container)

        assertEquals("$baseDirPath/$DEFAULT_REPORT_DIR", container.get(REPORT_DIR))
    }

    @Test
    fun `should create container with maven default report dir for maven project`() {
        val baseDirPath = "app"
        whenever(project.basePath).thenReturn(baseDirPath)
        whenever(mavenProjectDeterminer.isMavenProject(project)).thenReturn(true)

        defaultArgumentsContainerPopulator.addReportDir(project, container)

        assertEquals("$baseDirPath/$MAVEN_REPORT_DIR", container.get(REPORT_DIR))
    }

    @Test
    fun `should create container with gradle default report dir for gradle project`() {
        val baseDirPath = "app"
        whenever(project.basePath).thenReturn(baseDirPath)
        whenever(gradleProjectDeterminer.isGradleProject(project)).thenReturn(true)

        defaultArgumentsContainerPopulator.addReportDir(project, container)

        assertEquals("$baseDirPath/$GRADLE_REPORT_DIR", container.get(REPORT_DIR))
    }

    @Test
    fun `should create container with default source dir`() {
        val sourceRoot: VirtualFile = mock()
        val sourceRoots = arrayOf(sourceRoot)
        whenever(projectRootManager.contentSourceRoots).thenReturn(sourceRoots)
        val path = "somePath"
        whenever(sourceRoot.path).thenReturn(path)

        defaultArgumentsContainerPopulator.addSourceDir(container)

        assertEquals(path, container.get(SOURCE_DIRS))
    }

    @Test
    fun `should prefer java as source dir`() {
        val firstSourceRoot: VirtualFile = mock()
        val secondSourceRoot: VirtualFile = mock()
        val sourceRoots = arrayOf(firstSourceRoot, secondSourceRoot)
        whenever(projectRootManager.contentSourceRoots).thenReturn(sourceRoots)
        val firstPath = "src/main/resources"
        val secondPath = "src/main/java"
        whenever(firstSourceRoot.path).thenReturn(firstPath)
        whenever(secondSourceRoot.path).thenReturn(secondPath)

        defaultArgumentsContainerPopulator.addSourceDir(container)

        assertEquals(secondPath, container.get(SOURCE_DIRS))
    }

    @Test
    fun `should create container with target classes from group id for maven project`() {
        Files.writeString(tempDir.resolve(MavenProjectDeterminer.POM_FILE), "<project/>")
        val groupId = "pl.mjedynak"

        whenever(project.basePath).thenReturn(tempDir.toString())
        whenever(mavenProjectDeterminer.isMavenProject(project)).thenReturn(true)
        whenever(mavenPomReader.getGroupId(any())).thenReturn(groupId)

        defaultArgumentsContainerPopulator.addTargetClasses(project, container)

        assertEquals("$groupId$ALL_CLASSES_SUFFIX", container.get(TARGET_CLASSES))
    }
}
