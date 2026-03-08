package com.tessera.engine.server.recipes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tessera.engine.client.visuals.RecipeDisplay;
import org.lwjgl.nuklear.NkContext;

public abstract class Recipe {
    public abstract void drawRecipe(NkContext ctx, int groupHeight);

    //since this method starts with "get", @JsonIgnore is used to prevent it from being serialized
    @JsonIgnore
    public abstract RecipeDisplay getDisplayRecipe();
}
