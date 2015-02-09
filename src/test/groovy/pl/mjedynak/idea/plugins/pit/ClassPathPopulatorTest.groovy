package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList
import spock.lang.Specification

import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.LIB_DIR
import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.PITEST_COMMAND_LINE_JAR
import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.PITEST_JAR
import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.PITEST_VERSION
import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.PLUGIN_NAME
import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.SEPARATOR

class ClassPathPopulatorTest extends Specification {

    ClassPathPopulator classPathPopulator = new ClassPathPopulator()
    PathsList classPath = Mock()
    private final static String PLUGINS_PATH = 'path'

    def "should populate classpath"() {
        GroovyMock(PathManager, global:true)
        PathManager.pluginsPath >> PLUGINS_PATH

        when:
        classPathPopulator.populateClassPathWithPitJar(classPath)

        then:
        1 * classPath.add(PLUGINS_PATH + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR + PITEST_JAR)
        1 * classPath.add(PLUGINS_PATH + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR + PITEST_COMMAND_LINE_JAR)
    }

    def "should have the same PIT version as specified in build.gradle"() {
        when:
        File gradleBuildFile = new File('build.gradle')
        String lineWithVersion = gradleBuildFile.filterLine { String line -> line.startsWith('ext.pitVersion') }
        String version = lineWithVersion[lineWithVersion.indexOf("'") + 1 .. lineWithVersion.lastIndexOf("'") - 1]

        then:
        version == PITEST_VERSION
    }
}
