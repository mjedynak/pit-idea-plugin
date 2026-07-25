package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.util.JavaParametersUtil
import com.intellij.openapi.module.ModuleManager
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulator

class JavaParametersCreator {
    private val programParametersListPopulator = ProgramParametersListPopulator()
    private val classPathPopulator = ClassPathPopulator()

    fun createJavaParameters(
        runConfigurationModule: RunConfigurationModule,
        pitConfigurationForm: PitConfigurationForm,
    ): JavaParameters {
        val javaParameters = JavaParameters()
        javaParameters.isUseClasspathJar = true
        val moduleManager = ModuleManager.getInstance(runConfigurationModule.project)
        configureModules(moduleManager, javaParameters)
        programParametersListPopulator.populateProgramParametersList(
            javaParameters.programParametersList,
            pitConfigurationForm,
        )
        javaParameters.workingDirectory = runConfigurationModule.project.basePath
        javaParameters.mainClass = PIT_MAIN_CLASS
        classPathPopulator.populateClassPathWithPitJar(javaParameters.classPath)
        return javaParameters
    }

    private fun configureModules(
        moduleManager: ModuleManager,
        javaParameters: JavaParameters,
    ) {
        for (module in moduleManager.modules) {
            JavaParametersUtil.configureModule(
                module,
                javaParameters,
                JavaParameters.JDK_AND_CLASSES_AND_TESTS,
                null,
            )
        }
    }

    companion object {
        private const val PIT_MAIN_CLASS =
            "org.pitest.mutationtest.commandline.MutationCoverageReport"
    }
}
