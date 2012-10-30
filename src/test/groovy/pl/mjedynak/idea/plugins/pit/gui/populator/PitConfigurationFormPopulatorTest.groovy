package pl.mjedynak.idea.plugins.pit.gui.populator

import spock.lang.Specification
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory

import com.intellij.openapi.project.Project
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument

class PitConfigurationFormPopulatorTest extends Specification {

    PitConfigurationFormPopulator pitConfigurationFormPopulator = new PitConfigurationFormPopulator()

    PitConfigurationForm pitConfigurationForm = Mock()
    DefaultArgumentsContainerFactory defaultArgumentsContainerFactory = Mock()
    Project project = Mock()
    PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer = Mock()
    String reportDir = "reportDir"
    String sourceDir = "srcDir"
    String targetClasses = "targetClasses"

    def "should populate form text fields using argument container created by factory"() {
        defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project) >> pitCommandLineArgumentsContainer
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.REPORT_DIR) >> reportDir
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.SOURCE_DIRS) >> sourceDir
        pitCommandLineArgumentsContainer.get(PitCommandLineArgument.TARGET_CLASSES) >> targetClasses

        when:
        pitConfigurationFormPopulator.populateTextFieldsInForm(pitConfigurationForm, defaultArgumentsContainerFactory, project)

        then:
        1 * pitConfigurationForm.setReportDir(reportDir)
        1 * pitConfigurationForm.setSourceDir(sourceDir)
        1 * pitConfigurationForm.setTargetClasses(targetClasses)
        1 * pitConfigurationForm.setOtherParams(pitConfigurationFormPopulator.OTHER_PARAMS)
    }
}
