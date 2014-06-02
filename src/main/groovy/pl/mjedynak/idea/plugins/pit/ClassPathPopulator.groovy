package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList

class ClassPathPopulator {

    static final String PITEST_VERSION = '1.0.0'
    static final String PITEST_JAR = 'pitest-' + PITEST_VERSION + '.jar'
    static final String PITEST_COMMAND_LINE_JAR = 'pitest-command-line-' + PITEST_VERSION + '.jar'
    static final String SEPARATOR = System.getProperty('file.separator')
    static final String PLUGIN_NAME = 'pit-idea-plugin'
    static final String LIB_DIR = 'lib'

    void populateClassPathWithPitJar(PathsList classPath) {
        String pluginsPath = PathManager.pluginsPath
        String path = pluginsPath + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR
        classPath.add(path + PITEST_JAR)
        classPath.add(path + PITEST_COMMAND_LINE_JAR)
    }
}
