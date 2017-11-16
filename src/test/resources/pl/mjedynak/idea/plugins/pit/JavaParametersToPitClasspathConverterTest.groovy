package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.configurations.JavaParameters
import com.intellij.util.PathsList
import spock.lang.Specification

class JavaParametersToPitClasspathConverterTest extends Specification {

    private JavaParametersToPitClasspathConverter converter = new JavaParametersToPitClasspathConverter()

    def "converts java parameters to pit classpath represented as string"() {
        JavaParameters javaParameters = new JavaParameters()
        PathsList pathsList = javaParameters.getClassPath()
        pathsList.add("C:\\libs\\lib1.jar")
        pathsList.add("C:\\libs\\lib2.jar")
        pathsList.add("C:\\myClasses")

        when:
        String result = converter.convert(javaParameters)

        then:
        result == "C:\\libs\\lib1.jar,C:\\libs\\lib2.jar,C:\\myClasses"
    }
}
