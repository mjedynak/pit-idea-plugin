package pl.mjedynak.idea.plugins.pit.cli

import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument
import java.util.concurrent.ConcurrentHashMap

class PitCommandLineArgumentsContainerImpl : PitCommandLineArgumentsContainer {
    private val map = ConcurrentHashMap<PitCommandLineArgument, String>()

    override fun put(
        argument: PitCommandLineArgument,
        value: String,
    ) {
        map[argument] = value
    }

    override fun get(argument: PitCommandLineArgument): String? = map[argument]
}
