package pl.mjedynak.idea.plugins.pit

import spock.lang.Specification

import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.PITEST_VERSION

class ClassPathPopulatorTest extends Specification {

    def "should have the same PIT version as specified in build.gradle"() {
        when:
        File gradleBuildFile = new File('build.gradle')
        String lineWithVersion = gradleBuildFile.filterLine { String line -> line.startsWith('ext.pitVersion') }
        String version = lineWithVersion[lineWithVersion.indexOf("'") + 1 .. lineWithVersion.lastIndexOf("'") - 1]

        then:
        version == PITEST_VERSION
    }
}
