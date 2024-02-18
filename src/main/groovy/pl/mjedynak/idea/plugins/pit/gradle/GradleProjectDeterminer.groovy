package pl.mjedynak.idea.plugins.pit.gradle

import com.intellij.openapi.project.Project
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
class GradleProjectDeterminer {

	static final String BUILD_GRADLE_FILE = 'build.gradle'

	boolean isGradleProject(@NotNull Project project) {
		project.baseDir?.findChild(BUILD_GRADLE_FILE) != null
	}
}
