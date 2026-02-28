package com.tessera.engine.server.recipes.crafting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.recipes.RecipeRegistry;
import com.tessera.engine.server.recipes.AllRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CraftingRecipeRegistry extends RecipeRegistry<CraftingRecipe> {

    public CraftingRecipeRegistry() {
        super("Crafting", new TypeReference<List<CraftingRecipe>>() {
        });
    }

    static {
//        SimpleModule module = new SimpleModule();
//        module.addDeserializer(CraftingRecipe.class, new CraftingRecipeDeserializer());
//        module.addSerializer(CraftingRecipe.class, new CraftingRecipeSerializer());
//        smileJsonMapper.registerModule(module);
    }

    public ArrayList<CraftingRecipe> getFromOutput(Item outputID) {
        ArrayList<CraftingRecipe> recipes = new ArrayList<>();
        for (CraftingRecipe recipe : recipeList) {//Iterate over all recipes
            if (recipe.output.equals(outputID.id)) {
                recipes.add(recipe); //Add the recipe to the recipes
            }
        }
        System.out.println(name + " recipes for " + outputID + ": " + recipes.size());
        return recipes;
    }

    public CraftingRecipe getFromInput(String[] input) {
        //Check for shaped recipes first
        for (CraftingRecipe recipe : recipeList) {
            if (!recipe.shapeless && inputMatchesShaped(recipe.input, input)) return recipe;
        }
        //Shapeless comes next
        for (CraftingRecipe recipe : recipeList) {
            if (recipe.shapeless && inputMatchesShapeless(recipe.input, input)) return recipe;
        }
        return null;
    }

    private boolean inputMatchesShapeless(String[] recipe, String[] itemIDs) {
        String[] itemIDsCopy = itemIDs.clone(); //We create a copy of the array and set already matched items to null

        for (int i = 0; i < recipe.length; i++) {
            String rItem = recipe[i];
            boolean foundPair = false;
            for (int j = 0; j < itemIDsCopy.length; j++) {
                String item = itemIDsCopy[j];
                if (item != null && AllRecipes.elementMatches(rItem, item)) {
                    foundPair = true;
                    itemIDsCopy[j] = null;
                    break;
                }
            }
            if (!foundPair) return false;
        }

        //If we have items left over, we can't match
        int nonNullElementsLeft = 0;
        for (int i = 0; i < itemIDsCopy.length; i++) {
            if (itemIDsCopy[i] != null) nonNullElementsLeft++;
        }
        if (nonNullElementsLeft > 0) return false;
        return true;
    }


    public static boolean inputMatchesShaped(String[] recipe, String[] itemIDs) {
        if (Objects.deepEquals(itemIDs, recipe)) { //If the inputs are the same
            return true;
        } else {
            for (int i = 0; i < recipe.length; i++) {
                String rItem = recipe[i];
                String item = itemIDs[i];
                if (!AllRecipes.elementMatches(rItem, item)) return false;
            }
            return true;
        }
    }

}
