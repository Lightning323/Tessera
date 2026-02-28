/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xbuilders.utils.resource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * @author zipCoder933
 */
public class ResourceUtils {

    //files
    public static File RESOURCE_DIR;
    public static File APP_DATA_DIR;
    public static File WORLDS_DIR;
    public static File LOCAL_DIR;

    //Individual paths
    public static File BLOCK_ICON_DIR, PLAYER_GLOBAL_INFO;


    static {
        System.out.println("RESOURCES:");
        LOCAL_DIR = new File(System.getProperty("user.dir")).getAbsoluteFile();
        RESOURCE_DIR = new File(LOCAL_DIR, "res");
        RESOURCE_DIR.mkdirs();

        System.out.println("\tLocal path: " + LOCAL_DIR);
        System.out.println("\tResource path: " + RESOURCE_DIR);

        BLOCK_ICON_DIR = resourceFile("items\\blocks\\icons");
    }

    public static void initialize(boolean gameDevResources, String appDataDir) {
        String appFolderName = appDataDir == null ? "xbuilders3" : appDataDir;
        APP_DATA_DIR = resolveUnderDirectory(getAppDataRootDirectory(), appFolderName);
        APP_DATA_DIR.mkdirs();
        System.out.println("\tApp Data path: " + APP_DATA_DIR);

        //Individual files
        PLAYER_GLOBAL_INFO = new File(APP_DATA_DIR, "player_global_info.dat");
        //Worlds
        WORLDS_DIR = new File(APP_DATA_DIR, (gameDevResources ? "game_dev" : "game"));
        WORLDS_DIR.mkdirs();
        System.out.println("\tWorlds path: " + WORLDS_DIR);
    }

    public static File localFile(String path) {
        return resolveUnderDirectory(LOCAL_DIR, path);
    }

    public static File resourceFile(String path) {
        return resolveUnderDirectory(RESOURCE_DIR, path);
    }

    public static File appDataFile(String path) {
        return resolveUnderDirectory(APP_DATA_DIR, path);
    }

    public static File worldFile(String path) {
        return resolveUnderDirectory(WORLDS_DIR, path);
    }

    public static String toFormattedPath(File f){
        return f.toPath().normalize().toAbsolutePath().toString().replace("\\", "/");
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

    private static File getAppDataRootDirectory() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (osName.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) return new File(localAppData);

            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) return new File(appData);

            return resolveUnderDirectory(new File(System.getProperty("user.home")), "AppData/Local");
        }

        if (osName.contains("mac")) {
            return resolveUnderDirectory(new File(System.getProperty("user.home")), "Library/Application Support");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) return new File(xdgDataHome);

        return resolveUnderDirectory(new File(System.getProperty("user.home")), ".local/share");
    }


    public static byte[] downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);  // 10 seconds
        connection.setReadTimeout(10000);     // 10 seconds

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }
}
