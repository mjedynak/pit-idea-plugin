package pl.mjedynak.idea.plugins.pit

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerFactory
import pl.mjedynak.idea.plugins.pit.cli.factory.DefaultArgumentsContainerPopulator
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument.REPORT_DIR
import java.io.File
import java.nio.file.Files

object PitOutputReader {
    @JvmStatic
    fun getLastProcessInfo(project: Project): String {
        val runContentManager = RunContentManager.getInstance(project)
        val descriptors = runContentManager.allDescriptors
        if (descriptors.isEmpty()) {
            return "No run content descriptors found"
        }

        val reportDir = resolveReportDir(project)
        val sb = StringBuilder()
        for ((i, desc) in descriptors.withIndex()) {
            sb
                .append("Descriptor #")
                .append(i)
                .append(": ")
                .append(desc.displayName)
                .append("\n")

            val processHandler: ProcessHandler? = desc.processHandler
            if (processHandler == null) {
                sb.append("  ProcessHandler: null\n")
                continue
            }

            sb
                .append("  Terminated: ")
                .append(processHandler.isProcessTerminated)
                .append("\n")
            if (processHandler.isProcessTerminated) {
                sb.append("  Exit code: ").append(processHandler.exitCode).append("\n")
            }

            if (processHandler is OSProcessHandler) {
                readCommandLine(processHandler, sb)
                readProcessOutput(processHandler, sb)
                readOutputLog(reportDir, sb)
            }

            if (processHandler.isProcessTerminated) {
                readReportDir(reportDir, sb)
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun getDefaultReportDir(project: Project): String = resolveReportDir(project).path

    private fun resolveReportDir(project: Project): File {
        var configuredReportDir: String? = null
        ApplicationManager.getApplication().runReadAction {
            val populator =
                DefaultArgumentsContainerPopulator(
                    ProjectRootManager.getInstance(project),
                    PsiManager.getInstance(project),
                )
            val container =
                DefaultArgumentsContainerFactory(populator).createDefaultPitCommandLineArgumentsContainer(project)
            configuredReportDir = container.get(REPORT_DIR)
        }
        if (!configuredReportDir.isNullOrBlank()) {
            return File(configuredReportDir)
        }
        val basePath = project.basePath
        return File(basePath ?: ".", DefaultArgumentsContainerPopulator.DEFAULT_REPORT_DIR)
    }

    private fun readCommandLine(
        handler: OSProcessHandler,
        sb: StringBuilder,
    ) {
        try {
            val getCommandLine = OSProcessHandler::class.java.getMethod("getCommandLine")
            val commandLine = getCommandLine.invoke(handler) as? String
            sb.append("  Command line:\n").append(commandLine).append("\n")
        } catch (_: NoSuchMethodException) {
            sb.append("  getCommandLine() not available\n")
        } catch (e: Exception) {
            sb.append("  Error getting command line: ").append(e.message).append("\n")
        }
    }

    private fun readProcessOutput(
        handler: OSProcessHandler,
        sb: StringBuilder,
    ) {
        try {
            val getProcess = OSProcessHandler::class.java.getMethod("getProcess")
            val proc = getProcess.invoke(handler)
            if (proc is Process) {
                val stdoutBytes = proc.inputStream.readAllBytes()
                val stderrBytes = proc.errorStream.readAllBytes()
                if (stdoutBytes.isNotEmpty()) {
                    sb
                        .append("  STDOUT:\n  ")
                        .append(String(stdoutBytes).replace("\n", "\n  "))
                        .append("\n")
                }
                if (stderrBytes.isNotEmpty()) {
                    sb
                        .append("  STDERR:\n  ")
                        .append(String(stderrBytes).replace("\n", "\n  "))
                        .append("\n")
                }
            }
        } catch (_: NoSuchMethodException) {
            sb.append("  getProcess() not available\n")
        } catch (e: Exception) {
            sb.append("  Error reading process: ").append(e.message).append("\n")
        }
    }

    private fun readOutputLog(
        reportDir: File,
        sb: StringBuilder,
    ) {
        val outputLog = File(reportDir, "pit-output.log")
        if (!outputLog.exists()) {
            sb.append("  pit-output.log not found in ").append(reportDir.absolutePath).append("\n")
            return
        }
        try {
            val logContent = Files.readString(outputLog.toPath())
            sb
                .append("  pit-output.log (")
                .append(reportDir.absolutePath)
                .append("):\n  ")
                .append(logContent.replace("\n", "\n  "))
                .append("\n")
        } catch (e: Exception) {
            sb.append("  Error reading pit-output.log: ").append(e.message).append("\n")
        }
    }

    private fun readReportDir(
        reportDir: File,
        sb: StringBuilder,
    ) {
        if (!reportDir.exists()) {
            sb.append("  Report dir does not exist: ").append(reportDir.absolutePath).append("\n")
            return
        }
        sb.append("  Report dir contents (").append(reportDir.absolutePath).append("):\n")
        val files = reportDir.listFiles()
        if (files.isNullOrEmpty()) {
            sb.append("    (empty or no files)\n")
            return
        }
        for (f in files) {
            sb
                .append("    ")
                .append(f.name)
                .append(" (")
                .append(f.length())
                .append(" bytes)\n")
        }
    }
}
