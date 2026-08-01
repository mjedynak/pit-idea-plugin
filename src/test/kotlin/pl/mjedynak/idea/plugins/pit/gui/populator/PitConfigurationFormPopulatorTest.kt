package pl.mjedynak.idea.plugins.pit.gui.populator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulator.Companion.OTHER_PARAMS

class PitConfigurationFormPopulatorTest {
    private val pitConfigurationForm = PitConfigurationForm()
    private val pitConfigurationFormPopulator = PitConfigurationFormPopulator()

    @Test
    fun `should fill blank fields with values from container`() {
        val container = containerWithValues()

        pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, container)

        assertEquals("report", pitConfigurationForm.reportDir)
        assertEquals("src/main/java", pitConfigurationForm.sourceDir)
        assertEquals("pl.example.*", pitConfigurationForm.targetClasses)
        assertEquals("pl.example.CalculatorTest", pitConfigurationForm.targetTests)
        assertEquals(OTHER_PARAMS, pitConfigurationForm.otherParams)
    }

    @Test
    fun `should not overwrite already filled fields`() {
        pitConfigurationForm.reportDir = "stored/report"
        pitConfigurationForm.targetTests = "stored.CalculatorTest"
        val container = containerWithValues()

        pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, container)

        assertEquals("stored/report", pitConfigurationForm.reportDir)
        assertEquals("src/main/java", pitConfigurationForm.sourceDir)
        assertEquals("pl.example.*", pitConfigurationForm.targetClasses)
        assertEquals("stored.CalculatorTest", pitConfigurationForm.targetTests)
        assertEquals(OTHER_PARAMS, pitConfigurationForm.otherParams)
    }

    private fun containerWithValues(): PitCommandLineArgumentsContainer {
        val container = PitCommandLineArgumentsContainerImpl()
        container.put(REPORT_DIR, "report")
        container.put(SOURCE_DIRS, "src/main/java")
        container.put(TARGET_CLASSES, "pl.example.*")
        container.put(TARGET_TESTS, "pl.example.CalculatorTest")
        return container
    }
}
