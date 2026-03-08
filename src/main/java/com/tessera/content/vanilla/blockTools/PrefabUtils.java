package com.tessera.content.vanilla.blockTools;

import com.tessera.utils.bytes.ByteUtils;
import com.tessera.utils.FileDialog;
import com.tessera.utils.resource.PathHandler;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import org.joml.Vector3i;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tessera.Main.LOGGER;
import static com.tessera.engine.common.world.chunk.saving.ChunkSavingLoadingUtils.BLOCK_DATA_MAX_BYTES;
import static com.tessera.utils.bytes.ByteUtils.bytesToShort;
import static com.tessera.utils.bytes.ByteUtils.shortToBytes;

public class PrefabUtils {


    public static void writeBlockData(BlockData data, OutputStream out) throws IOException {
        if (data == null) {
            out.write(new byte[]{0, 0});//Just write 0 for the length
            return;
        }

        if (data.size() > BLOCK_DATA_MAX_BYTES) {
            LOGGER.info("Block data too large: " + data.size());
            out.write(new byte[]{0, 0});//Just write 0 for the length
            return;
        }
        //First write the length of the block data as an unsigned short
        out.write(shortToBytes(data.size() & 0xffff));

        //Then write the bytes
        byte[] bytes = data.toByteArray();
        out.write(bytes);
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
            LOGGER.info("error", e);
            return null; //Catch the error just to be safe
        }
    }


    public static void toBytes(ChunkVoxels blocks, OutputStream out) throws IOException {
        //Write the size first!
        out.write(ByteUtils.shortToBytes(blocks.size.x));
        out.write(ByteUtils.shortToBytes(blocks.size.y));
        out.write(ByteUtils.shortToBytes(blocks.size.z));

        for (int x = 0; x < blocks.size.x; x++) {
            for (int y = 0; y < blocks.size.y; y++) {
                for (int z = 0; z < blocks.size.z; z++) {
                    short block = blocks.getBlock(x, y, z);
                    out.write(ByteUtils.shortToBytes(block));//Block
                    writeBlockData(blocks.getBlockData(x, y, z), out); //Block data
                }
            }
        }
    }

    public static ChunkVoxels fromBytes(byte[] bytes, int start, int end) {
        Vector3i size = new Vector3i(0, 0, 0);
        //Read and load the size first!
        size.x = ByteUtils.bytesToShort(bytes[start], bytes[start + 1]);
        size.y = ByteUtils.bytesToShort(bytes[start + 2], bytes[start + 3]);
        size.z = ByteUtils.bytesToShort(bytes[start + 4], bytes[start + 5]);

        if (size.x == 0 || size.y == 0 || size.z == 0) {
            return new ChunkVoxels(0, 0, 0);
        }

        ChunkVoxels data = new ChunkVoxels(size.x, size.y, size.z);


        AtomicInteger startIndex = new AtomicInteger(start + 6);
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    short block = (short) ByteUtils.bytesToShort(bytes[startIndex.get()], bytes[startIndex.get() + 1]);
                    data.setBlock(x, y, z, block);
                    startIndex.set(startIndex.get() + 2);

                    BlockData blockData = readBlockData(bytes, startIndex);
                    data.setBlockData(x, y, z, blockData);
                }
            }
        }
        return data;
    }


    public static ChunkVoxels loadPrefabFromFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return fromBytes(bytes, 0, bytes.length);
    }

    public static void savePrefabToFile(ChunkVoxels data, File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toBytes(data, baos);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        Files.write(file.toPath(), baos.toByteArray());
    }

    public static ChunkVoxels loadPrefabFromFileDialog() {
        File outFile = FileDialog.fileDialog((fd) -> {
            File prefabFolder = PathHandler.appDataPath("prefabs");
            if (!prefabFolder.exists()) {
                prefabFolder.mkdirs();
            }
            fd.setDirectory(prefabFolder.getAbsolutePath());
            fd.setFilenameFilter(filter);
            fd.setFile("*.xbprefab");
            fd.setMode(java.awt.FileDialog.LOAD);
        });

        if (outFile != null) {
            try {
                return loadPrefabFromFile(outFile);
            } catch (IOException e) {
                LOGGER.info("error", e);
            }
        }
        return new ChunkVoxels(0, 0, 0);
    }

    static FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".xbprefab");
        }
    };

    public static void savePrefabToFileDialog(ChunkVoxels data) {
        FileDialog.fileDialog((fd) -> {
            File prefabFolder = PathHandler.appDataPath("prefabs");
            if (!prefabFolder.exists()) {
                prefabFolder.mkdirs();
            }
            fd.setDirectory(prefabFolder.getAbsolutePath());
            fd.setFilenameFilter(filter);
            fd.setFile("*.xbprefab");
            fd.setMode(java.awt.FileDialog.SAVE);
        }, (file) -> {
            if (file == null) {
                return;
            }
            try {
                PrefabUtils.savePrefabToFile(data, file);
            } catch (IOException e) {
                LOGGER.info("error", e);
            }
        });
    }


}
