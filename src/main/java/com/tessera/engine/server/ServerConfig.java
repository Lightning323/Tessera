package com.tessera.engine.server;

import com.tessera.engine.client.ClientSettings;
import com.tessera.utils.config.Config;
import com.tessera.utils.config.ConfigUtils;
import com.tessera.utils.resource.PathHandler;

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
