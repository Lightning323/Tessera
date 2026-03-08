package com.tessera.content.vanilla;

import com.tessera.Main;
import com.tessera.content.vanilla.items.*;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.builtinMechanics.liquid.LiquidPropagationTask;
import com.tessera.engine.server.ItemUtils;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.entity.LivingEntity;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

public class Items {

    public static final Item TOOL_ANIMAL_FEED = new AnimalFeed();

    public static Item getBlockWithSharedTexture(
            Predicate<Block> customPredicate, Item original, int... validVariantTypes) {
        Block originalBlock = original.getBlock();
        if (originalBlock == null) return null;
        //Get the block variant
        for (Item i : Registrys.items.getList()) {
            if (i.getBlock() != null
                    && i.getBlock().texture.equals(originalBlock.texture)) {
                for (int rt : validVariantTypes) {
                    if (i.getBlock().type == rt && customPredicate.test(i.getBlock())) {
                        return i;
                    }
                }
            }
        }
        return null;
    }

    public static Item getBlockWithSharedName(
            Predicate<Block> customPredicate,
            Item originalItem, String[] invalidIdMatches, int... validBlockTypes) {
        //Get the block variant
        for (Item i : Registrys.items.getList()) {

            if (i.getBlock() == null) continue;
            String thisId = originalItem.id;
            String blockId = i.getBlock().alias;

            String commonWorld = getCommonWord(thisId, blockId, "_");
            if (commonWorld.isEmpty()) continue;
            boolean foundInvalidMatch = false;
            for (String invalidMatch : invalidIdMatches) {
                if (commonWorld.equalsIgnoreCase(invalidMatch)) {
                    foundInvalidMatch = true;
                    break;
                }
            }
            if (foundInvalidMatch) continue;

//            System.out.println("this: " + thisId + " \t other: " + blockId + " \t common: " + commonWorld);
            for (int rt : validBlockTypes) {
                if (i.getBlock().type == rt && customPredicate.test(i.getBlock())) {
                    return i;
                }
            }

        }
        return null;
    }

    public static String getCommonWord(String s1, String s2, String delimiter) {
        String[] words1 = s1.split(delimiter);
        String[] words2 = s2.split(delimiter);
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    return word1;
                }
            }
        }
        return "";
    }

    public static ArrayList<Item> startup_getItems() {
        /**
         * Tools
         */
        ArrayList<Item> itemList = new ArrayList<>();
        itemList.add(new Shovel("wooden", 25, 0.7f));
        itemList.add(new Pickaxe("wooden", 25, 0.7f));
        itemList.add(new Axe("wooden", 25, 0.7f));
        itemList.add(new Sword("wooden", 25, 0.5f));
        itemList.add(new Hoe("wooden", 5));

        itemList.add(new Shovel("stone", 100, 1f));
        itemList.add(new Pickaxe("stone", 100, 1.8f));
        itemList.add(new Axe("stone", 100, 1f));
        itemList.add(new Sword("stone", 100, 1f));
        itemList.add(new Hoe("stone", 15));

        itemList.add(new Shovel("iron", 200, 1f));
        itemList.add(new Pickaxe("iron", 200, 2f));
        itemList.add(new Axe("iron", 200, 2f));
        itemList.add(new Sword("iron", 200, 5f));
        itemList.add(new Hoe("iron", 200));

        itemList.add(new Shovel("golden", 400, 2f));
        itemList.add(new Pickaxe("golden", 400, 3f));
        itemList.add(new Axe("golden", 400, 3f));
        itemList.add(new Sword("golden", 400, 10f));
        itemList.add(new Hoe("golden", 1200));

        itemList.add(new Shovel("diamond", 4000, 6f));
        itemList.add(new Pickaxe("diamond", 4000, 6f));
        itemList.add(new Axe("diamond", 4000, 6f));
        itemList.add(new Sword("diamond", 4000, 20f));
        itemList.add(new Hoe("diamond", 8000));

        itemList.add(new FlintAndSteel());

        itemList.add(new Swatter(500));
        itemList.add(new EntityRemovalTool((e) -> e instanceof LivingEntity,
                "tessera:animal_removal_tool",
                "Animal Removal Tool"));

        itemList.add(new EntityRemovalTool((e) -> true,
                "tessera:entity_removal_tool",
                "Entity Removal Tool"));

        itemList.add(new Saddle());

        itemList.add(new Flashlight());
        itemList.add(new Camera());
        itemList.add(TOOL_ANIMAL_FEED);

        /**
         * Json Items
         */
        ItemUtils.getJsonItemsFromResource("data/tessera/items").forEach(itemList::add);

        return itemList;
    }


    public static void editItems(ClientWindow window) {

        Item item = Registrys.getItem("tessera:bucket");
        if (item != null) {
            item.createClickEvent = (ray, stack) -> {
                fillBucket(ray, stack);
                return true;
            };
            item.destroyClickEvent = (ray, stack) -> {
                fillBucket(ray, stack);
                return true;
            };
        }
        item = Registrys.getItem("tessera:water_bucket");
        if (item != null) {
            item.createClickEvent = (ray, stack) -> {
                emptyBucket(ray, stack);
                return true;
            };
            item.destroyClickEvent = (ray, stack) -> {
                emptyBucket(ray, stack);
                return true;
            };
        }
        item = Registrys.getItem("tessera:lava_bucket");
        if (item != null) {
            item.createClickEvent = (ray, stack) -> {
                emptyBucket(ray, stack);
                return true;
            };
            item.destroyClickEvent = (ray, stack) -> {
                emptyBucket(ray, stack);
                return true;
            };
        }
        item = Registrys.getItem("tessera:animal_apple");
        if (item != null) {
            AnimalFood.makeAnimalFood(item, null);
            item.foodAdd = 0.2f;
        }

    }


    private static void fillBucket(CursorRay ray, ItemStack stack) {
        int x = ray.getHitPos().x;
        int y = ray.getHitPos().y;
        int z = ray.getHitPos().z;

        Block hitPos = Client.world.getBlock(x, y, z);
        System.out.println("Hit: " + hitPos);
        if (hitPos.isLiquid()) {
            int flow = LiquidPropagationTask.getFlow(Client.world.getBlockData(x, y, z), 0);
            if (flow >= hitPos.liquidMaxFlow + 1) {
                Main.getServer().setBlock(Blocks.BLOCK_AIR, null, x, y, z);

                if (hitPos.id == Blocks.BLOCK_WATER) {
                    stack.item = Objects.requireNonNull(Registrys.getItem("tessera:water_bucket"));
                } else if (hitPos.id == Blocks.BLOCK_LAVA) {
                    stack.item = Objects.requireNonNull(Registrys.getItem("tessera:lava_bucket"));
                }
            }
        }
    }

    private static void emptyBucket(CursorRay ray, ItemStack stack) {
        int x = ray.getHitPos().x;
        int y = ray.getHitPos().y;
        int z = ray.getHitPos().z;
        if (!Client.world.getBlock(x, y, z).getType().replaceOnSet) {
            x = ray.getHitPosPlusNormal().x;
            y = ray.getHitPosPlusNormal().y;
            z = ray.getHitPosPlusNormal().z;
        }

        Main.getServer().setBlock(stack.item.getBlock().id, x, y, z);
        if (Main.getServer().getGameMode() == GameMode.ADVENTURE)
            stack.item = Objects.requireNonNull(Registrys.getItem("tessera:bucket"));
    }

}
