package pl.mjedynak.idea.plugins.pit.cli;

import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;

public interface PitCommandLineArgumentsContainer {

    void put(PitCommandLineArgument argument, String value);

    String get(PitCommandLineArgument argument);
}
