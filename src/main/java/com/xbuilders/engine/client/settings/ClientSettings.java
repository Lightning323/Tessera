package com.xbuilders.engine.client.settings;

import com.xbuilders.engine.client.visuals.topMenu.multiplayer.ServerEntry;
import com.xbuilders.engine.common.option.BoundedFloat;
import com.xbuilders.engine.common.option.BoundedInt;
import com.xbuilders.utils.resource.ResourceUtils;
import com.xbuilders.engine.common.world.ClientWorld;
import com.xbuilders.engine.common.world.chunk.Chunk;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;

import static com.xbuilders.Main.LOGGER;
import static com.xbuilders.engine.client.settings.EngineSettingsUtils.gson;

public class ClientSettings {

    /**
     * NEVER declare a primitive typeReference as static. It will cause issues when reading the values.
     */
    public boolean game_switchMouseButtons = false;
    public boolean game_autoJump = false;
    public boolean video_fullscreen = true;
    public final BoundedFloat video_fullscreenSize = new BoundedFloat(1);
    public boolean video_vsync = true;
    public BoundedInt video_entityDistance = new BoundedInt(100);

    //Dev commandRegistry
    public long dev_blockBoundaryAreaLimit = 1000000;
    public boolean dev_allowOPCommands = false;

    //Internal commandRegistry (These wont ever be seen by the user, or shown in the UI)
    public ArrayList<ServerEntry> internal_serverList = new ArrayList<>();
    public boolean internal_smallWindow = false;
    public final BoundedInt internal_viewDistance = new BoundedInt(Chunk.WIDTH * 5);
    public final BoundedInt internal_simulationDistance = new BoundedInt(Chunk.WIDTH * 3);

    //Player information
    public String internal_playerName;
    public int internal_skinID;

    public ClientSettings initVariables() {
        internal_viewDistance.setBounds(ClientWorld.VIEW_DIST_MIN, ClientWorld.VIEW_DIST_MAX);
        internal_simulationDistance.setBounds(ClientWorld.VIEW_DIST_MIN / 2, ClientWorld.VIEW_DIST_MAX);
        video_entityDistance.setBounds(20, 100);
        video_fullscreenSize.setBounds(0.5f, 1.0f);
        video_fullscreenSize.clamp();

        if (internal_playerName == null) internal_playerName = System.getProperty("user.name");
        return this;
    }

    public static ClientSettings load() {
        try {
            File settingsFile = ResourceUtils.appDataFile("settings.json");
            if (settingsFile.exists()) {
                String jsonString = Files.readString(settingsFile.toPath());
                return gson.fromJson(jsonString, ClientSettings.class).initVariables();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ClientSettings().initVariables();
    }

    public void save() {
        this.initVariables();
        File settingsFile = ResourceUtils.appDataFile("settings.json");
        // Save to JSON
        try {
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
                settingsFile.setWritable(true);
                settingsFile.setReadable(true);
            }
            // Save to JSON
            Files.write(settingsFile.toPath(), gson.toJson(this).getBytes());
        } catch (Exception e) {
            LOGGER.info("error", e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Field field : ClientSettings.class.getDeclaredFields()) {
            try {
                sb.append(field.getName()).append(": ").append(field.get(this)).append("\n");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }
}
