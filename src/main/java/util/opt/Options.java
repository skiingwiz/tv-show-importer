package util.opt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {
    private Map<String, String> map = new HashMap<String, String>();
    private Map<String, Option> options = new HashMap<String, Option>();
    private List<String> pos = new ArrayList<String>();


    public Options(List<Option> options) {
        for(Option o : options) {
            this.options.put(o.name, o);
        }
    }

    /**
     * Note: Positional parameters are 1 indexed <b> NOT 0 indexed</b>
     * @param pos
     * @param value
     */
    public void addPositional(int pos, String value) {
        this.pos.add(pos - 1, value);
    }

    public void addResult(Option option, String value) {
        map.put(option.name, value);
    }

    public void addResult(Option option, boolean b) {
        map.put(option.name, Boolean.toString(b));
    }


    public void addResult(Option option, Object value) {
        map.put(option.name, value.toString());
    }

    public Object get(String name) {
        return map.get(name);
    }

    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(getValue(name));
    }

    public String getString(String name) {
        return getValue(name);
    }

    public int getInt(String name) {
        return Integer.parseInt(getValue(name));
    }

    public long getLong(String name) {
        return Long.parseLong(getValue(name));
    }

    public float getFloat(String name) {
        return Float.parseFloat(getValue(name));
    }

    public double getDouble(String name) {
        return Double.parseDouble(getValue(name));
    }

    public BigDecimal getBigDecimal(String name) {
        return new BigDecimal(getValue(name));
    }

    public String getPositional(int i) {
        return pos.get(i - 1);
    }

    public List<String> getPositionals() {
        return Collections.unmodifiableList(pos);
    }

    public int countPositionals() {
        return pos.size();
    }

    private String getValue(String name) {
        String res = map.get(name);
        if(res == null) {
            Option o = options.get(name);
            if(o != null) {
                res = o.def;
            }
        }

        return res;
    }

    /**
     * Merge a second Options object into this one.  If there are conflicts the new options take precedence.
     * @return
     */
    public Options merge(Options o2) {
        map.putAll(o2.map);
        options.putAll(o2.options);
        pos.addAll(o2.pos);
        return this;
    }
}
