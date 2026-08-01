package pl.mjedynak.idea.plugins.pit.editor

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.JBColor
import pl.mjedynak.idea.plugins.pit.console.DirectoryReader
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.io.File
import javax.swing.Icon

class PitCoverageAnnotator(
    private val project: Project,
) {
    private val logger = Logger.getInstance(PitCoverageAnnotator::class.java)
    private var mutationsByClassAndLine: Map<String, Map<Int, List<MutationRecord>>> = emptyMap()
    private var sourceFilesByClass: Map<String, String> = emptyMap()
    private val directoryReader = DirectoryReader()

    init {
        val listener =
            object : EditorFactoryListener {
                override fun editorCreated(event: EditorFactoryEvent) {
                    annotateEditor(event.editor)
                }

                override fun editorReleased(event: EditorFactoryEvent) {
                    removeAnnotation(event.editor)
                }
            }
        EditorFactory.getInstance().addEditorFactoryListener(listener, project)
    }

    fun updateFromReport(reportDir: File): String {
        val mutationsFile =
            resolveMutationsFile(reportDir)
                ?: return "PIT coverage: no mutations.xml found under ${reportDir.absolutePath}"
        val records =
            try {
                MutationReportParser().parse(mutationsFile)
            } catch (e: Exception) {
                logger.warn("PIT coverage: failed to parse ${mutationsFile.absolutePath}", e)
                return "PIT coverage: failed to parse ${mutationsFile.absolutePath}"
            }
        if (records.isEmpty()) {
            logger.warn("PIT coverage: no mutations in ${mutationsFile.absolutePath}")
            return "PIT coverage: no mutations in ${mutationsFile.absolutePath}"
        }
        mutationsByClassAndLine =
            records.groupBy { it.mutatedClass }.mapValues { (_, classRecords) -> classRecords.groupBy { it.lineNumber } }
        sourceFilesByClass =
            records.groupBy { it.mutatedClass }.mapValues { entry -> entry.value.first().sourceFile }
        val openEditors =
            EditorFactory
                .getInstance()
                .allEditors
                .filter { it.project == this.project }
        var coveredLines = 0
        var uncoveredLines = 0
        var markedEditors = 0
        openEditors.forEach { editor ->
            removeAnnotation(editor)
            val (editorCovered, editorUncovered) = annotateEditor(editor)
            if (editorCovered > 0 || editorUncovered > 0) {
                coveredLines += editorCovered
                uncoveredLines += editorUncovered
                markedEditors++
            }
        }
        val resolvedFiles = mutationsByClassAndLine.keys.associateWith { resolveFileForClass(it)?.path ?: "NOT FOUND" }
        val resolution = resolvedFiles.entries.joinToString(", ") { "${it.key} -> ${it.value}" }
        val unresolvedClasses = resolvedFiles.filterValues { it == "NOT FOUND" }.keys
        if (unresolvedClasses.isNotEmpty()) {
            logger.warn("PIT coverage: could not resolve editor file for classes: ${unresolvedClasses.joinToString()}")
        }
        logger.info("PIT coverage: $resolution")
        return "PIT coverage: ${records.size} mutations, ${mutationsByClassAndLine.size} classes, " +
            "$coveredLines covered + $uncoveredLines uncovered line(s) marked in $markedEditors open editor(s). " +
            "Classes: $resolution"
    }

    /**
     * Removes the coverage markings (status bands + gutter icons) from all open editors of this
     * project and forgets the previously parsed report. Called before each PIT run starts so the
     * editor is clean while the new run executes; must be dispatched to the EDT. Forgetting the
     * cached maps also prevents a file opened *during* the run from being re-marked with stale
     * data by the [EditorFactoryListener.editorCreated] hook.
     */
    fun clearAnnotations() {
        if (project.isDisposed) {
            return
        }
        mutationsByClassAndLine = emptyMap()
        sourceFilesByClass = emptyMap()
        EditorFactory
            .getInstance()
            .allEditors
            .filter { it.project == project }
            .forEach { removeAnnotation(it) }
    }

    private fun resolveMutationsFile(reportDir: File): File? {
        val reportRoot =
            if (File(reportDir, "mutations.xml").exists()) {
                reportDir
            } else {
                val latestDirectory = directoryReader.getLatestDirectoryFrom(reportDir)
                if (latestDirectory.isPresent && File(latestDirectory.get(), "mutations.xml").exists()) {
                    latestDirectory.get()
                } else {
                    return null
                }
            }
        return File(reportRoot, "mutations.xml")
    }

    private fun annotateEditor(editor: Editor): Pair<Int, Int> {
        if (editor.isDisposed || project.isDisposed || mutationsByClassAndLine.isEmpty()) {
            return 0 to 0
        }
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return 0 to 0
        var covered = 0
        var uncovered = 0
        mutationsByClassAndLine.forEach { (mutatedClass, linesByLineNumber) ->
            val target = resolveFileForClass(mutatedClass) ?: return@forEach
            if (target != file) {
                return@forEach
            }
            linesByLineNumber.forEach { (line, mutations) ->
                if (line - 1 < editor.document.lineCount) {
                    val status = lineStatus(mutations)
                    val tooltip = buildTooltip(mutations)
                    val startOffset = editor.document.getLineStartOffset(line - 1)
                    val endOffset = editor.document.getLineEndOffset(line - 1)
                    val highlighter =
                        editor.markupModel.addRangeHighlighter(
                            startOffset,
                            endOffset,
                            HighlighterLayer.SYNTAX,
                            null,
                            HighlighterTargetArea.LINES_IN_RANGE,
                        )
                    highlighter.lineMarkerRenderer = CoverageLineMarkerRenderer(status)
                    highlighter.setErrorStripeMarkColor(errorStripeColor(status))
                    highlighter.errorStripeTooltip = tooltip
                    if (tooltip.isNotBlank()) {
                        highlighter.setGutterIconRenderer(CoverageGutterIconRenderer(status, tooltip))
                    }
                    if (status == LineCoverageStatus.COVERED) {
                        covered++
                    } else {
                        uncovered++
                    }
                }
            }
        }
        return covered to uncovered
    }

    private fun lineStatus(mutations: List<MutationRecord>): LineCoverageStatus =
        when {
            mutations.any { it.status == MutationStatus.SURVIVED || it.status == MutationStatus.NO_COVERAGE } -> {
                LineCoverageStatus.UNCOVERED
            }

            mutations.any { it.status == MutationStatus.KILLED || it.status == MutationStatus.NON_VIABLE } -> {
                LineCoverageStatus.COVERED
            }

            else -> {
                LineCoverageStatus.UNKNOWN
            }
        }

    private fun buildTooltip(mutations: List<MutationRecord>): String =
        mutations
            .mapIndexed { index, mutation -> "${index + 1}. ${mutation.mutatedMethod} : ${mutation.description} → ${mutation.status}" }
            .joinToString("\n")

    private fun errorStripeColor(status: LineCoverageStatus): JBColor =
        when (status) {
            LineCoverageStatus.COVERED -> JBColor(0x4CAF50, 0x4CAF50)
            LineCoverageStatus.UNCOVERED -> JBColor(0xF44336, 0xF44336)
            LineCoverageStatus.UNKNOWN -> JBColor(0x90A4AE, 0x90A4AE)
        }

    /**
     * Resolves the source file for a mutated class. `findClass` is used first, but only a real
     * source file (`.java`/`.kt`) is accepted — a class file from compiled output or a dependency
     * would never match an open editor. Falls back to `FilenameIndex` by the source file name,
     * preferring a file that is currently open in an editor.
     */
    private fun resolveFileForClass(mutatedClass: String): VirtualFile? {
        val psiClass = JavaPsiFacade.getInstance(project).findClass(mutatedClass, GlobalSearchScope.projectScope(project))
        val containingFile = psiClass?.containingFile?.virtualFile
        if (containingFile != null && containingFile.extension in SOURCE_EXTENSIONS) {
            return containingFile
        }
        return resolveSourceFileByName(mutatedClass)
    }

    private fun resolveSourceFileByName(mutatedClass: String): VirtualFile? {
        val sourceFileName = sourceFilesByClass[mutatedClass] ?: return null
        val files = FilenameIndex.getFilesByName(project, sourceFileName, GlobalSearchScope.projectScope(project))
        if (files.size == 1) {
            return files.first().virtualFile
        }
        val openFiles =
            EditorFactory
                .getInstance()
                .allEditors
                .filter { it.project == project }
                .mapNotNull { FileDocumentManager.getInstance().getFile(it.document) }
                .toSet()
        return files.firstOrNull { it.virtualFile in openFiles }?.virtualFile
    }

    private companion object {
        val SOURCE_EXTENSIONS = setOf("java", "kt")
    }

    private fun removeAnnotation(editor: Editor) {
        if (editor.isDisposed) {
            return
        }
        editor.markupModel.allHighlighters.toList().forEach { highlighter ->
            if (highlighter.lineMarkerRenderer is CoverageLineMarkerRenderer) {
                editor.markupModel.removeHighlighter(highlighter)
            }
        }
    }
}

