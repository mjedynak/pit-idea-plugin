package pl.mjedynak.idea.plugins.pit.cli

import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument

interface PitCommandLineArgumentsContainer {
    fun put(
        argument: PitCommandLineArgument,
        value: String,
    )

    fun get(argument: PitCommandLineArgument): String?
}
