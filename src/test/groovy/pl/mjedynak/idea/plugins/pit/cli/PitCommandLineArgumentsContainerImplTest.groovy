package pl.mjedynak.idea.plugins.pit.cli

import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument
import spock.lang.Specification

class PitCommandLineArgumentsContainerImplTest extends Specification {

	PitCommandLineArgumentsContainer pitCommandLineArgumentsContainer = new PitCommandLineArgumentsContainerImpl()

	def "should hold value of command line argument"() {
		PitCommandLineArgument argument = PitCommandLineArgument.REPORT_DIR
		String reportDir = 'report'

		when:
		pitCommandLineArgumentsContainer.put(argument, reportDir)

		then:
		pitCommandLineArgumentsContainer.get(argument) == reportDir
	}

	def "should hold multiple values of command line arguments"() {
		PitCommandLineArgument argument = PitCommandLineArgument.REPORT_DIR
		String reportDir = 'report'
		PitCommandLineArgument secondArgument = PitCommandLineArgument.SOURCE_DIRS
		String sourceDirs = 'src/main/java'

		when:
		pitCommandLineArgumentsContainer.put(argument, reportDir)
		pitCommandLineArgumentsContainer.put(secondArgument, sourceDirs)

		then:
		pitCommandLineArgumentsContainer.get(argument) == reportDir
		pitCommandLineArgumentsContainer.get(secondArgument) == sourceDirs
	}
}
