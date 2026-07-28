package pl.mjedynak.idea.plugins.pit.cli.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PitCommandLineArgumentTest {
    @Test
    fun `report dir argument is mapped`() {
        val pitCommandLineArgument = PitCommandLineArgument.REPORT_DIR

        assertEquals("--reportDir", pitCommandLineArgument.argumentName)
    }

    @Test
    fun `source dirs argument is mapped`() {
        val pitCommandLineArgument = PitCommandLineArgument.SOURCE_DIRS

        assertEquals("--sourceDirs", pitCommandLineArgument.argumentName)
    }

    @Test
    fun `target classes argument is mapped`() {
        val pitCommandLineArgument = PitCommandLineArgument.TARGET_CLASSES

        assertEquals("--targetClasses", pitCommandLineArgument.argumentName)
    }
}
