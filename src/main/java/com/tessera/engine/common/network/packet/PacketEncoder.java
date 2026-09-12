package com.tessera.engine.common.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * There can only be one decoder,handler and encoder per channel.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        // Reserve space for the length prefix, then write id + payload, then
        // backfill the length. We must remember the placeholder index instead
        // of assuming it is 0: the buffer may be reused/pooled or contain
        // other data, and setInt(0, ...) would corrupt it.
        int lengthIndex = out.writerIndex();
        out.writeInt(0); // Placeholder for length, updated later
        int startIndex = out.writerIndex(); // Mark position

        out.writeByte(packet.id);  // Write packet ID
        packet.encode(ctx, packet, out); // Encode packet

        int endIndex = out.writerIndex();
        out.setInt(lengthIndex, endIndex - startIndex); // Update length at the placeholder
    }
}