package pl.mjedynak.idea.plugins.pit.cli.model;

public enum PitCommandLineArgument {
    REPORT_DIR("--reportDir"),
    SOURCE_DIRS("--sourceDirs"),
    TARGET_CLASSES("--targetClasses");

    private String name;

    private PitCommandLineArgument(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
