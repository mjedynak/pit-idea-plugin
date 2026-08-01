package pl.mjedynak.idea.plugins.pit.editor

/** PIT mutation status, mirroring the `status` attribute of `<mutation>` in mutations.xml. */
enum class MutationStatus {
    KILLED,
    SURVIVED,
    NO_COVERAGE,
    NON_VIABLE,
    TIMED_OUT,
    MEMORY_ERROR,
    RUN_ERROR,
    NOT_STARTED,
    UNKNOWN,
    ;

    companion object {
        fun fromXml(status: String?): MutationStatus =
            status?.trim()?.uppercase()?.let { raw ->
                entries.firstOrNull { it.name == raw }
            } ?: UNKNOWN
    }
}
