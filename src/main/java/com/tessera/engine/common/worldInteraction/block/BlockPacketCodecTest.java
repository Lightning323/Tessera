package com.tessera.engine.common.worldInteraction.block;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.network.packet.PacketDecoder;
import com.tessera.engine.common.packets.AllPackets;
import com.tessera.engine.common.packets.BlockBreakRequestPacket;
import com.tessera.engine.common.packets.BlockPlaceRequestPacket;
import com.tessera.engine.common.packets.BlockUpdatePacket;
import com.tessera.engine.common.packets.GameStatePacket;
import com.tessera.engine.common.world.chunk.BlockData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * Headless checks for the block-edit packets. No window, GL, world, or server
 * required: every test encodes a packet to bytes and decodes it again, exactly
 * as {@code PacketEncoder} + {@code LengthFieldBasedFrameDecoder} +
 * {@code PacketDecoder} do on the wire.
 *
 * <p>Run from an IDE or {@code java ... BlockPacketCodecTest}. Returns
 * non-zero exit on failure so CI can gate on it. The in-game
 * {@link BlockInteractionTester} covers live singleplayer/multiplayer
 * convergence on top of these codec guarantees.
 */
public final class BlockPacketCodecTest {

    private BlockPacketCodecTest() {
    }

    public static void main(String[] args) {
        AllPackets.registerPackets();
        int failures = 0;
        failures += check("break roundtrip", () -> {
            BlockBreakRequestPacket in = new BlockBreakRequestPacket(12, -34, 56);
            BlockBreakRequestPacket out = roundTrip(in, BlockBreakRequestPacket.class);
            assertEq(in.x, out.x, "x");
            assertEq(in.y, out.y, "y");
            assertEq(in.z, out.z, "z");
        });
        failures += check("place roundtrip without data", () -> {
            BlockPlaceRequestPacket in = new BlockPlaceRequestPacket(1, 2, 3, (short) 42, (byte[]) null);
            BlockPlaceRequestPacket out = roundTrip(in, BlockPlaceRequestPacket.class);
            assertEq(in.x, out.x, "x");
            assertEq(in.y, out.y, "y");
            assertEq(in.z, out.z, "z");
            assertEq(in.blockId, out.blockId, "blockId");
            if (out.blockDataBytes != null) throw new AssertionError("expected null data");
        });
        failures += check("place roundtrip with data", () -> {
            byte[] data = new byte[]{3, 1, 7, -5};
            BlockPlaceRequestPacket in = new BlockPlaceRequestPacket(-99, 100, -101, (short) 7, data);
            BlockPlaceRequestPacket out = roundTrip(in, BlockPlaceRequestPacket.class);
            assertEq(in.blockId, out.blockId, "blockId");
            assertBytes(data, out.blockDataBytes);
        });
        failures += check("place roundtrip with empty data", () -> {
            BlockPlaceRequestPacket in = new BlockPlaceRequestPacket(0, 0, 0, (short) 1, new byte[0]);
            BlockPlaceRequestPacket out = roundTrip(in, BlockPlaceRequestPacket.class);
            assertBytes(new byte[0], out.blockDataBytes);
        });
        failures += check("update roundtrip with data", () -> {
            byte[] data = new byte[]{0, 2};
            BlockUpdatePacket in = new BlockUpdatePacket(5, 6, 7, (short) 9, data);
            BlockUpdatePacket out = roundTrip(in, BlockUpdatePacket.class);
            assertEq(in.x, out.x, "x");
            assertEq(in.blockId, out.blockId, "blockId");
            assertBytes(data, out.blockDataBytes);
        });
        failures += check("update roundtrip null data", () -> {
            BlockUpdatePacket in = new BlockUpdatePacket(5, 6, 7, (short) 0, null);
            BlockUpdatePacket out = roundTrip(in, BlockUpdatePacket.class);
            if (out.blockDataBytes != null) throw new AssertionError("expected null data");
        });
        failures += check("game state roundtrip", () -> {
            GameStatePacket in = new GameStatePacket(com.tessera.engine.server.GameMode.ADVENTURE, com.tessera.engine.server.Difficulty.HARD);
            GameStatePacket out = roundTrip(in, GameStatePacket.class);
            assertEq(in.gameModeOrdinal, out.gameModeOrdinal, "mode");
            assertEq(in.difficultyOrdinal, out.difficultyOrdinal, "difficulty");
        });
        failures += check("packet ids unique", () -> {
            int[] ids = {
                    AllPackets.BLOCK_BREAK_REQUEST, AllPackets.BLOCK_PLACE_REQUEST,
                    AllPackets.BLOCK_UPDATE, AllPackets.GAME_STATE
            };
            for (int id : ids) {
                if (id < 0 || id > 127) throw new AssertionError("id out of byte range: " + id);
                if (PacketDecoder.PACKET_REGISTRY.get((byte) id) == null) {
                    throw new AssertionError("id " + id + " not registered");
                }
            }
            if (AllPackets.BLOCK_BREAK_REQUEST == AllPackets.BLOCK_PLACE_REQUEST
                    || AllPackets.BLOCK_BREAK_REQUEST == AllPackets.BLOCK_UPDATE
                    || AllPackets.BLOCK_PLACE_REQUEST == AllPackets.BLOCK_UPDATE) {
                throw new AssertionError("block packet ids collide");
            }
        });
        failures += check("BlockData clone isolation", () -> {
            byte[] raw = new byte[]{1, 2, 3};
            BlockData data = new BlockData(raw.clone());
            BlockPlaceRequestPacket p = new BlockPlaceRequestPacket(0, 0, 0, (short) 1, data);
            raw[0] = 99;
            if (p.blockDataBytes[0] != 1) throw new AssertionError("packet must copy input bytes");
        });

        if (failures > 0) {
            System.out.println("BlockPacketCodecTest FAILED: " + failures + " failure(s)");
            System.exit(1);
        }
        System.out.println("BlockPacketCodecTest PASSED (all codec roundtrips ok)");
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
    private static <T> T roundTrip(com.tessera.engine.common.network.packet.Packet in, Class<T> type) {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeByte(in.id);
            in.encode(null, in, buf);
            byte id = buf.readByte();
            com.tessera.engine.common.network.packet.Packet proto =
                    PacketDecoder.PACKET_REGISTRY.get(id);
            if (proto == null) throw new AssertionError("no prototype for id " + id);
            List<Object> out = new ArrayList<>(1);
            proto.decode(null, buf, out);
            if (out.isEmpty()) throw new AssertionError("decode produced nothing");
            Object decoded = out.get(0);
            if (!type.isInstance(decoded)) {
                throw new AssertionError("decoded as " + decoded.getClass() + ", expected " + type);
            }
            if (buf.isReadable()) {
                throw new AssertionError(buf.readableBytes() + " trailing bytes after decode");
            }
            return (T) decoded;
        } finally {
            buf.release();
        }
    }

