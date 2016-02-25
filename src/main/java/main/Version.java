package main;

import java.io.IOException;
import java.util.Properties;

public class Version {
    private static String version;

	public static String getFullNameVersion() {
	    if(version == null) {
	        loadVersion();
	    }
	    return "TV File Processor (v" + version + ")";
	}


	private static void loadVersion() {
	    Properties props = new Properties();
	    try {
	        props.load(Version.class.getResourceAsStream("/buildinfo.properties"));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    version = props.getProperty("version");
	}


	private Version() {}
}
