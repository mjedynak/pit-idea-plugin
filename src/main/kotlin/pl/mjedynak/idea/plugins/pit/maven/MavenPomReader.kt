package pl.mjedynak.idea.plugins.pit.maven

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class MavenPomReader {
    fun getGroupId(pomFile: InputStream): String {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(pomFile)
        val groupIdElements = document.getElementsByTagName("groupId")
        return if (groupIdElements.length > 0) {
            groupIdElements.item(0).textContent
        } else {
            ""
        }
    }
}
