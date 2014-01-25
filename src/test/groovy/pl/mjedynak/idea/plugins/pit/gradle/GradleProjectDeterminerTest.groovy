package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import spock.lang.Specification

class GradleProjectDeterminerTest extends Specification {

    Project project = Mock()
    VirtualFile baseDir = Mock()
    GradleProjectDeterminer projectDeterminer = new GradleProjectDeterminer()

    def "should determine that project is gradle one if it has build.gradle"() {
        project.baseDir >> baseDir
        VirtualFile buildGradleFile = Mock()
        baseDir.findChild(GradleProjectDeterminer.BUILD_GRADLE_FILE) >> buildGradleFile

        when:
        def result = projectDeterminer.isGradleProject(project)

        then:
        result == true
    }

    def "should determine that project is not gradle one if build.gradle not found"() {
        project.baseDir >> baseDir

        when:
        def result = projectDeterminer.isGradleProject(project)

        then:
        result == false
    }

    def "should determine that project is not gradle one if base dir not found"() {
        when:
        def result = projectDeterminer.isGradleProject(project)

        then:
        result == false
    }
}
