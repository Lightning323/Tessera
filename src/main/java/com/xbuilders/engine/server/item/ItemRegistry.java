/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.item;

import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.block.BlockArrayTexture;
import com.xbuilders.engine.server.entity.EntitySupplier;
import com.xbuilders.utils.IntMap;
import com.xbuilders.utils.resource.PathHandler;

import java.util.HashMap;
import java.util.List;

import static com.xbuilders.Main.LOGGER;

/**
 * @author zipCoder933
 */
public class ItemRegistry {


    public final HashMap<String, Item> idMap = new HashMap<>();
    private Item[] list;

    public Item[] getList() {
        return list;
    }

    public Item getItem(String id) {
        return idMap.get(id);
    }

    public Item getItemFromTag(String tag) {
        for (Item item : list) {
            if (item.getTags().contains(tag)) {
                return item;
            }
        }
        return null;
    }

    public ItemRegistry() {
    }


    private void assignMapAndVerify(List<Item> inputItems) {
        System.out.println("\nChecking item IDs");
        idMap.clear();
        for (int i = 0; i < inputItems.size(); i++) {
            Item item = inputItems.get(i);
            if (item == null) {
                System.err.println("item at index " + i + " is null");
                continue;
            }
            if (idMap.containsKey(item.id)) {
                System.err.println("Item ID \"" + item.id + "\" already taken");
            } else idMap.put(item.id, item);
        }
    }

    public void setup(int defaultIcon,
                      BlockArrayTexture textures,
                      IntMap<Block> blockMap,
                      HashMap<String, EntitySupplier> entityMap,
                      HashMap<String, Short> blockAliasToIDMap,
                      HashMap<String, Short> entityAliasToIDMap,
                      List<Item> inputBlocks) {
        list = inputBlocks.toArray(new Item[0]);
        assignMapAndVerify(inputBlocks);

        //Initialize all items
        for (Item item : getList()) {
            try {
                if (item.initializationCallback != null) {
                    item.initializationCallback.accept(item);
                }
                item.init(blockMap, entityMap, blockAliasToIDMap, entityAliasToIDMap,
                        textures, PathHandler.BLOCK_ICON_DIR, defaultIcon);
            } catch (Exception e) {
                LOGGER.info( "An error occured setting up item \"" + item.toString() + "\"", e);
            }
        }
    }
}
