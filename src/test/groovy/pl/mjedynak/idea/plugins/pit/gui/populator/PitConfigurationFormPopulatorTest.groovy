package pl.mjedynak.idea.plugins.pit.gui.populator

import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import spock.lang.Specification

class PitConfigurationFormPopulatorTest extends Specification {

    PitConfigurationFormPopulator pitConfigurationFormPopulator = new PitConfigurationFormPopulator()

    PitConfigurationForm pitConfigurationForm = Mock()
    PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer = Mock()
    String reportDir = 'reportDir'
    String sourceDir = 'srcDir'
    String targetClasses = 'targetClasses'

    def "should populate form text fields using argument container created by factory"() {
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.REPORT_DIR) >> reportDir
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.SOURCE_DIRS) >> sourceDir
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.TARGET_CLASSES) >> targetClasses

        when:
        pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, pitCommandLineArgumentsContainer)

        then:
        1 * pitConfigurationForm.setReportDir(reportDir)
        1 * pitConfigurationForm.setSourceDir(sourceDir)
        1 * pitConfigurationForm.setTargetClasses(targetClasses)
        1 * pitConfigurationForm.setOtherParams(pitConfigurationFormPopulator.OTHER_PARAMS)
    }
}
