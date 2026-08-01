package pl.mjedynak.idea.plugins.pit.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class PitConfigurationType : ConfigurationType {
    private val myFactory: ConfigurationFactory =
        object : ConfigurationFactory(this) {
            override fun createTemplateConfiguration(project: Project): RunConfiguration {
                val pitRunConfigurationFactory = PitRunConfigurationFactory()
                return pitRunConfigurationFactory.createConfiguration(project)
            }

            override fun getIcon(configuration: RunConfiguration): Icon = this@PitConfigurationType.icon

            override fun getId(): @NonNls String = name
        }

    override fun getDisplayName(): String = DISPLAY_NAME

    override fun getConfigurationTypeDescription(): String = CONFIGURATION_DESCRIPTION

    override fun getIcon(): Icon = ICON

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(myFactory)

    override fun getId(): @NonNls String = ID

    companion object {
        private val ICON: Icon = IconLoader.getIcon("/pit.svg", PitConfigurationType::class.java)
        private const val DISPLAY_NAME = "PIT Runner"
        private const val ID = "PIT"
        private const val CONFIGURATION_DESCRIPTION = "Executes PIT mutation testing"

        fun getInstance(): PitConfigurationType? =
            ContainerUtil.findInstance(
                ConfigurationType.CONFIGURATION_TYPE_EP.extensionList,
                PitConfigurationType::class.java,
            )
    }
}
