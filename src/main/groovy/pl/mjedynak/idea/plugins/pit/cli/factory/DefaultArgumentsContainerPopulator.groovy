package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import groovy.transform.CompileStatic
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument
import pl.mjedynak.idea.plugins.pit.gradle.GradleProjectDeterminer
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader
import pl.mjedynak.idea.plugins.pit.maven.MavenProjectDeterminer

import static org.apache.commons.lang3.ArrayUtils.isEmpty
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES

@CompileStatic
class DefaultArgumentsContainerPopulator {

	static final String DEFAULT_REPORT_DIR = 'report'
	static final String MAVEN_REPORT_DIR = 'target/report'
	static final String GRADLE_REPORT_DIR = 'build/reports/pit'
	static final String ALL_CLASSES_SUFFIX = '.*'

	private ProjectRootManager projectRootManager
	private PsiManager psiManager
	private MavenProjectDeterminer mavenProjectDeterminer = new MavenProjectDeterminer()
	private GradleProjectDeterminer gradleProjectDeterminer = new GradleProjectDeterminer()
	private MavenPomReader mavenPomReader = new MavenPomReader()

	DefaultArgumentsContainerPopulator(ProjectRootManager projectRootManager, PsiManager psiManager) {
		this.projectRootManager = projectRootManager
		this.psiManager = psiManager
	}

	void addReportDir(Project project, PitCommandLineArgumentsContainer container) {
		VirtualFile baseDir = project.baseDir
		String reportDir
		if (baseDir != null) {
			String suffix = DEFAULT_REPORT_DIR
			if (mavenProjectDeterminer.isMavenProject(project)) {
				suffix = MAVEN_REPORT_DIR
			} else if (gradleProjectDeterminer.isGradleProject(project)) {
				suffix = GRADLE_REPORT_DIR
			}
			reportDir = baseDir.path + '/' + suffix
			container.put(PitCommandLineArgument.REPORT_DIR, reportDir)
		}
	}

	void addSourceDir(PitCommandLineArgumentsContainer container) {
		VirtualFile[] sourceRoots = projectRootManager.contentSourceRoots
		VirtualFile javaSrcFolder = sourceRoots.find { VirtualFile sourceRoot ->
			sourceRoot.path.contains('java')
		}
		if (javaSrcFolder != null) {
			container.put(SOURCE_DIRS, javaSrcFolder.path)
		} else if (hasAtLeastOneSourceRoot(sourceRoots)) {
			String sourceRootPath = sourceRoots[0].path
			container.put(SOURCE_DIRS, sourceRootPath)
		}
	}

	void addTargetClasses(Project project, PitCommandLineArgumentsContainer container) {
		if (mavenProjectDeterminer.isMavenProject(project)) {
			addTargetClassesForMavenProject(project, container)
		} else {
			addTargetClassesForNonMavenProject(container)
		}
	}

	private void addTargetClassesForMavenProject(Project project, PitCommandLineArgumentsContainer container) {
		VirtualFile baseDir = project.baseDir
		VirtualFile pomVirtualFile = baseDir.findChild(MavenProjectDeterminer.POM_FILE)
		String groupId = mavenPomReader.getGroupId(pomVirtualFile.inputStream)
		container.put(TARGET_CLASSES, groupId + ALL_CLASSES_SUFFIX)
	}

	private void addTargetClassesForNonMavenProject(PitCommandLineArgumentsContainer container) {
		VirtualFile[] sourceRoots = projectRootManager.contentSourceRoots
		if (hasAtLeastOneSourceRoot(sourceRoots)) {
			PsiDirectory directory = psiManager.findDirectory(sourceRoots[0])
			addTargetClassesIfDirectoryExists(container, directory)
		}
	}

	private static void addTargetClassesIfDirectoryExists(PitCommandLineArgumentsContainer container, PsiDirectory directory) {
		if (directory != null) {
			PsiDirectory[] subdirectories = directory.subdirectories
			if (hasAtLeastOneSubdirectory(subdirectories)) {
				container.put(TARGET_CLASSES, subdirectories[0].name + ALL_CLASSES_SUFFIX)
			}
		}
	}

	private static boolean hasAtLeastOneSourceRoot(VirtualFile[] sourceRoots) {
		!isEmpty(sourceRoots)
	}

	private static boolean hasAtLeastOneSubdirectory(PsiDirectory[] subdirectories) {
		!isEmpty(subdirectories)
	}
}
