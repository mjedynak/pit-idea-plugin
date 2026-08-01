package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import pl.mjedynak.idea.plugins.pit.editor.CoverageLineMarkerRenderer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object PitCoverageTestHelper {
    private fun hasNonBlankGutterTooltip(highlighter: RangeHighlighter): Boolean {
        val gutterRenderer = highlighter.gutterIconRenderer ?: return false
        return !gutterRenderer.tooltipText.isNullOrBlank()
    }

    private const val CALCULATOR_SOURCE_PATH = "src/main/java/calculator/Calculator.java"

    private const val COVERAGE_TIMEOUT_MS = 30_000L

    @JvmStatic
    fun getCoveredLinesInEditor(project: Project): String {
        val diagnostics = StringBuilder()
        try {
            diagnostics.append("Step 1: Locating Calculator.java...\n")
            val virtualFile =
                LocalFileSystem.getInstance().findFileByPath("${project.basePath}/$CALCULATOR_SOURCE_PATH")
                    ?: return "ERROR\nCalculator.java not found under ${project.basePath}"

            diagnostics.append("Step 2: Opening Calculator.java in the editor...\n")
            val editorRef = AtomicReference<Editor?>()
            val openError = AtomicReference<Exception>()
            val openLatch = CountDownLatch(1)
            ApplicationManager
                .getApplication()
                .invokeAndWait(
                    {
                        try {
                            FileEditorManager.getInstance(project).openFile(virtualFile, true)
                            val textEditor =
                                FileEditorManager.getInstance(project).getEditors(virtualFile).firstOrNull() as? TextEditor
                            editorRef.set(textEditor?.editor)
                        } catch (e: Exception) {
                            openError.set(e)
                        } finally {
                            openLatch.countDown()
                        }
                    },
                    ModalityState.nonModal(),
                )
            openLatch.await(10, TimeUnit.SECONDS)

            val editor = editorRef.get()
            if (editor == null) {
                val cause = openError.get()?.let { ": ${it.message}" }.orEmpty()
                return "ERROR\nEditor for Calculator.java was not opened$cause"
            }

            diagnostics.append("Step 3: Polling for coverage line markers...\n")
            val deadline = System.currentTimeMillis() + COVERAGE_TIMEOUT_MS
            var lastSeenLines: List<String> = emptyList()
            var lastException: Exception? = null
            while (System.currentTimeMillis() < deadline) {
                val pollResult = AtomicReference<List<String>>(emptyList())
                val pollError = AtomicReference<Exception>()
                val pollLatch = CountDownLatch(1)
                ApplicationManager
                    .getApplication()
                    .invokeAndWait(
                        {
                            try {
                                val annotatedLines =
                                    editor.markupModel.allHighlighters
                                        .filter { it.lineMarkerRenderer is CoverageLineMarkerRenderer }
                                        .map { highlighter ->
                                            val lineNumber = editor.document.getLineNumber(highlighter.startOffset) + 1
                                            val status = (highlighter.lineMarkerRenderer as CoverageLineMarkerRenderer).status
                                            val hasTooltip = hasNonBlankGutterTooltip(highlighter)
                                            "$lineNumber:${status.name}:${if (hasTooltip) 1 else 0}"
                                        }.distinct()
                                        .sortedBy { it.substringBefore(":").toInt() }
                                lastSeenLines = annotatedLines
                                pollResult.set(annotatedLines)
                            } catch (e: Exception) {
                                lastException = e
                                pollError.set(e)
                            } finally {
                                pollLatch.countDown()
                            }
                        },
                        ModalityState.nonModal(),
                    )
                pollLatch.await(10, TimeUnit.SECONDS)

                val annotatedLines = pollResult.get()
                if (annotatedLines.isNotEmpty()) {
                    diagnostics.append("  Annotated lines: ").append(annotatedLines.joinToString(",")).append("\n")
                    return "SUCCESS\n" + annotatedLines.joinToString(",")
                }
                Thread.sleep(1000)
            }

            diagnostics.append("  Timed out waiting for coverage line markers\n")
            val lastSeenText = if (lastSeenLines.isEmpty()) "none" else lastSeenLines.joinToString(",")
            val exceptionText = lastException?.let { "\n  Last exception: ${it.javaClass.name}: ${it.message}" }.orEmpty()
            return "ERROR\nTimed out waiting for coverage markers. Last seen: $lastSeenText$exceptionText\n$diagnostics"
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