    private static void assertEq(int a, int b, String field) {
        if (a != b) throw new AssertionError(field + ": " + a + " != " + b);
    }

    private static void assertBytes(byte[] a, byte[] b) {
        if (a == null && b == null) return;
        if (a == null || b == null) throw new AssertionError("one side null");
        if (a.length != b.length) throw new AssertionError("length " + a.length + " != " + b.length);
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) throw new AssertionError("byte " + i + ": " + a[i] + " != " + b[i]);
        }
    }

    /** Programmatic entry for in-game "self test" buttons (same checks, no exit). */
    public static String runSelfTest() {
        try {
            AllPackets.registerPackets();
            // Minimal subset that never touches statics beyond the registry.
            BlockBreakRequestPacket b = new BlockBreakRequestPacket(1, 2, 3);
            BlockBreakRequestPacket b2 = roundTrip(b, BlockBreakRequestPacket.class);
            assertEq(b.x, b2.x, "x");
            BlockUpdatePacket u = new BlockUpdatePacket(1, 2, 3, (short) 5, new byte[]{1});
            BlockUpdatePacket u2 = roundTrip(u, BlockUpdatePacket.class);
            assertBytes(u.blockDataBytes, u2.blockDataBytes);
            Client client = Main.getClient();
            String conn = client == null ? "no client" : "client ok";
            return "codec self-test ok (" + conn + ")";
        } catch (Throwable t) {
            return "codec self-test FAILED: " + t.getMessage();
        }
    }
}