/**
 * Gutter icon renderer attached to each annotated line's highlighter via
 * [com.intellij.openapi.editor.markup.RangeHighlighter.setGutterIconRenderer]. Shows a
 * status-colored square in the gutter; hovering it displays the mutation descriptions in the
 * report-style tooltip format (same text as the error-stripe tooltip). [DumbAware] keeps the icon
 * visible while indexing runs. Removing the highlighter releases the renderer.
 */
private class CoverageGutterIconRenderer(
    private val status: LineCoverageStatus,
    private val tooltip: String,
) : GutterIconRenderer(),
    DumbAware {
    override fun getIcon(): Icon = StatusSquareIcon(gutterIconColor(status))

    override fun getTooltipText(): String = tooltip

    override fun getAlignment(): GutterIconRenderer.Alignment = GutterIconRenderer.Alignment.LEFT

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = System.identityHashCode(this)

    private fun gutterIconColor(status: LineCoverageStatus): Color =
        when (status) {
            LineCoverageStatus.COVERED -> Color(0x2F6B2F)
            LineCoverageStatus.UNCOVERED -> Color(0x8B3333)
            LineCoverageStatus.UNKNOWN -> Color(0x3A4A5C)
        }
}

/** Small status-colored square drawn as the gutter icon of an annotated line. */
private class StatusSquareIcon(
    private val color: Color,
) : Icon {
    override fun getIconWidth(): Int = SIZE

    override fun getIconHeight(): Int = SIZE

    override fun paintIcon(
        c: Component,
        g: Graphics,
        x: Int,
        y: Int,
    ) {
        val graphics = g.create() as? Graphics2D ?: return
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.color = color
            graphics.fillRoundRect(x, y, SIZE, SIZE, 4, 4)
        } finally {
            graphics.dispose()
        }
    }

    private companion object {
        const val SIZE = 8
    }
}
