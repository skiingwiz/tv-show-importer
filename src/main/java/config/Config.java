package config;

import java.util.List;

public interface Config {

    boolean hasKey(String key);

    String getString(String key);

    boolean getBoolean(String rename);

    long getShort(String key);

    long getInt(String key);

    long getLong(String mirrorFileLife);

    List<String> getStringList(String key, String seperator);

    List<String> getStringList(String key);
}
