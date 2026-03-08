package com.tessera.content.vanilla.items;

import com.tessera.content.vanilla.Items;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.recipes.crafting.CraftingRecipe;
import com.tessera.engine.server.recipes.crafting.CraftingRecipeRegistry;
import com.tessera.utils.resource.PathHandler;
import com.tessera.content.vanilla.blocks.RenderType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Predicate;

public class Recipes {

    private void synthesizeBlockDyedRecipes(ArrayList<Item> itemList) throws IOException {
        System.out.println("Synthesizing block variants...");


        CraftingRecipeRegistry dyed = new CraftingRecipeRegistry();
        CraftingRecipeRegistry doorsTrapdoors = new CraftingRecipeRegistry();


        String[] colors = new String[]{
                "red",
                "orange",
                "yellow",
                "green",
                "lime",
                "cyan",
                "blue",
                "magenta",
                "purple",
                "gray",
                "light_gray",
                "brown",
                "black",
                "white",};

        for (Item item : itemList) {
            //If the item id has a color
            colorLoop:
            for (String color : colors) {
                if (item.id.contains(color)) {
                    String nonColorName = item.id
                            .replace(color, "")
                            .replace("tessera:", "").replace("_", " ").trim();
                    String dye = "tessera:" + color + "_dye";
                    Block block = item.getBlock();
                    if (block == null) continue;

                    //System.out.println("Synthesizing " + nonColorName + " " + color);

                    if (block.type == RenderType.BLOCK && block.alias.contains("glass")) {
                        String base = "tessera:glass";
                        dyed.add(new CraftingRecipe(
                                base, base, dye,
                                base, base, null,
                                null, null, null,
                                item.id, 4));
                    } else if (block.type == RenderType.BLOCK && nonColorName.contains("concrete")) {
                        String base = "tessera:concrete";
                        dyed.add(new CraftingRecipe(
                                base, base, dye,
                                base, base, null,
                                null, null, null,
                                item.id, 4));
                    } else if (block.type == RenderType.BLOCK && nonColorName.contains("wool")) {
                        String base = "tessera:wool";
                        dyed.add(new CraftingRecipe(
                                base, base, dye,
                                base, base, null,
                                null, null, null,
                                item.id, 4));
                    } else if (block.type == RenderType.BLOCK && nonColorName.contains("stained_wood")) {
                        String base = "#wood";
                        dyed.add(new CraftingRecipe(
                                base, base, dye,
                                base, base, null,
                                null, null, null,
                                item.id, 4));
                    } else if (block.type == RenderType.BLOCK && nonColorName.contains("siding")) {
                        String base = "tessera:tan_siding";
                        dyed.add(new CraftingRecipe(
                                base, base, dye,
                                base, base, null,
                                null, null, null,
                                item.id, 4));
                    }

                    break colorLoop;
                }
            }
        }
        dyed.writeToFile(PathHandler.resourcePath("items/recipes/crafting/dyed.json"));
        System.out.println("Done synthesizing");
    }

    private void synthesizeBlockVariantRecipes(ArrayList<Item> itemList) throws IOException {
        System.out.println("Synthesizing block variants...");


        CraftingRecipeRegistry variants = new CraftingRecipeRegistry();
        CraftingRecipeRegistry doorsTrapdoors = new CraftingRecipeRegistry();

        Predicate<Block> matchPredicate = (b) -> !b.alias.contains("log") && !b.alias.contains("leaves");

        for (Item item : itemList) {

            if (item.getBlock() != null) {
                Block block = item.getBlock();
                //MatchTexture
                Item baseBlock = Items.getBlockWithSharedTexture(
                        matchPredicate,
                        item, //item
                        BlockRegistry.DEFAULT_BLOCK_TYPE_ID, RenderType.ORIENTABLE_BLOCK);
                //Match name
                if (baseBlock == null) baseBlock = Items.getBlockWithSharedName(
                        matchPredicate,
                        item, //item
                        new String[]{"door", "trapdoor"}, //invalid matches
                        BlockRegistry.DEFAULT_BLOCK_TYPE_ID, RenderType.ORIENTABLE_BLOCK);

                if (baseBlock == null) continue;

                if (block.type == RenderType.SLAB) {
                    variants.add(new CraftingRecipe(
                            null, null, null,
                            null, null, null,
                            baseBlock.id, baseBlock.id, baseBlock.id,
                            item.id, 6));
                } else if (block.type == RenderType.STAIRS) {
                    variants.add(new CraftingRecipe(
                            null, null, baseBlock.id,
                            null, baseBlock.id, baseBlock.id,
                            baseBlock.id, baseBlock.id, baseBlock.id,
                            item.id, 6));
                } else if (block.type == RenderType.FENCE) {
                    variants.add(new CraftingRecipe(
                            null, null, null,
                            baseBlock.id, "tessera:stick", baseBlock.id,
                            baseBlock.id, "tessera:stick", baseBlock.id,
                            item.id, 10));
                } else if (block.type == RenderType.FENCE_GATE) {
                    variants.add(new CraftingRecipe(
                            null, null, null,
                            "tessera:stick", baseBlock.id, "tessera:stick",
                            "tessera:stick", baseBlock.id, "tessera:stick",
                            item.id, 4));
                } else if (block.type == RenderType.PILLAR) {
                    variants.add(new CraftingRecipe(
                            null, baseBlock.id, null,
                            null, baseBlock.id, null,
                            null, baseBlock.id, null,
                            item.id, 4));
                } else if (block.type == RenderType.DOOR_HALF) {
                    doorsTrapdoors.add(new CraftingRecipe(
                            baseBlock.id, baseBlock.id, null,
                            baseBlock.id, baseBlock.id, null,
                            baseBlock.id, baseBlock.id, null,
                            item.id, 3));
                } else if (block.type == RenderType.TRAPDOOR) {
                    doorsTrapdoors.add(new CraftingRecipe(
                            null, null, null,
                            baseBlock.id, baseBlock.id, baseBlock.id,
                            baseBlock.id, baseBlock.id, baseBlock.id,
                            item.id, 6));
                } else if (block.type == RenderType.PANE) {
                    variants.add(new CraftingRecipe(
                            baseBlock.id, null, baseBlock.id,
                            baseBlock.id, null, baseBlock.id,
                            baseBlock.id, null, baseBlock.id,
                            item.id, 12));
                }
            } else {
                Item baseBlock = Items.getBlockWithSharedName(
                        matchPredicate,
                        item,
                        new String[]{"door", "trapdoor"},
                        BlockRegistry.DEFAULT_BLOCK_TYPE_ID, RenderType.ORIENTABLE_BLOCK);
                if (baseBlock == null) continue;
                if (item.id.contains("boat")) {
                    System.out.println(item.id);
                    variants.add(new CraftingRecipe(
                            null, null, null,
                            baseBlock.id, null, baseBlock.id,
                            baseBlock.id, baseBlock.id, baseBlock.id,
                            item.id, 1));
                }
            }
        }
        System.out.println("Writing recipes...");
        variants.writeToFile(PathHandler.resourcePath("items/recipes/crafting/variants.json"));
        doorsTrapdoors.writeToFile(PathHandler.resourcePath("items/recipes/crafting/doors.json"));
        System.out.println("Done synthesizing");
    }

}
