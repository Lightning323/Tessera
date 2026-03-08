package com.tessera.engine.common.network.packet;

import com.tessera.engine.common.network.ChannelBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

import javax.imageio.stream.IIOByteBuffer;
import java.util.List;

public abstract class Packet {

    public final byte id;

    public Packet(int id) {
        this.id = (byte) id;
    }

    public abstract void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out);

    /**
     *
     * @param ctx         the channel
     * @param in          the packet buffer
     * @param packetsRead the packets that have been read from the buffer
     */
    public abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> packetsRead);

    public abstract void handleClientSide(ChannelBase ctx, Packet packet);

    public abstract void handleServerSide(ChannelBase ctx, Packet packet);


    public static void register(SocketChannel ch, Packet p) {
        PacketDecoder.PACKET_REGISTRY.put(p.id, p);
    }

    public static void register(Packet p) {
        PacketDecoder.PACKET_REGISTRY.put(p.id, p);
    }
}
