package com.tessera.engine.common.option;

import java.util.HashMap;

public class OptionsList extends HashMap<String, Object> {

    public OptionsList(OptionsList options) {
        super(options);
    }

    public OptionsList() {
        super();
    }

    public boolean getBoolean(String key) {
        try {
            return (Boolean) get(key);
        } catch (Exception e) {
            return false;
        }
    }

    public BoundedInt getBoundedInt(String key) {
        try {
            return (BoundedInt) get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public BoundedFloat getBoundedFloat(String key) {
        try {
            return (BoundedFloat) get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getString(String key) {
        try {
            return (String) get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public int getInteger(String key) {
        try {
            return (int) get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    public float getFloat(String key) {
        try {
            return (float) get(key);
        } catch (Exception e) {
            return 0;
        }
    }

}
