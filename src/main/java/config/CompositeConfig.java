package config;

import java.util.Collection;

public final class CompositeConfig extends BaseConfig {

    private final Collection<BaseConfig> configs;

    public CompositeConfig(Collection<BaseConfig> configs) {
        this.configs = configs;
    }

    @Override
    public boolean hasKey(String key) {
        for(Config c : configs) {
            if(c.hasKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getValue(String key) {
        for(BaseConfig c : configs) {
            if(c.hasKey(key)) {
                return c.getValue(key);
            }
        }
        return null;
    }
}
