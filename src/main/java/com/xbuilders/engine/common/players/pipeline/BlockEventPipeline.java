package com.xbuilders.engine.common.players.pipeline;

import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.common.threadPoolExecutor.PriorityExecutor.PriorityThreadPoolExecutor;
import com.xbuilders.engine.common.threadPoolExecutor.PriorityExecutor.comparator.HighValueComparator;
import com.xbuilders.utils.BFS.ChunkNode;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.block.construction.BlockType;
import com.xbuilders.engine.common.world.World;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.world.WorldData;
import com.xbuilders.engine.common.world.light.SunlightUtils;
import com.xbuilders.engine.common.world.light.TorchUtils;
import com.xbuilders.engine.common.world.wcc.WCCi;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockEventPipeline {


    private final Map<Vector3i, BlockHistory> events = new HashMap<>(); //ALL events must be submitted to this


    WCCi wcc = new WCCi();
    World world;
    final Object eventClearLock = new Object();

    public BlockEventPipeline(World world) {
        this.world = world;
    }

    public void addEvent(Vector3i worldPos, BlockHistory blockHist) {
        if (blockHist != null) {
//            if (!MultiplayerPendingBlockChanges.changeCanBeLoaded(player, worldPos)) {
//                //If there is a block event that is on a empty chunk or too far away, don't add it
////                LocalClient.world.multiplayerPendingBlockChanges.addBlockChange(worldPos, blockHist);
//                return;
//            }
            blockChangesThisFrame++;
            synchronized (eventClearLock) {
                if (events.containsKey(worldPos)) { //We need to get the original previous block
                    blockHist.previousBlock = events.get(worldPos).previousBlock;
                } else if (blockHist.previousBlock == null) {
                    blockHist.previousBlock = Client.world.getBlock(worldPos.x, worldPos.y, worldPos.z);
                }
                if (blockHist.previousBlock.opaque != blockHist.newBlock.opaque) {
                    lightChangesThisFrame++;
                }
                events.put(worldPos, blockHist);
            }
        }
    }

    public void addEvent(WCCi wcc, BlockHistory event) {
        addEvent(WCCi.chunkSpaceToWorldSpace(wcc), event);
    }


    /**
     * corePoolSize: The number of threads to keep in the pool, even if they are idle.
     * maximumPoolSize: The maximum number of threads to allow in the pool.
     * keepAliveTime: The amount of time that idle threads will wait before terminating.
     * unit: The time unit for the keepAliveTime parameter.
     * workQueue: The BlockingQueue to use for holding tasks before they are executed.
     * threadFactory: The factory to use when creating new threads.
     * handler: The handler to use when tasks cannot be executed.
     */
    public PriorityThreadPoolExecutor bulkBlockThread;
    public PriorityThreadPoolExecutor eventThread;
    public PriorityThreadPoolExecutor clickEventThread;
    WorldData worldInfo;

    public void startGameEvent(WorldData worldInfo) {
        eventThread = new PriorityThreadPoolExecutor(
                100, 1000,
                0L, TimeUnit.MILLISECONDS,
                new HighValueComparator());

        clickEventThread = new PriorityThreadPoolExecutor(
                100, 1000,
                0L, TimeUnit.MILLISECONDS,
                new HighValueComparator());

        bulkBlockThread = new PriorityThreadPoolExecutor(
                5, 5,
                100L, TimeUnit.MILLISECONDS,
                new HighValueComparator());

        this.worldInfo = worldInfo;
    }

    public void stopGameEvent() {
        events.clear();
        if (eventThread != null) eventThread.shutdown();
        if (bulkBlockThread != null) bulkBlockThread.shutdown();
    }


    int blockChangesThisFrame, lightChangesThisFrame;
    int framesThatHadEvents = 0;

    //By limiting the number of frames in a row we can prevent block events that are called in an infinite loop
    //Note that this does not prevent the pipeline from handling existing events, it just
    // prevents block.setBlockEvent() and block.onLocalChange() from being called
    final int MAX_FRAMES_WITH_EVENTS_IN_A_ROW = 10;

    public void update() {
        if (ClientWindow.devkeyF3 && Client.DEV_MODE)
            return;//Check to see if the block pipeline could be causing problems, It could also the the threads?

//        if (LocalClient.world.multiplayerPendingBlockChanges.periodicRangeSendCheck(5000)) {
//            int changes = LocalClient.world.multiplayerPendingBlockChanges.readApplicableChanges((worldPos, history) -> {
//                addEvent(worldPos, history);
//            });
//            ClientWindow.printlnDev("Loaded " + changes + " local changes");
//        }

        if (events.isEmpty()) {
            framesThatHadEvents = 0;
            return;
        }
        framesThatHadEvents++;

        boolean allowBlockEvents = framesThatHadEvents < MAX_FRAMES_WITH_EVENTS_IN_A_ROW;

        HashSet<Chunk> affectedChunks = new HashSet<>();
        ArrayList<ChunkNode> sunNode_OpaqueToTrans = new ArrayList<>();
        ArrayList<ChunkNode> sunNode_transToOpaque = new ArrayList<>();

        resolveQueue(allowBlockEvents, framesThatHadEvents,
                affectedChunks, sunNode_OpaqueToTrans, sunNode_transToOpaque);

        boolean multiThreadedMode = blockChangesThisFrame > 100 || lightChangesThisFrame > 20 ||
                sunNode_transToOpaque.size() > 10 || sunNode_OpaqueToTrans.size() > 50;

//        Main.printlnDev("\nUPDATING " + events.size() + " EVENTS: [  "
//                + "  frames in row=" + framesThatHadEvents
//                + "  block changes=" + blockChangesThisFrame
//                + "  light changes=" + lightChangesThisFrame
//                + "  opaque>trans=" + sunNode_OpaqueToTrans.size()
//                + "  trans>opaque=" + sunNode_transToOpaque.size()
//                + "  ]\tMultiThread: " + multiThreadedMode + " allowBlockEvents: " + allowBlockEvents);

        lightChangesThisFrame = 0;
        blockChangesThisFrame = 0;

        if (multiThreadedMode) {
            bulkBlockThread.submit(System.currentTimeMillis(),
                    () -> {
                        updateSunlightAndMeshes(affectedChunks, sunNode_OpaqueToTrans, sunNode_transToOpaque);
                    });
        } else {
            updateSunlightAndMeshes(affectedChunks, sunNode_OpaqueToTrans, sunNode_transToOpaque);
        }
    }


    private void resolveQueue(boolean allowBlockEvents, int framesInARow,
                              final HashSet<Chunk> affectedChunks,
                              final List<ChunkNode> sunNode_OpaqueToTrans,
                              final List<ChunkNode> sunNode_transToOpaque
    ) {

        HashMap<Vector3i, BlockHistory> eventsCopy;
        //We always run through each event the frame of/after the event was added
        synchronized (eventClearLock) {
            eventsCopy = new HashMap<>(events);
            events.clear(); //We want to clear the old events before iterating over and picking up new ones
        }

        eventsCopy.forEach((worldPos, blockHist) -> {
            if (!world.inYBounds(worldPos.y)) return;
            wcc.set(worldPos);
            Chunk chunk = world.getChunk(wcc.chunk);
            if (chunk == null) return;

            BlockType type = Registrys.blocks.getBlockType(blockHist.newBlock.type);
            BlockData newBlockData = null;
            if (blockHist.updateBlockData) {
                newBlockData = blockHist.newBlockData;
            } else {
                newBlockData = blockHist.newBlock.getInitialBlockData(
                        chunk.voxels.getBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z));
            }
            blockHist.newBlockData = newBlockData; //Store the new block data so that we can use it later

            if (!blockHist.previousBlock.equals(blockHist.newBlock)) { //If the 2 blocks are different
                if (blockHist.newBlock.allowExistence(worldPos.x, worldPos.y, worldPos.z) //Should we set the block?
                        && type.allowExistence(blockHist.newBlock, worldPos.x, worldPos.y, worldPos.z)) {

                    //set block
//                    if (!blockHist.fromNetwork)  //only send change if not from network
//                        Main.getServer().server.addBlockChange(worldPos, blockHist.newBlock, newBlockData);
                    chunk.markAsModified();
                    chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, blockHist.newBlock.id);

                    //set block data
                    blockHist.previousBlockData = chunk.voxels.getBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                    chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, newBlockData);

                    // <editor-fold defaultstate="collapsed" desc="update torchlight and add nodes for sunlight">
                    if (blockHist.previousBlock.opaque && !blockHist.newBlock.opaque) {
                        SunlightUtils.addNodeForPropagation(sunNode_OpaqueToTrans, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                        TorchUtils.opaqueToTransparent(affectedChunks, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);//TODO: We might need to optimize this by creating the nodes first and then propagating once
                    } else if (!blockHist.previousBlock.opaque && blockHist.newBlock.opaque) {
                        SunlightUtils.addNodeForErasure(sunNode_transToOpaque, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                        TorchUtils.transparentToOpaque(affectedChunks, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                    }

                    if (!blockHist.previousBlock.isLuminous() && blockHist.newBlock.isLuminous()) {
                        TorchUtils.setTorch(affectedChunks, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z,
                                blockHist.newBlock.torchlightStartingValue);
                    } else if (blockHist.previousBlock.isLuminous() && !blockHist.newBlock.isLuminous()) {
                        TorchUtils.removeTorch(affectedChunks, chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                    }
// </editor-fold>

                    affectedChunks.add(chunk);
                }
            } else { //If both blocks are the same, just update the block data
                if (!blockHist.fromNetwork) //only send change if not from network
//                    Main.getServer().server.addBlockChange(worldPos, blockHist.newBlock, newBlockData);

                blockHist.previousBlockData = chunk.voxels.getBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, newBlockData);
                chunk.markAsModified();
                affectedChunks.add(chunk);
            }
        });


        //Block events
        if (allowBlockEvents) {
            eventsCopy.forEach((worldPos, blockHist) -> {
                if (world.inYBounds(worldPos.y)) {
                    if (!blockHist.fromNetwork) {//Dont do block events if the block was set by the Main.getServer()

                        if (//TODO: Try to check for block data changes without setting off infinite recursion
                                blockHist.previousBlock != blockHist.newBlock //If the blocks are different
                        ) {
                            startLocalChange(worldPos, blockHist, allowBlockEvents);
                            Main.getServer().livePropagationHandler.addNode(worldPos, blockHist);
                            blockHist.previousBlock.run_RemoveBlockEvent(eventThread, worldPos, blockHist);
                            blockHist.newBlock.run_SetBlockEvent(eventThread, worldPos);
                        }
                    }
                }
            });
        }
//        Main.getServer().server.sendNearBlockChanges();
    }


    public void updateSunlightAndMeshes(HashSet<Chunk> affectedChunks,
                                        ArrayList<ChunkNode> sunNode_OpaqueToTrans,
                                        ArrayList<ChunkNode> sunNode_transToOpaque) {
//        Main.printlnDev("\tOpaque > trans: " + sunNode_OpaqueToTrans.size() + "; Trans > opaque: " + sunNode_transToOpaque.size());


        AtomicBoolean longSunlight = new AtomicBoolean(false);
        AtomicBoolean firstChunkUpdate = new AtomicBoolean(true);

        long elapsedMS = SunlightUtils.updateFromQueue(
                sunNode_OpaqueToTrans,
                sunNode_transToOpaque,
                affectedChunks,
                (time) -> {
                    if (time > 1000 && firstChunkUpdate.get()) {
                        updateAffectedChunks(affectedChunks);
                        firstChunkUpdate.set(false);
                    } else if (time > 3000 && !longSunlight.get()) {
                        Main.getClient().consoleOut("The lighting is being calculated. This may take a while.");
                        longSunlight.set(true);
                    }
                });

        if (longSunlight.get()) {
            Main.getClient().consoleOut("Sunlight calculation finished " + (elapsedMS / 1000) + "s");
        }

        //Resolve affected chunks
        updateAffectedChunks(affectedChunks);
        affectedChunks.clear();
    }

    private void updateAffectedChunks(HashSet<Chunk> affectedChunks) {
//        ClientWindow.printlnDev("Updating " + affectedChunks.size() + " chunks");
        for (Chunk chunk : affectedChunks) {
            //If a block was set next to something else, we have to acount for that in the mesh update
            //TODO: This mesh updating could be optimized by only updating the affected chunks
           // chunk.updateMesh(true, 0, 0, 0);
        }
    }


    private void startLocalChange(Vector3i originPos, BlockHistory hist, boolean dispatchBlockEvent) {
        checkAndStartBlock(originPos.x, originPos.y - 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y + 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x - 1, originPos.y, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x + 1, originPos.y, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y, originPos.z - 1, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y, originPos.z + 1, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x - 1, originPos.y - 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x + 1, originPos.y - 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y - 1, originPos.z - 1, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y - 1, originPos.z + 1, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x - 1, originPos.y + 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x + 1, originPos.y + 1, originPos.z, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y + 1, originPos.z - 1, originPos, hist, dispatchBlockEvent);
        checkAndStartBlock(originPos.x, originPos.y + 1, originPos.z + 1, originPos, hist, dispatchBlockEvent);
    }


    private void checkAndStartBlock(
            int nx, int ny, int nz,//Neighboring voxel that we notify of the change
            Vector3i originPos, //Where the change actually happened
            BlockHistory hist, //What changed
            boolean dispatchBlockEvent) {
        WCCi wcc = new WCCi().set(nx, ny, nz);
        Chunk chunk = wcc.getChunk(Client.world);
        if (chunk != null) {
            Block nBlock = Registrys.getBlock(chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z));//The block at the neighboring voxel
            if (nBlock != null && !nBlock.isAir()) {
                if (!Registrys.blocks.getBlockType(nBlock.type).allowExistence(hist.newBlock, nx, ny, nz)) {

                    //Set blocks that are not allowed here to air
                    short previousBlock = chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                    chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, BlockRegistry.BLOCK_AIR.id);
                    addEvent(wcc, new BlockHistory(previousBlock, BlockRegistry.BLOCK_AIR.id));

                } else if (dispatchBlockEvent) {
                    BlockHistory nhist = new BlockHistory(nBlock, nBlock);
                    Main.getServer().livePropagationHandler.addNode(new Vector3i(nx, ny, nz), nhist);
                    nBlock.run_LocalChangeEvent(eventThread, hist, originPos, new Vector3i(nx, ny, nz));
                }
            }
        }
    }

}
