package pl.mjedynak.idea.plugins.pit.cli.model;

public enum PitCommandLineArgument {
    REPORT_DIR("--reportDir"),
    SOURCE_DIRS("--sourceDirs"),
    TARGET_CLASSES("--targetClasses"),
    TARGET_TESTS("--targetTests"),
    INCLUDE_LAUNCH_CLASSPATH("--includeLaunchClasspath"),
    CLASSPATH("--classPath");

    private String name;

    PitCommandLineArgument(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
