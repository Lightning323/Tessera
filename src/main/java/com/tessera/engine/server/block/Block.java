package com.tessera.engine.server.block;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.common.players.pipeline.BlockHistory;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.PriorityThreadPoolExecutor;
import com.tessera.engine.common.worldInteraction.collision.PositionHandler;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.function.Consumer;

public class Block {

    //Some fields should be set in the constructor
    public final short id;
    public final String alias;
    public final HashMap<String, String> properties = new HashMap<>();
    public final BlockTexture texture;
    public final int type;

    //Others are ok to be changed later
    public boolean solid = true;
    public boolean climbable = false;
    public boolean opaque = true;
    public byte torchlightStartingValue = 0;
    public int liquidMaxFlow;
    public final float[] colorInPlayerHead = {0, 0, 0, 0};//If set to null, we default to drawing block texture in player head
    public float surfaceCoast = PositionHandler.DEFAULT_COAST; //The "Coast" of the block
    public float surfaceFriction = 0; //The "Friction" of the block
    public float bounciness = 0; //The "Bounciness" of the block
    public float toughness = 1; //The difficulty of breaking the block
    public String easierMiningTool_tag = null; //The blocks that are easier to mine this block with (specify by item tags)
    public String[] toolsThatCanMine_tags = null; //The tools that can mine this block (If the tool cant mine it, it wont drop loot) (specify by item tags)
    public float enterDamage = 0; //The damage dealt when entering the block

    public int getLiquidSourceValue() {
        return liquidMaxFlow + 1;
    }

    public BlockType getType() {
        return Registrys.blocks.getBlockType(type);
    }

    public final boolean isLuminous() {
        return torchlightStartingValue > 0;
    }

    public final boolean isLiquid() {
        return type == BlockRegistry.LIQUID_BLOCK_TYPE_ID;
    }


    // <editor-fold defaultstate="collapsed" desc="block events">
    //Create a functional interface for setBlockEvent
    @FunctionalInterface
    public interface SetBlockEvent {
        public void run(int x, int y, int z);
    }

    @FunctionalInterface
    public interface RandomTickEvent {
        public boolean run(int x, int y, int z);
    }

    @FunctionalInterface
    public interface RemoveBlockEvent {
        public void run(int x, int y, int z, BlockHistory history);
    }


    //A functional interface for onLocalChange
    @FunctionalInterface
    public interface OnLocalChange {
        public void run(BlockHistory history, Vector3i changedPosition, Vector3i thisPosition);
    }

    private boolean setBlockEvent_isMultithreaded = false;
    private boolean removeBlockEvent_isMultithreaded = false;
    private boolean clickEvent_isMultithreaded = false;
    private boolean localChangeEvent_isMultithreaded = false;

    private SetBlockEvent setBlockEvent = null;
    private OnLocalChange localChangeEvent = null;
    private RemoveBlockEvent removeBlockEvent = null;
    private SetBlockEvent clickEvent = null;
    public InitialBlockData initialBlockData = null;

    public BlockData getInitialBlockData(BlockData existingData) {
        if (initialBlockData != null) return initialBlockData.get(existingData);
        return null;
    }

    @FunctionalInterface
    public interface InitialBlockData {
        BlockData get(BlockData existingData);
    }

    public RandomTickEvent randomTickEvent = null;


    public boolean allowExistence(int worldX, int worldY, int worldZ) {
        return true;
    }

    public void clickEvent(boolean multithreaded, SetBlockEvent clickEvent) {
        this.clickEvent = clickEvent;
        clickEvent_isMultithreaded = multithreaded;
    }

    /**
     * Called AFTER the block has been set
     *
     * @param multithreaded
     * @param setBlockEvent
     */
    public void setBlockEvent(boolean multithreaded, SetBlockEvent setBlockEvent) {
        this.setBlockEvent = setBlockEvent;
        setBlockEvent_isMultithreaded = multithreaded;
    }

