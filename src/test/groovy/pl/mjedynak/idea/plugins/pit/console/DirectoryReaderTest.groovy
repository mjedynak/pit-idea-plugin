package pl.mjedynak.idea.plugins.pit.console

import com.google.common.base.Optional
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static java.util.concurrent.TimeUnit.MILLISECONDS

class DirectoryReaderTest extends Specification {

    DirectoryReader directoryReader = new DirectoryReader()

    def "should read latest directory from given parent directory"() {
        File tempDir = createTempDir()
        tempDir.deleteOnExit()
        File olderDir = new File(tempDir, "older")
        olderDir.mkdir()
        olderDir.deleteOnExit()
        MILLISECONDS.sleep(1)
        File newerDir = new File(tempDir, "newer")
        newerDir.mkdir()
        newerDir.deleteOnExit()

        when:
        Optional<File> result = directoryReader.getLatestDirectoryFrom(tempDir)

        then:
        result.get() == newerDir
    }

    def "should return empty option when no directories in parent directory"() {
        File tempDir = createTempDir()
        tempDir.deleteOnExit()

        when:
        Optional<File> result = directoryReader.getLatestDirectoryFrom(tempDir)

        then:
        result.isPresent() == false
    }

    def "should return empty option when given incorrect argument"() {
        when:
        Optional<File> result = directoryReader.getLatestDirectoryFrom(new File('notExistingDirectory'))

        then:
        result.isPresent() == false
    }
}
