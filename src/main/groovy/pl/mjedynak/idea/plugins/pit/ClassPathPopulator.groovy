package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList

class ClassPathPopulator {

    static final String PITEST_VERSION = '1.5.1'
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
            addFirst(path + PITEST_JAR)
            addFirst(path + PITEST_COMMAND_LINE_JAR)
            addFirst(path + PITEST_ENTRY_JAR)
            addFirst(path + 'xstream-1.4.8.jar')
            addFirst(path + 'xmlpull-1.1.3.1.jar')
            addFirst(path + 'xpp3_min-1.1.4c.jar')
        }
    }
}
