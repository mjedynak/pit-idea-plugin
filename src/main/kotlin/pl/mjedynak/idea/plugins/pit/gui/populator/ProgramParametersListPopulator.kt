package pl.mjedynak.idea.plugins.pit.gui.populator

import com.intellij.execution.configurations.ParametersList
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm

class ProgramParametersListPopulator {
    fun populateProgramParametersList(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        addReportDir(programParametersList, pitConfigurationForm)
        addSourceDir(programParametersList, pitConfigurationForm)
        addTargetClasses(programParametersList, pitConfigurationForm)
        addTargetTests(programParametersList, pitConfigurationForm)
        addOtherParams(programParametersList, pitConfigurationForm)
    }

    private fun addReportDir(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        programParametersList.add(REPORT_DIR.argumentName)
        programParametersList.add(pitConfigurationForm.reportDir)
    }

    private fun addSourceDir(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        programParametersList.add(SOURCE_DIRS.argumentName)
        programParametersList.add(pitConfigurationForm.sourceDir)
    }

    private fun addTargetClasses(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        programParametersList.add(TARGET_CLASSES.argumentName)
        programParametersList.add(pitConfigurationForm.targetClasses)
    }

    private fun addTargetTests(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        if (!pitConfigurationForm.targetTests.isEmpty()) {
            programParametersList.add(TARGET_TESTS.argumentName)
            programParametersList.add(pitConfigurationForm.targetTests)
        }
    }

    private fun addOtherParams(
        programParametersList: ParametersList,
        pitConfigurationForm: PitConfigurationForm,
    ) {
        val otherParams = pitConfigurationForm.otherParams
        val namesAndValues = otherParams.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (param in namesAndValues) {
            programParametersList.add(param)
        }
    }
}
