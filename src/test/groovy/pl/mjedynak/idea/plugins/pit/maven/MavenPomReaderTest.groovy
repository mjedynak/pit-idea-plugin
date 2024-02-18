package pl.mjedynak.idea.plugins.pit.maven

import spock.lang.Specification

class MavenPomReaderTest extends Specification {

	MavenPomReader mavenPomReader = new MavenPomReader()

	def "should read group id from maven pom file"() {
		String groupId = 'group'
		String pomFileText = """<project>
                                <groupId>$groupId</groupId>
                                <artifactId>artifact</artifactId>
                                <version>1.0.0</version>
                            </project>"""
		InputStream pomFile = new ByteArrayInputStream(pomFileText.bytes)
		when:
		String result = mavenPomReader.getGroupId(pomFile)
		then:
		result == groupId
	}
}
