/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.ui;

import com.tessera.Main;
import com.tessera.engine.client.visuals.RecipeDisplay;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemIndex;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemWindow;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.recipes.AllRecipes;
import com.tessera.engine.server.recipes.RecipeRegistry;
import com.tessera.window.NKWindow;
import com.tessera.window.WindowEvents;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * @author zipCoder933
 */
public class UI_RecipeIndex extends UI_ItemWindow implements WindowEvents {

    public static final int KEY_OPEN_RECIPE_INDEX = GLFW.GLFW_KEY_R;

    Item selectedItem;
    RecipeRegistry selectedRecipeClass;
    HashMap<RecipeRegistry, ArrayList<RecipeDisplay>> availableRecipes = new HashMap<>();

    public UI_RecipeIndex(NkContext ctx, Item[] itemList, NKWindow window) {
        super(ctx, window, "Recipe List");

        allItems = new UI_ItemIndex(this);
        allItems.setItemList(itemList);
        allItems.itemClickEvent = this::selectItem;

        menuDimensions.y = Allitems_Height + recipeView_Height + 100;

        nk_begin(ctx, title, NkRect.create(), windowFlags);
        nk_end(ctx);
        setOpen(false);
    }

    private void selectItem(Item item) {
        System.out.println("\nSelected item: " + item.name);
        availableRecipes.clear();
        selectedItem = item;
        for (RecipeRegistry registry : AllRecipes.allRecipeLists) {
            ArrayList<RecipeDisplay> recipes = registry.getDisplayRecipesFromOutput(item);
            if (recipes.isEmpty()) continue;
            availableRecipes.put(registry, recipes);
        }


        if (!availableRecipes.isEmpty()) {
            //Get the first key and assign it to the recipe
            Map.Entry<RecipeRegistry, ArrayList<RecipeDisplay>> entry = availableRecipes.entrySet().iterator().next();
            selectRecipeClass(entry.getKey());
        }

        System.out.println("Available recipes: ");
        availableRecipes.forEach((entry, value) ->
                {
                    System.out.println("\t" + entry.name + ":");
                    for (RecipeDisplay recipeDisplay : value) {
                        System.out.println("\t\t" + recipeDisplay.toString());
                    }
                    System.out.println();
                }
        );
    }

    final int Allitems_Height = 200; //total item list window size
    final int recipeView_Height = 240; //player inventory window size
    UI_ItemIndex allItems;


    public void onOpenEvent() {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) setOpen(false);
    }

    public void onCloseEvent() {
    }

    @Override
    public void drawWindow(MemoryStack stack, NkRect windowDims2) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) {
            setOpen(false);
        }

        nk_layout_row_dynamic(ctx, recipeView_Height, 1);
        if (nk_group_begin(ctx, "Recipe view", NK_WINDOW_BORDER)) {
            if (selectedItem != null && !availableRecipes.isEmpty()) {
                nk_layout_row_dynamic(ctx, 20, availableRecipes.size());


                /**
                 * Draw recipe classes (Crafting, smelting)
                 */
                for (RecipeRegistry list : availableRecipes.keySet()) {
                    if (selectedRecipeClass != null && list == selectedRecipeClass) {
                        ctx.style().button().normal().data().color().set(Theme.color_lightGray);
                        ctx.style().button().border_color().set(Theme.color_white);
                    } else Theme.resetEntireButtonStyle(ctx);

                    if (nk_button_text(ctx, list.name)) {
                        selectRecipeClass(list);
                    }
                }
                Theme.resetEntireButtonStyle(ctx);

                /**
                 * Draw visible recipes
                 */
                if (selectedRecipeClass != null) {
                    //todo: THIS CAUSES A CRASH
                    //TODO: having a group in the recipe causes a crash if the height of the pane is too great
                    //j  org.lwjgl.nuklear.Nuklear.nnk_group_end(J)V+0
                    //j  org.lwjgl.nuklear.Nuklear.nk_group_end(Lorg/lwjgl/nuklear/NkContext;)V+4
                    availableRecipes.get(selectedRecipeClass).forEach((recipe) -> {
                        //  Nuklear.nk_layout_row_dynamic(ctx, 400,  1);//This will cause an immediate crash if there is a group in the recipe
                        // Nuklear.nk_text(ctx, "Test", Nuklear.NK_TEXT_ALIGN_LEFT);
                        recipe.drawRecipe(ctx, recipeView_Height - 50);
                    });
                }
            }
        }
        nk_group_end(ctx);


        allItems.draw(ctx, stack, Allitems_Height);

        Theme.resetEntireButtonStyle(ctx);
    }

    private void selectRecipeClass(RecipeRegistry list) {
        selectedRecipeClass = list;
        System.out.println("Switching to " + list.name);
    }


    @Override
    public void windowResizeEvent(int width, int height) {

    }

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;

        if (allItems.keyEvent(key, scancode, action, mods)) return true;
        if (action == GLFW.GLFW_RELEASE && key == KEY_OPEN_RECIPE_INDEX) {
            setOpen(!isOpen());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;
        allItems.mouseScrollEvent(scroll, xoffset, yoffset);
        return true;
    }

}
