package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.world.ClientWorld;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.ClientChunk;
import com.tessera.engine.common.world.wcc.WCCi;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.joml.Vector3i;

import java.util.List;

/**
 * Server -&gt; client broadcast of the authoritative block at a position.
 *
 * <p>Applied directly to the {@code ClientWorld} (voxels + mesh refresh). This
 * is the only path by which the server world mutates the client world, keeping
 * the separation complete: clients never write the server world, and the
 * server never writes client fields except through this packet.
 */
public class BlockUpdatePacket extends Packet {

    public int x, y, z;
    public short blockId;
    /** Nullable raw BlockData bytes. */
    public byte[] blockDataBytes;

    public BlockUpdatePacket() {
        super(AllPackets.BLOCK_UPDATE);
    }

    public BlockUpdatePacket(int x, int y, int z, short blockId, byte[] blockDataBytes) {
        super(AllPackets.BLOCK_UPDATE);
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.blockDataBytes = blockDataBytes;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        BlockUpdatePacket p = (BlockUpdatePacket) packet;
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
        out.add(new BlockUpdatePacket(x, y, z, blockId, data));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        BlockUpdatePacket p = (BlockUpdatePacket) packet;
        Client client = Main.getClient();
        if (client == null) return;
        ClientWorld world = Client.world;
        if (world == null || world.getData() == null) return;
        if (!world.inBounds(p.x, p.y, p.z)) return;

        WCCi wcc = new WCCi().set(p.x, p.y, p.z);
        Vector3i chunkPos = new Vector3i(wcc.chunk);
        ClientChunk chunk = world.getChunk(chunkPos);
        if (chunk == null) {
            // Chunk not loaded yet: stash for when terrain arrives so the edit
            // is not silently lost.
            world.newFutureChunk(chunkPos).addBlock(p.blockId, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
            // BlockData on a future chunk is not yet supported; the data rides
            // along on the next BlockUpdate once the chunk exists. Log it.
            if (p.blockDataBytes != null) {
                System.out.println("BlockUpdate for unloaded chunk " + chunkPos.x + "," + chunkPos.y + "," + chunkPos.z
                        + " (block data deferred until chunk load)");
            }
            return;
        }

        BlockData data = p.blockDataBytes == null ? null : new BlockData(p.blockDataBytes.clone());
        chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, p.blockId);
        chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, data);
        chunk.markAsModified();
        world.updateMesh(false, true, p.x, p.y, p.z);
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        // Broadcasts flow server -> client only. If a client echoes one back,
        // ignore it: clients are never authoritative.
    }

    /** Test helper: applies this update to any ClientWorld without statics. */
    public void applyTo(ClientWorld world) {
        if (world == null || !world.inBounds(x, y, z)) return;
        WCCi wcc = new WCCi().set(x, y, z);
        Chunk chunk = world.getChunk(new Vector3i(wcc.chunk));
        if (chunk == null) return;
        BlockData data = blockDataBytes == null ? null : new BlockData(blockDataBytes.clone());
        chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, blockId);
        chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, data);
        chunk.markAsModified();
    }
}
