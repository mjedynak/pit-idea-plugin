package pl.mjedynak.idea.plugins.pit.configuration

import groovy.transform.CompileStatic
import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES

@CompileStatic
class PitRunConfigurationStorer {

    static final String OTHER_PARAMS = 'otherParams'

    void readExternal(PitConfigurationForm pitConfigurationForm, Element element) {
        pitConfigurationForm.reportDir = element.getAttribute(REPORT_DIR.toString())?.value
        pitConfigurationForm.sourceDir = element.getAttribute(SOURCE_DIRS.toString())?.value
        pitConfigurationForm.targetClasses = element.getAttribute(TARGET_CLASSES.toString())?.value
        pitConfigurationForm.otherParams = element.getAttribute(OTHER_PARAMS)?.value
    }

    void writeExternal(PitConfigurationForm pitConfigurationForm, Element element) {
        element.setAttribute(REPORT_DIR.toString(), pitConfigurationForm.reportDir)
        element.setAttribute(SOURCE_DIRS.toString(), pitConfigurationForm.sourceDir)
        element.setAttribute(TARGET_CLASSES.toString(), pitConfigurationForm.targetClasses)
        element.setAttribute(OTHER_PARAMS, pitConfigurationForm.otherParams)
    }

}
