package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList

class ClassPathPopulator {

    static final String PITEST_JAR = "pitest-0.32.jar"
    static final String PITEST_COMMAND_LINE_JAR = "pitest-command-line-0.32.jar"
    static final String SEPARATOR = System.getProperty("file.separator")

    void populateClassPathWithPitJar(PathsList classPath) {
        String pluginsPath = PathManager.getPluginsPath()
        String path = pluginsPath + SEPARATOR + "pit-idea-plugin" + SEPARATOR + "lib" + SEPARATOR
        classPath.add(path + PITEST_JAR)
        classPath.add(path + PITEST_COMMAND_LINE_JAR)
    }
}
