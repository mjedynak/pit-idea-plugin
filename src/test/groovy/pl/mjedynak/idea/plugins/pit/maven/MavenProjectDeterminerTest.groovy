package pl.mjedynak.idea.plugins.pit.maven

import spock.lang.Specification
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MavenProjectDeterminerTest extends Specification {

	MavenProjectDeterminer projectDeterminer = new MavenProjectDeterminer()
	Project project = Mock()
	VirtualFile baseDir = Mock()

	def "should determine that project is mavenized if it has pom.xml"() {
		project.baseDir >> baseDir
		VirtualFile pomFile = Mock()
		baseDir.findChild(MavenProjectDeterminer.POM_FILE) >> pomFile

		when:
		def result = projectDeterminer.isMavenProject(project)

		then:
		result == true
	}

	def "should determine that project is not mavenized if pom.xml not found"() {
		project.baseDir >> baseDir

		when:
		def result = projectDeterminer.isMavenProject(project)

		then:
		result == false
	}

	def "should determine that project is not mavenized if base dir not found"() {
		when:
		def result = projectDeterminer.isMavenProject(project)

		then:
		result == false
	}
}
