package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration;
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfigurationFactory;

public class PitTestHelper {

    public static String executePitForTest(Project project) {
        StringBuilder diagnostics = new StringBuilder();
        try {
            diagnostics.append("Step 1: Creating PIT run configuration...\n");
            PitRunConfigurationFactory factory = new PitRunConfigurationFactory();
            PitRunConfiguration config = factory.createConfiguration(project);
            config.setName("PIT E2E Test");

            diagnostics.append("Step 2: Executing on EDT...\n");
            AtomicReference<Exception> error = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            ApplicationManager.getApplication()
                    .invokeAndWait(
                            () -> {
                                try {
                                    ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder.create(
                                            DefaultRunExecutor.getRunExecutorInstance(), config);
                                    ProgramRunnerUtil.executeConfiguration(
                                            builder.contentToReuse(null)
                                                    .dataContext(null)
                                                    .activeTarget()
                                                    .build(),
                                            true,
                                            true);
                                } catch (Exception e) {
                                    error.set(e);
                                }
                                latch.countDown();
                            },
                            ModalityState.nonModal());

            latch.await(10, TimeUnit.SECONDS);

            if (error.get() != null) {
                diagnostics.append("  ERROR: ").append(error.get().getMessage()).append("\n");
                return "ERROR\n" + diagnostics;
            }

            diagnostics.append("  Run configuration executed successfully\n");
            return "SUCCESS\n" + diagnostics;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            diagnostics
                    .append("ERROR: ")
                    .append(cause.getClass().getName())
                    .append(": ")
                    .append(cause.getMessage())
                    .append("\n");
            return "ERROR\n" + diagnostics;
        }
    }
}
