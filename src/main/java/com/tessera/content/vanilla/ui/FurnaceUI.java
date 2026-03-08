package com.tessera.content.vanilla.ui;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemStackGrid;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.engine.server.recipes.AllRecipes;
import com.tessera.engine.server.recipes.smelting.SmeltingRecipe;
import com.tessera.window.NKWindow;
import org.joml.Vector3f;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.tessera.Main.LOGGER;
import static org.lwjgl.nuklear.Nuklear.*;

public class FurnaceUI extends ContainerUI {
    UI_ItemStackGrid inputGrid, fuelGrid, playerGrid, outputGrid;
    FurnaceData furnaceData;

    public FurnaceUI(NkContext ctx, NKWindow window) {
        super(ctx, window, "Smelting");
        menuDimensions.y = 500;
        inputGrid = new UI_ItemStackGrid(window, "Input", new StorageSpace(1), this, true);
        fuelGrid = new UI_ItemStackGrid(window, "Fuel", new StorageSpace(1), this, true);
        outputGrid = new UI_ItemStackGrid(window, "Output", new StorageSpace(1), this, true);
        outputGrid.showButtons = false;
        fuelGrid.showButtons = false;
        inputGrid.showButtons = false;

        outputGrid.itemFilter = (stack) -> false;
        fuelGrid.itemFilter = (stack) -> stack.item.tags.contains("flammable");

        //Change events when items are dragged
        inputGrid.dragFromEvent = (item, indx, rightClick) -> {
            writeDataToWorld();
        };
        inputGrid.dragToEvent = (item, indx, rightClick) -> {
            smeltWhenReady();
            writeDataToWorld();
        };

        fuelGrid.dragFromEvent = (item, indx, rightClick) -> {
            writeDataToWorld();
        };

        fuelGrid.dragToEvent = (item, indx, rightClick) -> {
            smeltWhenReady();
            writeDataToWorld();
        };

        outputGrid.dragFromEvent = (item, indx, rightClick) -> {
            writeDataToWorld();
        };


        playerGrid = new UI_ItemStackGrid(window, "Player", Client.userPlayer.inventory, this, true);
    }

    private void smeltWhenReady() {
        if (furnaceData.lastSmeltTime == 0) {
            furnaceData.lastSmeltTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - furnaceData.lastSmeltTime > getSmeltTime()) {
            if (!smelt()) furnaceData.lastSmeltTime = 0;
        }
    }


    private boolean smelt() {
        try {
            ItemStack input = inputGrid.storageSpace.get(0);
            ItemStack fuel = fuelGrid.storageSpace.get(0);

            if (input == null || input.stackSize == 0) return false; //Nothing to smelt


            SmeltingRecipe recipe = AllRecipes.smeltingRecipes.getFromInput(input.item.id);
            if (recipe == null) return false; //No recipe
            Item outputItem = Registrys.getItem(recipe.output);
            if (outputItem == null) return false; //No recipe

            //System.out.println("Smelting: " + input.item.name + " -> " + outputItem.name);


            //Reduce fuel first
            furnaceData.fuel -= FUEL_CONSUMPTION;
            if (furnaceData.fuel <= 0 && fuel != null) {
                if (fuelGrid.storageSpace.get(0).stackSize > 0) {
                    furnaceData.fuel = 1;
                    fuelGrid.storageSpace.get(0).stackSize--;
                }
            }

            //If no fuel, return
            if (furnaceData.fuel <= 0) return false;


            ItemStack outputStack = outputGrid.storageSpace.get(0);
            if (outputStack == null) {
                ItemStack output = new ItemStack(outputItem, recipe.amount);
                outputGrid.storageSpace.set(0, output);
            } else if (outputStack.item.id.equals(outputItem.id)) {
                outputGrid.storageSpace.get(0).stackSize += recipe.amount;
            } else { //Wrong item
                return false;
            } //We already have an item in the output grid and its not the one we want

            //Reduce input
            input.stackSize--;
            if (input.stackSize <= 0) {
                inputGrid.storageSpace.set(0, null);
            }
            furnaceData.lastSmeltTime = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            LOGGER.info("Error saving container data", e);
            return false;
        }
    }

