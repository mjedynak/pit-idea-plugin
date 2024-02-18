package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import spock.lang.Specification

class DefaultArgumentsContainerFactoryTest extends Specification {

	DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator = Mock()
	Project project = Mock()

	DefaultArgumentsContainerFactory defaultArgumentsContainerFactory = new DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator)

	def "should delegate creation to populator"() {
		when:
		PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

		then:
		container != null
		1 * defaultArgumentsContainerPopulator.addReportDir(project, _ as PitCommandLineArgumentsContainer)
		1 * defaultArgumentsContainerPopulator.addSourceDir(_ as PitCommandLineArgumentsContainer)
		1 * defaultArgumentsContainerPopulator.addTargetClasses(project, _ as PitCommandLineArgumentsContainer)
	}
}
