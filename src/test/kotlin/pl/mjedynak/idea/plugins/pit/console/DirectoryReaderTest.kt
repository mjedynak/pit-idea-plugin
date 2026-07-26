package pl.mjedynak.idea.plugins.pit.console

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Optional
import kotlin.io.path.createTempDirectory

class DirectoryReaderTest {
    private val directoryReader = DirectoryReader()

    @Test
    fun `should return empty option when no directories in parent directory`() {
        val tempDir = createTempDirectory().toFile()
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
