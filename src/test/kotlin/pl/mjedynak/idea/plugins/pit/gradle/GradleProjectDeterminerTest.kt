package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GradleProjectDeterminerTest {
    private val project: Project = mock()
    private val baseDir: VirtualFile = mock()
    private val projectDeterminer = GradleProjectDeterminer()

    @Test
    fun `should determine that project is gradle one if it has build gradle`() {
        whenever(project.baseDir).thenReturn(baseDir)
        val buildGradleFile: VirtualFile = mock()
        whenever(baseDir.findChild(GradleProjectDeterminer.BUILD_GRADLE_FILE)).thenReturn(buildGradleFile)

        val result = projectDeterminer.isGradleProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is gradle one if it has build gradle kts`() {
        whenever(project.baseDir).thenReturn(baseDir)
        val buildGradleKtsFile: VirtualFile = mock()
        whenever(baseDir.findChild(GradleProjectDeterminer.BUILD_GRADLE_KTS_FILE)).thenReturn(buildGradleKtsFile)

        val result = projectDeterminer.isGradleProject(project)

        assertTrue(result)
    }

    @Test
    fun `should determine that project is not gradle one if build gradle not found`() {
        whenever(project.baseDir).thenReturn(baseDir)

        val result = projectDeterminer.isGradleProject(project)

        assertFalse(result)
    }

    @Test
    fun `should determine that project is not gradle one if base dir not found`() {
        val result = projectDeterminer.isGradleProject(project)

        assertFalse(result)
    }
}
