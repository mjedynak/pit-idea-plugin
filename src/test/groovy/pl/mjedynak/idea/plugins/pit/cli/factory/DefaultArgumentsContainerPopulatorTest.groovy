package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader
import spock.lang.Specification

import pl.mjedynak.idea.plugins.pit.maven.ProjectDeterminer

import static ProjectDeterminer.POM_FILE
import static DefaultArgumentsContainerPopulator.ALL_CLASSES_SUFFIX
import static DefaultArgumentsContainerPopulator.DEFAULT_REPORT_DIR
import static DefaultArgumentsContainerPopulator.MAVEN_REPORT_DIR
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.SOURCE_DIRS
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES

class DefaultArgumentsContainerPopulatorTest extends Specification {

    Project project = Mock()
    ProjectRootManager projectRootManager = Mock()
    PsiManager psiManager = Mock()
    ProjectDeterminer projectDeterminer = Mock()
    MavenPomReader mavenPomReader = Mock()
    DefaultArgumentsContainerPopulator defaultArgumentsContainerPopulator = new DefaultArgumentsContainerPopulator(projectRootManager, psiManager, projectDeterminer, mavenPomReader)
    PitCommandLineArgumentsContainer container = new PitCommandLineArgumentsContainerImpl()

    def "should create container with default report dir"() {
        String baseDirPath = "app"
        VirtualFile baseDir = Mock()
        project.getBaseDir() >> baseDir
        baseDir.getPath() >> baseDirPath

        when:
        defaultArgumentsContainerPopulator.addReportDir(project, container)

        then:
        container.get(REPORT_DIR) == baseDirPath + '/' + DEFAULT_REPORT_DIR
    }

    def "should create container with maven default report dir for maven project"() {
        String baseDirPath = "app"
        VirtualFile baseDir = Mock()
        project.getBaseDir() >> baseDir
        baseDir.getPath() >> baseDirPath
        projectDeterminer.isMavenProject(project) >> true

        when:
        defaultArgumentsContainerPopulator.addReportDir(project, container)

        then:
        container.get(REPORT_DIR) == baseDirPath + '/' + MAVEN_REPORT_DIR
    }

    def "should create container with default source dir"() {
        VirtualFile sourceRoot = Mock()
        VirtualFile[] sourceRoots = [sourceRoot]
        projectRootManager.getContentSourceRoots() >> sourceRoots
        String path = "src/main/java"
        sourceRoot.getPath() >> path

        when:
        defaultArgumentsContainerPopulator.addSourceDir(container)

        then:
        container.get(SOURCE_DIRS) == path
    }

    def "should create container with default target classes"() {
        VirtualFile sourceRoot = Mock()
        PsiDirectory directory = Mock()
        PsiDirectory subdirectory = Mock()
        String packageName = "com"
        VirtualFile[] sourceRoots = [sourceRoot]
        PsiDirectory[] subdirectories = [subdirectory]
        projectRootManager.getContentSourceRoots() >> sourceRoots
        sourceRoot.getPath() >> "anyPath"
        psiManager.findDirectory(sourceRoot) >> directory
        directory.getSubdirectories() >> subdirectories
        subdirectory.getName() >> packageName

        when:
        defaultArgumentsContainerPopulator.addTargetClasses(project, container)

        then:
        container.get(TARGET_CLASSES) == packageName + ALL_CLASSES_SUFFIX
    }

    def "should create container with target classes from group id for maven project"() {
        VirtualFile baseDir = Mock()
        VirtualFile pomVirtualFile = Mock()
        InputStream pomFile = Mock()
        String groupId = "pl.mjedynak"

        project.baseDir >> baseDir
        baseDir.findChild(POM_FILE) >> pomVirtualFile
        pomVirtualFile.inputStream >> pomFile
        projectDeterminer.isMavenProject(project) >> true
        mavenPomReader.getGroupId(pomFile) >> groupId

        when:
        defaultArgumentsContainerPopulator.addTargetClasses(project, container)

        then:
        container.get(TARGET_CLASSES) == groupId + ALL_CLASSES_SUFFIX

    }

}
