package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList
import java.nio.file.FileSystems

class ClassPathPopulator {
    fun populateClassPathWithPitJar(classPath: PathsList) {
        val pluginsPath = PathManager.getPluginsPath()
        val path = "$pluginsPath${SEPARATOR}$PLUGIN_NAME${SEPARATOR}$LIB_DIR$SEPARATOR"
        classPath.apply {
            addFirst("${path}pitest-$PITEST_VERSION.jar")
            addFirst("${path}pitest-command-line-$PITEST_VERSION.jar")
            addFirst("${path}pitest-entry-$PITEST_VERSION.jar")
            addFirst("${path}commons-text-1.14.0.jar")
            addFirst("${path}commons-lang3-3.18.0.jar") // transitive dependency of commons-text
            addFirst("${path}pitest-junit5-plugin-$PITEST_JUNIT5_PLUGIN_VERSION.jar")
            if (noPlatformLauncherDependency(classPath)) {
                addFirst("${path}junit-platform-launcher-1.9.2.jar")
            }
        }
    }

    private fun noPlatformLauncherDependency(classPath: PathsList): Boolean =
        classPath.pathList.none { it.contains("junit-platform-launcher") }

    companion object {
        const val PITEST_VERSION = "1.20.7"
        const val PITEST_JUNIT5_PLUGIN_VERSION = "1.2.3"
        val SEPARATOR: String = FileSystems.getDefault().separator
        const val PLUGIN_NAME = "pit-idea-plugin"
        const val LIB_DIR = "lib"
    }
}
