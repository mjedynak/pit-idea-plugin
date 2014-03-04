package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.util.JavaParametersUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
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
        ModuleManager moduleManager = ModuleManager.getInstance(runConfigurationModule.project);
        configureModules(moduleManager, javaParameters)
        javaParameters.setMainClass(PIT_MAIN_CLASS)
        programParametersListPopulator.populateProgramParametersList(javaParameters.programParametersList, pitConfigurationForm)
        classPathPopulator.populateClassPathWithPitJar(javaParameters.classPath)
        javaParameters
    }

    private static void configureModules(ModuleManager moduleManager, JavaParameters javaParameters) {
        Module[] modules = moduleManager.modules
        modules.each { Module module ->
            JavaParametersUtil.configureModule(module, javaParameters, JavaParameters.JDK_AND_CLASSES_AND_TESTS, null)
        }
    }
}
