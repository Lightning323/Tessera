package com.xbuilders.engine.common.packets;

import com.xbuilders.Main;
import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.packet.Packet;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.world.chunk.ClientChunk;
import com.xbuilders.engine.common.world.chunk.saving.ChunkReadingException;
import com.xbuilders.engine.common.world.chunk.saving.ChunkSavingLoadingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.joml.Vector3i;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This packet is all about sending chunks to the client
 * The server injects the data into a chunk on the client
 */
public class ChunkDataPacket extends Packet {

    public Vector3i chunkPosition;
    public byte[] chunkData;

    public ChunkDataPacket() {
        super(AllPackets.CHUNK_DATA);
    }

    /**
     * Load the chunk into bytes
     *
     * @param chunk the chunk to have binary data uploaded
     */
    public ChunkDataPacket(Chunk chunk) {
        super(AllPackets.CHUNK_DATA);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChunkSavingLoadingUtils.writeChunk(chunk, out);
        this.chunkData = out.toByteArray();
        this.chunkPosition = chunk.position;
    }

    public ChunkDataPacket(Vector3i chunkPosition, byte[] chunkData) {
        super(AllPackets.CHUNK_DATA);
        this.chunkPosition = chunkPosition;
        this.chunkData = chunkData;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        ChunkDataPacket packetInstance = (ChunkDataPacket) packet;

        //Write the chunk position
        out.writeInt(packetInstance.chunkPosition.x);
        out.writeInt(packetInstance.chunkPosition.y);
        out.writeInt(packetInstance.chunkPosition.z);

        //Write the chunk data
        out.writeInt(packetInstance.chunkData.length);
        out.writeBytes(packetInstance.chunkData);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //Read the chunk position
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        Vector3i chunkPosition = new Vector3i(x, y, z);

        //Read the chunk data
        int length = in.readInt();
        byte[] chunkData = new byte[length];
        in.readBytes(chunkData);

        //Add the packet
        out.add(new ChunkDataPacket(chunkPosition, chunkData));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        ChunkDataPacket packetInstance = (ChunkDataPacket) packet;

        //Create or get the chunk
        ClientChunk chunk = Main.getClient().world.addChunk(packetInstance.chunkPosition);


        //Set the data to the chunk
        AtomicBoolean fileReadCorrectly = new AtomicBoolean(false);
        AtomicBoolean hasDetectedIfFileWasReadCorrectly = new AtomicBoolean(false);
        try {
            ChunkSavingLoadingUtils.readChunk(chunk, new ByteArrayInputStream(chunkData),
                    fileReadCorrectly,
                    hasDetectedIfFileWasReadCorrectly);
            chunk.markAsModified();
            chunk.progressGenState(ClientChunk.GEN_VOXELS_GENERATED);
        } catch (IOException | ChunkReadingException e) {
            Main.LOGGER.warn("Failed to read chunk data", e);
        }
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {

    }
}
