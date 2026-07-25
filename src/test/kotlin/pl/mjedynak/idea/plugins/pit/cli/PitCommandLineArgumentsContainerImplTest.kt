package pl.mjedynak.idea.plugins.pit.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument

class PitCommandLineArgumentsContainerImplTest {
    private val pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer =
        PitCommandLineArgumentsContainerImpl()

    @Test
    fun `should hold value of command line argument`() {
        val argument = PitCommandLineArgument.REPORT_DIR
        val reportDir = "report"

        pitCommandLineArgumentsContainer.put(argument, reportDir)

        assertEquals(reportDir, pitCommandLineArgumentsContainer.get(argument))
    }

    @Test
    fun `should hold multiple values of command line arguments`() {
        val argument = PitCommandLineArgument.REPORT_DIR
        val reportDir = "report"
        val secondArgument = PitCommandLineArgument.SOURCE_DIRS
        val sourceDirs = "src/main/java"

        pitCommandLineArgumentsContainer.put(argument, reportDir)
        pitCommandLineArgumentsContainer.put(secondArgument, sourceDirs)

        assertEquals(reportDir, pitCommandLineArgumentsContainer.get(argument))
        assertEquals(sourceDirs, pitCommandLineArgumentsContainer.get(secondArgument))
    }
}
