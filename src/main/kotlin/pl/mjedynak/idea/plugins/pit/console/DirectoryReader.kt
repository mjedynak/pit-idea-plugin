package pl.mjedynak.idea.plugins.pit.console

import com.google.common.base.Optional
import java.io.File

class DirectoryReader {
    fun getLatestDirectoryFrom(parentDir: File): Optional<File> {
        if (!parentDir.isDirectory) {
            return Optional.absent()
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
            Optional.absent()
        }
    }
}
