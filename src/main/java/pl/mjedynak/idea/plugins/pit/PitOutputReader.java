package pl.mjedynak.idea.plugins.pit;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;

public class PitOutputReader {

    public static String getLastProcessInfo(Project project) {
        RunContentManager runContentManager = RunContentManager.getInstance(project);
        if (runContentManager == null) {
            return "RunContentManager not available";
        }

        var descriptors = runContentManager.getAllDescriptors();
        if (descriptors.isEmpty()) {
            return "No run content descriptors found";
        }

        String basePath = project.getBasePath();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptors.size(); i++) {
            RunContentDescriptor desc = descriptors.get(i);
            sb.append("Descriptor #")
                    .append(i)
                    .append(": ")
                    .append(desc.getDisplayName())
                    .append("\n");

            ProcessHandler processHandler = desc.getProcessHandler();
            if (processHandler == null) {
                sb.append("  ProcessHandler: null\n");
                continue;
            }

            sb.append("  Terminated: ")
                    .append(processHandler.isProcessTerminated())
                    .append("\n");
            if (processHandler.isProcessTerminated()) {
                sb.append("  Exit code: ").append(processHandler.getExitCode()).append("\n");
            }

            if (processHandler instanceof OSProcessHandler osHandler) {
                try {
                    Method getCommandLine = OSProcessHandler.class.getMethod("getCommandLine");
                    String commandLine = (String) getCommandLine.invoke(osHandler);
                    sb.append("  Command line:\n").append(commandLine).append("\n");
                } catch (NoSuchMethodException e) {
                    sb.append("  getCommandLine() not available\n");
                } catch (Exception e) {
                    sb.append("  Error getting command line: ")
                            .append(e.getMessage())
                            .append("\n");
                }

                try {
                    Method getProcess = OSProcessHandler.class.getMethod("getProcess");
                    Object proc = getProcess.invoke(osHandler);
                    if (proc instanceof Process p) {
                        try {
                            byte[] stdoutBytes = p.getInputStream().readAllBytes();
                            byte[] stderrBytes = p.getErrorStream().readAllBytes();
                            if (stdoutBytes.length > 0) {
                                sb.append("  STDOUT:\n  ")
                                        .append(new String(stdoutBytes).replace("\n", "\n  "))
                                        .append("\n");
                            }
                            if (stderrBytes.length > 0) {
                                sb.append("  STDERR:\n  ")
                                        .append(new String(stderrBytes).replace("\n", "\n  "))
                                        .append("\n");
                            }
                        } catch (Exception e) {
                            sb.append("  Error reading process output: ")
                                    .append(e.getMessage())
                                    .append("\n");
                        }
                    }
                } catch (NoSuchMethodException e) {
                    sb.append("  getProcess() not available\n");
                } catch (Exception e) {
                    sb.append("  Error reading process: ")
                            .append(e.getMessage())
                            .append("\n");
                }

                if (basePath != null) {
                    File outputLog = new File(basePath, "report/pit-output.log");
                    if (outputLog.exists()) {
                        try {
                            String logContent = Files.readString(outputLog.toPath());
                            sb.append("  pit-output.log:\n  ")
                                    .append(logContent.replace("\n", "\n  "))
                                    .append("\n");
                        } catch (Exception e) {
                            sb.append("  Error reading pit-output.log: ")
                                    .append(e.getMessage())
                                    .append("\n");
                        }
                    }
                }
            }

            if (processHandler.isProcessTerminated()) {
                if (basePath != null) {
                    File reportDir = new File(basePath, "report");
                    if (reportDir.exists()) {
                        sb.append("  Report dir contents:\n");
                        File[] files = reportDir.listFiles();
                        if (files != null && files.length > 0) {
                            for (File f : files) {
                                sb.append("    ")
                                        .append(f.getName())
                                        .append(" (")
                                        .append(f.length())
                                        .append(" bytes)\n");
                            }
                        } else {
                            sb.append("    (empty or no files)\n");
                        }
                    } else {
                        sb.append("  Report dir does not exist\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
