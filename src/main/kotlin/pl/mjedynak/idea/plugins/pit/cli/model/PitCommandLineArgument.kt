package pl.mjedynak.idea.plugins.pit.cli.model

enum class PitCommandLineArgument(
    val argumentName: String,
) {
    REPORT_DIR("--reportDir"),
    SOURCE_DIRS("--sourceDirs"),
    TARGET_CLASSES("--targetClasses"),
    TARGET_TESTS("--targetTests"),
}
