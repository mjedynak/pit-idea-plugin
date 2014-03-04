package pl.mjedynak.idea.plugins.pit.configuration

import spock.lang.Specification

import javax.swing.Icon

class PitConfigurationTypeTest extends Specification {

    PitConfigurationType pitConfigurationType = new PitConfigurationType()

    def "should have initialized icon"() {
        when:
        Icon icon = pitConfigurationType.icon
        then:
        icon != null
    }

    def "should have display name"() {
        when:
        String displayName = pitConfigurationType.displayName
        then:
        displayName == PitConfigurationType.DISPLAY_NAME
    }

    def "should have id"() {
        when:
        String id = pitConfigurationType.id
        then:
        id == PitConfigurationType.ID
    }

    def "should have configuration description"() {
        when:
        String description = pitConfigurationType.configurationTypeDescription
        then:
        description == PitConfigurationType.CONFIGURATION_DESCRIPTION
    }

    def "should return one configuration factory" () {
        when:
        def factories = pitConfigurationType.configurationFactories

        then:
        factories.length == 1
        factories[0] == pitConfigurationType.myFactory
    }
}
