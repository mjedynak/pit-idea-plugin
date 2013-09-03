package pl.mjedynak.idea.plugins.pit.console

import com.google.common.base.Optional
import groovy.transform.CompileStatic

@CompileStatic
class DirectoryReader {

    Optional<File> getLatestDirectoryFrom(File parentDir) {
        Optional<File> result = Optional.absent()
        if (parentDir.isDirectory()) {
            List<File> files = parentDir.listFiles().findAll { File f -> f.isDirectory() }.sort { File f -> f.lastModified() }
            if (!files.empty) {
                result = Optional.of(files[-1])
            }
        }
        result
    }
}
