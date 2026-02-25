package com.tessera.engine.client.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tessera.Main;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.player.camera.Camera;
import com.tessera.engine.client.visuals.gameScene.GameUI;
import com.tessera.engine.server.Difficulty;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.GameSceneEvents;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.engine.server.players.Player;
import com.tessera.engine.server.players.PositionLock;
import com.tessera.engine.server.world.World;
import com.tessera.engine.server.world.data.WorldData;
import com.tessera.engine.utils.ErrorHandler;
import com.tessera.engine.utils.json.gson.ItemStackTypeAdapter;
import com.tessera.engine.utils.math.MathUtils;
import com.tessera.engine.utils.worldInteraction.collision.PositionHandler;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkVec2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.tessera.content.vanilla.Blocks.BLOCK_AIR;
import static com.tessera.engine.client.visuals.gameScene.GameUI.printKeyConsumption;

public class UserControlledPlayer extends Player implements GameSceneEvents {


    public Camera camera;
    private final ClientWindow window;
    final Vector4f lastOrientation = new Vector4f();
    public boolean runningMode;
    Matrix4f projection;
    Matrix4f view;
    boolean isClimbing = false;
    public PositionHandler positionHandler;
    boolean usePositionHandler = true;

    public PositionLock positionLock;
    boolean autoForward = false;
    boolean autoJump_unCollided = true;
    float autoJump_ticksWhileColidingWithBlock = 0;

    //Health
    //TODO: Saturation is a status effect, meaning we have to implement effects before implementing saturation
    private boolean dieMode;
    public final static float MAX_HEALTH = 20f;
    public final static float MAX_FOOD = 20f;
    public final static float MAX_OXYGEN = 20f;

    public final static float IDLE_FOOD_DEPLETION = 0.00001f;
    public final static float MOVING_FOOD_DEPLETION = 0.0003f;
    public final static float RUNNING_FOOD_DEPLETION = 0.0007f;
    public final static float HEALTH_REGEN_SPEED = 0.0006f;


    private float status_health;
    private float status_food;
    private float status_oxygen;

    public boolean isDieMode() {
        return dieMode;
    }

    public boolean canMove() {
        return !isDieMode();
    }

    public float getOxygenLevel() {
        return status_oxygen;
    }

    public float getFoodLevel() {
        return status_food;
    }

    public float getHealth() {
        return status_health;
    }

    public void addHealth(float damage) {
        status_health += damage;
    }

    public void addFood(float hungerPoints) {
        status_food += hungerPoints;
    }


    private void updateHealthbars(Block playerHead, Block playerFeet, Block playerWaist) {
        if (Main.getServer().getGameMode() == GameMode.ADVENTURE) {

            float multiplier = 1;

            /**
             * Food can go higher than the maximum level, but it cant go lower than zero
             */
            if (status_food > 0) {
                //Scale hunger depletion based on difficulty
                float difficulty = 1;
                if (Main.getServer().getDifficulty() == Difficulty.EASY) difficulty = 0.5f;
                if (Main.getServer().getDifficulty() == Difficulty.HARD) difficulty = 2f;

                if (isRidingEntity()) { //Dont deplete hunger if we are riding something
                    status_food -= IDLE_FOOD_DEPLETION * difficulty * multiplier; //Baseline hunger deplation
                } else {
                    if (runningMode) status_food -= RUNNING_FOOD_DEPLETION * difficulty * multiplier; //Running
                    else if (forwardKeyPressed() || backwardKeyPressed())
                        status_food -= MOVING_FOOD_DEPLETION * difficulty * multiplier; //Walking
                    else status_food -= IDLE_FOOD_DEPLETION * difficulty * multiplier; //Baseline hunger deplation
                }
            }

            /**
             * Damage
             */
            float enterDamage = Math.max(Math.max(playerHead.enterDamage, playerFeet.enterDamage), playerWaist.enterDamage);
            if (enterDamage > 0) {
                status_health -= enterDamage;
            } else if (status_oxygen <= 0 || status_food <= 0) {
                status_health -= 0.2f * multiplier;
            } else if (status_health < MAX_HEALTH && status_food > 3) {//Regenerate
                status_health += HEALTH_REGEN_SPEED * multiplier;
            }

            /**
             * Oxygen
             */
            if (playerHead.isLiquid()) {
                status_oxygen -= 0.02f * multiplier;
            } else if (status_oxygen < MAX_OXYGEN) status_oxygen += 0.02f * multiplier;


            if (status_health <= 0) {
                die();
            }
            status_health = MathUtils.clamp(status_health, 0, MAX_HEALTH);
            status_oxygen = MathUtils.clamp(status_oxygen, 0, MAX_OXYGEN);
        }
    }

