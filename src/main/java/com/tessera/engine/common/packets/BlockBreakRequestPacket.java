package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.Server;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.loot.AllLootTables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.joml.Vector3f;

import java.util.List;

/**
 * Client -&gt; server request to break the block at a world position.
 *
 * <p>The server is authoritative: it validates, applies the change to the
 * {@code ServerWorld} via {@link Server#setBlock}, drops loot, and broadcasts
 * a {@link BlockUpdatePacket} to all clients. Clients must never mutate the
 * server world directly.
 */
public class BlockBreakRequestPacket extends Packet {

    public int x, y, z;

    public BlockBreakRequestPacket() {
        super(AllPackets.BLOCK_BREAK_REQUEST);
    }

    public BlockBreakRequestPacket(int x, int y, int z) {
        super(AllPackets.BLOCK_BREAK_REQUEST);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        BlockBreakRequestPacket p = (BlockBreakRequestPacket) packet;
        out.writeInt(p.x);
        out.writeInt(p.y);
        out.writeInt(p.z);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        out.add(new BlockBreakRequestPacket(x, y, z));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        // Requests flow client -> server only.
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        BlockBreakRequestPacket p = (BlockBreakRequestPacket) packet;
        Server server = Main.getServer();
        if (server == null) return;

        Player player = ctx.getPlayer();
        if (player == null) return;

        if (server.getGameMode() == GameMode.SPECTATOR) return;
        if (!server.world.inBounds(p.x, p.y, p.z)) return;

        Block existing = server.world.getBlock(p.x, p.y, p.z);
        if (existing == null || existing.isAir()) return;

        // Authoritative break. Loot only in adventure (matches previous client behavior).
        if (server.getGameMode() == GameMode.ADVENTURE) {
            try {
                AllLootTables.blockLootTables.dropLoot(existing.alias, new Vector3f(p.x, p.y, p.z), false, false);
            } catch (Exception e) {
                Main.LOGGER.warn("Loot drop failed for " + existing.alias, e);
            }
        }

        server.setBlock(BlockRegistry.BLOCK_AIR.id, p.x, p.y, p.z);

        // Broadcast so every client (including the requester) converges.
        server.writeAndFlushToAllPlayers(new BlockUpdatePacket(p.x, p.y, p.z, BlockRegistry.BLOCK_AIR.id, null));
    }
}
