package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl
import pl.mjedynak.idea.plugins.pit.gradle.GradleProjectDeterminer
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader
import pl.mjedynak.idea.plugins.pit.maven.MavenProjectDeterminer
import spock.lang.Ignore
import spock.lang.Specification

import static DefaultArgumentsContainerPopulator.ALL_CLASSES_SUFFIX
import static DefaultArgumentsContainerPopulator.DEFAULT_REPORT_DIR
import static DefaultArgumentsContainerPopulator.MAVEN_REPORT_DIR
import static MavenProjectDeterminer.POM_FILE
import static pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator.GRADLE_REPORT_DIR
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES

class DefaultArgumentsContainerPopulatorTest extends Specification {

	Project project = Mock()
	ProjectRootManager projectRootManager = Mock()
	PsiManager psiManager = Mock()
	MavenProjectDeterminer mavenProjectDeterminer = Mock()
	GradleProjectDeterminer gradleProjectDeterminer = Mock()
	MavenPomReader mavenPomReader = Mock()
	DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator = new DefaultArgumentsContainerPopulator(projectRootManager, psiManager)
	PitCommandLineArgumentsContainer container = new PitCommandLineArgumentsContainerImpl()

	def setup() {
		defaultArgumentsContainerPopulator.mavenProjectDeterminer = mavenProjectDeterminer
		defaultArgumentsContainerPopulator.mavenPomReader = mavenPomReader
		defaultArgumentsContainerPopulator.gradleProjectDeterminer = gradleProjectDeterminer
	}

	def "should create container with default report dir"() {
		String baseDirPath = 'app'
		VirtualFile baseDir = Mock()
		project.baseDir >> baseDir
		baseDir.path >> baseDirPath

		when:
		defaultArgumentsContainerPopulator.addReportDir(project, container)

		then:
		container.get(REPORT_DIR) == baseDirPath + '/' + DEFAULT_REPORT_DIR
	}

	def "should create container with maven default report dir for maven project"() {
		String baseDirPath = 'app'
		VirtualFile baseDir = Mock()
		project.baseDir >> baseDir
		baseDir.path >> baseDirPath
		mavenProjectDeterminer.isMavenProject(project) >> true

		when:
		defaultArgumentsContainerPopulator.addReportDir(project, container)

		then:
		container.get(REPORT_DIR) == baseDirPath + '/' + MAVEN_REPORT_DIR
	}

	def "should create container with gradle default report dir for gradle project"() {
		String baseDirPath = 'app'
		VirtualFile baseDir = Mock()
		project.baseDir >> baseDir
		baseDir.path >> baseDirPath
		gradleProjectDeterminer.isGradleProject(project) >> true

		when:
		defaultArgumentsContainerPopulator.addReportDir(project, container)

		then:
		container.get(REPORT_DIR) == baseDirPath + '/' + GRADLE_REPORT_DIR
	}

	def "should create container with default source dir"() {
		VirtualFile sourceRoot = Mock()
		VirtualFile[] sourceRoots = [sourceRoot]
		projectRootManager.contentSourceRoots >> sourceRoots
		String path = 'somePath'
		sourceRoot.path >> path

		when:
		defaultArgumentsContainerPopulator.addSourceDir(container)

		then:
		container.get(SOURCE_DIRS) == path
	}

	def "should prefer java as source dir"() {
		VirtualFile firstSourceRoot = Mock()
		VirtualFile secondSourceRoot = Mock()
		VirtualFile[] sourceRoots = [
			firstSourceRoot,
			secondSourceRoot
		]
		projectRootManager.contentSourceRoots >> sourceRoots
		String firstPath = 'src/main/resources'
		String secondPath = 'src/main/java'
		firstSourceRoot.path >> firstPath
		secondSourceRoot.path >> secondPath

		when:
		defaultArgumentsContainerPopulator.addSourceDir(container)

		then:
		container.get(SOURCE_DIRS) == secondPath
	}

	@Ignore
	def "should create container with default target classes"() {
		VirtualFile sourceRoot = Mock()
		PsiDirectory directory = Mock()
		PsiDirectory subdirectory = Mock()
		String packageName = 'com'
		VirtualFile[] sourceRoots = [sourceRoot]
		PsiDirectory[] subdirectories = [subdirectory]
		projectRootManager.contentSourceRoots >> sourceRoots
		sourceRoot.path >> 'anyPath'
		psiManager.findDirectory(sourceRoot) >> directory
		directory.subdirectories >> subdirectories
		subdirectory.name >> packageName

		when:
		defaultArgumentsContainerPopulator.addTargetClasses(project, container)

		then:
		container.get(TARGET_CLASSES) == packageName + ALL_CLASSES_SUFFIX
	}

	def "should create container with target classes from group id for maven project"() {
		VirtualFile baseDir = Mock()
		VirtualFile pomVirtualFile = Mock()
		InputStream pomFile = Mock()
		String groupId = 'pl.mjedynak'

		project.baseDir >> baseDir
		baseDir.findChild(POM_FILE) >> pomVirtualFile
		pomVirtualFile.inputStream >> pomFile
		mavenProjectDeterminer.isMavenProject(project) >> true
		mavenPomReader.getGroupId(pomFile) >> groupId

		when:
		defaultArgumentsContainerPopulator.addTargetClasses(project, container)

		then:
		container.get(TARGET_CLASSES) == groupId + ALL_CLASSES_SUFFIX
	}
}
