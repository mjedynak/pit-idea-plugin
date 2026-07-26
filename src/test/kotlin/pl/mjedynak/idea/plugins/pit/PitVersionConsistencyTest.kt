package pl.mjedynak.idea.plugins.pit

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PitVersionConsistencyTest {
    private val gradleLines = File("build.gradle.kts").readLines()
    private val populatorSource = File("src/main/kotlin/pl/mjedynak/idea/plugins/pit/ClassPathPopulator.kt").readText()
    private val pluginXml = File("META-INF/plugin.xml").readText()

    @Test
    fun `should have all pitest dependencies from build gradle in classpath populator`() {
        val dependencies = extractPitestDependencies()

        dependencies.forEach { (artifactId, version) ->
            assertArtifactIsReferenced(artifactId)
            assertVersionIsPresent(artifactId, version)
        }
    }

    @Test
    fun `should have pit versions in plugin xml matching build gradle`() {
        val markers = findPitestDependencyMarkers()
        val variables = parseVariablesBefore(markers.first())
        val pitVersion = variables["pitVersion"] ?: error("pitVersion not found in build.gradle.kts")
        val pitJunit5PluginVersion = variables["pitJunit5PluginVersion"] ?: error("pitJunit5PluginVersion not found in build.gradle.kts")

        assertTrue(
            pluginXml.contains("PIT $pitVersion"),
            "plugin.xml should contain PIT version '$pitVersion' but doesn't. Description: ${pluginXml.substringAfter(
                "<description>",
                "",
            ).substringBefore("</description>")}",
        )
        assertTrue(
            pluginXml.contains("JUnit5 plugin $pitJunit5PluginVersion"),
            "plugin.xml should contain JUnit5 plugin version '$pitJunit5PluginVersion' but doesn't. Description: ${pluginXml.substringAfter(
                "<description>",
                "",
            ).substringBefore("</description>")}",
        )
    }

    private fun extractPitestDependencies(): List<Pair<String, String>> {
        val markers = findPitestDependencyMarkers()
        val variables = parseVariablesBefore(markers.first())

        return gradleLines
            .subList(markers.first() + 1, markers.last())
            .mapNotNull { Regex("""implementation\("([^"]+)"\)""").find(it) }
            .map { it.groupValues[1] }
            .map { coord -> resolveArtifactAndVersion(coord, variables) }
    }

    private fun findPitestDependencyMarkers(): List<Int> {
        val markers = gradleLines.indices.filter { gradleLines[it].trim() == "// -- pitest dependencies marker" }
        require(markers.size == 2) { "Expected exactly two pitest dependency markers in build.gradle.kts" }
        return markers
    }

    private fun parseVariablesBefore(lineIndex: Int): Map<String, String> =
        gradleLines
            .take(lineIndex)
            .mapNotNull { Regex("""val\s+(\w+)\s*=\s*"(.+?)"""").find(it) }
            .associate { it.groupValues[1] to it.groupValues[2] }

    private fun resolveArtifactAndVersion(
        coordinate: String,
        variables: Map<String, String>,
    ): Pair<String, String> {
        val match = Regex("""([^:]+):([^:]+):(.+)""").find(coordinate) ?: error("Invalid coordinate: $coordinate")
        val artifactId = match.groupValues[2]
        val rawVersion = match.groupValues[3]
        val version =
            if (rawVersion.startsWith(
                    "\$",
                )
            ) {
                variables[rawVersion.removePrefix("$")] ?: error("Unknown variable: $rawVersion")
            } else {
                rawVersion
            }
        return artifactId to version
    }

    private fun assertArtifactIsReferenced(artifactId: String) {
        assertTrue(
            populatorSource.contains("$artifactId-"),
            "ClassPathPopulator should reference artifact '$artifactId' but it doesn't",
        )
    }

    private fun assertVersionIsPresent(
        artifactId: String,
        version: String,
    ) {
        val literalJar = "$artifactId-$version.jar"
        if (!populatorSource.contains(literalJar)) {
            assertTrue(
                populatorSource.contains("\"$version\""),
                "ClassPathPopulator should contain version '$version' for artifact '$artifactId' but it doesn't",
            )
        }
    }
}
