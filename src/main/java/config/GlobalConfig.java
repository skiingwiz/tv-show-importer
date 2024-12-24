package config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GlobalConfig {
    public static final String LANGUAGE = "language";
    public static final String RENAME = "rename";
    public static final String RENAME_PATTERN = "rename-pattern";
    public static final String CLEAR_CACHE = "clear-cache";
    public static final String CACHE_DIR = "cache-dir";
    public static final String RECURSE = "recurse";
    public static final String SERIES_BANNERS = "series-banners";
    public static final String SEASON_BANNERS = "season-banners";
    public static final String EXCLUDE_PATTERNS = "excluded-endings";
    public static final String MIRROR_FILE_LIFE = "mirror-file-life";
    public static final String IMAGE_FORMAT = "image-format";
    public static final String FANART_DIR = "fanart-dir";
    public static final String PROPERTIES_FILE = "properties-file";
    public static final String FANART = "fanart";
    public static final String PRINT_VERSION = "version";
    public static final String LARGEST_FILE_IN_DIR = "preprocess-largest-file-in-dir";

    private static final String CONFIG = "config";
    private static final String HELP = "help";

    private static CompositeConfig config;
    private static Options options;

    static {
        setupOptions();
    }

    public static Config get() {
        return config;
    }

    /**
     *
     * @param args
     * @return The positional arguments from the command line arguments
     */
    public static List<String> parse(String[] args) throws Exception {
        CommandLine commandLine = new DefaultParser().parse(options, args);

        if(commandLine.hasOption(HELP)) {
            printUsage();
            System.exit(0);
        }

        List<BaseConfig> configs = new ArrayList<>();
        configs.add(new CommandLineConfig(commandLine));
        if (commandLine.hasOption(CONFIG)) {
            for (String file : commandLine.getOptionValues(CONFIG)) {
                configs.add(new PropertyFileConfig(file));
            }
        }
        configs.add(new DefaultsConfig());

        config = new CompositeConfig(configs);

        return Collections.unmodifiableList(commandLine.getArgList());
    }

    public static void printUsage() {
        new HelpFormatter().printHelp("java -jar <this jar> [options] <file>", options);
    }

    private static void setupOptions() {
        options = new Options().addOption(Option.builder("h").longOpt(HELP).desc("Print this usage message").build())
                .addOption(Option.builder("P").longOpt("property").hasArgs().valueSeparator().build())
                .addOption(Option.builder().longOpt(CLEAR_CACHE).desc("Clear cache and exit").build())
                .addOption(Option.builder("v").longOpt("version").desc("Print version and exit").build())
                .addOption(Option.builder("c").longOpt(CONFIG).hasArg().argName("config-file").build());

        /*
         * parser = new OptionParser();
         * parser.setUsage("Usage: Main [options] file [file ...]");
         *
         * parser.addOption('l', "language", GlobalConfig.LANGUAGE,
         * "Set the language for episode information.  Default is English",
         * OptionParser.Action.STORE_VALUE, "en"); parser.addOption('n', "no-rename",
         * GlobalConfig.RENAME, "Do not rename files as a part of processing",
         * OptionParser.Action.STORE_FALSE, "true");
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "clear-cache",
         * GlobalConfig.CLEAR_CACHE, "Clear TheTvDb.com cache before processing",
         * OptionParser.Action.STORE_TRUE); parser.addOption(OptionParser.NO_SHORTNAME,
         * GlobalConfig.CACHE_DIR, GlobalConfig.CACHE_DIR,
         * "Set TheTvDb.com cache directory", OptionParser.Action.STORE_VALUE, "cache");
         * parser.addOption('x', "no-recurse", GlobalConfig.RECURSE,
         * "Do not recurse through subdirectories when a directory is given.",
         * OptionParser.Action.STORE_FALSE, "true"); parser.addOption('s',
         * "series-banners", GlobalConfig.SERIES_BANNERS, "Download series banners.",
         * OptionParser.Action.STORE_TRUE); parser.addOption('b', "season-banners",
         * GlobalConfig.SEASON_BANNERS, "Download season banners.",
         * OptionParser.Action.STORE_TRUE);
         *
         * parser.addOption('i', "image-format", GlobalConfig.IMAGE_FORMAT,
         * "The format for downloaded images.  Default is jpeg.",
         * OptionParser.Action.STORE_VALUE, "jpeg");
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "no-properties",
         * GlobalConfig.PROPERTIES_FILE, "Don't write .properties files",
         * OptionParser.Action.STORE_FALSE, "true");
         * parser.addOption(OptionParser.NO_SHORTNAME, "no-fanart", GlobalConfig.FANART,
         * "Don't download fanart", OptionParser.Action.STORE_FALSE, "true");
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "fanart-dir",
         * GlobalConfig.FANART_DIR, "The base directory for fanart.",
         * OptionParser.Action.STORE_VALUE, "./");
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "rename-pattern",
         * GlobalConfig.RENAME_PATTERN,
         * "The pattern to use when renaming the media file.",
         * OptionParser.Action.STORE_VALUE,
         * "%season-num%x%episode-num% - %episode-title%");
         * parser.addOption(OptionParser.NO_SHORTNAME, "excluded-endings",
         * GlobalConfig.EXCLUDED_ENDINGS,
         * "The endings of files to be excluded. Separate multiple endings using |",
         * OptionParser.Action.STORE_VALUE, ".properties|.!ut");
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "version", GlobalConfig.VERSION,
         * "Print the version and exit", OptionParser.Action.STORE_TRUE);
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, "thetvdb-mirror-file-life",
         * GlobalConfig.MIRROR_FILE_LIFE,
         * "The amount of time to cache theTvDb.com's mirror file before re-requesting it (seconds). Default is 30 days."
         * , OptionParser.Action.STORE_VALUE, 3600L * 24L * 30L);
         *
         * parser.addOption(OptionParser.NO_SHORTNAME, GlobalConfig.LARGEST_FILE_IN_DIR,
         * GlobalConfig.LARGEST_FILE_IN_DIR,
         * "Special Preprocessing Step: Process largest file in the parent directory only"
         * , OptionParser.Action.STORE_TRUE);
         *
         * parser.addOption('c', "config", GlobalConfig.CONFIG,
         * "The path to a properties file for configuration.",
         * OptionParser.Action.STORE_VALUE);
         */
    }
}
