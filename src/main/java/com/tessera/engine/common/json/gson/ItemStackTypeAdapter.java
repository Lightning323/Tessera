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
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;

import java.lang.reflect.Type;

/**
 * Note that this is for saving and loading items as a JSON file, it is not used for item ids
 */
public class ItemStackTypeAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", src.item.id);
        jsonObject.addProperty("stackSize", src.stackSize);
        jsonObject.addProperty("durability", src.durability);
        if (src.nbtData != null) jsonObject.addProperty("data", new String(src.nbtData)); //We add serialized NBT data

        return jsonObject;
    }

    /**
     * This should be called AFTER registering all items
     *
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws JsonParseException
     */
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String itemID = jsonObject.get("item").getAsString();
        Item item = Registrys.items.getItem(itemID); //We get the item from the registry

        ItemStack itemStack = new ItemStack(item); //We create the ItemStack
        itemStack.stackSize = jsonObject.get("stackSize").getAsByte();

        if (jsonObject.has("durability"))
            itemStack.durability = jsonObject.get("durability").getAsInt();

        if (jsonObject.has("data")) {
            itemStack.nbtData = jsonObject.get("durability").getAsString().getBytes();
        }

        return itemStack;
    }
}
