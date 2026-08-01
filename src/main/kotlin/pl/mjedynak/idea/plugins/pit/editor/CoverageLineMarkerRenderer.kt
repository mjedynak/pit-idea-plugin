package pl.mjedynak.idea.plugins.pit.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.LineMarkerRendererEx
import com.intellij.ui.JBColor
import java.awt.Graphics
import java.awt.Rectangle

/** Aggregated annotation status for a single source line. */
enum class LineCoverageStatus { COVERED, UNCOVERED, UNKNOWN }

/**
 * Paints the gutter band for annotated lines.
 *
 * Implements [LineMarkerRendererEx] with [LineMarkerRendererEx.Position.LEFT]: a plain
 * [com.intellij.openapi.editor.markup.LineMarkerRenderer] defaults to the RIGHT free-painters area,
 * which has zero width on the New UI — the band is attached to the markup model but never painted.
 * The LEFT area has a guaranteed minimum width (IntelliJ's own coverage markers rely on it).
 */
class CoverageLineMarkerRenderer(
    val status: LineCoverageStatus,
) : LineMarkerRendererEx {
    override fun getPosition(): LineMarkerRendererEx.Position = LineMarkerRendererEx.Position.LEFT

    override fun paint(
        editor: Editor,
        g: Graphics,
        r: Rectangle,
    ) {
        val width = r.width
        val height = r.height - 2
        if (width <= 0 || height <= 0) {
            return
        }
        g.color = colorForStatus(status)
        g.fillRoundRect(r.x, r.y + 1, width, height, 4, 4)
    }

    private fun colorForStatus(status: LineCoverageStatus): JBColor =
        when (status) {
            LineCoverageStatus.COVERED -> JBColor(0xAAFFAA, 0x2F6B2F)
            LineCoverageStatus.UNCOVERED -> JBColor(0xFFAAAA, 0x8B3333)
            LineCoverageStatus.UNKNOWN -> JBColor(0xDDE7EF, 0x3A4A5C)
        }
}
