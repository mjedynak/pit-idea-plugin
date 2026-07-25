package pl.mjedynak.idea.plugins.pit.maven

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MavenPomReaderTest {
    private val mavenPomReader = MavenPomReader()

    @Test
    fun `should read group id from maven pom file`() {
        val groupId = "group"
        val pomFileText = """<project>
                                <groupId>$groupId</groupId>
                                <artifactId>artifact</artifactId>
                                <version>1.0.0</version>
                            </project>"""
        val pomFile = pomFileText.byteInputStream()

        val result = mavenPomReader.getGroupId(pomFile)

        assertEquals(groupId, result)
    }
}
