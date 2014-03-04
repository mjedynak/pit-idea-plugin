package pl.mjedynak.idea.plugins.pit.gui

import spock.lang.Specification
import javax.swing.JTextField

class PitConfigurationFormTest extends Specification {

    PitConfigurationForm pitConfigurationForm = new PitConfigurationForm()
    JTextField textFieldMock = Mock()
    String text = 'someText'

    def setup() {
        textFieldMock.text >> text
    }

    def "should get text from report dir field"() {
        pitConfigurationForm.reportDirTextField = textFieldMock

        when:
        String result = pitConfigurationForm.reportDir

        then:
        result == text
    }

    def "should get text from source dir field"() {
        pitConfigurationForm.sourceDirTextField = textFieldMock

        when:
        String result = pitConfigurationForm.sourceDir

        then:
        result == text
    }

    def "should get text from target classes field"() {
        pitConfigurationForm.targetClassesTextField = textFieldMock

        when:
        String result = pitConfigurationForm.targetClasses

        then:
        result == text
    }

    def "should get text from other params field"() {
        pitConfigurationForm.otherParamsTextField = textFieldMock

        when:
        String result = pitConfigurationForm.otherParams

        then:
        result == text
    }

    def "should set text for report dir field"() {
        pitConfigurationForm.reportDirTextField = textFieldMock

        when:
        pitConfigurationForm.setReportDir(text)

        then:
        1 * textFieldMock.setText(text)
    }

    def "should set text for source dir field"() {
        pitConfigurationForm.sourceDirTextField = textFieldMock

        when:
        pitConfigurationForm.setSourceDir(text)

        then:
        1 * textFieldMock.setText(text)
    }

    def "should set text for target classes field"() {
        pitConfigurationForm.targetClassesTextField = textFieldMock

        when:
        pitConfigurationForm.setTargetClasses(text)

        then:
        1 * textFieldMock.setText(text)
    }

    def "should set text for other params field"() {
        pitConfigurationForm.otherParamsTextField = textFieldMock

        when:
        pitConfigurationForm.setOtherParams(text)

        then:
        1 * textFieldMock.setText(text)
    }

    def "create editor returns panel"() {
        when:
        def editor = pitConfigurationForm.createEditor()

        then:
        editor == pitConfigurationForm.panel
    }
}
