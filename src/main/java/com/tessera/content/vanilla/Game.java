/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla;

import com.tessera.Main;
import com.tessera.content.vanilla.blockTools.BlockTools;
import com.tessera.content.vanilla.blocks.RenderType;
import com.tessera.content.vanilla.blocks.type.*;
import com.tessera.content.vanilla.propagation.LavaPropagation;
import com.tessera.content.vanilla.propagation.WaterPropagation;
import com.tessera.content.vanilla.terrain.BasicTerrain;
import com.tessera.content.vanilla.terrain.DevTerrain;
import com.tessera.content.vanilla.terrain.FlatTerrain;
import com.tessera.content.vanilla.terrain.defaultTerrain.DefaultTerrain;
import com.tessera.content.vanilla.ui.*;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.client.visuals.gameScene.GameUI;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Server;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.loot.AllLootTables;
import com.tessera.engine.server.recipes.AllRecipes;
import com.tessera.engine.common.world.WorldData;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tessera.engine.client.visuals.gameScene.GameUI.printKeyConsumption;

/**
 * @author zipCoder933
 */
public class Game extends com.tessera.engine.server.Game {


    public Game() {
    }

    public HashMap<String, String> getCommandHelp() {
        HashMap<String, String> commandHelp = new HashMap<>();
        return commandHelp;
    }


    public boolean drawCursor(CursorRay cursorRay) {
        if (Main.getServer().getGameMode() != GameMode.FREEPLAY) return false;
        return blockTools.getSelectedTool().drawCursor(cursorRay, GameScene.projection, GameScene.view);
    }

    public boolean clickEvent(final CursorRay ray, boolean isCreationMode) {
        return blockTools.clickEvent(ray, isCreationMode);
    }


    public boolean releaseMouse() {
        return blockTools.releaseMouse();
    }


    //Builtin menus

    public BlockTools blockTools;


    //Custom menus
    public BarrelUI barrelUI;
    public CraftingUI craftingUI;
    public FurnaceUI smeltingUI;
    public UI_Inventory inventoryUI;
    public UI_RecipeIndex recipeIndexUI;

    public GameMenus gameMenus = new GameMenus();


    @Override
    public boolean uiDraw(MemoryStack stack) {
        if (gameMenus.draw(stack)) {
            return true;
        } else {
            blockTools.draw(stack);
        }
        return false;
    }


    @Override
    public boolean uiMouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (gameMenus.mouseScrollEvent(scroll, xoffset, yoffset)) {
            return true;
        } else if (blockTools.mouseScrollEvent(scroll, xoffset, yoffset)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        //Menus
        if (gameMenus.keyEvent(key, scancode, action, mods)) {
            return true;
        } else if (blockTools.keyEvent(key, scancode, action, mods)) {
            printKeyConsumption(blockTools.getClass());
            return true;
        } else {
            //Wake keys
            if (action == GLFW.GLFW_RELEASE && key == UI_RecipeIndex.KEY_OPEN_RECIPE_INDEX) {
                recipeIndexUI.setOpen(!recipeIndexUI.isOpen());
                return true;
            } else if (action == GLFW.GLFW_RELEASE && key == UI_Inventory.KEY_OPEN_INVENTORY) {
                inventoryUI.setOpen(!inventoryUI.isOpen());
                return true;
            }
        }
        return false;
    }

    public void gameModeChangedEvent(GameMode gameMode) {
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
        blockTools.selectDefaultTool();
    }

