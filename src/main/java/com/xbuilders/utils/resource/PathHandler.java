/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xbuilders.utils.resource;

import com.xbuilders.Main;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zipCoder933
 */
public class PathHandler {


    public static File RESOURCE_DIR;
    private static File APP_DATA_DIR;
    public static File SAVES_DIR;

    //Individual paths
    public static File BLOCK_ICON_DIR, PLAYER_GLOBAL_INFO,CLIENT_SETTINGS,SERVER_SETTINGS;
    public static final String CHUNK_SHADER_DIR = "/shaders/chunkShader";

    static {
        File LOCAL_DIR = new File(System.getProperty("user.dir")).getAbsoluteFile();
        RESOURCE_DIR = new File(LOCAL_DIR, "res");
        RESOURCE_DIR.mkdirs();
        Main.LOGGER.info("Resource path: " + RESOURCE_DIR);

        BLOCK_ICON_DIR = resourcePath("items\\blocks\\icons");

    }

    public static void initialize(String appDataDir) {
        APP_DATA_DIR = resolveUnderDirectory(new File(System.getProperty("user.dir")),
                appDataDir == null ? "data" : appDataDir);
        APP_DATA_DIR.mkdirs();
        Main.LOGGER.info("Data path: " + APP_DATA_DIR);

        //Individual files
        PLAYER_GLOBAL_INFO = appDataPath("player_global_info.dat");
        CLIENT_SETTINGS = appDataPath("client_config.json");
        SERVER_SETTINGS = appDataPath("client_config.json");
        SAVES_DIR = appDataPath("saves");
        SAVES_DIR.mkdirs();
        Main.LOGGER.info("Saves path: " + SAVES_DIR);
    }


    public static File resourcePath(String path) {
        return resolveUnderDirectory(RESOURCE_DIR, path);
    }

    public static File appDataPath(String path) {
        return resolveUnderDirectory(APP_DATA_DIR, path);
    }

    private static File resolveUnderDirectory(File baseDirectory, String path) {
        if (baseDirectory == null) throw new IllegalStateException("Base directory has not been initialized.");
        if (path == null || path.isBlank()) return baseDirectory;

        String normalizedPath = path.replace("\\", "/").trim();

        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        if (normalizedPath.isEmpty()) return baseDirectory;
        Path resolved = Paths.get(baseDirectory.getAbsolutePath(), normalizedPath).normalize();
        return resolved.toFile();
    }
}