    /**
     * Called AFTER the block has been removed
     *
     * @param multithreaded
     * @param removeBlockEvent
     */
    public void removeBlockEvent(boolean multithreaded, RemoveBlockEvent removeBlockEvent) {
        this.removeBlockEvent = removeBlockEvent;
        removeBlockEvent_isMultithreaded = multithreaded;
    }

    /**
     * Called AFTER a local change has occured
     *
     * @param multithreaded
     * @param onLocalChange
     */
    public void localChangeEvent(boolean multithreaded, OnLocalChange onLocalChange) {
        this.localChangeEvent = onLocalChange;
        localChangeEvent_isMultithreaded = multithreaded;
    }

    public boolean clickThrough() {
        return clickEvent == null;
    }

    /**
     * @param eventThread
     * @param worldPos
     * @return if the event was consumed
     */
    public boolean run_ClickEvent(PriorityThreadPoolExecutor eventThread,
                                  Vector3i worldPos) {
        if (clickEvent != null) {
            WCCi wcc = new WCCi().set(worldPos);
            Chunk chunk = wcc.getChunk(Client.world);

            if (chunk == null || clickEvent == null) return false;

            if (clickEvent_isMultithreaded) {
                eventThread.submit(System.currentTimeMillis(), () -> {
                    clickEvent.run(worldPos.x, worldPos.y, worldPos.z);
                 //   chunk.updateMesh(false, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                });
            } else {
                clickEvent.run(worldPos.x, worldPos.y, worldPos.z);
             //   chunk.updateMesh(false, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
            }
        }
        return !clickThrough();
    }

    public void run_RemoveBlockEvent(PriorityThreadPoolExecutor eventThread,
                                     Vector3i worldPos, BlockHistory history) {
        if (removeBlockEvent == null) return;
        if (removeBlockEvent_isMultithreaded)
            eventThread.submit(System.currentTimeMillis(),
                    () -> removeBlockEvent.run(worldPos.x, worldPos.y, worldPos.z, history));
        else removeBlockEvent.run(worldPos.x, worldPos.y, worldPos.z, history);
    }

    public void run_SetBlockEvent(PriorityThreadPoolExecutor eventThread,
                                  Vector3i worldPos) {
        if (setBlockEvent == null) return;
        if (setBlockEvent_isMultithreaded)
            eventThread.submit(System.currentTimeMillis(),
                    () -> setBlockEvent.run(worldPos.x, worldPos.y, worldPos.z));
        else setBlockEvent.run(worldPos.x, worldPos.y, worldPos.z);
    }

    public void run_LocalChangeEvent(PriorityThreadPoolExecutor eventThread,
                                     BlockHistory history, Vector3i changedPosition, Vector3i thisPosition) {
        if (localChangeEvent == null) return;
        if (localChangeEvent_isMultithreaded)
            eventThread.submit(System.currentTimeMillis(),
                    () -> localChangeEvent.run(history, changedPosition, thisPosition));
        else localChangeEvent.run(history, changedPosition, thisPosition);
    }
    // </editor-fold>

    public boolean isAir() {
        return false;
    }

    private void initPropertiesFromBlockType(BlockType type) {
        //Initialize properties of this block, in its block type
        if (type != null) {
            Consumer<Block> typeInitCallback = type.initializationCallback;
            if (typeInitCallback != null) typeInitCallback.accept(this);
        }
    }

    public Block(int id, String alias) {
        this.id = (short) id;
        this.alias = Registrys.formatAlias(alias);
        type = BlockRegistry.DEFAULT_BLOCK_TYPE_ID;
        this.texture = null;
    }

    public Block(int id, String alias, BlockTexture texture) {
        this.id = (short) id;
        this.alias = Registrys.formatAlias(alias);
        this.type = BlockRegistry.DEFAULT_BLOCK_TYPE_ID;
        this.texture = texture;
    }

    public Block(int id, String alias, BlockTexture texture, int type) {
        this.id = (short) id;
        this.alias = Registrys.formatAlias(alias);
        this.texture = texture;
        this.type = type;
        initPropertiesFromBlockType(Registrys.blocks.getBlockType(type));
    }


    @Override
    public String toString() {
        return "\"" + alias + "\" Block (id: " + id + ")";
    }
}
