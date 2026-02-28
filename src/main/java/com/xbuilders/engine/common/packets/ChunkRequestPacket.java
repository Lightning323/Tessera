package com.xbuilders.engine.common.packets;

import com.xbuilders.Main;
import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.packet.Packet;
import com.xbuilders.utils.MiscUtils;
import com.xbuilders.engine.common.world.chunk.ServerChunk;
import com.xbuilders.engine.server.Server;
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
        System.out.println("Requesting chunk at " + MiscUtils.printVec(requestedCoordinates));
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

        //Make the chunk on the server first
        ServerChunk chunk = server.world.addChunk(packetInstance.requestedCoordinates);

        //Then generate it
        server.world.generateChunk(chunk, distToPlayer);
    }
}
