package pl.mjedynak.idea.plugins.pit.cli.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;
import pl.mjedynak.idea.plugins.pit.maven.MavenPomReader;
import pl.mjedynak.idea.plugins.pit.maven.ProjectDeterminer;

import java.io.IOException;

import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.TARGET_CLASSES;

public class DefaultArgumentsContainerPopulator {

    static final String DEFAULT_REPORT_DIR = "report";
    static final String MAVEN_REPORT_DIR = "target/report";
    static final String ALL_CLASSES_SUFFIX = ".*";

    private ProjectRootManager projectRootManager;
    private PsiManager psiManager;
    private ProjectDeterminer projectDeterminer;
    private MavenPomReader mavenPomReader;

    public DefaultArgumentsContainerPopulator(ProjectRootManager projectRootManager, PsiManager psiManager, ProjectDeterminer projectDeterminer, MavenPomReader mavenPomReader) {
        this.projectRootManager = projectRootManager;
        this.psiManager = psiManager;
        this.projectDeterminer = projectDeterminer;
        this.mavenPomReader = mavenPomReader;
    }

    public void addReportDir(Project project, PitCommandLineArgumentsContainer container) {
        VirtualFile baseDir = project.getBaseDir();
        String reportDir;
        if (baseDir != null) {
            String suffix = DEFAULT_REPORT_DIR;
            if (projectDeterminer.isMavenProject(project)) {
                suffix = MAVEN_REPORT_DIR;
            }
            reportDir = baseDir.getPath() + "/" + suffix;
            container.put(PitCommandLineArgument.REPORT_DIR, reportDir);
        }
    }

    public void addSourceDir(PitCommandLineArgumentsContainer container) {
        VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();
        if (hasAtLeastOneSourceRoot(sourceRoots)) {
            String sourceRootPath = sourceRoots[0].getPath();
            container.put(PitCommandLineArgument.SOURCE_DIRS, sourceRootPath);
        }
    }

    public void addTargetClasses(Project project, PitCommandLineArgumentsContainer container) {
        if (projectDeterminer.isMavenProject(project)) {
            addTargetClassesForMavenProject(project, container);
        } else {
            addTargetClassesForNonMavenProject(container);
        }
    }

    private void addTargetClassesForMavenProject(Project project, PitCommandLineArgumentsContainer container) {
        VirtualFile baseDir = project.getBaseDir();
        VirtualFile pomVirtualFile = baseDir.findChild(ProjectDeterminer.POM_FILE);
        try {
            String groupId = mavenPomReader.getGroupId(pomVirtualFile.getInputStream());
            container.put(TARGET_CLASSES, groupId + ALL_CLASSES_SUFFIX);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addTargetClassesForNonMavenProject(PitCommandLineArgumentsContainer container) {
        VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();
        if (hasAtLeastOneSourceRoot(sourceRoots)) {
            PsiDirectory directory = psiManager.findDirectory(sourceRoots[0]);
            addTargetClassesIfDirectoryExists(container, directory);
        }
    }

    private void addTargetClassesIfDirectoryExists(PitCommandLineArgumentsContainer container, PsiDirectory directory) {
        if (directory != null) {
            PsiDirectory[] subdirectories = directory.getSubdirectories();
            if (hasAtLeastOneSubdirectory(subdirectories)) {
                container.put(TARGET_CLASSES, subdirectories[0].getName() + ALL_CLASSES_SUFFIX);
            }
        }
    }

    private boolean hasAtLeastOneSourceRoot(VirtualFile[] sourceRoots) {
        return !isEmpty(sourceRoots);
    }

    private boolean hasAtLeastOneSubdirectory(PsiDirectory[] subdirectories) {
        return !isEmpty(subdirectories);
    }
}