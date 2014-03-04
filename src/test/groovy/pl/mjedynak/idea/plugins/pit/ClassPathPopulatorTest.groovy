package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList
import spock.lang.Specification

import static pl.mjedynak.idea.plugins.pit.ClassPathPopulator.*

class ClassPathPopulatorTest extends Specification {

    ClassPathPopulator classPathPopulator = new ClassPathPopulator()
    PathsList classPath = Mock()
    private final static String PLUGINS_PATH = 'path'

    def "should populate classpath"() {
        GroovyMock(PathManager, global: true)
        PathManager.pluginsPath >> PLUGINS_PATH

        when:
        classPathPopulator.populateClassPathWithPitJar(classPath)

        then:
        1 * classPath.add(PLUGINS_PATH + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR + PITEST_JAR)
        1 * classPath.add(PLUGINS_PATH + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR + PITEST_COMMAND_LINE_JAR)
    }
}
