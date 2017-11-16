package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.configurations.JavaParameters
import groovy.transform.CompileStatic

@CompileStatic
class JavaParametersToPitClasspathConverter {

    String convert(JavaParameters javaParameters) {
        List<String> pathsList = javaParameters.classPath.pathList
        pathsList.join(',')
    }
}
