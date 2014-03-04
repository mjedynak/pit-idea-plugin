package pl.mjedynak.idea.plugins.pit.gui.populator

import spock.lang.Specification
import com.intellij.execution.configurations.ParametersList
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument

class ProgramParametersListPopulatorTest extends Specification {

    ProgramParametersListPopulator parametersListPopulator = new ProgramParametersListPopulator()

    def "should populate program parameter list with pit configuration form content"() {
        ParametersList programParametersList = Mock()
        PitConfigurationForm pitConfigurationForm = Mock()
        String reportDir = 'reportDir'
        String targetClasses = 'targetClasses'
        String sourceDir = 'sourceDir'
        String paramName = 'paramName'
        String paramValue = 'paramValue'
        pitConfigurationForm.reportDir >> reportDir
        pitConfigurationForm.targetClasses >> targetClasses
        pitConfigurationForm.sourceDir >> sourceDir
        pitConfigurationForm.otherParams >> paramName + ' ' + paramValue

        when:
        parametersListPopulator.populateProgramParametersList(programParametersList, pitConfigurationForm)

        then:
        1 * programParametersList.add(PitCommandLineArgument.REPORT_DIR.name)
        1 * programParametersList.add(reportDir)
        1 * programParametersList.add(PitCommandLineArgument.SOURCE_DIRS.name)
        1 * programParametersList.add(sourceDir)
        1 * programParametersList.add(PitCommandLineArgument.TARGET_CLASSES.name)
        1 * programParametersList.add(targetClasses)
        1 * programParametersList.add(paramName)
        1 * programParametersList.add(paramValue)

    }
}
