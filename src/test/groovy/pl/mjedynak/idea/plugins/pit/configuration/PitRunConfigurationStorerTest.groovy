package pl.mjedynak.idea.plugins.pit.configuration

import org.jdom.Attribute
import org.jdom.Element
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import spock.lang.Specification

import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.*
import static pl.mjedynak.idea.plugins.pit.configuration.PitRunConfigurationStorer.OTHER_PARAMS

class PitRunConfigurationStorerTest extends Specification {

    PitRunConfigurationStorer pitRunConfigurationStorer = new PitRunConfigurationStorer()

    PitConfigurationForm pitConfigurationForm = Mock()
    Element element = Mock()
    Attribute reportDirAttribute = Mock()
    Attribute sourceDirAttribute = Mock()
    Attribute targetClassesAttribute = Mock()
    Attribute otherParamsAttribute = Mock()

    String reportDir = 'reportDir'
    String sourceDir = 'sourceDir'
    String targetClasses = 'targetClasses'
    String otherParams = 'params'

    def "should populate configuration form with attributes"() {
        element.getAttribute(REPORT_DIR.toString()) >> reportDirAttribute
        reportDirAttribute.value >> reportDir

        element.getAttribute(SOURCE_DIRS.toString()) >> sourceDirAttribute
        sourceDirAttribute.value >> sourceDir

        element.getAttribute(TARGET_CLASSES.toString()) >> targetClassesAttribute
        targetClassesAttribute.value >> targetClasses

        element.getAttribute(OTHER_PARAMS) >> otherParamsAttribute
        otherParamsAttribute.value >> otherParams

        when:
        pitRunConfigurationStorer.readExternal(pitConfigurationForm, element)

        then:
        1 * pitConfigurationForm.setReportDir(reportDir)
        1 * pitConfigurationForm.setSourceDir(sourceDir)
        1 * pitConfigurationForm.setTargetClasses(targetClasses)
        1 * pitConfigurationForm.setOtherParams(otherParams)
    }

    def "should not throw NPE when parameter is missing"() {
        when:
        pitRunConfigurationStorer.readExternal(pitConfigurationForm, element)

        then:
        1 * pitConfigurationForm.setReportDir(null)
        1 * pitConfigurationForm.setSourceDir(null)
        1 * pitConfigurationForm.setTargetClasses(null)
        1 * pitConfigurationForm.setOtherParams(null)
    }

    def "should set attributes from configuration form"() {
        pitConfigurationForm.reportDir >> reportDir
        pitConfigurationForm.sourceDir >> sourceDir
        pitConfigurationForm.targetClasses >> targetClasses
        pitConfigurationForm.otherParams >> otherParams

        when:
        pitRunConfigurationStorer.writeExternal(pitConfigurationForm, element)

        then:
        1 * element.setAttribute(REPORT_DIR.toString(), reportDir)
        1 * element.setAttribute(SOURCE_DIRS.toString(), sourceDir)
        1 * element.setAttribute(TARGET_CLASSES.toString(), targetClasses)
        1 * element.setAttribute(OTHER_PARAMS, otherParams)
    }

}