    private boolean isRidingEntity() {
        return positionLock != null;
    }

    private void resetHealthStats() {
        dieMode = false;
        status_health = MAX_HEALTH;
        status_food = MAX_FOOD;
        status_oxygen = MAX_OXYGEN;
    }

    public void die() {
        dismount();
        dieMode = true;
        setFlashlight(0);
        ClientWindow.popupMessage.message("Game Over!", "Press OK to teleport to spawnpoint", () -> {
            if (!inventory.isEmpty()) {
                //Make sure the flag is placed somewhere safe (where it wont displace a block)
                System.out.println("Placing flag...");
                Vector3f flagPos = findSuitablePlacement(worldPosition, 20, 20, (v) -> canSpawnHere(Client.world, v.x, v.y, v.z));
                Main.getServer().setBlock(Blocks.BLOCK_FLAG_BLOCK, (int) flagPos.x, (int) flagPos.y, (int) flagPos.z);
                Main.getClient().consoleOut("Flag placed at (" + (int) (flagPos.x) + ", " + (int) flagPos.y + ", " + (int) flagPos.z + ")");
            }
            System.out.println("Teleporting to spawnpoint... ("
                    + status_spawnPosition.x + ", " + status_spawnPosition.y + ", " + status_spawnPosition.z + ")");
            respawn(status_spawnPosition);
            resetHealthStats();
            dieMode = false;
        });
    }


    private void respawn(Vector3f target) {
        aabb.updateBox();
        Vector3f spawn = findSuitableSpawnPoint(Main.getServer().world, target);
        aabb.updateBox();
        teleport(spawn.x, spawn.y, spawn.z);
    }

    public static Vector3f findSuitableSpawnPoint(World world, Vector3f target) {
        //Come down from top of world
        int startY;
        for (startY = (int) target.y; startY < World.WORLD_BOTTOM_Y; startY++) {
            if (world.getBlock((int) target.x, startY, (int) target.z).solid) break;
        }

        Vector3f spawnPoint = UserControlledPlayer.findSuitablePlacement(
                new Vector3f(target.x, startY, target.z), 10, World.WORLD_SIZE_Y + 10,
                (v) -> {
                    return Client.userPlayer.canSpawnHere(world, (int) v.x, (int) v.y, (int) v.z);
                });
        return spawnPoint;
    }

    public boolean canSpawnHere(World world,
                                int playerWorldPosX,
                                int playerWorldPosY,
                                int playerWorldPosZ) {

        //We are looking at the player foot
        int playerFeetY = (int) (playerWorldPosY + PLAYER_HEIGHT);

        Block footBlock = world.getBlock(playerWorldPosX, playerFeetY, playerWorldPosZ);
        Block bodyBlock1 = world.getBlock(playerWorldPosX, playerFeetY - 1, playerWorldPosZ);
        Block bodyBlock2 = world.getBlock(playerWorldPosX, playerFeetY - 2, playerWorldPosZ);
        Block bodyBlock3 = world.getBlock(playerWorldPosX, playerFeetY - 3, playerWorldPosZ);

        return footBlock.solid //Ground is solid
                && !bodyBlock1.solid //The player can move
                && !bodyBlock2.solid
                && !bodyBlock3.solid
                //The ground and air is safe to stand in
                && footBlock.enterDamage < 0.01
                && bodyBlock1.enterDamage < 0.01
                && bodyBlock2.enterDamage < 0.01
                && bodyBlock3.enterDamage < 0.01;
    }


