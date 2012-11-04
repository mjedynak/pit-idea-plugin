package pl.mjedynak.idea.plugins.pit

import spock.lang.Specification
import javax.swing.Icon

class PitConfigurationTypeTest extends Specification {

    PitConfigurationType pitConfigurationType = new PitConfigurationType()

    def "should have initialized icon"() {
        when:
        Icon icon = pitConfigurationType.getIcon()
        then:
        icon != null
    }

    def "should have display name"() {
        when:
        String displayName = pitConfigurationType.getDisplayName()
        then:
        displayName == PitConfigurationType.DISPLAY_NAME
    }

    def "should have id"() {
        when:
        String id = pitConfigurationType.getId()
        then:
        id == PitConfigurationType.ID
    }

    def "should have configuration description"() {
        when:
        String description = pitConfigurationType.getConfigurationTypeDescription()
        then:
        description == PitConfigurationType.CONFIGURATION_DESCRIPTION
    }

    def "should return one configuration factory" () {
        when:
        def factories = pitConfigurationType.getConfigurationFactories()

        then:
        factories.length == 1
        factories[0] == pitConfigurationType.myFactory
    }
}
