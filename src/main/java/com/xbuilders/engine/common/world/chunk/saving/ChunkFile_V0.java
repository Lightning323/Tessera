package com.xbuilders.engine.common.world.chunk.saving;

import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.Chunk;
import org.joml.Vector3f;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xbuilders.utils.bytes.ByteUtils.*;

public class ChunkFile_V0 {

    public static final byte NEWLINE_BYTE = Byte.MIN_VALUE;
    public static final byte VOXEL_BYTE = -127;
    public static final byte ENTITY_BYTE = -126;
    public static final byte BYTE_SKIP_ALL_VOXELS = -125;
    protected final static float maxMult16bits = (float) ((Math.pow(2, 10) / Chunk.WIDTH) - 1);
    public static final int REMAINING_METADATA_BYTES = 1;



    static void readChunk(final Chunk chunk, byte[] bytes) throws IOException {
        AtomicInteger start = new AtomicInteger(0);

        //Load the allEntities
        boolean hasEntities = false;
        while (bytes[start.get()] == ENTITY_BYTE) {
            makeEntity(chunk, bytes, start);
            hasEntities = true;
        }
        if (hasEntities) {
            start.set(start.get() + 1);  //We have to move 1 byte past the entity newline byte to start reading voxels
        }
//                System.out.println("Voxels start byte: " + printSubList(bytes, start.get(), 10));

        //Load the voxels
        chunkVoxels:
        for (int y = chunk.voxels.size.y - 1; y >= 0; y--) {
            for (int x = 0; x < chunk.voxels.size.x; ++x) {
                for (int z = 0; z < chunk.voxels.size.z; ++z) {
                    final byte startByte = bytes[start.get()];
                    if (startByte == BYTE_SKIP_ALL_VOXELS) {
                        start.set(start.get() + 1);
                        continue chunkVoxels;
                    } else if (startByte == NEWLINE_BYTE) { //Newline means end of voxel
                        start.set(start.get() + 1);
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
//        System.out.println("Reading as " + x + ", " + y + ", " + z);
        start.set(start.get() + 6);
        return new Vector3f(x, y, z);
    }

    protected static void makeEntity(Chunk chunk, final byte[] bytes, AtomicInteger start) {
////        System.out.println("\nStarting to read entity: " + printSubList(bytes, start.get(), 5));
//        final short entityID = (short) bytesToShort(bytes[start.get() + 1], bytes[start.get() + 2]);
//        EntitySupplier link = Registrys.getEntity(entityID);
//        start.set(start.get() + 3);
//
//        //Read position
//        Vector3f chunkVox = readChunkVoxelCoords(start, bytes);
//
//        //Read entity data
//        ByteArrayOutputStream entityBytes = new ByteArrayOutputStream();
//        while (true) {
//            final byte b = bytes[start.get()];
//            start.set(start.get() + 1);
//            if (b == NEWLINE_BYTE) {
//                break;
//            } else {
//                entityBytes.write(b);
//            }
//        }
//
//        if (bytes[start.get()] != ENTITY_BYTE) {
//            start.set(start.get() - 1);
//        }
//        return chunk.allEntities.placeNew(link, 0,
//                chunkVox.x + chunk.position.x * Chunk.WIDTH,
//                chunkVox.y + chunk.position.y * Chunk.WIDTH,
//                chunkVox.z + chunk.position.z * Chunk.WIDTH,
//                entityBytes.toByteArray());
    }

    protected static void readVoxel(
            final byte[] bytes,
            final Chunk chunk, final int x, final int y,
            final int z, AtomicInteger start) {

        //Read light
        chunk.voxels.setPackedLight(x, y, z, bytes[start.get() + 1]);
        //Read block id
        final short blockID = (short) bytesToShort(bytes[start.get() + 2], bytes[start.get() + 3]);
        chunk.voxels.setBlock(x, y, z, blockID);
        start.set(start.get() + 4);

        final ByteArrayOutputStream blockDataBytes = new ByteArrayOutputStream();
        while (true) {
            final byte b3 = bytes[start.get()];
            if (b3 == NEWLINE_BYTE) {
                break;
            }
            blockDataBytes.write(b3);
            start.set(start.get() + 1);
        }
        byte[] blockData = blockDataBytes.toByteArray();
        if (blockData.length != 0) {
            BlockData data = new BlockData(blockData);
            chunk.voxels.setBlockData(x, y, z, data);
        }
        start.set(start.get() + 1);
    }

}