    @Override
    public boolean uiMouseButtonEvent(int button, int action, int mods) {
        if (gameMenus.mouseButtonEvent(button, action, mods)) {
            return true;
        } else if (blockTools.UIMouseButtonEvent(button, action, mods)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean menusAreOpen() {
        return blockTools.isOpen() || gameMenus.isOpen();
    }

    WorldData currentWorld;

    @Override
    public void startGameEvent(WorldData worldInfo) {
        this.currentWorld = worldInfo;
    }

    @Override
    public void stopGameEvent() {
    }


    @Override
    public void setupClient(ClientWindow window, NkContext ctx, GameUI gameUI) throws Exception {


        //Add block types FIRST. We need them to be able to setup blocks properly
        Registrys.blocks.addBlockType("sprite", RenderType.SPRITE, new SpriteRenderer());
        Registrys.blocks.addBlockType("floor", RenderType.FLOOR, new FloorItemRenderer());
        Registrys.blocks.addBlockType("orientable", RenderType.ORIENTABLE_BLOCK, new OrientableBlockRenderer());
        Registrys.blocks.addBlockType("slab", RenderType.SLAB, new SlabRenderer());
        Registrys.blocks.addBlockType("stairs", RenderType.STAIRS, new StairsRenderer());
        Registrys.blocks.addBlockType("fence", RenderType.FENCE, new FenceRenderer("/assets/tessera/models/block/fence"));
        Registrys.blocks.addBlockType("wall", RenderType.WALL_ITEM, new WallItemRenderer());
        Registrys.blocks.addBlockType("lamp", RenderType.LAMP, new LampRenderer());
        Registrys.blocks.addBlockType("pane", RenderType.PANE, new PaneRenderer());
        Registrys.blocks.addBlockType("track", RenderType.RAISED_TRACK, new RaisedTrackRenderer());
        Registrys.blocks.addBlockType("torch", RenderType.TORCH, new TorchRenderer());
        Registrys.blocks.addBlockType("pillar", RenderType.PILLAR, new PillarRenderer());
        Registrys.blocks.addBlockType("trapdoor", RenderType.TRAPDOOR, new TrapdoorRenderer());
        Registrys.blocks.addBlockType("fence gate", RenderType.FENCE_GATE, new FenceGateRenderer());
        Registrys.blocks.addBlockType("door half", RenderType.DOOR_HALF, new DoorHalfRenderer());


        System.out.println("Initializing items...");
        ArrayList<Item> itemList = Items.startup_getItems();

        System.out.println("Initializing blocks...");
        ArrayList<Block> blockList = Blocks.starup_getBlocks();

        System.out.println("Initializing allEntities...");
        ArrayList<EntitySupplier> entityList = Entities.startup_getEntities(window);


        Registrys.initialize(blockList, entityList, itemList);

        //Load Loot
        AllLootTables.blockLootTables.register("/data/tessera/loot/block");
        AllLootTables.animalFeedLootTables.register("/data/tessera/loot/animalFeed");

        //Load Recipes
        AllRecipes.craftingRecipes.register("/data/tessera/recipes/crafting");
        AllRecipes.smeltingRecipes.register("/data/tessera/recipes/smelting");


        //Add terrains;
        terrainsList.add(new DefaultTerrain());
        terrainsList.add(new FlatTerrain());
        terrainsList.add(new BasicTerrain());
        if (Client.DEV_MODE) terrainsList.add(new DevTerrain());


        //Menus
        System.out.println("Initializing menus...");
        barrelUI = new BarrelUI(ctx, window);
        craftingUI = new CraftingUI(ctx, window);
        smeltingUI = new FurnaceUI(ctx, window);
        recipeIndexUI = new UI_RecipeIndex(ctx, Registrys.items.getList(), window);
        inventoryUI = new UI_Inventory(ctx, Registrys.items.getList(), window, GameUI.hotbar);
        blockTools = new BlockTools(ctx, window, Client.userPlayer.camera.cursorRay);
        gameMenus.menus.add(barrelUI);
        gameMenus.menus.add(craftingUI);
        gameMenus.menus.add(smeltingUI);
        gameMenus.menus.add(recipeIndexUI);
        gameMenus.menus.add(inventoryUI);


        //blocks after everything else is done
        Blocks.editBlocks(window);
        Items.editItems(window);
    }

    @Override
    public void setupServer(Server server) {
        //propagations
        server.livePropagationHandler.addTask(new WaterPropagation());
        server.livePropagationHandler.addTask(new LavaPropagation());
    }


}
