package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.PathManager
import com.intellij.util.PathsList
import groovy.transform.CompileStatic

@CompileStatic
class ClassPathPopulator {

	static final String PITEST_VERSION = '1.20.0'
	static final String PITEST_JUNIT5_PLUGIN_VERSION = '1.2.3'
	static final String SEPARATOR = System.getProperty('file.separator')
	static final String PLUGIN_NAME = 'pit-idea-plugin'
	static final String LIB_DIR = 'lib'

	void populateClassPathWithPitJar(PathsList classPath) {
		String pluginsPath = PathManager.pluginsPath
		String path = pluginsPath + SEPARATOR + PLUGIN_NAME + SEPARATOR + LIB_DIR + SEPARATOR
		classPath.with {
			addFirst(path + "pitest-${PITEST_VERSION}.jar")
			addFirst(path + "pitest-command-line-${PITEST_VERSION}.jar")
			addFirst(path + "pitest-entry-${PITEST_VERSION}.jar")
			addFirst(path + 'commons-lang3-3.12.0.jar')
			addFirst(path + 'commons-text-1.10.0.jar')
			addFirst(path + "pitest-junit5-plugin-${PITEST_JUNIT5_PLUGIN_VERSION}.jar")
			if (noPlatformLauncherDependency(classPath)) {
				addFirst(path + 'junit-platform-launcher-1.9.2.jar')
			}
		}
	}

	private static boolean noPlatformLauncherDependency(PathsList classPath) {
		return !classPath.pathList.find { it.contains("junit-platform-launcher") }
	}
}
