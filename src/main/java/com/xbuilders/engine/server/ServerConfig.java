package com.xbuilders.engine.server;

import com.xbuilders.engine.client.ClientSettings;
import com.xbuilders.engine.client.visuals.topMenu.multiplayer.ServerEntry;
import com.xbuilders.engine.common.option.BoundedFloat;
import com.xbuilders.engine.common.option.BoundedInt;
import com.xbuilders.engine.common.world.ClientWorld;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.utils.config.Config;
import com.xbuilders.utils.config.ConfigUtils;
import com.xbuilders.utils.resource.PathHandler;

import java.util.ArrayList;

public class ServerConfig extends Config {
    public void save() {
        ConfigUtils.saveConfig(this, PathHandler.SERVER_SETTINGS);
    }
    public static ClientSettings load(){
        return ConfigUtils.loadConfig(PathHandler.SERVER_SETTINGS, ClientSettings.class);
    }

    /**
     * NEVER declare a primitive typeReference as static. It will cause issues when reading the values.
     */
    public String saveDir = "";
    public int maxPlayers = 20;

    public void initVariables() {
    }
}
