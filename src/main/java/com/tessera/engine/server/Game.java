/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.GameUI;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.WorldData;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;

/**
 * @author zipCoder933
 */
public abstract class Game implements GameSceneEvents {

    public final ArrayList<Terrain> terrainsList = new ArrayList<>();


    public boolean releaseMouse() {
        return false;
    }

    public Game(){}

    public abstract void setupClient(ClientWindow window,  NkContext ctx, GameUI gameUI) throws Exception;

    public abstract void setupServer(Server server);

    public final Terrain getTerrainFromInfo(WorldData info) {
        for (Terrain terrain : terrainsList) {
            if (terrain.name.equals(info.getTerrain())) {
                terrain.initForWorld(info.getSeed(), info.data.terrainOptions, info.data.terrainVersion);
                return terrain;
            }
        }
        return null;
    }

    public boolean uiDraw(MemoryStack stack) {
        return false;
    }

    public abstract boolean menusAreOpen();

    public abstract boolean uiMouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset);

    public abstract boolean keyEvent(int key, int scancode, int action, int mods);

    public abstract boolean uiMouseButtonEvent(int button, int action, int mods);

}
