package com.xbuilders.utils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xbuilders.engine.common.json.gson.BoundedFloatTypeAdapter;
import com.xbuilders.engine.common.json.gson.BoundedIntTypeAdapter;
import com.xbuilders.engine.common.option.BoundedFloat;
import com.xbuilders.engine.common.option.BoundedInt;

import java.io.File;
import java.nio.file.Files;

import static com.xbuilders.Main.LOGGER;

public class ConfigUtils {//This is supposed to be a private class
    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(BoundedInt.class, new BoundedIntTypeAdapter())
            .registerTypeAdapter(BoundedFloat.class, new BoundedFloatTypeAdapter())
            .create();


    public static <T> T loadConfig(File file, Class<T> clazz) {
        T ret;
        try {
            // 1. Create a new instance of the class type
            ret = clazz.getDeclaredConstructor().newInstance();

            if (file.exists()) {
                String jsonString = Files.readString(file.toPath());
                // 2. Pass the class to GSON
                ret = gson.fromJson(jsonString, clazz);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading config for " + clazz.getSimpleName(), e);
            // Fallback: try to return a blank instance even on failure
            try { return clazz.getDeclaredConstructor().newInstance(); }
            catch (Exception ex) { return null; }
        }
        return ret;
    }

    public static <T> void saveConfig(T cfg, File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                // Note: On Ubuntu/Linux, setWritable(true) follows umask
                file.setWritable(true);
                file.setReadable(true);
            }
            // 3. Serialize the generic object 'cfg'
            Files.write(file.toPath(), gson.toJson(cfg).getBytes());
        } catch (Exception e) {
            LOGGER.error("Error saving config", e);
        }
    }
}
