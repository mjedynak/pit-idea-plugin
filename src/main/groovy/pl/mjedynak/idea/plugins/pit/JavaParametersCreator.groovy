package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.util.JavaParametersUtil
import groovy.transform.CompileStatic
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator

@CompileStatic
class JavaParametersCreator {

    private static final String PIT_MAIN_CLASS = "org.pitest.mutationtest.commandline.MutationCoverageReport"

    ProgramParametersListPopulator programParametersListPopulator = new ProgramParametersListPopulator()
    ClassPathPopulator classPathPopulator = new ClassPathPopulator()

    JavaParameters createJavaParameters(RunConfigurationModule runConfigurationModule, PitConfigurationForm pitConfigurationForm) {
        JavaParameters javaParameters = new JavaParameters()
        int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS
        JavaParametersUtil.configureModule(runConfigurationModule, javaParameters, classPathType, null)
        javaParameters.setMainClass(PIT_MAIN_CLASS)
        programParametersListPopulator.populateProgramParametersList(javaParameters.getProgramParametersList(), pitConfigurationForm)
        classPathPopulator.populateClassPathWithPitJar(javaParameters.getClassPath())
        javaParameters
    }
}
