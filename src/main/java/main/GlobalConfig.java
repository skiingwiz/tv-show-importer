package main;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.opt.InvalidOptionException;
import util.opt.OptionParser;
import util.opt.Options;


public class GlobalConfig {
	public static final String LANGUAGE = "language";
	public static final String RENAME = "rename";
	public static final String RENAME_PATTERN = "rename-pattern";
	public static final String CLEAR_CACHE = "clearcache";
	public static final String CACHE_DIR = "cache-dir";
	public static final String RECURSE = "recurse";
	public static final String SERIES_BANNERS = "series-banners";
	public static final String SEASON_BANNERS = "season-banners";
	public static final String EXCLUDED_ENDINGS = "excluded-endings";
	public static final String MIRROR_FILE_LIFE = "mirror-file-life";
	public static final String IMAGE_FORMAT = "image-format";
	public static final String FANART_DIR = "fanart-dir";
	public static final String PROPERTIES_FILE = "properties-file";
	public static final String FANART = "fanart";
	public static final String VERSION = "print-version";
	public static final String LARGEST_FILE_IN_DIR = "preprocess-largest-file-in-dir";
	public static final String CONFIG = "config";

	private static OptionParser parser;
	private static Options options;

	private static Logger log = LoggerFactory.getLogger(GlobalConfig.class);

	public static void setOptions(Options options) {
		GlobalConfig.options = options;
	}

	public static Options getOptions() {
		if(options == null) {
			try {
				parse(null);
			} catch (InvalidOptionException e) {
			    log.error("Invalid options given.", e);
			} catch (FileNotFoundException e) {
                log.error("Given config file could not be found", e);
            } catch (IOException e) {
                log.error("Problem reading given config file", e);
            }
		}
		return options;
	}

	public static void parse(String[] args) throws InvalidOptionException, FileNotFoundException, IOException {
		if(parser == null) {
			setupOptions();
		}

		options = parser.parse(args);

		String configFile = options.getString(CONFIG);
		if(configFile != null) {
		    Options fileOptions = parser.load(configFile);
		    options = fileOptions.merge(options);
		}
	}

	private static void setupOptions() {
		parser = new OptionParser();
		parser.setUsage("Usage: Main [options] file [file ...]");

		parser.addOption('l', "language", GlobalConfig.LANGUAGE, "Set the language for episode information.  Default is English", OptionParser.Action.STORE_VALUE, "en");
		parser.addOption('n', "no-rename", GlobalConfig.RENAME, "Do not rename files as a part of processing", OptionParser.Action.STORE_FALSE, "true");

		parser.addOption(OptionParser.NO_SHORTNAME, "clear-cache", GlobalConfig.CLEAR_CACHE, "Clear TheTvDb.com cache before processing", OptionParser.Action.STORE_TRUE);
		parser.addOption(OptionParser.NO_SHORTNAME, GlobalConfig.CACHE_DIR, GlobalConfig.CACHE_DIR, "Set TheTvDb.com cache directory", OptionParser.Action.STORE_VALUE, "cache");
		parser.addOption('x', "no-recurse", GlobalConfig.RECURSE, "Do not recurse through subdirectories when a directory is given.", OptionParser.Action.STORE_FALSE, "true");
		parser.addOption('s', "series-banners", GlobalConfig.SERIES_BANNERS, "Download series banners.", OptionParser.Action.STORE_TRUE);
		parser.addOption('b', "season-banners", GlobalConfig.SEASON_BANNERS, "Download season banners.", OptionParser.Action.STORE_TRUE);

		parser.addOption('i', "image-format", GlobalConfig.IMAGE_FORMAT, "The format for downloaded images.  Default is jpeg.", OptionParser.Action.STORE_VALUE, "jpeg");

		parser.addOption(OptionParser.NO_SHORTNAME, "no-properties", GlobalConfig.PROPERTIES_FILE, "Don't write .properties files", OptionParser.Action.STORE_FALSE, "true");
		parser.addOption(OptionParser.NO_SHORTNAME, "no-fanart", GlobalConfig.FANART, "Don't download fanart", OptionParser.Action.STORE_FALSE, "true");

		parser.addOption(OptionParser.NO_SHORTNAME, "fanart-dir", GlobalConfig.FANART_DIR, "The base directory for fanart.", OptionParser.Action.STORE_VALUE, "./");

		parser.addOption(OptionParser.NO_SHORTNAME, "rename-pattern", GlobalConfig.RENAME_PATTERN, "The pattern to use when renaming the media file.", OptionParser.Action.STORE_VALUE, "%season-num%x%episode-num% - %episode-title%");
		parser.addOption(OptionParser.NO_SHORTNAME, "excluded-endings", GlobalConfig.EXCLUDED_ENDINGS, "The endings of files to be excluded. Separate multiple endings using |", OptionParser.Action.STORE_VALUE, ".properties|.!ut");

		parser.addOption(OptionParser.NO_SHORTNAME, "version", GlobalConfig.VERSION, "Print the version and exit", OptionParser.Action.STORE_TRUE);

		parser.addOption(OptionParser.NO_SHORTNAME, "thetvdb-mirror-file-life", GlobalConfig.MIRROR_FILE_LIFE, "The amount of time to cache theTvDb.com's mirror file before re-requesting it (seconds). Default is 30 days.", OptionParser.Action.STORE_VALUE, 3600L * 24L * 30L);

		parser.addOption(OptionParser.NO_SHORTNAME, GlobalConfig.LARGEST_FILE_IN_DIR, GlobalConfig.LARGEST_FILE_IN_DIR,
		        "Special Preprocessing Step: Process largest file in the parent directory only",
		        OptionParser.Action.STORE_TRUE);

		parser.addOption('c', "config", GlobalConfig.CONFIG, "The path to a properties file for configuration.", OptionParser.Action.STORE_VALUE);
	}
}
