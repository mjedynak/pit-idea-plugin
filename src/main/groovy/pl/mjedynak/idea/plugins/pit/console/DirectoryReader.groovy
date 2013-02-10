package pl.mjedynak.idea.plugins.pit.console

class DirectoryReader {

    File getLatestDirectoryFrom(File parentDir) {
        parentDir.listFiles().findAll { File f -> f.isDirectory() }.sort { File f -> f.lastModified() }[-1]
    }
}
