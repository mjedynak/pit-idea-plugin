package pl.mjedynak.idea.plugins.pit.configuration

import groovy.transform.CompileStatic
import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.*

@CompileStatic
class PitRunConfigurationStorer {

    void readExternal(PitConfigurationForm pitConfigurationForm, Element element) {
        pitConfigurationForm.setReportDir(element.getAttribute(REPORT_DIR.toString())?.getValue());
        pitConfigurationForm.setSourceDir(element.getAttribute(SOURCE_DIRS.toString())?.getValue());
        pitConfigurationForm.setTargetClasses(element.getAttribute(TARGET_CLASSES.toString())?.getValue());
        pitConfigurationForm.setOtherParams(element.getAttribute("otherParams")?.getValue());
    }

    void writeExternal(PitConfigurationForm pitConfigurationForm, Element element) {
        element.setAttribute(REPORT_DIR.toString(), pitConfigurationForm.getReportDir());
        element.setAttribute(SOURCE_DIRS.toString(), pitConfigurationForm.getSourceDir());
        element.setAttribute(TARGET_CLASSES.toString(), pitConfigurationForm.getTargetClasses());
        element.setAttribute("otherParams", pitConfigurationForm.getOtherParams());
    }

}
