package pl.mjedynak.idea.plugins.pit.configuration;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class PitConfigurationType implements ConfigurationType {

    private static final Icon ICON = IconLoader.getIcon("/pit.gif");
    private static final String DISPLAY_NAME = "PIT Runner";
    private static final String ID = "PIT";
    private static final String CONFIGURATION_DESCRIPTION = "Executes PIT mutation testing";

    private final ConfigurationFactory myFactory;

    public PitConfigurationType() {
        myFactory = new ConfigurationFactoryEx(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                PitRunConfigurationFactory pitRunConfigurationFactory = new PitRunConfigurationFactory();
                return pitRunConfigurationFactory.createConfiguration(project);
            }

            @Override
            public Icon getIcon(@NotNull final RunConfiguration configuration) {
                return getIcon();
            }

            @Override
            public void onNewConfigurationCreated(@NotNull RunConfiguration configuration) {
                ((ModuleBasedConfiguration) configuration).onNewConfigurationCreated();
            }
        };
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getConfigurationTypeDescription() {
        return CONFIGURATION_DESCRIPTION;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    @Nullable
    public static PitConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), PitConfigurationType.class);
    }
}