    public static Vector3f findSuitablePlacement(Vector3f target, int horizontalRadius, int verticalRadius, Predicate<Vector3i> test) {
        Vector3f newTarget = new Vector3f(target);
        if (
                !test.test(new Vector3i((int) target.x, (int) target.y, (int) target.z))
        ) {
            System.out.println("Cant spawn here (" + newTarget.x + ", " + newTarget.y + ", " + newTarget.z + "), Looking around");
            //Go around the spawn point and find a safe place to spawn

            HashSet<Vector3i> checked = new HashSet<>();
            HashSet<Vector3i> nodes = new HashSet<>();
            nodes.add(new Vector3i((int) target.x, (int) target.y, (int) target.z));

            while (!nodes.isEmpty()) {
                Vector3i node = nodes.iterator().next();
                nodes.remove(node);
                //If we have already checked this node or it is too far away, skip
                if (checked.contains(node) ||
                        MathUtils.dist(target.x, target.z, node.x, node.z) > horizontalRadius ||
                        Math.abs(node.y - target.y) > verticalRadius)
                    continue;
                checked.add(node);
                if (test.test(node)) {
                    //LocalClient.world.terrain.canSpawnHere(LocalClient.world, node.x, node.y, node.z)
                    newTarget = new Vector3f(node.x, node.y, node.z);
                    System.out.println("\tFound spawn point (" + newTarget.x + ", " + newTarget.y + ", " + newTarget.z + ")");
                    return newTarget;
                }
                checkSpawnNode(nodes, checked, node.x, node.y - 1, node.z);
                checkSpawnNode(nodes, checked, node.x - 1, node.y, node.z);
                checkSpawnNode(nodes, checked, node.x + 1, node.y, node.z);
                checkSpawnNode(nodes, checked, node.x, node.y, node.z - 1);
                checkSpawnNode(nodes, checked, node.x, node.y, node.z + 1);
                checkSpawnNode(nodes, checked, node.x, node.y + 1, node.z);
            }

        }

        System.out.println("\tCould not find spawn point Trying backup test.");
        /**
         * Preform a backup test where we JUST check for air
         */
        test = (v) -> {
            short b = Client.world.getBlockID(v.x, v.y, v.z);
            short b1 = Client.world.getBlockID(v.x, v.y - 1, v.z);
            short b2 = Client.world.getBlockID(v.x, v.y - 2, v.z);
            return b == BLOCK_AIR
                    && b1 == BLOCK_AIR
                    && b2 == BLOCK_AIR;
        };
        if (
                !test.test(new Vector3i((int) target.x, (int) target.y, (int) target.z))
        ) {
            //Go around the spawn point and find a safe place to spawn
            HashSet<Vector3i> checked = new HashSet<>();
            HashSet<Vector3i> nodes = new HashSet<>();
            nodes.add(new Vector3i((int) target.x, (int) target.y, (int) target.z));

            while (!nodes.isEmpty()) {
                Vector3i node = nodes.iterator().next();
                nodes.remove(node);
                //If we have already checked this node or it is too far away, skip
                if (checked.contains(node) ||
                        MathUtils.dist(target.x, target.z, node.x, node.z) > horizontalRadius * 2 ||
                        Math.abs(node.y - target.y) > verticalRadius)
                    continue;
                checked.add(node);
                if (test.test(node)) {
                    //LocalClient.world.terrain.canSpawnHere(LocalClient.world, node.x, node.y, node.z)
                    newTarget = new Vector3f(node.x, node.y, node.z);
                    System.out.println("\t\tFound spawn point (" + newTarget.x + ", " + newTarget.y + ", " + newTarget.z + ")");
                    return newTarget;
                }
                checkSpawnNode(nodes, checked, node.x, node.y - 1, node.z);
                checkSpawnNode(nodes, checked, node.x - 1, node.y, node.z);
                checkSpawnNode(nodes, checked, node.x + 1, node.y, node.z);
                checkSpawnNode(nodes, checked, node.x, node.y, node.z - 1);
                checkSpawnNode(nodes, checked, node.x, node.y, node.z + 1);
                checkSpawnNode(nodes, checked, node.x, node.y + 1, node.z);
            }
        }

        System.out.println("\tCould not find spawn point, defaulting to (" + newTarget.x + ", " + newTarget.y + ", " + newTarget.z + ")");
        return newTarget;
    }

    private static void checkSpawnNode(HashSet<Vector3i> nodes,
                                       HashSet<Vector3i> checked,
                                       int x, int y, int z) {
        if (!checked.contains(new Vector3i(x, y, z))) {
            nodes.add(new Vector3i(x, y, z));
        }
    }


    public void teleport(float x, float y, float z) {
        previous_playerBlock = null;
        previous_CameraBlock = null;
        worldPosition.set(x, y, z);
        Main.getClient().consoleOut("Teleported to " + x + ", " + y + ", " + z);
    }

    private boolean isSafeHeadPos(Block block) {
        return block == BlockRegistry.BLOCK_AIR;
        //return //!block.solid && block.enterDamage <= 0;
    }

    //Other processes could mess with this if we dont have the proper way to set the spawnpoint
    private Vector3f status_spawnPosition = new Vector3f();

