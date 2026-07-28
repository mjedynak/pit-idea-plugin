package pl.mjedynak.idea.plugins.pit.gui

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import pl.mjedynak.idea.plugins.pit.configuration.PitRunConfiguration
import java.awt.Dimension
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class PitConfigurationForm : SettingsEditor<PitRunConfiguration>() {
    private var panel: JPanel? = null
    private var targetClassesLabel: JLabel? = null
    private var targetClassesTextField: JTextField? = null
    private var sourceDirLabel: JLabel? = null
    private var sourceDirTextField: JTextField? = null
    private var reportDirLabel: JLabel? = null
    private var reportDirTextField: JTextField? = null
    private var otherParamsTextField: JTextField? = null
    private var otherParamsLabel: JLabel? = null
    private var targetTestsTextField: JTextField? = null
    private var targetTestsLabel: JLabel? = null

    var reportDir: String
        get() = reportDirTextField?.text ?: ""
        set(value) {
            reportDirTextField?.text = value
        }

    var sourceDir: String
        get() = sourceDirTextField?.text ?: ""
        set(value) {
            sourceDirTextField?.text = value
        }

    var targetClasses: String
        get() = targetClassesTextField?.text ?: ""
        set(value) {
            targetClassesTextField?.text = value
        }

    var targetTests: String
        get() = targetTestsTextField?.text ?: ""
        set(value) {
            targetTestsTextField?.text = value
        }

    var otherParams: String
        get() = otherParamsTextField?.text ?: ""
        set(value) {
            otherParamsTextField?.text = value
        }

    override fun resetEditorFrom(s: PitRunConfiguration) {}

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(s: PitRunConfiguration) {}

    override fun createEditor(): JComponent = panel!!

    override fun disposeEditor() {}

    init {
        `$$$setupUI$$$`()
    }

    @Suppress("ktlint:standard:function-naming")
    private fun `$$$setupUI$$$`() {
        panel = JPanel()
        panel!!.layout = GridLayoutManager(5, 2, Insets(0, 0, 0, 0), -1, -1)
        targetClassesTextField = JTextField()
        panel!!.add(
            targetClassesTextField,
            GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false,
            ),
        )
        val spacer1 = Spacer()
        panel!!.add(
            spacer1,
            GridConstraints(
                4,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_VERTICAL,
                1,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null,
                null,
                null,
                0,
                false,
            ),
        )
        targetClassesLabel = JLabel()
        targetClassesLabel!!.text = "Target classes"
        panel!!.add(
            targetClassesLabel,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false,
            ),
        )
        sourceDirTextField = JTextField()
        panel!!.add(
            sourceDirTextField,
            GridConstraints(
                1,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false,
            ),
        )
        sourceDirLabel = JLabel()
        sourceDirLabel!!.text = "Source dir"
        panel!!.add(
            sourceDirLabel,
            GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false,
            ),
        )
        reportDirTextField = JTextField()
        panel!!.add(
            reportDirTextField,
            GridConstraints(
                2,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false,
            ),
        )
        reportDirLabel = JLabel()
        reportDirLabel!!.text = "Report dir"
        panel!!.add(
            reportDirLabel,
            GridConstraints(
                2,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false,
            ),
        )
        otherParamsTextField = JTextField()
        panel!!.add(
            otherParamsTextField,
            GridConstraints(
                3,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false,
            ),
        )
        otherParamsLabel = JLabel()
        otherParamsLabel!!.text = "Other params"
        panel!!.add(
            otherParamsLabel,
            GridConstraints(
                3,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false,
            ),
        )
    }

    @Suppress("ktlint:standard:function-naming")
    fun `$$$getRootComponent$$$`(): JComponent = panel!!
}
