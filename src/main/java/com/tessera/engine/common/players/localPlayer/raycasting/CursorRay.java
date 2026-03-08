package com.tessera.engine.common.players.localPlayer.raycasting;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.loot.AllLootTables;
import com.tessera.engine.common.players.localPlayer.camera.Camera;
import com.tessera.utils.MiscUtils;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.tessera.engine.common.world.World;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class CursorRay {

    public boolean hitTarget() {
        return angelPlacementMode || cursorRay.hitTarget;
    }

    public Vector3i getHitPos() {
        return cursorRay.getHitPositionAsInt();
    }

    public Vector3i getHitPosPlusNormal() {
        return cursorRay.getHitPosPlusNormal();
    }

    public Vector3i getHitNormalAsInt() {
        return cursorRay.getHitNormalAsInt();
    }

    public Vector3f getHitNormal() {
        return cursorRay.getHitNormal();
    }

    public Entity getEntity() {
        return cursorRay.entity;
    }

    public CursorRay(Camera camera, ClientWindow window) {
        this.camera = camera;
        this.window = window;
        cursorRay = new Ray();
    }

    public void init() {
        cursorBox = new Box();
        cursorBox.setPosAndSize(0, 0, 0, 1, 1, 1);
        cursorBox.setColor(DEFAULT_COLOR);
        cursorBox.setLineWidth(2);
    }

    public final Vector4f DEFAULT_COLOR = new Vector4f(1, 1, 1, 1);
    public final Camera camera;
    public Box cursorBox;
    public final Ray cursorRay;

    public int angelPlacementReachDistance = 7;
    public boolean angelPlacementMode = false;
    ClientWindow window;

    // Boundary mode:
    private boolean useBoundary = false;
    private boolean boundary_isStartNodeSet = false;
    public boolean boundary_useHitPos = false;
    public boolean boundary_lockToPlane = false;
    private BiConsumer<AABB, Boolean> boundaryConsumer;
    private final Vector3i boundary_startNode = new Vector3i();
    private final Vector3i boundary_endNode = new Vector3i();
    private final AABB boundary_aabb = new AABB();

    public void enableBoundaryMode(BiConsumer<AABB, Boolean> createBoundaryConsumer) {
        useBoundary = true;
        boundary_isStartNodeSet = false;
        this.boundaryConsumer = createBoundaryConsumer;
    }

    public void disableBoundaryMode() {
        useBoundary = false;
        boundaryConsumer = null;
    }

    /**
     * @return if the event was consumed
     */
    public boolean clickEvent(boolean creationMode) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;
        breakAmt = 0;
        breakPercentage = 0;
        ItemStack selectedItem = Client.userPlayer.getSelectedItem();
        if (!hitTarget()) return false;

        if (Main.game.clickEvent(this, creationMode)) { //Game click event
            return true;
        } else if (useBoundary) { //Boundary click event
            boundaryClickEvent(creationMode);
            return true;
        }


        if (creationMode &&
                Client.world.getBlock(getHitPos().x, getHitPos().y, getHitPos().z)
                        .run_ClickEvent(Main.getServer().eventPipeline.clickEventThread, getHitPos())) { //Block click event
            return true;
        }

        if (itemClickEvent(selectedItem, creationMode)) return true; //Item click event

        if (creationMode && cursorRay.entity != null && cursorRay.entity.run_ClickEvent()) { //Entity click event
            return true;
        }


        if (!creationMode) { //By default, remove anything the cursor is pointing at
            breakBlock(false, selectedItem);
            return true;
        }
        return false;
    }


    private boolean itemClickEvent(ItemStack selectedItem, boolean creationMode) {
        if (selectedItem != null) {

            boolean consumed = false;


            if (creationMode) {
                boolean canEat = true;
                if (selectedItem.item.createClickEvent != null) {
                    if (selectedItem.item.createClickEvent.run(this, selectedItem)) {
                        consumed = true;
                        canEat = false;
                    }
                } else if (selectedItem.item.getBlock() != null || selectedItem.item.getEntity() != null) {
                    if (defaultSetEvent(selectedItem)) {
                        canEat = false;
                        consumed = true;
                    }
                }
                //If we are not interrupting the eating process, we can eat
                if (canEat && selectedItem.item.isFood()) {

                    eatFood(selectedItem);
                    return true;
                }
            } else if (selectedItem.item.destroyClickEvent != null) {
                return selectedItem.item.destroyClickEvent.run(this, selectedItem);
            }

            return consumed;
        }
        return false;
    }

    private float breakAmt = 0;
    public float breakPercentage = 0;
    private final Vector3i lastBreakPos = new Vector3i();


    private float getMiningSpeed(ItemStack selectedItem) {
        float miningSpeed = 0.015f;
        if (selectedItem != null) miningSpeed *= selectedItem.item.miningSpeedMultiplier;
        return miningSpeed;
    }


    private void eatFood(ItemStack selectedItem) {
        System.out.println("Eating food");
        if (Client.userPlayer.getFoodLevel() >= LocalPlayer.MAX_FOOD * 0.9) {
            return;
        }
        Client.userPlayer.addFood(selectedItem.item.foodAdd);
        selectedItem.stackSize--;
    }

    private boolean isMiningWithWrongTool(Block block, ItemStack tool) {
        if (tool != null) {
            //if the block specifies
            if (block.easierMiningTool_tag != null) {
                //If this tool doesnt match the requested one
                return !tool.item.tags.contains(block.easierMiningTool_tag);
            }
            return false;
        }
        return true;
    }

    private boolean hasToolThatCanMine(Block block, ItemStack tool) {
        if (block.toolsThatCanMine_tags != null) {
            if (tool != null) {
                for (String toolTag : block.toolsThatCanMine_tags) {
                    if (tool.item.tags.contains(toolTag)) {
                        return true;
                    }
                }
            }
            return false;
        } else return true;
    }

    private void breakBlock(boolean isHeld, ItemStack selectedItem) {
        if (!Client.world.inBounds(getHitPos().x, getHitPos().y, getHitPos().z)) return;

        if (isHeld) {
            if (Main.getServer().getGameMode() != GameMode.FREEPLAY) {
                if (!getHitPos().equals(lastBreakPos)) {
                    System.out.println("Changed block");
                    breakAmt = 0;
                    lastBreakPos.set(getHitPos());
                }
                Block existingBlock = Client.world.getBlock(getHitPos().x, getHitPos().y, getHitPos().z);
                if (existingBlock.isLiquid()) return;
                float miningSpeed = getMiningSpeed(selectedItem);

                //If we are mining with the wrong tool, mine slower
                if (isMiningWithWrongTool(existingBlock, selectedItem)) {
                    miningSpeed *= 0.15f;
                }

                //If the block requires a tool to mine it, and the player doesn't have the right tool, don't mine at all
                if (!hasToolThatCanMine(existingBlock, selectedItem)) {
                    System.out.println("No tool for block");
                    breakAmt = 0;
                    return;
                }


                float blockToughness = existingBlock.toughness;
                breakPercentage = breakAmt / blockToughness;
                breakAmt += miningSpeed;

                if (selectedItem != null && selectedItem.item.maxDurability > 0) {
                    selectedItem.durability -= 0.005f;
                    if (selectedItem.durability <= 0) selectedItem.destroy();
                }
                if (breakAmt >= blockToughness) {
                    AllLootTables.blockLootTables.dropLoot(existingBlock.alias, new Vector3f(getHitPos()), false, false);
                    Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, new WCCi().set(getHitPos()));
                    breakAmt = 0;
                    System.out.println("Resetting after broken block");
                }
            }
        } else { //Click
            if (Main.getServer().getGameMode() == GameMode.FREEPLAY) {
                if (getEntity() != null) {
                    getEntity().destroy();
                } else {
                    Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, new WCCi().set(getHitPos()));
                }
            }
        }
    }

    long autoClick_timeSinceReleased;
    long autoClick_lastClicked = 0;
    final int AUTO_CLICK_INTERVAL = 250;

    public void update() {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return;

        if (!Main.getClient().window.gameScene.ui.anyMenuOpen()) {
            //Auto click
            if (window.isMouseButtonPressed(LocalPlayer.getCreateMouseButton())) {
                if (System.currentTimeMillis() - autoClick_timeSinceReleased > AUTO_CLICK_INTERVAL * 1.5 &&
                        System.currentTimeMillis() - autoClick_lastClicked > AUTO_CLICK_INTERVAL) {
                    autoClick_lastClicked = System.currentTimeMillis();
                    camera.cursorRay.clickEvent(true);
                }
            } else if (Main.getServer().getGameMode() == GameMode.FREEPLAY && window.isMouseButtonPressed(LocalPlayer.getDeleteMouseButton())) {
                if (System.currentTimeMillis() - autoClick_timeSinceReleased > AUTO_CLICK_INTERVAL * 1.5 &&
                        System.currentTimeMillis() - autoClick_lastClicked > AUTO_CLICK_INTERVAL) {
                    autoClick_lastClicked = System.currentTimeMillis();
                    camera.cursorRay.clickEvent(false);
                }
            } else autoClick_timeSinceReleased = System.currentTimeMillis();

            //Removal
            if (window.isMouseButtonPressed(LocalPlayer.getDeleteMouseButton())) {
                lastDeletePressTime = System.currentTimeMillis();
                ItemStack selectedItem = Client.userPlayer.getSelectedItem();
                if (selectedItem == null || !selectedItem.item.isFood()) {
                    breakBlock(true, selectedItem);
                }
                //If its been more than 200ms since we last held the mouse, reset the progress
                //We do this so that in case the mouse isnt holding down the whole time, we can still break the block
            } else if (System.currentTimeMillis() - lastDeletePressTime > 300)
                breakPercentage = 0;
        }
    }

    private long lastDeletePressTime;

    private boolean blockIntersectsPlayer(Block block, Vector3i set) {
        AABB boxAABB = new AABB();
        //If the block is too close to the player, don't place
        AtomicBoolean intersects = new AtomicBoolean(false);
        BlockData initialData = block.getInitialBlockData(null);

        block.getType().getCollisionBoxes((aabb) -> {
            if (aabb.intersects(Client.userPlayer.aabb.box) &&
                    Client.userPlayer.aabb.box.max.y > aabb.min.y + 0.1f) //small padding to help with placing
                intersects.set(true);
        }, boxAABB, block, initialData, set.x, set.y, set.z);

        return intersects.get();
    }

    private boolean defaultSetEvent(ItemStack stack) {
        Block block = stack.item.getBlock();
        EntitySupplier entity = stack.item.getEntity();

        if (stack.stackSize <= 0) return false;
        if (block != null) {
            Block hitBlock = Client.world.getBlock(cursorRay.getHitPositionAsInt());
            Vector3i set = cursorRay.getHitPositionAsInt();

            if (!(hitBlock.getType().replaceOnSet && !block.getType().replaceOnSet)) {
                set = cursorRay.getHitPosPlusNormal();
                if (blockIntersectsPlayer(block, set)) return false;
            }
            if (Main.getServer().getGameMode() != GameMode.FREEPLAY) stack.stackSize--;
            Main.getServer().setBlock(block.id, set.x, set.y, set.z);
            return true;
        } else if (entity != null) {
            Vector3f pos = new Vector3f(cursorRay.getHitPosPlusNormal());
            if (Main.getServer().getGameMode() != GameMode.FREEPLAY) stack.stackSize--;
            Main.getServer().placeEntity(entity, pos, null);
            return true;
        }
        return false;
    }

    private void boundaryClickEvent(boolean create) {
        if (!boundary_isStartNodeSet) {
            setBoundaryStartNode(boundary_startNode);
            boundary_isStartNodeSet = true;
        } else {
            if (boundaryIsWithinArea()) {
                if (boundaryConsumer != null)
                    boundaryConsumer.accept(boundary_aabb, create);
                makeAABBFrom2Points(boundary_startNode, boundary_endNode, boundary_aabb);
                boundary_isStartNodeSet = false;
            } else Main.getClient().consoleOut("Boundary is too large");
        }
    }

    public static final int BOUNDARY_USE_HIT_POS_KEY = GLFW.GLFW_KEY_K;

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            if (useBoundary) {
                if (key == BOUNDARY_USE_HIT_POS_KEY) {
                    boundary_useHitPos = true;
                    return false;
                }
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (useBoundary) {
                if (key == BOUNDARY_USE_HIT_POS_KEY) {
                    boundary_useHitPos = false;
                    return false;
                }
            }
        }
        return false;
    }

    private void makeAABBFrom2Points(Vector3i start, Vector3i end, AABB aabb) {
        int minX = Math.min(start.x, end.x);
        int minY = Math.min(start.y, end.y);
        int minZ = Math.min(start.z, end.z);
        int maxX = Math.max(start.x, end.x);
        int maxY = Math.max(start.y, end.y);
        int maxZ = Math.max(start.z, end.z);
        aabb.setPosAndSize(
                minX,
                minY,
                minZ,
                (int) MathUtils.dist(minX, maxX + 1),
                (int) MathUtils.dist(minY, maxY + 1),
                (int) MathUtils.dist(minZ, maxZ + 1));
    }

    private void setBoundaryStartNode(Vector3i node) {
        if (boundary_useHitPos || (boundary_lockToPlane && boundary_isStartNodeSet))
            node.set(getHitPos());
        else
            node.set(getHitPosPlusNormal());
    }

    private void setBoundaryEndNode(Vector3i node) {
        setBoundaryStartNode(node);
    }


    public boolean boundaryIsWithinArea() {
        int maxWidth = Client.world.getDeletionViewDistance() - Chunk.WIDTH;
        return boundary_aabb.getXLength() < maxWidth &&
                boundary_aabb.getZLength() < maxWidth
                &&
                (
                        boundary_aabb.getXLength() *
                                boundary_aabb.getZLength() *
                                boundary_aabb.getYLength() <
                                ClientWindow.settings.dev_blockBoundaryAreaLimit);
    }

    public void drawRay() {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) {
            //We sometimes want to draw the cursor even when spectating
            if (Main.getClient().window.gameScene.ui.infoBoxVisible()) {
                cursorBox.setPosAndSize(
                        (int) cursorRay.getHitPosition().x,
                        (int) cursorRay.getHitPosition().y,
                        (int) cursorRay.getHitPosition().z,
                        1, 1, 1);
                cursorBox.setColor(1, 0, 0, 1);
                cursorBox.draw(camera.projection, camera.view);
            }
        } else if (hitTarget() && useBoundary) {
            if (!boundary_isStartNodeSet) {
                setBoundaryStartNode(boundary_startNode);
                boundary_aabb.setPosAndSize(boundary_startNode.x, boundary_startNode.y, boundary_startNode.z, 1, 1,
                        1);
            } else {
                setBoundaryEndNode(boundary_endNode);
                makeAABBFrom2Points(boundary_startNode, boundary_endNode, boundary_aabb);
            }

            if (boundaryIsWithinArea()) {
                cursorBox.setColor(DEFAULT_COLOR);
            } else {
                cursorBox.setColor(1, 0, 0, 1);
            }
            Main.getClient().window.gameScene.client_hudText(boundary_aabb.getXLength() + " x " + boundary_aabb.getYLength() + " x " + boundary_aabb.getZLength());
            cursorBox.set(boundary_aabb);
            cursorBox.draw(camera.projection, camera.view);
        } else if (Main.game.drawCursor(this)) {
        } else if (hitTarget()) {
            if (cursorRay.entity != null) {
                cursorBox.set(cursorRay.entity.aabb.box);
                cursorBox.draw(camera.projection, camera.view);
            } else if (cursorRay.cursorBoxes == null) {
                cursorBox.setPosAndSize(
                        (int) cursorRay.getHitPosition().x,
                        (int) cursorRay.getHitPosition().y,
                        (int) cursorRay.getHitPosition().z,
                        1, 1, 1);
                cursorBox.draw(camera.projection, camera.view);
            } else {
                for (AABB box : cursorRay.cursorBoxes) {
                    cursorBox.set(box);
                    cursorBox.draw(camera.projection, camera.view);
                }
            }
        }
    }

    final int MAX_RAY_DISTANCE = 512;
    private int rayDistance = MAX_RAY_DISTANCE;// Max distance for front ray

    public void cast(Vector3f position, Vector3f cursorRaycastLook, World world) {

        Vector2i simplifiedPanTilt = Client.userPlayer.camera.simplifiedPanTilt;


        int distance = getRayDistance();
        if (angelPlacementMode) distance = Math.min(distance, angelPlacementReachDistance);

        RayCasting.traceComplexRay(cursorRay, position, cursorRaycastLook, distance,
                ((block, forbiddenBlock, rx, ry, rz) -> {
                    // block ray if we are locking boundary to plane
                    if (useBoundary && boundary_lockToPlane && boundary_isStartNodeSet) {
                        if (simplifiedPanTilt.y != 0) {
                            if (ry == boundary_startNode.y) {
                                return true;
                            }
                        } else if (simplifiedPanTilt.x == 1 || simplifiedPanTilt.x == 3) {
                            if (rx == boundary_startNode.x) {
                                return true;
                            }
                        } else {
                            if (rz == boundary_startNode.z) {
                                return true;
                            }
                        }
                    }
                    if (angelPlacementMode) {
                        return block != forbiddenBlock;
                    } else
                        return block != BlockRegistry.BLOCK_AIR.id &&
                                block != forbiddenBlock;
                }),
                ((entity) -> {
                    return true;
                }),
                world);


        if (useBoundary && boundary_lockToPlane && boundary_isStartNodeSet) {
            if (simplifiedPanTilt.y != 0) {
                cursorRay.hitPostition.y = boundary_startNode.y;
            } else if (simplifiedPanTilt.x == 1 || simplifiedPanTilt.x == 3) {
                cursorRay.hitPostition.x = boundary_startNode.x;
            } else {
                cursorRay.hitPostition.z = boundary_startNode.z;
            }
        }


        //Clamp the cursor position to the world bounds
        cursorRay.hitPostition.x = MathUtils.clamp(cursorRay.hitPostition.x, World.WORLD_SIZE_NEG_X, World.WORLD_SIZE_POS_X);
        cursorRay.hitPostition.z = MathUtils.clamp(cursorRay.hitPostition.z, World.WORLD_SIZE_NEG_Z, World.WORLD_SIZE_POS_Z);

        if (cursorRay.hitPostition.y < World.WORLD_TOP_Y + 1)
            cursorRay.hitPostition.y = World.WORLD_TOP_Y + 1;
    }

    public String toString() {
        return MiscUtils.printVec(cursorRay.getHitPosition());
    }


    public int getRayDistance() {
        return rayDistance;
    }

    public void setRayDistance(int rayDistance) {
        this.rayDistance = MathUtils.clamp(rayDistance, 1, MAX_RAY_DISTANCE);
    }
}
