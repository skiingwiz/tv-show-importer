package config;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;

public class CommandLineConfig extends BaseConfig {

    private final CommandLine commandLine;
    private final Properties props;

    public CommandLineConfig(CommandLine commandLine) {
        this.commandLine = commandLine;
        //TODO move from properties to multimap
        props = commandLine.getOptionProperties("property");
    }

    @Override
    public boolean hasKey(String key) {
        return commandLine.hasOption(key) || props.containsKey(key);
    }

    @Override
    protected String getValue(String key) {
        return commandLine.hasOption(key) ? commandLine.getOptionValue(key) : props.getProperty(key);
    }

}
