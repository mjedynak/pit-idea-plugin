package pl.mjedynak.idea.plugins.pit.cli;

import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PitCommandLineArgumentsContainerImpl implements PitCommandLineArgumentsContainer {

    private Map<PitCommandLineArgument, String> map = new ConcurrentHashMap<PitCommandLineArgument, java.lang.String>();

    @Override
    public void put(PitCommandLineArgument argument, String value) {
        map.put(argument, value);
    }

    @Override
    public String get(PitCommandLineArgument argument) {
        return map.get(argument);
    }
}
