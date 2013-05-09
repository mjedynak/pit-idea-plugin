package pl.mjedynak.idea.plugins.pit.console

import groovy.transform.CompileStatic

@CompileStatic
class DirectoryReader {

    File getLatestDirectoryFrom(File parentDir) {
        parentDir.listFiles().findAll { File f -> f.isDirectory() }.sort { File f -> f.lastModified() }[-1]
    }
}
