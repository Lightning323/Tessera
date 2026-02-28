package com.tessera.engine.common.world.chunk.saving;

import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.utils.bytes.ByteUtils;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tessera.Main.LOGGER;
import static com.tessera.utils.bytes.ByteUtils.bytesToInt;
import static com.tessera.utils.bytes.ByteUtils.bytesToShort;

public class ChunkFile_V1 {

    public static final byte START_READING_VOXELS = -128;
    public static final byte BYTE_SKIP_ALL_VOXELS = -125;
    protected final static float maxMult16bits = (float) ((Math.pow(2, 10) / Chunk.WIDTH) - 1);
    public static final int REMAINING_METADATA_BYTES = 56;


    static void readChunk(final Chunk chunk, byte[] bytes) throws IOException {
        final AtomicInteger start = new AtomicInteger(0);

        //Load the allEntities
        while (true) {
            if (bytes[start.get()] == START_READING_VOXELS) { //This flags the end of the allEntities
                start.set(start.get() + 1);
                break;
            }
            makeEntity(chunk, bytes, start);
        }


        //Load the voxels
        chunkVoxels:
        for (int y = chunk.voxels.size.y - 1; y >= 0; y--) {
            for (int x = 0; x < chunk.voxels.size.x; ++x) {
                for (int z = 0; z < chunk.voxels.size.z; ++z) {
                    final byte startByte = bytes[start.get()];
                    if (startByte == BYTE_SKIP_ALL_VOXELS) {
                        start.set(start.get() + 1);
                        continue chunkVoxels;
                    } else { //Every non air voxel starts with pipe byte
                        readVoxel(bytes, chunk, x, y, z, start);
                    }
                }
            }
        }

    }


    private static Vector3f readChunkVoxelCoords(AtomicInteger start, byte[] bytes) {
        float x = bytesToShort(bytes[start.get()], bytes[start.get() + 1]);
        float y = bytesToShort(bytes[start.get() + 2], bytes[start.get() + 3]);
        float z = bytesToShort(bytes[start.get() + 4], bytes[start.get() + 5]);
        x = x / maxMult16bits;
        y = y / maxMult16bits;
        z = z / maxMult16bits;
        start.set(start.get() + 6);
        return new Vector3f(x, y, z);
    }

    public static byte[] readEntityData(byte[] bytes, AtomicInteger start) {
        int length = bytesToInt(bytes[start.get()], bytes[start.get() + 1], bytes[start.get() + 2], bytes[start.get() + 3]);
        start.set(start.get() + 4);

        byte[] data = new byte[length];
        System.arraycopy(bytes, start.get(), data, 0, length);
        start.set(start.get() + length);

        return data;
    }

    protected static void makeEntity(Chunk chunk, final byte[] bytes, AtomicInteger start) {
        final short entityID = (short) bytesToShort(bytes[start.get()], bytes[start.get() + 1]); //read entity id

        start.set(start.get() + 2);

        long identifier = ByteUtils.bytesToLong(bytes, start); //read entity identifier

        Vector3f chunkVox = readChunkVoxelCoords(start, bytes);  //Read position
        byte[] entityData = readEntityData(bytes, start);//Read entity data

        //        EntitySupplier link = Registrys.getEntity(entityID);
//         chunk.allEntities.placeNew(link, identifier,
//                chunkVox.x + chunk.position.x * Chunk.WIDTH,
//                chunkVox.y + chunk.position.y * Chunk.WIDTH,
//                chunkVox.z + chunk.position.z * Chunk.WIDTH,
//                entityData);
    }

    public static BlockData readBlockData(byte[] bytes, AtomicInteger start) {
        //Get the length from unsigned short to int
        int length = bytesToShort(bytes[start.get()], bytes[start.get() + 1]) & 0xffff;
        start.set(start.get() + 2);

        try {
            //Read the bytes
            byte[] data = new byte[length];
            System.arraycopy(bytes, start.get(), data, 0, length);
            start.set(start.get() + length);
            return new BlockData(data);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.info("Error", e);
            return null; //Catch the error just to be safe
        }
    }

    protected static void readVoxel(
            final byte[] bytes,
            final Chunk chunk, final int x, final int y, final int z,
            AtomicInteger start) {

        final short blockID = (short) bytesToShort(bytes[start.get()], bytes[start.get() + 1]);     //Read block id
        chunk.voxels.setBlock(x, y, z, blockID);

        chunk.voxels.setPackedLight(x, y, z, bytes[start.get() + 2]);   //Read light
        start.set(start.get() + 3);

        if (blockID != BlockRegistry.BLOCK_AIR.id) { //We dont read block data if the block is air
            BlockData blockData = readBlockData(bytes, start); //Read block data
            if (blockData != null && blockData.size() > 0) {
                chunk.voxels.setBlockData(x, y, z, blockData);
            }
        }


    }


}
