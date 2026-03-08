/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.topMenu;

import com.tessera.engine.common.world.Terrain;

import java.util.ArrayList;

import com.tessera.engine.common.option.NuklearField;
import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.nk_button_label;

/**
 * @author zipCoder933
 */
public class TerrainSelector {

    public TerrainSelector(ArrayList<Terrain> terrainList, NkContext ctx) {
        this.terrainList = terrainList;
        selectedTerrain = 0;
        initSelectedTerrain();
        this.ctx = ctx;
    }

    private NkContext ctx;
    private ArrayList<Terrain> terrainList;
    private int selectedTerrain;
    public ArrayList<NuklearField> optionFields = new ArrayList<>();

    public Terrain getSelectedTerrain() {
        return terrainList.get(selectedTerrain);
    }

    public void reset() {
        selectedTerrain = 0;
    }

    public void draw() {
        if (NewWorld.labeledButton(ctx, "Terrain:", terrainList.get(selectedTerrain).name)) {
            selectedTerrain++;
            if (selectedTerrain >= terrainList.size()) {
                selectedTerrain = 0;
            }
            initSelectedTerrain();
        }
    }

    private void initSelectedTerrain() {
        Terrain terrain = terrainList.get(selectedTerrain);
        optionFields = terrain.options_resetAndGetNKOptionList();
    }
}
