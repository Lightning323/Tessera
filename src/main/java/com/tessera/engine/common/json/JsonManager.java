/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.json;


import com.fasterxml.jackson.core.JsonGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.json.gson.*;
import com.tessera.engine.common.option.BoundedFloat;
import com.tessera.engine.common.option.BoundedInt;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;

/**
 * @author zipCoder933
 */
public class JsonManager {

    @FunctionalInterface
    public interface SimpleJsonGenerator {
        public void write(JsonGenerator generator);
    }

    public static final String SMILE_HEADER = ":)\n";
    public static final BlockTextureTypeAdapter textureAdapter = new BlockTextureTypeAdapter();

    /**
     * One GSON instance for all classes, this helps simplify serialization
     */
    public static Gson gson_classes_adapter = new GsonBuilder()
            .registerTypeHierarchyAdapter(Item.class, new ItemTypeAdapter())
            .registerTypeHierarchyAdapter(BoundedFloat.class, new BoundedFloatTypeAdapter())
            .registerTypeHierarchyAdapter(BoundedInt.class, new BoundedIntTypeAdapter())
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(BlockTexture.class, textureAdapter)
            .registerTypeAdapter(BlockTexture.class, new OptionTypeAdapter())
            .registerTypeAdapter(OptionsList.class, new OptionsListTypeAdapter())
            .setPrettyPrinting()
            .create();


    public static String printSmileData(byte[] data) {
        return (data.length + " bytes\t " + new String(data).replaceAll("\n", ""));
    }
}