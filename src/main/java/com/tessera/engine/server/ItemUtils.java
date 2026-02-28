package com.tessera.engine.server;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.item.Item;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.utils.resource.ResourceLoader;
import com.tessera.engine.common.json.JsonManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tessera.Main.LOGGER;

public class ItemUtils {

    static ResourceLoader resourceLoader = new ResourceLoader();

    public static ArrayList<Block> getJsonBlocksFromResource(String jsonDirectory) {
        System.out.println("Adding all json blocks from " + jsonDirectory);
        ArrayList<Block> allItems = new ArrayList<>();
        try {
            for (String path : ResourceLister.listSubResources(jsonDirectory)) {
                String name = resourceLoader.getName(path);
                if (!name.endsWith(".json")) continue;
                if (!Client.DEV_MODE && name.contains("devmode")) continue;
                System.out.println("\t" + name);
                String jsonString = new String(resourceLoader.getResourceBytes(path));
                Block[] jsonBlocks2 = JsonManager.gson_classes_adapter.fromJson(jsonString, Block[].class);
                if (jsonBlocks2 != null) {
                    allItems.addAll(Arrays.asList(jsonBlocks2));  // append to list
                }
            }
            return allItems;
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
        return new ArrayList<>();
    }


    public static ArrayList<Item> getJsonItemsFromResource(String jsonDirectory) {
        System.out.println("Adding all json items from " + jsonDirectory);
        ArrayList<Item> allItems = new ArrayList<>();
        try {
            for (String path : ResourceLister.listSubResources(jsonDirectory)) {

                String name = resourceLoader.getName(path);
                if (!name.endsWith(".json")) continue;
                if (!Client.DEV_MODE && name.contains("devmode")) continue;

                String jsonString = new String(resourceLoader.getResourceBytes(path));
                Item[] jsonBlocks2 = JsonManager.gson_classes_adapter.fromJson(jsonString, Item[].class);
                if (jsonBlocks2 != null) {
                    allItems.addAll(Arrays.asList(jsonBlocks2));  // append to list
                }
            }
            return allItems;
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
        return new ArrayList<>();
    }

    public static ArrayList<Block> getJsonBlocksFromFile(File jsonDirectory) {
        System.out.println("Adding all json blocks from " + jsonDirectory.getAbsolutePath());
        if (!jsonDirectory.exists()) jsonDirectory.mkdirs();
        ArrayList<Block> allBlocks = new ArrayList<>();
        try {
            for (File file : jsonDirectory.listFiles()) {

                if (!file.getName().endsWith(".json")) continue;
                if (!Client.DEV_MODE && file.getName().contains("devmode")) continue;

                String jsonString = Files.readString(file.toPath());
                Block[] jsonBlocks2 = JsonManager.gson_classes_adapter.fromJson(jsonString, Block[].class);
/*
//DANGER! This code will Change the JSON and write it back
//                System.out.println("Read " + jsonBlocks2.length + " blocks from " + file.getName());
//                for(Block block : jsonBlocks2) {
//                    block.alias = BlockRegistry.formatAlias(block.alias);
//                }
//                //Write it back to json
//                String json = JsonManager.gson_blockAdapter.toJson(jsonBlocks2);
//                Files.writeString(file.toPath(), json);
 */
                if (jsonBlocks2 != null && jsonBlocks2.length > 0) {
                    // append to list
                    for (Block block : jsonBlocks2) {
                        allBlocks.add(block);
                    }
                }
            }

            return allBlocks;
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
        return new ArrayList<>();
    }

    public static ArrayList<Item> getJsonItemsFromFile(File jsonDirectory) {
        System.out.println("Adding all json items from " + jsonDirectory.getAbsolutePath());
        if (!jsonDirectory.exists()) jsonDirectory.mkdirs();
        ArrayList<Item> allItems = new ArrayList<>();
        try {
            for (File file : jsonDirectory.listFiles()) {
                if (!file.getName().endsWith(".json")) continue;
                if (!Client.DEV_MODE && file.getName().contains("devmode")) continue;
                System.out.println("\t" + file.getName());
                String jsonString = Files.readString(file.toPath());
                Item[] jsonBlocks2 = JsonManager.gson_classes_adapter.fromJson(jsonString, Item[].class);
                if (jsonBlocks2 != null && jsonBlocks2.length > 0) {
                    // append to list
                    for (Item block : jsonBlocks2) {
                        allItems.add(block);
                    }
                }
            }
            return allItems;
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
        return new ArrayList<>();
    }

    private static String nameToJavaName(String prefix, String name) {
        name = name.replaceFirst("tessera:", "").trim().toUpperCase();
        if (!name.startsWith(prefix.toUpperCase())) name = prefix + "_" + name;
        return name.toUpperCase()
                .replaceAll("hidden", "")
                .replaceAll("[^A-Z0-9_]", "")
                .replaceAll("\\s+", "_");
    }

    public static void block_makeClassJavaFiles(ArrayList<Block> blocks, File directory) throws IOException {
        StringBuilder blockClasses = new StringBuilder();
        StringBuilder blockIDs = new StringBuilder();
        for (Block block : blocks) {
            if (block == null) {
                System.err.println("A block is null");
                continue;
            }


            blockClasses.append("public static Block " +
                            nameToJavaName("block", block.alias) + " = ItemList.getBlock((short)")
                    .append(block.id).append(");").append("\n");
            blockIDs.append("public static short " +
                    nameToJavaName("block", block.alias) + " = ").append(block.id).append(";").append("\n");
        }
        Files.writeString(new File(directory, "BlockClasses.java").toPath(), blockClasses.toString());
        Files.writeString(new File(directory, "BlockIDs.java").toPath(), blockIDs.toString());
    }


    public static void exportBlocksToJson(List<Block> list, File out) {
        //Save list as json
        try {
            String jsonString = JsonManager.gson_classes_adapter.toJson(list);
            Files.writeString(out.toPath(), jsonString);
            System.out.println("Saved " + list.size() + " blocks to " + out.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void item_makeClassJavaFiles(ArrayList<Item> items, File directory) throws IOException {
//        StringBuilder itemClasses = new StringBuilder();
//        for (Item item : items) {
//            String entry = "public static final Item " + nameToJavaName("item", item.name) +
//                    " = new Item(\"" + item.id + "\", \"" + item.name + "\");\n";
//            if (item.getBlock() != null) {
//
//            }
//            itemClasses.append(entry);
//        }
//        itemClasses.append("""
//
//                    public static void initItems(
//                            IntMap<Block> blocks,
//                            IntMap<EntitySupplier> allEntities) {
//                """);
//        for (Item item : items) {
//            if (item.getBlock() != null) {
//                itemClasses.append(nameToJavaName("item", item.name) + ".block = blocks.get(" + item.getBlock().id + ");\n");
//            } else if (item.getEntity() != null) {
//                itemClasses.append(nameToJavaName("item", item.name) + ".entity = allEntities.get(" + item.getEntity().id + ");\n");
//            }
//        }
//
//        itemClasses.append("\n\n}");
//        Files.writeString(new File(directory, "ItemClasses.java").toPath(), itemClasses.toString());
//    }


}
