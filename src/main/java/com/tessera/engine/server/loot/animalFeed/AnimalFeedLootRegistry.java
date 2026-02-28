package com.tessera.engine.server.loot.animalFeed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tessera.engine.server.loot.LootTableRegistry;
import com.tessera.engine.server.loot.output.LootList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static com.tessera.engine.server.loot.AllLootTables.lootMapper;


public class AnimalFeedLootRegistry extends LootTableRegistry {
    public ArrayList<AnimalFeedLoot> list;

    protected static final TypeReference<ArrayList<AnimalFeedLoot>> typeReference = new TypeReference<ArrayList<AnimalFeedLoot>>() {
    };

    public AnimalFeedLootRegistry() {
        super("animalFeed");
        list = new ArrayList<>();
    }

    public LootList getLoot(String animalId, String foodId) {
        for (int i = 0; i < list.size(); i++) {
            AnimalFeedLoot entry = list.get(i);
            if (entry.animalID.equals(animalId) && entry.foodID.equals(foodId)) {
                return entry.output;
            }
        }
        return new LootList();
    }

//    public void loadFromFile(File json) throws IOException {
//        String jsonString = Files.readString(json.toPath());
//        if (jsonString.isBlank()) return;
//        list.addAll(lootMapper.readValue(jsonString, typeReference));
//        System.out.println("Loaded " + list.size() + " loot tables from " + json.getAbsolutePath());
//    }


    public void registerFromResource(String path) throws IOException {
        String json = new String(resourceLoader.readResource(path));
        System.out.println("Loading " + name + " loot from " + path);
        ArrayList<AnimalFeedLoot> loadedRecipes = lootMapper.readValue(json, typeReference);
        list.addAll(loadedRecipes);
    }


    public static void writeToFile(ArrayList<AnimalFeedLoot> table, File json) throws IOException {
        String jsonString = lootMapper.writeValueAsString(table);
        Files.writeString(json.toPath(), jsonString);
    }
}
