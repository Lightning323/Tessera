package com.tessera.engine.common.world.chunk.saving;

import com.esotericsoftware.kryo.io.Input;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3f;

import java.io.IOException;

import static com.tessera.engine.common.world.chunk.saving.ChunkSavingLoadingUtils.kryo;

public class ChunkFile_V2 {
    public static final byte DONE_READING_ENTITIES = -128;
    protected final static float maxMult16bits = (float) ((Math.pow(2, 10) / Chunk.WIDTH) - 1);
    public static final int REMAINING_METADATA_BYTES = 96;
    final static Object errorLock = new Object();


    static void readChunk(final Chunk chunk, byte[] bytes) throws IOException {
        Input input = new Input(bytes);
//        System.out.println("Input position: " + input.position() + " Input limit: " + input.limit() + " input available: " + input.available());

        try {

            for (int y = chunk.voxels.size.y - 1; y >= 0; y--) {   //Load the voxels
                for (int x = 0; x < chunk.voxels.size.x; ++x) {
                    for (int z = 0; z < chunk.voxels.size.z; ++z) {

                        //read a voxel
                        short id = input.readShort(); //read id
                        chunk.voxels.setBlock(x, y, z, id);

                        byte light = input.readByte(); //read light
                        chunk.voxels.setPackedLight(x, y, z, light);

                        byte[] bData = kryo.readByteArrayShort(input); //Read block data
                        if (bData.length > 0) {
                            chunk.voxels.setBlockData(x, y, z, new BlockData(bData));
                        }

                    }
                }
            }


            while (true) {  //Load the allEntities
                int currentPosition = input.position(); // Save the current position
                //If there are no bytes to read or, if the next byte is the end of the allEntities flag
                if (input.available() == 0 || input.readByte() == DONE_READING_ENTITIES) { //This flags the end of the allEntities
                    //System.out.println("End of allEntities");
                    break;
                } else input.setPosition(currentPosition); // Restore the position

                //Read an entity
                String id = input.readString();//read entity id
                long identifier = input.readLong(); //read entity identifier

                float cx = input.readShortUnsigned() / maxMult16bits;//Read position
                float cy = input.readShortUnsigned() / maxMult16bits;
                float cz = input.readShortUnsigned() / maxMult16bits;
                Vector3f chunkVox = new Vector3f(cx, cy, cz);

                byte[] entityData = kryo.readByteArray(input);//Read entity data

                EntitySupplier link = Registrys.getEntity(id);
                chunk.entities.placeNew(link, identifier,
                        chunkVox.x + chunk.position.x * Chunk.WIDTH,
                        chunkVox.y + chunk.position.y * Chunk.WIDTH,
                        chunkVox.z + chunk.position.z * Chunk.WIDTH,
                        entityData);
            }

        } catch (Exception e) {
            synchronized (errorLock) {
                System.out.println("\n\n\nCHUNK_DATA V2 EXCEPTION:");
                ChunkSavingLoadingUtils.printSubList(input.getBuffer(), input.position(), 10);
                StackTraceElement[] stackTrace = e.getStackTrace();
                System.out.println("Exception: " + e + ", stack trace length: " + stackTrace.length);
                for (StackTraceElement stackTraceElement : stackTrace) {
                    System.out.println(stackTraceElement);
                }
                throw e;
            }
        }
    }


}
