package pl.mjedynak.idea.plugins.pit.console

import java.io.File
import java.util.Optional

class DirectoryReader {
    fun getLatestDirectoryFrom(parentDir: File): Optional<File> {
        if (!parentDir.isDirectory) {
            return Optional.empty()
        }
        val files =
            parentDir
                .listFiles()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.lastModified() }
                .orEmpty()
        return if (files.isNotEmpty()) {
            Optional.of(files.last())
        } else {
            Optional.empty()
        }
    }
}
