package pl.mjedynak.idea.plugins.pit.maven

import com.intellij.openapi.project.Project
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
class MavenProjectDeterminer {
	public static final String POM_FILE = 'pom.xml'

	boolean isMavenProject(@NotNull Project project) {
		project.baseDir?.findChild(POM_FILE) != null
	}
}
