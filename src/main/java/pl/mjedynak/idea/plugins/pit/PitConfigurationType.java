package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactoryImpl;
import pl.mjedynak.idea.plugins.pit.gui.PitConfigurationForm;
import pl.mjedynak.idea.plugins.pit.gui.populator.PitConfigurationFormPopulatorImpl;
import pl.mjedynak.idea.plugins.pit.gui.populator.ProgramParametersListPopulatorImpl;

import javax.swing.Icon;

public class PitConfigurationType implements ConfigurationType {

    public static final Icon ICON = IconLoader.getIcon("/runConfigurations/junit.png");
    private final ConfigurationFactory myFactory;

    /**
     * reflection
     */
    public PitConfigurationType() {
        myFactory = new ConfigurationFactoryEx(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                DefaultArgumentsContainerFactoryImpl defaultArgumentsContainerFactory
                        = new DefaultArgumentsContainerFactoryImpl(ProjectRootManager.getInstance(project), PsiManager.getInstance(project));
                return new PitRunConfiguration("Pit Run Configuration", project, PitConfigurationType.getInstance().getConfigurationFactories()[0],
                        new PitConfigurationForm(), defaultArgumentsContainerFactory, new PitConfigurationFormPopulatorImpl(), new ProgramParametersListPopulatorImpl());
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

    public String getDisplayName() {
        return "Pit Runner";
    }

    public String getConfigurationTypeDescription() {
        return "Pit Runner description";
    }

    public Icon getIcon() {
        return ICON;
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }

    @NotNull
    public String getId() {
        return "Pit Runner Id";
    }

    @Nullable
    public static PitConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), PitConfigurationType.class);
    }
}
