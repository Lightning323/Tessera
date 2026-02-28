/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.xbuilders.utils.ArrayUtils.combineArrays;

/**
 * @author zipCoder933
 */
public class EntityRegistry {

    //Predefined allEntities
    public static EntitySupplier ENTITY_ITEM_DROP;

    final HashMap<String, EntitySupplier> idMap = new HashMap<>();
    public HashMap<String, Short> aliasToIDMap;
    private EntitySupplier[] list;
    public List<EntitySupplier> autonomousList;
    public static final int NULL_ID = -1;

    public EntityRegistry() {
    }


    public EntitySupplier[] getList() {
        return list;
    }

    public EntitySupplier getItem(String id) {
        return idMap.get(id);
    }

    public HashMap<String, EntitySupplier> getIdMap() {
        return idMap;
    }

    public void setup(List<EntitySupplier> inputEntities) {
        HashSet<String> uniqueAliases = new HashSet<>();
        ENTITY_ITEM_DROP = new EntitySupplier("xbuilders:item_drop", (uniqueID2) -> new ItemDrop(uniqueID2));
        inputEntities.add(ENTITY_ITEM_DROP);

        verifyEntityIds(inputEntities);
        list = inputEntities.toArray(new EntitySupplier[0]);
        //Now make a entities of autonomous allEntities
        autonomousList = new java.util.ArrayList<>();
        for (EntitySupplier entity : list) {
            if (entity.isAutonomous) {
                autonomousList.add(entity);
            }
        }
    }

    private void verifyEntityIds(List<EntitySupplier> inputItems) {
        System.out.println("\nChecking entity IDs");
        idMap.clear();
        for (int i = 0; i < inputItems.size(); i++) {
            if (inputItems.get(i) == null) {
                System.err.println("item at index " + i + " is null");
                continue;
            }
            String id = inputItems.get(i).get(1).getId();
            if (idMap.get(id) != null) {
                System.err.println("Entity " + inputItems.get(i) + " ID conflicts with an existing ID: " + id);
            }
            idMap.put(id, inputItems.get(i));
        }
    }


}