// 
// Decompiled by Procyon v0.5.36
// 
package com.xbuilders.engine.common.world;

import com.xbuilders.utils.FileUtils;
import com.xbuilders.utils.resource.PathHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.xbuilders.Main.LOGGER;

public class WorldsHandler {

    public static File worldFile(String name) {
        return new File(PathHandler.SAVES_DIR, formatWorldName(name));
    }

    public static final void makeNewWorld(final WorldData info) throws IOException {
        info.getDirectory().mkdirs();
        info.save();
    }

    public static String formatWorldName(final String name) {
        return name.replaceAll("[^A-z\\s0-9_-]", "").replace("^", "").strip();
    }

    public static boolean worldNameAlreadyExists(final String name) {
        return worldFile(name).exists();
    }


    public static void deleteWorld(WorldData info) throws IOException {
        if (info != null) {
            FileUtils.moveDirectoryToTrash(info.getDirectory());
        }
    }

    public static ArrayList<WorldData> listWorlds(ArrayList<WorldData> worlds) throws IOException {
        worlds.clear();
        File[] entries = PathHandler.SAVES_DIR.listFiles();
        if (entries == null) return worlds;

        for (final File subDir : entries) {
            System.out.println("SUBDIR "+subDir.getAbsolutePath());
            if (subDir.isDirectory()) {
                WorldData info = new WorldData();
                try {
                    info.load(subDir);
                    worlds.add(info);
                } catch (IOException ex) {
                    LOGGER.warn( "World \"" + formatWorldName(subDir.getName()) + "\" could not be loaded", ex);
                }
            }
        }
        return worlds;
    }
}
