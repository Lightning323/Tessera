package com.tessera.engine.client;

import com.tessera.engine.client.visuals.topMenu.multiplayer.ServerEntry;
import com.tessera.engine.common.option.BoundedFloat;
import com.tessera.engine.common.option.BoundedInt;
import com.tessera.utils.config.Config;
import com.tessera.utils.config.ConfigUtils;
import com.tessera.utils.resource.PathHandler;
import com.tessera.engine.common.world.ClientWorld;
import com.tessera.engine.common.world.chunk.Chunk;

import java.util.ArrayList;

public class ClientSettings extends Config {
    public void save() {
        ConfigUtils.saveConfig(this, PathHandler.CLIENT_SETTINGS);
    }
    public static ClientSettings load(){
       return ConfigUtils.loadConfig(PathHandler.CLIENT_SETTINGS, ClientSettings.class);
    }

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

    public void initVariables() {
        internal_viewDistance.setBounds(ClientWorld.VIEW_DIST_MIN, ClientWorld.VIEW_DIST_MAX);
        internal_simulationDistance.setBounds(ClientWorld.VIEW_DIST_MIN / 2, ClientWorld.VIEW_DIST_MAX);
        video_entityDistance.setBounds(20, 100);
        video_fullscreenSize.setBounds(0.5f, 1.0f);
        video_fullscreenSize.clamp();

        if (internal_playerName == null) internal_playerName = System.getProperty("user.name");
    }
}
