package com.tessera.content.vanilla.ui;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.engine.server.recipes.AllRecipes;
import com.tessera.engine.server.recipes.crafting.CraftingRecipe;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemStackGrid;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemWindow;
import com.tessera.window.NKWindow;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

public class CraftingUI_Base {
    public UI_ItemStackGrid inputGrid, outputGrid;

    public StorageSpace playerStorage;
    private CraftingRecipe recipe;
    UI_ItemWindow itemWindow;
    NkContext ctx;

    public CraftingUI_Base(NkContext ctx, NKWindow window, UI_ItemWindow itemWindow, StorageSpace playerStorage, int inputSize) {
        this.ctx = ctx;
        this.itemWindow = itemWindow;
        this.playerStorage = playerStorage;


        inputGrid = new UI_ItemStackGrid(window, "Grid", new StorageSpace(inputSize), itemWindow, true);
        outputGrid = new UI_ItemStackGrid(window, "Output", new StorageSpace(1), itemWindow, true);
        outputGrid.showButtons = false;
        outputGrid.itemFilter = (stack) -> false;

        inputGrid.dragFromEvent = (stack, index, rightClick) -> {
            System.out.println("dragFromEvent stack: " + stack + " index: " + index + " rightClick: " + rightClick);
        };

        inputGrid.dragToEvent = (stack, index, rightClick) -> {
            System.out.println("dragToEvent stack: " + stack + " index: " + index + " rightClick: " + rightClick);
        };

        /**
         * When we change the input grid
         * We set the output based on what is in the input
         */
        inputGrid.storageSpace.changeEvent = () -> {
            inputGrid.storageSpace.getList();
            String[] recipeInput = new String[9];
            for (int i = 0; i < inputGrid.storageSpace.size(); i++) {
                recipeInput[i] = inputGrid.storageSpace.get(i) == null ? null : inputGrid.storageSpace.get(i).item.id;
            }
            //Get the recipe from the input
            recipe = AllRecipes.craftingRecipes.getFromInput(recipeInput);
            if (recipe != null && recipe.output != null) {
                //Calculate how many of the output we can craft
                int output_quantity = Integer.MAX_VALUE;
                for (int i = 0; i < inputGrid.storageSpace.size(); i++) {
                    if (inputGrid.storageSpace.get(i) != null) {
                        output_quantity = Math.min(output_quantity, inputGrid.storageSpace.get(i).stackSize);
                    }
                }
                if (output_quantity == Integer.MAX_VALUE || output_quantity == 0) output_quantity = 1;
                System.out.println("output_quantity: " + output_quantity);

                //Get the output item
                Item item = Registrys.getItem(recipe.output);
                if (item == null) {
                    System.err.println("Recipe output not found: " + recipe.output);
                    return;
                }
                outputGrid.storageSpace.set(0, new ItemStack(item, recipe.amount * output_quantity));
            } else outputGrid.storageSpace.set(0, null);
        };

        /**
         * When we take something from the output grid
         */
        outputGrid.dragFromEvent = (takingStack, index, rightClick) -> {
            //We always take the entire stack
            int takeAmt = takingStack.stackSize / recipe.amount;
            //Remove the items from the crafting grid
            for (int i = 0; i < inputGrid.storageSpace.size(); i++) {
                if (inputGrid.storageSpace.get(i) != null) {
                    inputGrid.storageSpace.get(i).stackSize -= takeAmt;
                    if(inputGrid.storageSpace.get(i).stackSize == 0) inputGrid.storageSpace.set(i, null);
                }
            }
            //Run the change event
            inputGrid.storageSpace.changeEvent.run();
        };
        inputGrid.showButtons = false;
    }

    public void draw(MemoryStack stack, int inputColumns) {
        nk_layout_row_dynamic(ctx, 200, 2);
        inputGrid.draw(stack, ctx, inputColumns);
        outputGrid.draw(stack, ctx, 1);
    }


    public void onCloseEvent() {
        //Give the items back to the player
        for (int i = 0; i < inputGrid.storageSpace.size(); i++) {
            if (inputGrid.storageSpace.get(i) != null) {
                playerStorage.acquireItem(inputGrid.storageSpace.get(i));
            }
            inputGrid.storageSpace.set(i, null);
        }
    }
}
