package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.world.chunk.ServerChunk;
import com.tessera.engine.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.joml.Vector3i;

import java.util.List;

public class ChunkRequestPacket extends Packet {

    Vector3i requestedCoordinates;
    float distToPlayer;

    public ChunkRequestPacket() {
        super(AllPackets.CHUNK_REQUEST);
    }

    public ChunkRequestPacket(Vector3i requestedCoordinates, float distToPlayer) {
        super(AllPackets.CHUNK_REQUEST);
        this.requestedCoordinates = requestedCoordinates;
        this.distToPlayer = distToPlayer;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        ChunkRequestPacket packetInstance = (ChunkRequestPacket) packet;
        //Write the chunk request
        out.writeInt(packetInstance.requestedCoordinates.x);
        out.writeInt(packetInstance.requestedCoordinates.y);
        out.writeInt(packetInstance.requestedCoordinates.z);

        out.writeFloat(packetInstance.distToPlayer);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //Read the chunk request
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        Vector3i requestedCoordinates = new Vector3i(x, y, z);

        float distToPlayer = in.readFloat();

        out.add(new ChunkRequestPacket(requestedCoordinates, distToPlayer));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        ChunkRequestPacket packetInstance = (ChunkRequestPacket) packet;
        Server server = Main.getServer();
        if (server == null) return;

        //Make the chunk on the server first
        ServerChunk chunk = server.world.addChunk(packetInstance.requestedCoordinates);

        //Then generate it, unicasting back to the requester when possible.
        server.world.generateChunk(chunk, packetInstance.distToPlayer, ctx);
    }
}