    public void setSpawnPoint(float x, float y, float z) {
        status_spawnPosition.set(x, y, z);
        Main.getClient().consoleOut("Spawn set to " + status_spawnPosition.x + ", " + status_spawnPosition.y + ", " + status_spawnPosition.z);
        saveToWorld(Client.world.data); //Make SURE to save the spawnpoint
    }

    public void getPlayerBoxTop(Vector3f playerBoxBottom) {
        aabb.updateBox();
        playerBoxBottom.set(
                (Client.userPlayer.aabb.box.min.x + Client.userPlayer.aabb.box.max.x) / 2,
                Client.userPlayer.aabb.box.min.y,
                (Client.userPlayer.aabb.box.min.z + Client.userPlayer.aabb.box.max.z) / 2);
    }

    public void getPlayerBoxBottom(Vector3f playerBoxTop) {
        aabb.updateBox();
        playerBoxTop.set(
                (Client.userPlayer.aabb.box.min.x + Client.userPlayer.aabb.box.max.x) / 2,
                Client.userPlayer.aabb.box.max.y,
                (Client.userPlayer.aabb.box.min.z + Client.userPlayer.aabb.box.max.z) / 2
        );
    }

    //Saving/loading in world
    public final StorageSpace inventory;
    private int selectedItemIndex;
    private final Gson pdGson = new GsonBuilder()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    public void changeSelectedIndex(float increment) {
        selectedItemIndex += increment;
        selectedItemIndex = (MathUtils.clamp(selectedItemIndex, 0, inventory.size() - 1));
    }

