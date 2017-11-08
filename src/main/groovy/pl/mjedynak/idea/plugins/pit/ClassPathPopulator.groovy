package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList

class ClassPathPopulator {

    static final String PITEST_VERSION = '1.2.4'
    static final String PITEST_JAR = 'pitest-' + PITEST_VERSION + JAR_EXTENSION
    static final String PITEST_COMMAND_LINE_JAR = 'pitest-command-line-' + PITEST_VERSION + JAR_EXTENSION
    static final String PITEST_ENTRY_JAR = 'pitest-entry-' + PITEST_VERSION + JAR_EXTENSION
    static final String SEPARATOR = System.getProperty('file.separator')
    static final String PLUGIN_NAME = 'pit-idea-plugin'
    static final String LIB_DIR = 'lib'
    static final String JAR_EXTENSION = '.jar'

    void populateClassPathWithPitJar(PathsList classPath) {
        String pluginsPath = PathManager.pluginsPath
        String path = pluginsPath + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR
        classPath.with {
            add(path + PITEST_JAR)
            add(path + PITEST_COMMAND_LINE_JAR)
            add(path + PITEST_ENTRY_JAR)
            add(path + 'xstream-1.4.8.jar')
            add(path + 'xmlpull-1.1.3.1.jar')
            add(path + 'xpp3_min-1.1.4c.jar')
        }
    }
}
