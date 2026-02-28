/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.json.gson;

/**
 * @author zipCoder933
 */

import com.google.gson.*;
import com.tessera.engine.server.item.Item;

import java.lang.reflect.Type;

/**
 * Note that this is for saving and loading items as a JSON file, it is not used for item ids
 */
public class ItemTypeAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {

    @Override
    public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", src.id);
        jsonObject.addProperty("name", src.name);
        if (src.getBlock() != null) {
            jsonObject.addProperty("block", src.getBlock().id);
        }
        if (src.getEntity() != null) {
            jsonObject.addProperty("entity", src.getEntity().id);
        }
        jsonObject.addProperty("icon", src.iconFilename);
        jsonObject.addProperty("maxDurability", src.maxDurability);
        jsonObject.addProperty("maxStackSize", src.maxStackSize);
        jsonObject.addProperty("foodAdd", src.foodAdd);

        //Add tags as a property
        JsonArray tags = new JsonArray();
        for (String tag : src.getTags()) {
            tags.add(tag);
        }
        jsonObject.add("tags", tags);

        return jsonObject;
    }

    /**
     * This should be called AFTER blocks and allEntities are loaded
     *
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws JsonParseException
     */
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String id = jsonObject.get("id").getAsString();
        String name = jsonObject.get("name").getAsString();

        Item item = new Item(id, name);

        if (jsonObject.has("block")) {

            JsonElement blockJson = jsonObject.get("block");
            if (blockJson != null && !blockJson.isJsonNull()) {
                if (blockJson.isJsonPrimitive()) {
                    JsonPrimitive primitive = blockJson.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        //System.out.println("The value is a String: " + primitive.getAsString());
                        item.setBlock(primitive.getAsString());
                    } else if (primitive.isNumber()) {
                        //System.out.println("The value is a Number: " + primitive.getAsNumber());
                        short blockID = primitive.getAsNumber().shortValue();
                        item.setBlock(blockID);
                    }

                }
            }

        }
        if(jsonObject.has("maxDurability")) {
            item.maxDurability = jsonObject.get("maxDurability").getAsInt();
        }
        if(jsonObject.has("maxStackSize")) {
            item.maxStackSize = jsonObject.get("maxStackSize").getAsByte();
        }
        if (jsonObject.has("entity")) {
            String entityID = jsonObject.get("entity").getAsString();
            item.setEntity(entityID);
        }
        if (jsonObject.has("icon"))
            item.iconFilename = jsonObject.get("icon").getAsString();

        if (jsonObject.has("foodAdd"))
            item.foodAdd = jsonObject.get("foodAdd").getAsFloat();


        //Load tags
        if (jsonObject.has("tags")) {
            JsonArray tags = jsonObject.get("tags").getAsJsonArray();
            for (JsonElement tag : tags) {
                item.tags.add(tag.getAsString());
            }
        }


        return item;
    }
}
