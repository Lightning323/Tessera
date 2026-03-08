package com.tessera.content.vanilla.ui;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.recipes.crafting.CraftingRecipe;
import com.tessera.engine.server.recipes.smelting.SmeltingRecipe;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemGrid;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class RecipeDrawingUtils {
    public static UI_ItemGrid viewInputGrid, viewOutputGrid;
    private static int titleIndex = 0;

    static {
        viewInputGrid = new UI_ItemGrid("Grid", false);
        viewOutputGrid = new UI_ItemGrid("Grid", false);
    }

    public static void drawRecipe(NkContext ctx, CraftingRecipe recipe, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            nk_layout_row_dynamic(ctx, 170, 3);

            Item item = Registrys.getItem(recipe.output);
            viewInputGrid.items.clear();
            viewOutputGrid.items.clear();

            for (int i = 0; i < recipe.input.length; i++) {
                viewInputGrid.items.add(Registrys.getItem(recipe.input[i]));
            }
            viewOutputGrid.items.add(item);
            viewInputGrid.draw(stack, ctx, 3);

            viewOutputGrid.draw(stack, ctx, 1);

            //TODO: Somehow group.end causes a crash
            Nuklear.nk_label(ctx, "output: X" + recipe.amount, Nuklear.NK_TEXT_ALIGN_LEFT);
        }
    }

    public static void drawRecipe(NkContext ctx, SmeltingRecipe recipe, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            nk_layout_row_dynamic(ctx, 70, 3);

            viewInputGrid.items.clear();
            viewInputGrid.items.add(Registrys.getItem(recipe.input));
            viewInputGrid.draw(stack, ctx, 1);

            viewInputGrid.items.clear();
            viewInputGrid.items.add(Registrys.getItem(recipe.output));
            viewInputGrid.draw(stack, ctx, 1);

            Nuklear.nk_label(ctx, "output: X" + recipe.amount, Nuklear.NK_TEXT_ALIGN_LEFT);


        }
    }

}
