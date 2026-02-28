package com.tessera.engine.server.recipes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tessera.engine.client.visuals.RecipeDisplay;
import com.tessera.engine.server.item.Item;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.utils.resource.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class RecipeRegistry<T extends Recipe> {
    public final String name;
    public final List<T> recipeList = new ArrayList<>();
    private final TypeReference<List<T>> typeReference;

    public RecipeRegistry(String name, TypeReference typeReference) {
        this.name = name;
        this.typeReference = typeReference;
    }

    public void add(T recipe) {
        this.recipeList.add(recipe);
    }

    public void remove(T recipe) {
        this.recipeList.remove(recipe);
    }

    public void clear() {
        recipeList.clear();
    }


    public abstract ArrayList<T> getFromOutput(Item output);

    @JsonIgnore
    public ArrayList<RecipeDisplay> getDisplayRecipesFromOutput(Item output) {
        ArrayList<T> recipes = getFromOutput(output);
        ArrayList<RecipeDisplay> formattedRecipes = new ArrayList<>();
        for (T recipe : recipes) {
            formattedRecipes.add(recipe.getDisplayRecipe());
        }
        return formattedRecipes;
    }

    public boolean hasRecipeWithOutput(Item output) {
        return getFromOutput(output) != null;
    }



    public void writeToFile(File file) throws IOException {
        System.out.println("Writing " + recipeList.size() + " " + name + " recipes to " + file);
        String json = objectMapper.writeValueAsString(recipeList);

        Files.writeString(file.toPath(), json);
    }

    public final ObjectMapper objectMapper = new ObjectMapper();
   public static ResourceLoader resourceLoader = new ResourceLoader();

    public final void register(String resourcePath) throws IOException {
        System.out.println("Registering " + name + " recipes...");
        for (String path : ResourceLister.listSubResources(resourcePath)) {
            registerFromResource(path);
        }
    }

    public void registerFromResource(String path) throws IOException {
        String json = new String(resourceLoader.readResource(path));
        System.out.println("Loading " + name + " recipes from " + path);
        List<T> loadedRecipes = objectMapper.readValue(json, typeReference);
        this.recipeList.addAll(loadedRecipes);
    }

    public String toString() {
        return name + " recipes";// (x" + recipeList.size()+")";
    }

}
