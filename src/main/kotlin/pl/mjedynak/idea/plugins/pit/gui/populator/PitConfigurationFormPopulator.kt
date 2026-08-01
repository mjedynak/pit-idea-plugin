package pl.mjedynak.idea.plugins.pit.gui.populator

import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm

class PitConfigurationFormPopulator {
    fun populateTextFieldsInForm(
        pitConfigurationForm: PitConfigurationForm,
        pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer,
    ) {
        if (pitConfigurationForm.reportDir.isBlank()) {
            setReportDir(pitConfigurationForm, pitCommandLineArgumentsContainer)
        }
        if (pitConfigurationForm.sourceDir.isBlank()) {
            setSourceDir(pitConfigurationForm, pitCommandLineArgumentsContainer)
        }
        if (pitConfigurationForm.targetClasses.isBlank()) {
            setTargetClasses(pitConfigurationForm, pitCommandLineArgumentsContainer)
        }
        if (pitConfigurationForm.targetTests.isBlank()) {
            setTargetTests(pitConfigurationForm, pitCommandLineArgumentsContainer)
        }
        if (pitConfigurationForm.otherParams.isBlank()) {
            setOtherParams(pitConfigurationForm)
        }
    }

    private fun setTargetClasses(
        pitConfigurationForm: PitConfigurationForm,
        pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer,
    ) {
        val targetClasses = pitCommandLineArgumentsContainer.get(TARGET_CLASSES)
        pitConfigurationForm.targetClasses = targetClasses ?: ""
    }

    private fun setTargetTests(
        pitConfigurationForm: PitConfigurationForm,
        pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer,
    ) {
        val targetTests = pitCommandLineArgumentsContainer.get(TARGET_TESTS)
        pitConfigurationForm.targetTests = targetTests ?: ""
    }

    private fun setSourceDir(
        pitConfigurationForm: PitConfigurationForm,
        pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer,
    ) {
        val sourceDir = pitCommandLineArgumentsContainer.get(SOURCE_DIRS)
        pitConfigurationForm.sourceDir = sourceDir ?: ""
    }

    private fun setReportDir(
        pitConfigurationForm: PitConfigurationForm,
        pitCommandLineArgumentsContainer: PitCommandLineArgumentsContainer,
    ) {
        val reportDir = pitCommandLineArgumentsContainer.get(REPORT_DIR)
        pitConfigurationForm.reportDir = reportDir ?: ""
    }

    private fun setOtherParams(pitConfigurationForm: PitConfigurationForm) {
        pitConfigurationForm.otherParams = OTHER_PARAMS
    }

    companion object {
        const val OTHER_PARAMS = "--outputFormats XML,HTML"
    }
}
