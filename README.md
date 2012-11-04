PIT Idea Plugin
===============

IntelliJ IDEA plugin for PIT mutation testing (http://pitest.org).
For now the integration between IDE and PIT is pretty basic. Plugin adds a 'Run configuration' that allows to execute PIT within IDE.
In further plans there is an integration between mutation testing results and source code in IDE.

JUnit / TestNG tests are supported.
Dependency to PIT must be added to the tested project, e.g.: org.pitest:pitest:0.28


Build Requirements
==================
Requires IntelliJ IDEA installation for building.
- edit build.gradle and change values of 'ideaInstallationPath' and 'ideaJdk' to be in sync with your environment
- invoke 'gradle build' from command line to build
- invoke 'gradle idea' from command line to generate IDEA specific files - provides possibility to be opened as IDEA project.