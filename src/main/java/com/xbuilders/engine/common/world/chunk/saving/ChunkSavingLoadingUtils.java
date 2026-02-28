/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.common.world.chunk.saving;

import com.esotericsoftware.kryo.io.Output;
import com.xbuilders.engine.server.entity.Entity;
import com.xbuilders.utils.bytes.ByteUtils;
import com.xbuilders.utils.bytes.SimpleKyro;
import com.xbuilders.engine.common.math.MathUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.Chunk;
import org.lwjgl.system.MemoryStack;

import static com.xbuilders.Main.LOGGER;


public class ChunkSavingLoadingUtils {
    //DONT IMPORT ANY of these variables into the chunk file reading class. We want the previous chunk file versions to work no matter what.
    public static final int ENTITY_DATA_MAX_BYTES = Integer.MAX_VALUE - 8; //this is the max size for a java array
    public static int BLOCK_DATA_MAX_BYTES = (int) (Math.pow(2, 16) - 1); //Unsigned short
    public static final int REMAINING_METADATA_BYTES = 96;
    public static final int LATEST_FILE_VERSION = 3;

    //Codes
    public static final byte CODE_DONE_READING_ENTITIES = -128;
    public static final byte[] ENDING_OF_CHUNK_FILE = "END_OF_CHUNK_FILE".getBytes();


    public static SimpleKyro kryo = new SimpleKyro();


    protected static void printSubList(byte[] bytes, int target, int radius) {
        int start = MathUtils.clamp(target - radius, 0, bytes.length - 1);
        int end = MathUtils.clamp(target + radius, 0, bytes.length - 1);
        String str = "🎯=" + target + "(";
        for (int i = start; i < end; i++) {
            if (i == target) {
                str += ("<" + bytes[i] + "> ");
            } else {
                str += (bytes[i] + " ");
            }
        }
        System.out.println(str + ")");
    }

    protected final static float maxMult16bits = (float) ((Math.pow(2, 10) / Chunk.WIDTH) - 1);


    public static File backupFile(File f) {
        return new File(f.getParentFile(), "backups\\" + f.getName());
    }

    public static boolean writeChunkToFile(final Chunk chunk, final File f) {
        //try (MemoryStack stack = MemoryStack.stackPush()) {
        //Rename the existing file to a backup if it exists
        if (f.exists()) renameToBackup(chunk, f);

        //Write the chunk
        try (FileOutputStream fos = new FileOutputStream(f)) {
            return writeChunk(chunk, fos);
        } catch (IOException e) {
            LOGGER.error("Failed to write chunk to file", e);
            return false;
        }
    }

    public static boolean writeChunk(final Chunk chunk, OutputStream fos) {
        try (GZIPOutputStream outStream = new GZIPOutputStream(fos)) {
            //Write metadata
            outStream.write(LATEST_FILE_VERSION);//Write the version of the file
            outStream.write(ByteUtils.longToBytes(chunk.lastModifiedTime));//Write Last modified time
            outStream.write(new byte[REMAINING_METADATA_BYTES]); //Write remaining metadata as empty bytes

            //Start writing real stuff
            Output out = new Output(outStream);


            //Write voxels first
            for (int y = chunk.voxels.size.y - 1; y >= 0; y--) {
                for (int x = 0; x < chunk.voxels.size.x; ++x) {
                    for (int z = 0; z < chunk.voxels.size.z; ++z) {

                        short blockID = chunk.voxels.getBlock(x, y, z); //Write block id
                        out.writeShort(blockID);

                        byte light = chunk.voxels.getPackedLight(x, y, z); //Write light
                        out.writeByte(light);

                        final BlockData blockData = chunk.voxels.getBlockData(x, y, z); //Write block data
                        if (blockData == null) kryo.writeByteArrayShort(out, new byte[0]);
                        else kryo.writeByteArrayShort(out, blockData.toByteArray());
                    }
                }
            }

            //Write allEntities last
            //By making these last, there is less trouble if we dont know when to stop reading allEntities
            for (int i = 0; i < chunk.entities.list.size(); i++) {
                Entity entity = chunk.entities.list.get(i);
                out.writeString(entity.getId()); //write entity id
                out.writeLong(entity.getUniqueIdentifier()); //Write entity uuid

                entity.updatePosition();  //Write position (unsigned short)
                out.writeShort((short) (entity.chunkPosition.chunkVoxel.x * maxMult16bits) & 0xFFFF);
                out.writeShort((short) (entity.chunkPosition.chunkVoxel.y * maxMult16bits) & 0xFFFF);
                out.writeShort((short) (entity.chunkPosition.chunkVoxel.z * maxMult16bits) & 0xFFFF);

                //Write entity data
                byte[] entityBytes = entity.serializeDefinitionData();
                if (entityBytes == null) entityBytes = new byte[0];
                kryo.writeByteArray(out, entityBytes);
            }
            out.writeByte(CODE_DONE_READING_ENTITIES);


            //Write ending flag
            out.write(ENDING_OF_CHUNK_FILE);
            out.close();

        } catch (Exception e) {
            LOGGER.info("Error", e);
            return false;
        }
        // }
        return true;
    }

