package pl.mjedynak.idea.plugins.pit.gui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;
import pl.mjedynak.idea.plugins.pit.PitRunConfiguration;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PitConfigurationForm extends SettingsEditor<PitRunConfiguration>{

    private JPanel panel;
    private JLabel targetClassesLabel;
    private JTextField targetClassesTextField;
    private JLabel sourceDirLabel;
    private JTextField sourceDirTextField;
    private JLabel reportDirLabel;
    private JTextField reportDirTextField;


    public String getReportDir() {
        return reportDirTextField.getText();
    }

    public String getSourceDir() {
        return sourceDirTextField.getText();
    }

    public String getTargetClasses() {
        return targetClassesTextField.getText();
    }

    public void setReportDir(String reportDir) {
        reportDirTextField.setText(reportDir);
    }

    public void setSourceDir(String sourceDir) {
        sourceDirTextField.setText(sourceDir);
    }

    public void setTargetClasses(String targetClasses) {
        targetClassesTextField.setText(targetClasses);
    }

    @Override
    protected void resetEditorFrom(PitRunConfiguration s) {
    }

    @Override
    protected void applyEditorTo(PitRunConfiguration s) throws ConfigurationException {
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

    @Override
    protected void disposeEditor() {
    }
}
