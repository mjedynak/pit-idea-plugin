package pl.mjedynak.idea.plugins.pit.maven

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MavenProjectDeterminerTest {
    private val projectDeterminer = MavenProjectDeterminer()
    private val project: Project = mock()
    private val baseDir: VirtualFile = mock()

    @Test
    fun `should determine that project is mavenized if it has pom xml`() {
        whenever(project.baseDir).thenReturn(baseDir)
        val pomFile: VirtualFile = mock()
        whenever(baseDir.findChild(MavenProjectDeterminer.POM_FILE)).thenReturn(pomFile)

        val result = projectDeterminer.isMavenProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is not mavenized if pom xml not found`() {
        whenever(project.baseDir).thenReturn(baseDir)

        val result = projectDeterminer.isMavenProject(project)

        assertFalse(result)
    }

    @Test
    fun `should determine that project is not mavenized if base dir not found`() {
        val result = projectDeterminer.isMavenProject(project)

        assertFalse(result)
    }
}