    /**
     * In order to ensure safe saving, we need to:
     * 1. rename the old file first
     * 2. write the new file
     * <p>
     * Eventually we could delete the old file, but for now we will just rename it
     */
    private static boolean renameToBackup(Chunk chunk, File f) {
        File backupFile = backupFile(f);

        //Delete old backup if it exists
        if (backupFile.exists()) {
            try {
                Files.delete(backupFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Rename old file
        //https://stackoverflow.com/questions/13826045/file-renameto-fails
        try {
            //make sure the directory exists
            if (!backupFile.getParentFile().exists()) backupFile.getParentFile().mkdirs();
            //Files.move is a more reliable way to do this
            Files.move(f.toPath(), backupFile.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static long getLastSaved(File f) {
        try (FileInputStream fis = new FileInputStream(f);
             GZIPInputStream input = new GZIPInputStream(fis)) {
            int fileVersion = input.read();
            return ByteUtils.bytesToLong(input.readNBytes(Long.BYTES));
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean fileIsComplete(File f) {
        try (FileInputStream fis = new FileInputStream(f);
             GZIPInputStream input = new GZIPInputStream(fis)) {
            int fileVersion = input.read();
            final byte[] bytes = input.readAllBytes();

            //Any version less than 2 doesnt have an ending
            return fileVersion < 2 || hasEnding(bytes);

        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasEnding(byte[] allRemainingBytse) {
        byte[] endingBytes = new byte[ENDING_OF_CHUNK_FILE.length];

        if (endingBytes.length > allRemainingBytse.length) return false;

        System.arraycopy(allRemainingBytse,
                allRemainingBytse.length - endingBytes.length,
                endingBytes,
                0, endingBytes.length);
        return Arrays.equals(endingBytes, ENDING_OF_CHUNK_FILE);
    }

    public static int getFileVersion(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f); GZIPInputStream input = new GZIPInputStream(fis)) {
            //Read the file version
            return input.read();
        }
    }


    public static boolean readChunkFromFile(final Chunk chunk, final File f) {
        AtomicBoolean fileReadCorrectly = new AtomicBoolean(false);
        AtomicBoolean hasDetectedIfFileWasReadCorrectly = new AtomicBoolean(false);

        try (FileInputStream fis = new FileInputStream(f)) {
            return readChunk(chunk, fis,
                    fileReadCorrectly,
                    hasDetectedIfFileWasReadCorrectly);

        } catch (ChunkReadingException ex) {
            File backupFile = backupFile(f);

            //Create the log
            LOGGER.warn( "Error occurred reading chunk: " + chunk +
                    " \nBackup File Exists: " + backupFile.exists() +
                    (hasDetectedIfFileWasReadCorrectly.get() ? " \nFile Read Correctly: " + fileReadCorrectly : ""), ex);

            if (!fileReadCorrectly.get()) {
                //Load from backup
                if (backupFile.exists()) {
                    System.out.println("Loading " + chunk + " from backup");
                    chunk.voxels.reset();
                    return readChunkFromFile(chunk, backupFile);
                }
            }
            return false;
        } catch (IOException ex) {
            File backupFile = backupFile(f);

            //Create the log
            LOGGER.warn( "Error occurred reading chunk: " + chunk +
                    " \nBackup File Exists: " + backupFile.exists() +
                    (hasDetectedIfFileWasReadCorrectly.get() ? " \nFile Read Correctly: " + fileReadCorrectly : ""), ex);

            //Load from backup
            if (backupFile.exists()) {
                System.out.println("Loading " + chunk + " from backup");
                chunk.voxels.reset();
                return readChunkFromFile(chunk, backupFile);
            }
            return false;
        }
    }

    public static boolean readChunk(final Chunk chunk, final InputStream fis,
                                    AtomicBoolean fileReadCorrectly,
                                    AtomicBoolean hasDetectedIfFileWasReadCorrectly) throws IOException, ChunkReadingException {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            try (GZIPInputStream input = new GZIPInputStream(fis)) {
                //Read metadata
                int fileVersion = input.read(); //Read the file version
                chunk.lastModifiedTime = ByteUtils.bytesToLong(input.readNBytes(Long.BYTES)); //Read the last modified time
                //Read the remaining metadata (How many more bytes this version of the file has before we read the real stuff)

                switch (fileVersion) {
                    case 0 -> input.readNBytes(ChunkFile_V0.REMAINING_METADATA_BYTES);
                    case 1 -> input.readNBytes(ChunkFile_V1.REMAINING_METADATA_BYTES);
                    case 2 -> input.readNBytes(ChunkFile_V1.REMAINING_METADATA_BYTES);
                    default -> input.readNBytes(ChunkFile_V2.REMAINING_METADATA_BYTES);
                }

                //read all bytes
                final byte[] bytes = input.readAllBytes();

                //Read all ending bytes to see if the file was read correctly
                fileReadCorrectly.set(fileVersion < 2 || hasEnding(bytes));
                hasDetectedIfFileWasReadCorrectly.set(true);

                try {
                    if (bytes.length > 0) {
                        switch (fileVersion) {
                            case 0 -> ChunkFile_V0.readChunk(chunk, bytes);
                            case 1 -> ChunkFile_V1.readChunk(chunk, bytes);
                            case 2 -> ChunkFile_V1.readChunk(chunk, bytes);
                            default -> ChunkFile_V2.readChunk(chunk, bytes);
                        }
                    } else throw new IllegalStateException("File is empty past metadata!");
                } catch (Exception ex) {
                    throw new ChunkReadingException(ex);
                }
            }
        }
        return true;
    }
}