    private long getSmeltTime() {
        if (Client.DEV_MODE) {
            return DEV_SMELT_TIME_MS;
        } else {
            return SMELT_TIME_MS;
        }
    }

    final float FUEL_CONSUMPTION = 0.3f;
    final long DEV_SMELT_TIME_MS = 1000;
    final long SMELT_TIME_MS = 8000;

    @Override
    public void drawWindow(MemoryStack stack, NkRect windowDims2) {
        super.drawWindow(stack, windowDims2);
        nk_layout_row_dynamic(ctx, 100, 3);
        inputGrid.draw(stack, ctx, 1);
        fuelGrid.draw(stack, ctx, 1);
        outputGrid.draw(stack, ctx, 1);


        nk_layout_row_dynamic(ctx, 15, 1);
        nk_label(ctx, "Fuel: " + (furnaceData.fuel * 100) + "%", NK_TEXT_ALIGN_LEFT);

        nk_layout_row_dynamic(ctx, 20, 1);
        if (furnaceData.lastSmeltTime > 0) {
            nk_prog(ctx, System.currentTimeMillis() - furnaceData.lastSmeltTime, getSmeltTime(), false);
            smeltWhenReady();
        } else nk_prog(ctx, 0, getSmeltTime(), false);

        nk_layout_row_dynamic(ctx, 250, 1);
        playerGrid.draw(stack, ctx, maxColumns);
    }

    @Override
    public void dropAllStorage(int x, int y, int z) {
        Main.getServer().placeItemDrop(new Vector3f(x, y, z), inputGrid.storageSpace.get(0), false);
        Main.getServer().placeItemDrop(new Vector3f(x, y, z), fuelGrid.storageSpace.get(0), false);
        Main.getServer().placeItemDrop(new Vector3f(x, y, z), outputGrid.storageSpace.get(0), false);
    }

    @Override
    public void readContainerData(byte[] bytes) {
        inputGrid.storageSpace.clear();
        fuelGrid.storageSpace.clear();
        outputGrid.storageSpace.clear();

        if (bytes != null) {
            try {
                furnaceData = StorageSpace.binaryJsonMapper.readValue(bytes, FurnaceData.class);
                System.out.println("Loaded furnace data: " + furnaceData);

                //Set the storage spaces
                inputGrid.storageSpace.set(0, furnaceData.inputGrid);
                fuelGrid.storageSpace.set(0, furnaceData.fuelGrid);
                outputGrid.storageSpace.set(0, furnaceData.outputGrid);

            } catch (IOException e) {
                LOGGER.info("Error reading container data", e);
            }
        } else {
            furnaceData = new FurnaceData();
        }
    }

    @Override
    public byte[] writeContainerData() {
        try {
            furnaceData.inputGrid = inputGrid.storageSpace.get(0);
            furnaceData.fuelGrid = fuelGrid.storageSpace.get(0);
            furnaceData.outputGrid = outputGrid.storageSpace.get(0);
            System.out.println("Serializing furnace data: " + furnaceData);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StorageSpace.binaryJsonMapper.writeValue(baos, furnaceData);

            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.info("Error saving container data", e);
            return new byte[0];
        }
    }

    public void onOpenEvent() {
        if (furnaceData == null) furnaceData = new FurnaceData();
        furnaceData.fuel = MathUtils.clamp(furnaceData.fuel, 0, 1);

        //Smelt items that should have been smelted while the UI was closed
        if (furnaceData.lastSmeltTime == 0) return;
        long msSinceClosed = (System.currentTimeMillis() - furnaceData.lastSmeltTime);


        int rounds = (int) ((float) msSinceClosed / getSmeltTime());

        //We cant smelt more than MAX_STACK_SIZE times
        rounds = (int) Math.min(rounds, ItemStack.MAX_STACK_SIZE + 10);


        System.out.println("Smelting " + rounds + " times; Time since closed: " + (msSinceClosed / 1000) + "s");
        for (int i = 0; i < rounds; i++) {
            smelt();
        }
    }
}
