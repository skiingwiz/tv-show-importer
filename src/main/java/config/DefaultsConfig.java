package config;

import java.util.HashMap;
import java.util.Map;

public class DefaultsConfig extends BaseConfig {

    private Map<String, String> props = new HashMap<>();

    public DefaultsConfig() {
        props.put(GlobalConfig.LANGUAGE, "en");
        props.put(GlobalConfig.RENAME, "true");

        props.put(GlobalConfig.CACHE_DIR, "cache");
        props.put(GlobalConfig.RECURSE, "true");
        props.put(GlobalConfig.SERIES_BANNERS, "false");
        props.put(GlobalConfig.SEASON_BANNERS, "false");

        props.put(GlobalConfig.IMAGE_FORMAT, "jpeg");

        props.put(GlobalConfig.PROPERTIES_FILE, "true");
        props.put(GlobalConfig.FANART, "true");

        props.put(GlobalConfig.FANART_DIR, "./");

        props.put(GlobalConfig.RENAME_PATTERN, "%season-num%x%episode-num% - %episode-title%");
        props.put(GlobalConfig.EXCLUDE_PATTERNS, "");

        props.put(GlobalConfig.MIRROR_FILE_LIFE, Long.toString(3600L * 24L * 30L)); //30 Days

        props.put(GlobalConfig.LARGEST_FILE_IN_DIR, "false");
    }

    @Override
    public boolean hasKey(String key) {
        return props.containsKey(key);
    }

    @Override
    protected String getValue(String key) {
        return props.get(key);
    }

}
