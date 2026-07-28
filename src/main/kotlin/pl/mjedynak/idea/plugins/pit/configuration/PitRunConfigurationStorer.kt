package pl.mjedynak.idea.plugins.pit.configuration

import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_TESTS
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm

class PitRunConfigurationStorer {
    fun readExternal(
        pitConfigurationForm: PitConfigurationForm,
        element: Element,
    ) {
        pitConfigurationForm.reportDir = element.getAttribute(REPORT_DIR.toString())?.value ?: ""
        pitConfigurationForm.sourceDir = element.getAttribute(SOURCE_DIRS.toString())?.value ?: ""
        pitConfigurationForm.targetClasses = element.getAttribute(TARGET_CLASSES.toString())?.value ?: ""
        pitConfigurationForm.targetTests = element.getAttribute(TARGET_TESTS.toString())?.value ?: ""
        pitConfigurationForm.otherParams = element.getAttribute(OTHER_PARAMS)?.value ?: ""
    }

    fun writeExternal(
        pitConfigurationForm: PitConfigurationForm,
        element: Element,
    ) {
        element.setAttribute(REPORT_DIR.toString(), pitConfigurationForm.reportDir)
        element.setAttribute(SOURCE_DIRS.toString(), pitConfigurationForm.sourceDir)
        element.setAttribute(TARGET_CLASSES.toString(), pitConfigurationForm.targetClasses)
        element.setAttribute(TARGET_TESTS.toString(), pitConfigurationForm.targetTests)
        element.setAttribute(OTHER_PARAMS, pitConfigurationForm.otherParams)
    }

    companion object {
        const val OTHER_PARAMS = "otherParams"
    }
}
