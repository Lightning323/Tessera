package com.tessera.engine.server.recipes.crafting;

import com.tessera.engine.server.item.Item;
import com.tessera.engine.client.visuals.RecipeDisplay;
import com.tessera.engine.server.recipes.Recipe;
import com.tessera.engine.server.recipes.AllRecipes;
import com.tessera.engine.server.recipes.TagPossibilities;
import com.tessera.content.vanilla.ui.RecipeDrawingUtils;
import org.lwjgl.nuklear.NkContext;

import java.util.*;

public class CraftingRecipe extends Recipe {

    /**
     * There are a few ways to define a crafting recipe input:
     * <p>
     * #tag             Specific to a tag
     * tessera:item   Specific to an item
     */

    public boolean shapeless = false;
    public String[] input;
    public String output;
    public int amount = 1;


    public CraftingRecipe() {
    }

    public CraftingRecipe(CraftingRecipe recipe) {
        this.input = new String[recipe.input.length];
        this.output = recipe.output;
        this.amount = recipe.amount;
        this.shapeless = recipe.shapeless;
        System.arraycopy(recipe.input, 0, this.input, 0, recipe.input.length);
    }

    public CraftingRecipe(String a, String b, String c,
                          String d, String e, String f,
                          String g, String h, String i,
                          String output, int amount) {
        shapeless = false;
        this.input = new String[9];
        this.input[0] = a;
        this.input[1] = b;
        this.input[2] = c;
        this.input[3] = d;
        this.input[4] = e;
        this.input[5] = f;
        this.input[6] = g;
        this.input[7] = h;
        this.input[8] = i;

        this.output = output;
        this.amount = amount;
    }

    public CraftingRecipe(boolean shapeless, String[] input, String output, int amount) {
        this.shapeless = shapeless;
        this.input = input;
        this.output = output;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CraftingRecipe that)) return false;
        return Objects.deepEquals(input, that.input);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(input);
    }

    public String toString() {
        return Arrays.toString(input) + " -> " + output;
    }

    @Override
    public void drawRecipe(NkContext ctx, int groupHeight) {
        RecipeDrawingUtils.drawRecipe(ctx, this, groupHeight);
    }


    @Override
    public RecipeDisplay getDisplayRecipe() {
        //Make the tag possibilities from ALL inputs
        TagPossibilities tagPossibilities = new TagPossibilities(input);
        HashSet<String> exploredTags = new HashSet<>();

        //Make the formatted recipe
        RecipeDisplay formattedRecipe = new RecipeDisplay();

        if (tagPossibilities.isEmpty()) {
            System.out.println("No tags in formatted recipe");
            formattedRecipe.add(new CraftingRecipe(this));
            return formattedRecipe;
        }

        //If the input does have tags
        //20 possible combinations at the most!
        for (int x = 0; x < 30; x++) {
            System.out.println("recipe " + x);
            CraftingRecipe recipe = new CraftingRecipe(this); //Make a copy of Our crafting recipe
            exploredTags.clear();

            //Fill ALL the inputs that have tags with the first possible input
            for (int i = 0; i < recipe.input.length; i++) {
                if (AllRecipes.isTag(recipe.input[i])) {  //If the input is a tag
                    String tag = recipe.input[i];
                    List<Item> possibleInputs = tagPossibilities.get(tag); //Set the element to the first possible input
                    exploredTags.add(tag); //Add the tag to the explored list so we can remove the first element

                    if (possibleInputs.isEmpty()) { //If we can't find any possible inputs, return what we have so far
                        System.out.println("Could not find any more possible inputs for tag: " + recipe.input[i]);
                        return formattedRecipe;
                    }
                    recipe.input[i] = possibleInputs.get(0).id; //Get the first element
                }
            }
            formattedRecipe.add(recipe); //Add the recipe to the formatted recipe list
            tagPossibilities.removeElementOfTags(exploredTags, 0);  //Remove the first element from all explored tags so we dont use them again
        }
        return formattedRecipe;
    }


}
