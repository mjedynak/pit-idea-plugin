package pl.mjedynak.idea.plugins.pit.maven

class MavenPomReader {

    String getGroupId(InputStream pomFile) {
        Node project = new XmlParser().parse(pomFile)
        project.groupId.text()
    }
}
