package pl.mjedynak.idea.plugins.pit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.mjedynak.idea.plugins.pit.ClassPathPopulator.Companion.PITEST_JUNIT5_PLUGIN_VERSION
import pl.mjedynak.idea.plugins.pit.ClassPathPopulator.Companion.PITEST_VERSION
import java.io.File

class ClassPathPopulatorTest {
    @Test
    fun `should have the same PIT version as specified in build gradle`() {
        val gradleBuildFile = File("build.gradle.kts")
        val lineWithVersion = gradleBuildFile.readLines().first { it.startsWith("val pitVersion") }
        val version = lineWithVersion.substringAfter("\"").substringBeforeLast("\"")

        assertEquals(PITEST_VERSION, version)
    }

    @Test
    fun `should have the same PIT Junit5 Plugin version as specified in build gradle`() {
        val gradleBuildFile = File("build.gradle.kts")
        val lineWithVersion =
            gradleBuildFile.readLines().first { it.startsWith("val pitJunit5PluginVersion") }
        val version = lineWithVersion.substringAfter("\"").substringBeforeLast("\"")

        assertEquals(PITEST_JUNIT5_PLUGIN_VERSION, version)
    }
}
