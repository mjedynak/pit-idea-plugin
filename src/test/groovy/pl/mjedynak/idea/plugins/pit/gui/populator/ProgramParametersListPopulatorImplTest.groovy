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
        String reportDir = "reportDir"
        String targetClasses = "targetClasses"
        String sourceDir = "sourceDir"
        pitConfigurationForm.reportDir >> reportDir
        pitConfigurationForm.targetClasses >> targetClasses
        pitConfigurationForm.sourceDir >> sourceDir

        when:
        parametersListPopulator.populateProgramParametersList(programParametersList, pitConfigurationForm)

        then:
        1 * programParametersList.add(PitCommandLineArgument.REPORT_DIR.getName())
        1 * programParametersList.add(reportDir);
        1 * programParametersList.add(PitCommandLineArgument.SOURCE_DIRS.getName())
        1 * programParametersList.add(sourceDir)
        1 * programParametersList.add(PitCommandLineArgument.TARGET_CLASSES.getName())
        1 * programParametersList.add(targetClasses);

    }
}
