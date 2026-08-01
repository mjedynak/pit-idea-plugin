package pl.mjedynak.idea.plugins.pit.editor

import com.intellij.openapi.diagnostic.Logger
import org.w3c.dom.Element
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

data class MutationRecord(
    val sourceFile: String,
    val mutatedClass: String,
    val lineNumber: Int,
    val status: MutationStatus = MutationStatus.UNKNOWN,
    val detected: Boolean = false,
    val mutatedMethod: String = "",
    val description: String = "",
)

class MutationReportParser {
    private val logger = Logger.getInstance(MutationReportParser::class.java)

    fun parse(xmlFile: File): List<MutationRecord> =
        try {
            xmlFile.inputStream().use { inputStream ->
                parse(inputStream)
            }
        } catch (_: Exception) {
            logger.warn("Failed to parse PIT mutation report from file: ${xmlFile.absolutePath}")
            emptyList()
        }

    fun parse(inputStream: InputStream): List<MutationRecord> =
        try {
            val factory = DocumentBuilderFactory.newInstance()
            disableExternalEntities(factory)
            val document = factory.newDocumentBuilder().parse(inputStream)
            val mutationElements = document.getElementsByTagName("mutation")
            val mutations =
                (0 until mutationElements.length)
                    .mapNotNull { index ->
                        val mutationElement = mutationElements.item(index) as? Element
                        if (mutationElement == null) {
                            return@mapNotNull null
                        }
                        val lineNumber = childElementText(mutationElement, "lineNumber")?.toIntOrNull()
                        if (lineNumber == null) {
                            return@mapNotNull null
                        }
                        MutationRecord(
                            sourceFile = childElementText(mutationElement, "sourceFile").orEmpty(),
                            mutatedClass = childElementText(mutationElement, "mutatedClass").orEmpty(),
                            lineNumber = lineNumber,
                            status = MutationStatus.fromXml(mutationElement.getAttribute("status")),
                            detected = mutationElement.getAttribute("detected").equals("true", ignoreCase = true),
                            mutatedMethod = childElementText(mutationElement, "mutatedMethod").orEmpty(),
                            description = childElementText(mutationElement, "description").orEmpty(),
                        )
                    }
            if (mutations.isEmpty()) {
                logger.warn("PIT mutation report contains no mutations")
            }
            mutations
        } catch (_: Exception) {
            logger.warn("Failed to parse PIT mutation report")
            emptyList()
        }

    private fun childElementText(
        parent: Element,
        tagName: String,
    ): String? {
        val childNodes = parent.childNodes
        for (index in 0 until childNodes.length) {
            val child = childNodes.item(index)
            if (child is Element && child.tagName == tagName) {
                return child.textContent
            }
        }
        return null
    }

    private fun disableExternalEntities(factory: DocumentBuilderFactory) {
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        } catch (_: Exception) {
        }
    }
}
