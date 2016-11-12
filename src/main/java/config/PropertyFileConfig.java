package config;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import util.io.IterableBufferedReader;

public class PropertyFileConfig extends BaseConfig {

    //TODO this should be a multimap
    private Map<String, String> map = new HashMap<>();

    PropertyFileConfig(String filename) throws IOException {
        parse(filename);
    }

    private void parse(String filename) throws IOException {
        try (IterableBufferedReader r = new IterableBufferedReader(new FileReader(filename))) {
            for(String line = r.readLine(); line != null; line = r.readLine()) {

                if(!skip(line)) {
                    String[] keyVal = line.split("[=:]", 2);
                    String key = keyVal[0].trim();
                    String val = keyVal.length > 1 ? keyVal[1].trim() : "";

                    map.put(key, val);
                }
            }
        }
    }

    @Override
    public boolean hasKey(String key) {
        return map.containsKey(key);
    }

    @Override
    protected String getValue(String key) {
        return map.get(key);
    }

    private boolean skip(String str) {
        return !str.trim().matches("^[a-zA-Z_0-9].*");
    }

}
