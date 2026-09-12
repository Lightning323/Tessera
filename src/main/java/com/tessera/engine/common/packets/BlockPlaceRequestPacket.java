package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.Server;
import com.tessera.engine.server.block.Block;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Client -&gt; server request to place a block at a world position.
 *
 * <p>{@code blockData} is optional and computed client-side (e.g. stair
 * orientation from the camera). If present the server uses it verbatim so the
 * server never needs client-only state (camera, cursor) to derive orientation.
 * If absent the server falls back to the block's default initial data.
 */
public class BlockPlaceRequestPacket extends Packet {

    public int x, y, z;
    public short blockId;
    /** Nullable raw BlockData bytes. Null means "use server default". */
    public byte[] blockDataBytes;

    public BlockPlaceRequestPacket() {
        super(AllPackets.BLOCK_PLACE_REQUEST);
    }

    public BlockPlaceRequestPacket(int x, int y, int z, short blockId, byte[] blockDataBytes) {
        super(AllPackets.BLOCK_PLACE_REQUEST);
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.blockDataBytes = blockDataBytes;
    }

    public BlockPlaceRequestPacket(int x, int y, int z, short blockId, BlockData data) {
        this(x, y, z, blockId, data == null ? null : data.toByteArray().clone());
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        BlockPlaceRequestPacket p = (BlockPlaceRequestPacket) packet;
        out.writeInt(p.x);
        out.writeInt(p.y);
        out.writeInt(p.z);
        out.writeShort(p.blockId);
        if (p.blockDataBytes == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(p.blockDataBytes.length);
            out.writeBytes(p.blockDataBytes);
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        short blockId = in.readShort();
        int len = in.readInt();
        byte[] data = null;
        if (len >= 0) {
            data = new byte[len];
            in.readBytes(data);
        }
        out.add(new BlockPlaceRequestPacket(x, y, z, blockId, data));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        // Requests flow client -> server only.
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        BlockPlaceRequestPacket p = (BlockPlaceRequestPacket) packet;
        Server server = Main.getServer();
        if (server == null) return;

        Player player = ctx.getPlayer();
        if (player == null) return;

        if (server.getGameMode() == GameMode.SPECTATOR) return;
        if (!server.world.inBounds(p.x, p.y, p.z)) return;

        Block block = Registrys.getBlock(p.blockId);
        if (block == null || block.isAir()) {
            // Placing air is a break; reject here to keep semantics explicit.
            return;
        }

        BlockData data = p.blockDataBytes == null ? null : new BlockData(p.blockDataBytes.clone());
        if (data != null) {
            server.setBlock(p.blockId, data, p.x, p.y, p.z);
        } else {
            server.setBlock(p.blockId, p.x, p.y, p.z);
        }

        // Re-read the authoritative data the server actually stored (the
        // pipeline may have derived initial data) so clients converge exactly.
        BlockData authoritative = server.world.getBlockData(p.x, p.y, p.z);
        byte[] authBytes = authoritative == null ? p.blockDataBytes : authoritative.toByteArray().clone();
        server.writeAndFlushToAllPlayers(new BlockUpdatePacket(p.x, p.y, p.z, p.blockId, authBytes));
    }
}
