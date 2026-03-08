package com.tessera.engine.server.loot.block;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tessera.Main;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.loot.LootTableRegistry;
import com.tessera.engine.server.loot.output.LootList;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static com.tessera.engine.server.loot.AllLootTables.lootMapper;

public class BlockLootRegistry extends LootTableRegistry {
    HashMap<String, LootList> list;

    protected static final TypeReference<HashMap<String, LootList>> typeReference = new TypeReference<HashMap<String, LootList>>() {
    };

    public BlockLootRegistry() {
        super("block");
        list = new HashMap<>();
    }

//    public void loadFromFile(File json) throws IOException {
//        String jsonString = Files.readString(json.toPath());
//        if (jsonString.isBlank()) return;
//        putAll(lootMapper.readValue(jsonString, typeReference));
//        System.out.println("Loaded " + size() + " loot tables from " + json.getAbsolutePath());
//    }

    public void registerFromResource(String path) throws IOException {
        String json = new String(resourceLoader.readResource(path));
        System.out.println("Loading " + name + " loot from " + path);
        HashMap<String, LootList> loadedRecipes = lootMapper.readValue(json, typeReference);
        list.putAll(loadedRecipes);
    }

    public void writeToFile(HashMap<String, LootList> table, File json) throws IOException {
        String jsonString = lootMapper.writeValueAsString(table);
        Files.writeString(json.toPath(), jsonString);
    }

    public LootList getLoot(String blockId) {
        return list.get(blockId);
    }

    public void dropLoot(String blockId, Vector3f pos) {
        dropLoot(blockId, pos, false, true);
    }

    public void dropLoot(String blockId, Vector3f pos, boolean droppedFromPlayer, boolean cancelUnlessInAdventureMode) {
        if (cancelUnlessInAdventureMode && Main.getServer().getGameMode() != GameMode.ADVENTURE) return;

        LootList lootList = getLoot(blockId);
        if (lootList != null) {
            lootList.randomItems((itemStack) -> {
                Main.getServer().placeItemDrop(pos, itemStack, droppedFromPlayer);
            });
        }
    }
}
