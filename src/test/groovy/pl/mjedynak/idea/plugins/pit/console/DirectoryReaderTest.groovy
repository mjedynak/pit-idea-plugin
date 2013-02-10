package pl.mjedynak.idea.plugins.pit.console

import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class DirectoryReaderTest extends Specification {

    DirectoryReader directoryReader = new DirectoryReader()

    def "should read latest directory from given parent directory"() {
        createTempDir().deleteOnExit()
        File newerDir = createTempDir()
        newerDir.deleteOnExit()
        when:
        File result = directoryReader.getLatestDirectoryFrom(new File(System.getProperty("java.io.tmpdir")))
        then:
        result == newerDir
    }
}
