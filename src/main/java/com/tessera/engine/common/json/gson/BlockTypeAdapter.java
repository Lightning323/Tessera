/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.json.gson;

/**
 * @author zipCoder933
 */

import com.google.gson.*;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.common.json.JsonManager;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class BlockTypeAdapter implements JsonSerializer<Block>, JsonDeserializer<Block> {

    @Override
    public JsonElement serialize(Block src, Type typeOfSrc, JsonSerializationContext context) {
        // System.out.println("Using custom typeReference adapter");
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", src.id);
        jsonObject.addProperty("alias", src.alias);
        jsonObject.add("texture", JsonManager.textureAdapter.serialize(src.texture, typeOfSrc, context));
        jsonObject.addProperty("solid", src.solid);
        jsonObject.addProperty("opaque", src.opaque);
        jsonObject.addProperty("torch", src.torchlightStartingValue);
        jsonObject.addProperty("type", src.type);
        jsonObject.addProperty("toughness", src.toughness);
        jsonObject.addProperty("climbable", src.climbable);
        if (src.colorInPlayerHead != null) {
            JsonElement colorElement = context.serialize(src.colorInPlayerHead);
            jsonObject.add("colorInPlayerHead", colorElement);
        }
        if (src.toolsThatCanMine_tags != null) {
            JsonArray toolsThatCanMine_tags = new JsonArray();
            for (String tag : src.toolsThatCanMine_tags) {
                toolsThatCanMine_tags.add(new JsonPrimitive(tag));
            }
            jsonObject.add("toolsThatCanMine", toolsThatCanMine_tags);
        }
        if (src.easierMiningTool_tag != null) {
            jsonObject.addProperty("easierMiningTool", src.easierMiningTool_tag);
        }

        // Export properties (hashmap)
        JsonElement propertiesElement = context.serialize(src.properties);
        jsonObject.add("properties", propertiesElement);

        return jsonObject;
    }

    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        //Basic properties
        short id = jsonObject.get("id").getAsShort();
        String name = jsonObject.get("alias").getAsString();
        JsonObject textureProperty = jsonObject.get("texture").getAsJsonObject();
        BlockTexture texture = JsonManager.textureAdapter.deserialize(textureProperty, BlockTexture.class, context);
        int renderType = 0;

        if (jsonObject.has("type")) {
            String typeStr = jsonObject.get("type").getAsString();
            if (typeStr == null || isInteger(typeStr)) {// If the typeReference is an integer
                renderType = jsonObject.get("type").getAsInt();
            } else { // Otherwise it's a string
                renderType = Registrys.blocks.getBlockType(typeStr);
            }
        }
        Block block = new Block(id, name, texture, renderType);

        if (jsonObject.has("solid"))
            block.solid = jsonObject.get("solid").getAsBoolean();
        if (jsonObject.has("opaque"))
            block.opaque = jsonObject.get("opaque").getAsBoolean();
        if (jsonObject.has("torch"))
            block.torchlightStartingValue = jsonObject.get("torch").getAsByte();
        if (jsonObject.has("toughness"))
            block.toughness = jsonObject.get("toughness").getAsFloat();
        if (jsonObject.has("climbable"))
            block.climbable = jsonObject.get("climbable").getAsBoolean();

        // Import tools that can mine
        if (jsonObject.has("toolsThatCanMine")) {
            JsonArray toolsThatCanMine_tags = jsonObject.get("toolsThatCanMine").getAsJsonArray();
            block.toolsThatCanMine_tags = new String[toolsThatCanMine_tags.size()];
            for (int i = 0; i < toolsThatCanMine_tags.size(); i++) {
                block.toolsThatCanMine_tags[i] = toolsThatCanMine_tags.get(i).getAsString();
            }
        }
        if (jsonObject.has("easierMiningTool")) {
            block.easierMiningTool_tag = jsonObject.get("easierMiningTool").getAsString();
        }

        if (jsonObject.has("colorInPlayerHead")) {
            JsonElement colorElement = jsonObject.get("colorInPlayerHead");
            float[] jsonColor = context.deserialize(colorElement, float[].class);
            block.colorInPlayerHead[0] = jsonColor[0];
            block.colorInPlayerHead[1] = jsonColor[1];
            block.colorInPlayerHead[2] = jsonColor[2];
            if (jsonColor.length >= 4) block.colorInPlayerHead[3] = jsonColor[3];
            else block.colorInPlayerHead[3] = 1.0f;
        }

        // Import properties (hashmap)
        if (jsonObject.has("properties")) {
            JsonObject propertiesObject = jsonObject.get("properties").getAsJsonObject();
            //Convert it to hashmap
            Set<Map.Entry<String, JsonElement>> entries = propertiesObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                // System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().getAsString());
                block.properties.put(entry.getKey(), entry.getValue().getAsString());
            }
            // System.out.println(" Properties: " + block.properties);
        }


        return block;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
