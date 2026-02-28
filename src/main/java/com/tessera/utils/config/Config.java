package com.tessera.utils.config;

import java.lang.reflect.Field;

public abstract class Config {

//    /**
//     * 1. If you HAVE a No-Args Constructor
//     * If your Config class has a constructor with no parameters (either explicitly written or the default one provided by Java), GSON will call it.
//     */
    public abstract void initVariables();


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Field field : Config.class.getDeclaredFields()) {
            try {
                sb.append(field.getName()).append(": ").append(field.get(this)).append("\n");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }
}
