package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfigurationFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object PitTestHelper {
    @JvmStatic
    fun executePitForTest(project: Project): String {
        val diagnostics = StringBuilder()
        try {
            diagnostics.append("Step 1: Creating PIT run configuration...\n")
            val factory = PitRunConfigurationFactory()
            val config: PitRunConfiguration = factory.createConfiguration(project)
            config.name = "PIT E2E Test"

            diagnostics.append("Step 2: Executing on EDT...\n")
            val error = AtomicReference<Exception>()
            val latch = CountDownLatch(1)
            ApplicationManager
                .getApplication()
                .invokeAndWait(
                    {
                        try {
                            val builder =
                                ExecutionEnvironmentBuilder.create(
                                    DefaultRunExecutor.getRunExecutorInstance(),
                                    config,
                                )
                            ProgramRunnerUtil.executeConfiguration(
                                builder
                                    .contentToReuse(null)
                                    .dataContext(null)
                                    .activeTarget()
                                    .build(),
                                true,
                                true,
                            )
                        } catch (e: Exception) {
                            error.set(e)
                        }
                        latch.countDown()
                    },
                    ModalityState.nonModal(),
                )

            latch.await(10, TimeUnit.SECONDS)

            error.get()?.let {
                diagnostics.append("  ERROR: ").append(it.message).append("\n")
                return "ERROR\n$diagnostics"
            }

            diagnostics.append("  Run configuration executed successfully\n")
            return "SUCCESS\n$diagnostics"
        } catch (e: Exception) {
            val cause = e.cause ?: e
            diagnostics
                .append("ERROR: ")
                .append(cause.javaClass.name)
                .append(": ")
                .append(cause.message)
                .append("\n")
            return "ERROR\n$diagnostics"
        }
    }
}
