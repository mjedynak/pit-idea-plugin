package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path

class GradleProjectDeterminerTest {
    private val project: Project = mock()
    private val projectDeterminer = GradleProjectDeterminer()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should determine that project is gradle one if it has build gradle`() {
        Files.createFile(tempDir.resolve(GradleProjectDeterminer.BUILD_GRADLE_FILE))
        whenever(project.basePath).thenReturn(tempDir.toString())

        val result = projectDeterminer.isGradleProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is gradle one if it has build gradle kts`() {
        Files.createFile(tempDir.resolve(GradleProjectDeterminer.BUILD_GRADLE_KTS_FILE))
        whenever(project.basePath).thenReturn(tempDir.toString())

        val result = projectDeterminer.isGradleProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is not gradle one if build gradle not found`() {
        whenever(project.basePath).thenReturn(tempDir.toString())

        val result = projectDeterminer.isGradleProject(project)

        assertFalse(result)
    }

    @Test
    fun `should determine that project is not gradle one if base dir not found`() {
        val result = projectDeterminer.isGradleProject(project)

        assertFalse(result)
    }
}
