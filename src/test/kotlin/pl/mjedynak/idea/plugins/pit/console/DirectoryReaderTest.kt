package pl.mjedynak.idea.plugins.pit.console

import com.google.common.base.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.io.File

class DirectoryReaderTest {
    private val directoryReader = DirectoryReader()

    @Test
    fun `should return empty option when no directories in parent directory`() {
        val tempDir = createTempDir()
        tempDir.deleteOnExit()

        val result: Optional<File> = directoryReader.getLatestDirectoryFrom(tempDir)

        assertFalse(result.isPresent)
    }

    @Test
    fun `should return empty option when given incorrect argument`() {
        val result: Optional<File> = directoryReader.getLatestDirectoryFrom(File("notExistingDirectory"))

        assertFalse(result.isPresent)
    }
}
