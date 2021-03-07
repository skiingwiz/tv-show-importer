package config;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseConfig implements Config {

    protected abstract String getValue(String key);

    @Override
    public String getString(String key) {
        return getValue(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getValue(key));
    }

    @Override
    public long getShort(String key) {
        return Short.parseShort(getValue(key));
    }

    @Override
    public long getInt(String key) {
        return Integer.parseInt(getValue(key));
    }

    @Override
    public long getLong(String key) {
        return Long.parseLong(getValue(key));
    }

    @Override
    public List<String> getStringList(String key) {
        return getStringList(key, "\\|");
    }

    @Override
    public List<String> getStringList(String key, String seperator) {
        List<String> retVal = new ArrayList<>();
        if(hasKey(key)) {
            for(String s : getValue(key).split(seperator)) {
                if(!s.trim().isEmpty()) {
                    retVal.add(s.trim());
                }
            }
        }
        return retVal;
    }
}
