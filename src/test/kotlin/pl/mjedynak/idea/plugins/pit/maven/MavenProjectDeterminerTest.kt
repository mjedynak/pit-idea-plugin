package pl.mjedynak.idea.plugins.pit.maven

import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path

class MavenProjectDeterminerTest {
    private val projectDeterminer = MavenProjectDeterminer()
    private val project: Project = mock()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should determine that project is mavenized if it has pom xml`() {
        Files.createFile(tempDir.resolve(MavenProjectDeterminer.POM_FILE))
        whenever(project.basePath).thenReturn(tempDir.toString())

        val result = projectDeterminer.isMavenProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is not mavenized if pom xml not found`() {
        whenever(project.basePath).thenReturn(tempDir.toString())

        val result = projectDeterminer.isMavenProject(project)

        assertFalse(result)
    }

    @Test
    fun `should determine that project is not mavenized if base dir not found`() {
        val result = projectDeterminer.isMavenProject(project)

        assertFalse(result)
    }
}
