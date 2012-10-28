package pl.mjedynak.idea.plugins.pit.cli.model

import spock.lang.Specification

class PitCommandLineArgumentTest extends Specification {

    PitCommandLineArgument pitCommandLineArgument;

    def "report dir argument is mapped"() {
        when:
        pitCommandLineArgument = PitCommandLineArgument.REPORT_DIR

        then:
        pitCommandLineArgument.getName() == '--reportDir'
    }

    def "source dirs argument is mapped"() {
        when:
        pitCommandLineArgument = PitCommandLineArgument.SOURCE_DIRS

        then:
        pitCommandLineArgument.getName() == '--sourceDirs'
    }

    def "target classes argument is mapped"() {
        when:
        pitCommandLineArgument = PitCommandLineArgument.TARGET_CLASSES

        then:
        pitCommandLineArgument.getName() == '--targetClasses'
    }
}