    public void setSelectedIndex(int index) {
        selectedItemIndex = MathUtils.clamp(index, 0, inventory.size() - 1);
        selectedItemIndex = (MathUtils.clamp(selectedItemIndex, 0, inventory.size() - 1));
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public ItemStack getSelectedItem() {
        return inventory.get(selectedItemIndex);
    }


    public boolean holdingItem(Item item) {
        ItemStack s = getSelectedItem();
        if (s != null) {
            return s.item == item;
        }
        return false;
    }

    public void acquireItem(ItemStack itemStack) {
        inventory.acquireItem(itemStack, selectedItemIndex);
    }

    public final String PLAYER_DATA_FILE = "player.json";

    public void saveToWorld(WorldData worldData) {
        File playerFile = new File(worldData.getDirectory(), PLAYER_DATA_FILE);

        JsonObject jsonObject = new JsonObject();
        // Serialize playerStuff and other data
        jsonObject.add("inventory", pdGson.toJsonTree(inventory.getList()));
        jsonObject.addProperty("x", (float) worldPosition.x);
        jsonObject.addProperty("y", (float) worldPosition.y);
        jsonObject.addProperty("z", (float) worldPosition.z);

        jsonObject.addProperty("spawnX", (float) status_spawnPosition.x);
        jsonObject.addProperty("spawnY", (float) status_spawnPosition.y);
        jsonObject.addProperty("spawnZ", (float) status_spawnPosition.z);

        jsonObject.addProperty("health", status_health);
        jsonObject.addProperty("hunger", status_food);
        jsonObject.addProperty("oxygen", status_oxygen);

        try {
            Files.write(playerFile.toPath(), pdGson.toJson(jsonObject).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromWorld(WorldData worldData) {
        File playerFile = new File(worldData.getDirectory(), PLAYER_DATA_FILE);
        inventory.clear();
        if (playerFile.exists()) {
            try {
                String jsonData = new String(Files.readAllBytes(playerFile.toPath()));
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

                // Deserialize playerStuff if present
                if (jsonObject.has("inventory")) {
                    AtomicInteger i = new AtomicInteger(0);
                    jsonObject.get("inventory").getAsJsonArray().forEach(element -> {
                        ItemStack itemStack = pdGson.fromJson(element, ItemStack.class);
                        inventory.set(i.get(), itemStack);
                        i.addAndGet(1);
                    });
                }
                if (jsonObject.has("x") && jsonObject.has("y") && jsonObject.has("z")) {
                    worldPosition.x = jsonObject.get("x").getAsFloat();
                    worldPosition.y = jsonObject.get("y").getAsFloat();
                    worldPosition.z = jsonObject.get("z").getAsFloat();
                }
                if (jsonObject.has("spawnX") && jsonObject.has("spawnY") && jsonObject.has("spawnZ")) {
                    status_spawnPosition.x = jsonObject.get("spawnX").getAsFloat();
                    status_spawnPosition.y = jsonObject.get("spawnY").getAsFloat();
                    status_spawnPosition.z = jsonObject.get("spawnZ").getAsFloat();
                }
                if (jsonObject.has("health")) status_health = jsonObject.get("health").getAsFloat();
                if (jsonObject.has("hunger")) status_food = jsonObject.get("hunger").getAsFloat();
                if (jsonObject.has("oxygen")) status_oxygen = jsonObject.get("oxygen").getAsFloat();

                selectedItemIndex = 0;
            } catch (Exception e) {
                ErrorHandler.report(e);
            }
        }
    }


    // Keys
    public static final int KEY_CHANGE_RAYCAST_MODE = GLFW.GLFW_KEY_TAB;
    public static final int KEY_CREATE_MOUSE_BUTTON = GLFW.GLFW_KEY_EQUAL;
    public static final int KEY_DELETE_MOUSE_BUTTON = GLFW.GLFW_KEY_MINUS;
    public static final int KEY_TOGGLE_AUTO_FORWARD = GLFW.GLFW_KEY_J;
    public static final int KEY_TOGGLE_VIEW = GLFW.GLFW_KEY_O;

    public static final int KEY_MOVE_RIGHT = GLFW.GLFW_KEY_D;
    public static final int KEY_MOVE_LEFT = GLFW.GLFW_KEY_A;
    public static final int KEY_MOVE_FORWARD = GLFW.GLFW_KEY_W;
    public static final int KEY_MOVE_BACKWARD = GLFW.GLFW_KEY_S;

    private static final int KEY_FLY_UP = GLFW.GLFW_KEY_F;
    public static final int KEY_FLY_DOWN = GLFW.GLFW_KEY_LEFT_CONTROL;
    private static final int KEY_JUMP = GLFW.GLFW_KEY_SPACE;
    private static final int KEY_SPRINT = GLFW.GLFW_KEY_LEFT_SHIFT;

    final static float WALK_SPEED = 7f;
    final static float RUN_SPEED = 14f;
    final static float FLY_VERTICAL_SPEED = 14f;
    final static float FLY_WALK_SPEED = 14f;
    final static float FLY_RUN_SPEED = 30f;//XB2 runSpeed = 12f * 2.5f

    public static int getCreateMouseButton() {
        return (ClientWindow.settings.game_switchMouseButtons ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    public static int getDeleteMouseButton() {
        return (ClientWindow.settings.game_switchMouseButtons ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    private boolean keyInputAllowed() {
        return !Main.getClient().window.gameScene.ui.anyMenuOpen() && canMove();
    }

    public boolean leftKeyPressed() {
        if (!keyInputAllowed()) return false;
        return window.isKeyPressed(KEY_MOVE_LEFT);
    }

    public boolean rightKeyPressed() {
        if (!keyInputAllowed()) return false;
        return window.isKeyPressed(KEY_MOVE_RIGHT);
    }

    public boolean forwardKeyPressed() {
        if (autoForward) return true;
        if (!keyInputAllowed()) return false;
        return window.isKeyPressed(KEY_MOVE_FORWARD);
    }

    public boolean backwardKeyPressed() {
        if (!keyInputAllowed()) return false;
        return window.isKeyPressed(KEY_MOVE_BACKWARD);
    }


    private void disableGravity() {
        positionHandler.setGravityEnabled(false);
    }


    private void jump() {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) {
        } else {
            dismount();
            if (positionHandler.isGravityEnabled()) {
                if (usePositionHandler)
                    positionHandler.jump();
                usePositionHandler = true;
                positionHandler.collisionsEnabled = true;
                positionHandler.setGravityEnabled(true);
            } else disableFlying();
        }
    }

    private boolean jumpKeyPressed = false;

    public UserControlledPlayer(ClientWindow window,
                                Matrix4f projection, Matrix4f view,
                                Matrix4f centeredView) throws IOException {
        super();
        this.window = window;
        this.projection = projection;
        this.view = view;
        inventory = new StorageSpace(33);
        camera = new Camera(this, window, projection, view, centeredView);
        positionHandler = new PositionHandler(window, Client.world, aabb, aabb);
        positionHandler.callback_onGround = (fallDistance) -> {
            if (fallDistance > 3) {
                float damage = MathUtils.map(fallDistance, 3, 40, 0, MAX_HEALTH);
                status_health -= damage;
            }
            //System.out.println("onGround: " + fallDistance);
        };
        userInfo.loadFromDisk();
    }

    public void initGL() {
        camera.init();
    }

    public void setFlashlight(float distance) {
        Client.world.chunkShader.setFlashlightDistance(distance);
    }

    /**
     * Called at the beginning of a new world
     *
     * @param worldInfo
     */
    public void newWorldEvent(WorldData worldInfo) {
        resetHealthStats();
    }

    public void startGameEvent(WorldData world) {
        autoForward = false;
        isFlyingMode = true;
        loadFromWorld(world);
        gameModeChangedEvent(Main.getServer().getGameMode());
    }

    public void stopGameEvent() {

    }

    public void gameModeChangedEvent(GameMode gameMode) {
        resetHealthStats();
        if (gameMode == GameMode.SPECTATOR) {
            enableFlying();
            setFlashlight(100);
        } else if (gameMode == GameMode.FREEPLAY) {
            setFlashlight(0);
            camera.cursorRay.setRayDistance(90);
        } else if (gameMode == GameMode.ADVENTURE) {
            disableFlying();
            setFlashlight(0);
            camera.cursorRay.setRayDistance(6);
        }
        camera.cursorRay.angelPlacementMode = false;
    }

    public Block getBlockAtPlayerHead() {
        return Client.world.getBlock(
                (int) Math.floor(worldPosition.x),
                (int) Math.floor(worldPosition.y),
                (int) Math.floor(worldPosition.z));
    }

    public Block getBlockAtPlayerFeet() {
        return Client.world.getBlock(
                (int) Math.floor(worldPosition.x),
                (int) Math.floor(worldPosition.y + aabb.box.getYLength()),
                (int) Math.floor(worldPosition.z));
    }

    public Block getBlockAtPlayerWaist() {
        return Client.world.getBlock(
                (int) Math.floor(worldPosition.x),
                (int) Math.floor(worldPosition.y + aabb.box.getYLength()) - 1,
                (int) Math.floor(worldPosition.z));
    }

    public Block getBlockAtCameraPos() {
        return Client.world.getBlock(
                (int) Math.floor(camera.position.x),
                (int) Math.floor(camera.position.y),
                (int) Math.floor(camera.position.z));
    }

    public boolean isInsideOfLadder() {
        return getBlockAtPlayerHead().climbable
                ||
                Client.world.getBlock(
                        (int) Math.floor(worldPosition.x),
                        (int) Math.floor(worldPosition.y + aabb.box.getYLength()),
                        (int) Math.floor(worldPosition.z)).climbable;
    }


    Block previous_CameraBlock = BlockRegistry.BLOCK_AIR;
    Block previous_playerBlock = BlockRegistry.BLOCK_AIR;

    public void render(boolean holdMouse) {
    }

    public void updateAndRender(boolean holdMouse) {
        if (canMove()) camera.cursorRay.update();
        Block blockAtHead = getBlockAtPlayerHead();
        Block blockAtWaist = getBlockAtPlayerWaist();
        Block blockAtFeet = getBlockAtPlayerFeet();
        updateHealthbars(blockAtHead, blockAtFeet, blockAtWaist);


        if (positionLock != null && (positionLock.entity == null || positionLock.entity.isDestroyMode() || dieMode)) {
            //Dismount if riding entity is destroyed
            dismount();
        }

        Block newCameraBlock = getBlockAtCameraPos();
        if (dieMode) {
            Main.getClient().window.gameScene.ui.setOverlayColor(0.5f, 0, 0, 0.5f);
        } else if (previous_CameraBlock == null || newCameraBlock != previous_CameraBlock) {
            previous_CameraBlock = newCameraBlock;
            if (newCameraBlock.isAir()) {//Air is always transparent
                Main.getClient().window.gameScene.ui.setOverlayColor(0, 0, 0, 0);
            } else if (newCameraBlock.opaque && newCameraBlock.colorInPlayerHead[3] == 0
                    && positionHandler.collisionsEnabled && positionLock == null
                    && camera.getThirdPersonDist() == 0) { //If we are opaque, don't have a color and we are not in passthrough mode
                Main.getClient().window.gameScene.ui.setOverlayColor(0, 0, 0, 1);
            } else {
                Main.getClient().window.gameScene.ui.setOverlayColor(
                        newCameraBlock.colorInPlayerHead[0],
                        newCameraBlock.colorInPlayerHead[1],
                        newCameraBlock.colorInPlayerHead[2],
                        newCameraBlock.colorInPlayerHead[3]);
            }
        }


        /**
         * Update the block at the players head, this affects player gravity
         */
        if (previous_playerBlock == null || blockAtHead.id != previous_playerBlock.id) {
            previous_playerBlock = blockAtHead;
            if (blockAtHead.type == BlockRegistry.LIQUID_BLOCK_TYPE_ID) {
                positionHandler.setVelocity(0, 0, 0);
                positionHandler.setFallMedium(PositionHandler.DEFAULT_GRAVITY / 8,
                        PositionHandler.MAX_TERMINAL_VELOCITY / 30);
            } else if (blockAtHead.isAir()) {
                inAirEvent();
            }
        }


        if (positionHandler.isGravityEnabled()) {
            disableFlying();
        }

        float speed;
        if (isFlyingMode) {
            if (runningMode) speed = FLY_RUN_SPEED;
            else speed = FLY_WALK_SPEED;
        } else {
            if (runningMode && canRun()) speed = RUN_SPEED;
            else speed = WALK_SPEED;
        }

        if (positionLock != null) {
            worldPosition.set(positionLock.getPosition());
            usePositionHandler = false;
        } else if (canMove()) {
            usePositionHandler = true;
            if (forwardKeyPressed()) {
                positionHandler.setVelocity(
                        camera.cameraForward.x * speed * window.smoothFrameDeltaSec,
                        camera.cameraForward.z * speed * window.smoothFrameDeltaSec);
            }
            if (backwardKeyPressed()) {
                positionHandler.setVelocity(
                        -(camera.cameraForward.x * speed * window.smoothFrameDeltaSec),
                        -(camera.cameraForward.z * speed * window.smoothFrameDeltaSec));
            }

            if (leftKeyPressed()) {
                positionHandler.setVelocity(
                        -(camera.right.x * speed * window.smoothFrameDeltaSec),
                        -(camera.right.z * speed * window.smoothFrameDeltaSec));
            }
            if (rightKeyPressed()) {
                positionHandler.setVelocity(
                        camera.right.x * speed * window.smoothFrameDeltaSec,
                        camera.right.z * speed * window.smoothFrameDeltaSec);
            }
            runningMode = window.isKeyPressed(KEY_SPRINT);
//            /**
//             * We have to handle some keys in a separate way, due to some keyboards having key rollover
//             * https://en.wikipedia.org/wiki/Rollover_(key)
//             */
//            if (window.isKeyPressed(KEY_JUMP)) {
//                System.out.println("JUMP");
//                if (!jumpKeyPressed) {
//                    jumpKeyPressed = true;
//                    jump();
//                }
//            } else jumpKeyPressed = false;


            if (isInsideOfLadder()) {
                if (keyInputAllowed() && window.isKeyPressed(KEY_JUMP)) {
                    isClimbing = true;
                    disableFlying();
                    worldPosition.sub(0, 3f * window.smoothFrameDeltaSec, 0);
                } else {
                    isClimbing = true;
                    disableFlying();
                    worldPosition.add(0, 3f * window.smoothFrameDeltaSec, 0);
                }
                positionHandler.setGravityEnabled(false);
            } else {
                if (isClimbing) {
                    positionHandler.setGravityEnabled(true);
                    isClimbing = false;
                } else if (isFlyingMode) {
                    if (keyInputAllowed() && window.isKeyPressed(KEY_FLY_UP)) {
                        worldPosition.sub(0, FLY_VERTICAL_SPEED * window.smoothFrameDeltaSec, 0);
                        disableGravity();
                    } else if (keyInputAllowed() && downKeyPressed()) {
                        worldPosition.add(0, FLY_VERTICAL_SPEED * window.smoothFrameDeltaSec, 0);
                        disableGravity();
                    }
                } else if (previous_playerBlock.isLiquid() && keyInputAllowed() && window.isKeyPressed(KEY_JUMP)) {
                    positionHandler.addVelocity(0, -0.05f, 0);
                }
            }
        }

        if (usePositionHandler) {
            positionHandler.update();
            aabb.isSolid = true;
        } else {
            aabb.isSolid = false;
            aabb.update();
        }

        if (canMove()) {
            if (ClientWindow.settings.game_autoJump &&
                    (Math.abs(positionHandler.collisionHandler.collisionData.totalPenPerAxes.x) > 0.02 ||
                            Math.abs(positionHandler.collisionHandler.collisionData.totalPenPerAxes.z) > 0.02)
            ) {
                autoJump_ticksWhileColidingWithBlock += window.getMsPerFrame();
                if (autoJump_ticksWhileColidingWithBlock > 150 && autoJump_unCollided) {
                    positionHandler.jump();
                    autoJump_ticksWhileColidingWithBlock = 0;
                    autoJump_unCollided = false;
                }
            } else {
                autoJump_ticksWhileColidingWithBlock = 0;
                autoJump_unCollided = true;
            }
        }

        // The key to preventing shaking during collision is to update the camera AFTER
        // the position handler is done its job

        camera.update(holdMouse);
        this.pan = camera.pan;
        this.tilt = camera.tilt;

        camera.cursorRay.drawRay();
        if (camera.getThirdPersonDist() != 0.0f) {
            userInfo.getSkin().super_render(projection, view);
        }

        if (lastOrientation.x != worldPosition.x
                || lastOrientation.y != worldPosition.y
                || lastOrientation.z != worldPosition.z
                || lastOrientation.w != pan) {
            lastOrientation.set(worldPosition.x, worldPosition.y, worldPosition.z, pan);
            Main.getServer().server.sendPlayerPosition(lastOrientation);
        }
    }

    private void inAirEvent() {
        positionHandler.resetFallMedium();
    }

    private boolean canRun() {
        return
                Main.getServer().getGameMode() == GameMode.FREEPLAY
                        || Main.getServer().getGameMode() == GameMode.SPECTATOR
                        || status_food > 5;
    }


    private boolean downKeyPressed() {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR)
            return window.isKeyPressed(KEY_FLY_DOWN) || window.isKeyPressed(KEY_JUMP);
        return window.isKeyPressed(KEY_FLY_DOWN);
    }

    private boolean isFlyingMode = true;

    public void enableFlying() {
        if (Main.getServer().getGameMode() == GameMode.FREEPLAY || Main.getServer().getGameMode() == GameMode.SPECTATOR) {
            isFlyingMode = true;
            positionHandler.setGravityEnabled(false);
            positionHandler.collisionsEnabled = false;
        }
    }

    public void disableFlying() {
        isFlyingMode = false;
        positionHandler.setGravityEnabled(true);
        positionHandler.collisionsEnabled = true;
    }

    public boolean mouseButtonEvent(int button, int action, int mods) {
        if (isDieMode()) return false;

        if (action == GLFW.GLFW_PRESS) {
            if (button == UserControlledPlayer.getCreateMouseButton()) {
                camera.cursorRay.clickEvent(true);
                return true;
            } else if (button == UserControlledPlayer.getDeleteMouseButton()) {
                camera.cursorRay.clickEvent(false);
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                GameUI.hotbar.pickItem(camera.cursorRay, Main.getServer().getGameMode() == GameMode.FREEPLAY);
                return true;
            }
        }
        return false;
    }

    public void keyEvent(int key, int scancode, int action, int mods) {
        if (camera.cursorRay.keyEvent(key, scancode, action, mods)) {
            printKeyConsumption(camera.cursorRay.getClass());
        } else if (action == GLFW.GLFW_PRESS) {
            switch (key) {
                case KEY_JUMP -> {
                    jump();
                }
                case KEY_FLY_UP -> enableFlying();
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (key == KEY_CHANGE_RAYCAST_MODE && Main.getServer().getGameMode() == GameMode.FREEPLAY) {
                camera.cursorRay.angelPlacementMode = !camera.cursorRay.angelPlacementMode;
            }
            switch (key) {
                case KEY_TOGGLE_VIEW -> camera.cycleToNextView(10);
                case KEY_CREATE_MOUSE_BUTTON -> {
                    camera.cursorRay.clickEvent(true);
                }
                case KEY_DELETE_MOUSE_BUTTON -> {
                    camera.cursorRay.clickEvent(false);
                }
                case KEY_TOGGLE_AUTO_FORWARD -> autoForward = !autoForward;
            }
        }
    }

    private void dismount() {
        if (positionLock != null) {
            worldPosition.y = positionLock.entity.aabb.box.min.y - aabb.box.getYLength();
            positionLock = null;
        }
    }

    public Entity dropItem(ItemStack itemStack) {
        Vector3f pos = new Vector3f().set(Client.userPlayer.worldPosition);
        Vector3f addition = new Vector3f().set(Client.userPlayer.camera.look.x, 0, Client.userPlayer.camera.look.z).mul(1.5f);
        pos.add(addition);
        return Main.getServer().placeItemDrop(
                pos,
                itemStack,
                true);
    }


    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        return false;
    }


}
