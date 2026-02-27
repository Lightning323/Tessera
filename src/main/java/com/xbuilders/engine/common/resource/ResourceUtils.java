/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xbuilders.engine.common.resource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
        LOCAL_DIR = new File(System.getProperty("user.dir"));
        RESOURCE_DIR = new File(LOCAL_DIR, "res");
        RESOURCE_DIR.mkdirs();

        System.out.println("\tLocal path: " + LOCAL_DIR);
        System.out.println("\tResource path: " + RESOURCE_DIR);

        BLOCK_ICON_DIR = resourceFile("items\\blocks\\icons");
    }

    public static void initialize(boolean gameDevResources, String appDataDir) {
        APP_DATA_DIR = new File(System.getenv("LOCALAPPDATA"), appDataDir == null ? "xbuilders3" : appDataDir);
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
        return new File(LOCAL_DIR, path.replace("\\","/"));
    }

    public static File resourceFile(String path) {
        return new File(RESOURCE_DIR, path.replace("\\","/"));
    }

    public static File appDataFile(String path) {
        return new File(APP_DATA_DIR, path.replace("\\","/"));
    }

    public static File worldFile(String path) {
        return new File(WORLDS_DIR, path.replace("\\","/"));
    }

    public static String toFormattedPath(File f){
        return f.getAbsolutePath().replace("\\", "/");
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
