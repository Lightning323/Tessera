package com.tessera.engine.common.worldInteraction.block;

import com.tessera.engine.common.network.packet.PacketDecoder;
import com.tessera.engine.common.network.packet.PacketEncoder;
import com.tessera.engine.common.network.netty.NettyServer;
import com.tessera.engine.common.packets.AllPackets;
import com.tessera.engine.common.packets.BlockUpdatePacket;
import com.tessera.engine.common.packets.ChunkDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.joml.Vector3i;

/**
 * Verifies the full Netty framing path (PacketEncoder length prefix +
 * LengthFieldBasedFrameDecoder + PacketDecoder) for small block packets and
 * large chunk-sized payloads.
 *
 * <p>Run via {@code java ... PacketFramingTest}. The old 2KB
 * {@code MAX_FRAME_SIZE} dropped every chunk; this test fails if the limit is
 * ever regressed below ~256KB.
 */
public final class PacketFramingTest {

    private PacketFramingTest() {
    }

    public static void main(String[] args) {
        AllPackets.registerPackets();
        int failures = 0;
        failures += check("small BlockUpdate survives framing", () -> {
            BlockUpdatePacket in = new BlockUpdatePacket(1, 2, 3, (short) 5, new byte[]{9, 8});
            BlockUpdatePacket out = framingRoundTrip(in, BlockUpdatePacket.class);
            if (out.x != 1 || out.blockId != 5) throw new AssertionError("mismatch");
            if (out.blockDataBytes.length != 2) throw new AssertionError("data loss");
        });
        failures += check("large ChunkData survives framing (64KB)", () -> {
            byte[] big = new byte[64 * 1024];
            for (int i = 0; i < big.length; i++) big[i] = (byte) (i * 31);
            ChunkDataPacket in = new ChunkDataPacket(new Vector3i(1, 2, 3), big);
            ChunkDataPacket out = framingRoundTrip(in, ChunkDataPacket.class);
            if (out.chunkData.length != big.length) {
                throw new AssertionError("length " + out.chunkData.length + " != " + big.length);
            }
            for (int i = 0; i < big.length; i++) {
                if (out.chunkData[i] != big[i]) throw new AssertionError("byte " + i + " corrupt");
            }
            if (!out.chunkPosition.equals(new Vector3i(1, 2, 3))) {
                throw new AssertionError("position corrupt: " + out.chunkPosition);
            }
        });
        failures += check("MAX_FRAME_SIZE fits chunks", () -> {
            if (NettyServer.MAX_FRAME_SIZE < 256 * 1024) {
                throw new AssertionError("MAX_FRAME_SIZE=" + NettyServer.MAX_FRAME_SIZE + " too small for chunks");
            }
        });
        if (failures > 0) {
            System.out.println("PacketFramingTest FAILED: " + failures + " failure(s)");
            System.exit(1);
        }
        System.out.println("PacketFramingTest PASSED (framing + large payloads ok)");
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static int check(String name, ThrowingRunnable r) {
        try {
            r.run();
            System.out.println("[PASS] " + name);
            return 0;
        } catch (Throwable t) {
            System.out.println("[FAIL] " + name + ": " + t);
            t.printStackTrace(System.out);
            return 1;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T framingRoundTrip(com.tessera.engine.common.network.packet.Packet in, Class<T> type) {
        // Outbound: Packet -> bytes (with 4-byte length prefix).
        EmbeddedChannel outbound = new EmbeddedChannel(new PacketEncoder());
        ByteBuf framed = null;
        EmbeddedChannel inbound = null;
        try {
            if (!outbound.writeOutbound(in)) throw new AssertionError("encoder produced nothing");
            framed = outbound.readOutbound();
            if (framed == null) throw new AssertionError("no framed bytes");
            // Inbound: framed bytes -> Packet. Only framing + decode (no
            // PacketHandler, which would consume the message and need a Client).
            inbound = new EmbeddedChannel(
                    new LengthFieldBasedFrameDecoder(NettyServer.MAX_FRAME_SIZE, 0, 4, 0, 4),
                    new PacketDecoder((com.tessera.engine.common.network.netty.NettyServer) null));
            if (!inbound.writeInbound(framed.retain())) {
                throw new AssertionError("decoder produced nothing (frame dropped? size=" + framed.readableBytes() + ")");
            }
            Object decoded = inbound.readInbound();
            if (decoded == null) throw new AssertionError("no inbound message");
            if (!type.isInstance(decoded)) {
                throw new AssertionError("decoded as " + decoded.getClass() + ", expected " + type);
            }
            return (T) decoded;
        } finally {
            if (framed != null) framed.release();
            if (inbound != null) inbound.finishAndReleaseAll();
            outbound.finishAndReleaseAll();
        }
    }
}
