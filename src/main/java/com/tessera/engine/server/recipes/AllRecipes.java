package com.tessera.engine.server.recipes;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.recipes.crafting.CraftingRecipeRegistry;
import com.tessera.engine.server.recipes.smelting.SmeltingRecipeRegistry;
import com.tessera.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class AllRecipes {

    public final static String TAG_PREFIX = "#";

    public static final CraftingRecipeRegistry craftingRecipes = new CraftingRecipeRegistry();
    public static final SmeltingRecipeRegistry smeltingRecipes = new SmeltingRecipeRegistry();

    public static final RecipeRegistry[] allRecipeLists = new RecipeRegistry[]{
            craftingRecipes, smeltingRecipes};

//    public static void init() {
//        craftingRecipes.init();
//        smeltingRecipes.init();
//    }

    public static boolean isTag(String input) {
        return input != null && input.startsWith(TAG_PREFIX);
    }

    public static List<Item> getMatchingItems(String recipe) {
        ArrayList<Item> items = new ArrayList<>();
        if (recipe == null) return items;

        if (isTag(recipe)) {
            String A_tag = recipe.substring(1);
            for (Item item : Registrys.items.getList()) {
                if (item != null && item.tags.contains(A_tag)) {
                    items.add(item);
                }
            }
        } else items.add(Registrys.getItem(recipe));

        return items;
    }

    public static boolean elementMatches(String recipe, String item) {
        //if any input doesn't match
        if (isTag(recipe)) {
            String A_tag = recipe.substring(1);
            Item B_item = Registrys.items.getItem(item);
            if (B_item == null) return false;
            if (!B_item.getTags().contains(A_tag)) {
                return false;
            }
        } else return MiscUtils.equalOrNull(recipe, item);
        return true;
    }


}
