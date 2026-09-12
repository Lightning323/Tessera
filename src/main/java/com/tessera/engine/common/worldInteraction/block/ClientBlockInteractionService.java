package com.tessera.engine.common.worldInteraction.block;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.packets.BlockBreakRequestPacket;
import com.tessera.engine.common.packets.BlockPlaceRequestPacket;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;

/**
 * Production {@link BlockInteractionService} that sends request packets over
 * whatever channel the {@link Client} currently holds (fake loopback for
 * singleplayer, Netty for hosted/joined multiplayer).
 *
 * <p>Validation here is intentionally shallow (spectator lockout, bounds, air
 * checks against the client world for UX). The server re-validates everything
 * authoritatively and may reject; rejection simply yields no
 * {@code BlockUpdatePacket}.
 */
public class ClientBlockInteractionService implements BlockInteractionService {

    @Override
    public Result requestBreak(int x, int y, int z) {
        Client client = Main.getClient();
        if (client == null) return Result.rejected("no client");
        if (client.getGameMode() == GameMode.SPECTATOR) return Result.rejected("spectator cannot break");
        if (!Client.world.inBounds(x, y, z)) return Result.rejected("out of bounds");

        Block existing = Client.world.getBlock(x, y, z);
        if (existing == null || existing.isAir()) return Result.rejected("already air");

        ChannelBase channel = channel();
        if (channel == null || !channel.isActive()) return Result.rejected("not connected");

        channel.writeAndFlush(new BlockBreakRequestPacket(x, y, z));
        return Result.ok();
    }

    @Override
    public Result requestPlace(int x, int y, int z, short blockId, BlockData blockData) {
        Client client = Main.getClient();
        if (client == null) return Result.rejected("no client");
        if (client.getGameMode() == GameMode.SPECTATOR) return Result.rejected("spectator cannot place");
        if (!Client.world.inBounds(x, y, z)) return Result.rejected("out of bounds");

        Block block = Registrys.getBlock(blockId);
        if (block == null || block.isAir()) return Result.rejected("cannot place air/unknown block");

        ChannelBase channel = channel();
        if (channel == null || !channel.isActive()) return Result.rejected("not connected");

        // Resolve orientation/data client-side when the caller did not supply
        // it. This keeps camera-dependent logic on the client; the server uses
        // whatever bytes arrive verbatim.
        BlockData data = blockData;
        if (data == null) {
            try {
                BlockData existing = Client.world.getBlockData(x, y, z);
                data = block.getInitialBlockData(existing);
            } catch (Exception e) {
                data = null;
            }
        }
        channel.writeAndFlush(new BlockPlaceRequestPacket(x, y, z, blockId, data));
        return Result.ok();
    }

    private ChannelBase channel() {
        try {
            Client client = Main.getClient();
            if (client == null || client.endpoint == null) return null;
            return client.endpoint.getChannel();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String connectionName() {
        try {
            Client client = Main.getClient();
            if (client == null || client.endpoint == null) return "none";
            ChannelBase ch = client.endpoint.getChannel();
            if (ch == null) return client.endpoint.getClass().getSimpleName() + " (no channel)";
            boolean toServer = false;
            try {
                // FakeChannel exposes direction; Netty channels do not need it.
                toServer = ch.getClass().getSimpleName().equals("FakeChannel");
            } catch (Exception ignored) {
            }
            String mode;
            if (client.endpoint.getClass().getSimpleName().contains("Fake")) {
                mode = Main.getServer() != null ? "singleplayer (FakeChannel loopback)" : "fake (no server)";
            } else {
                mode = "multiplayer (" + client.endpoint.getClass().getSimpleName() + ")";
            }
            return mode + " active=" + ch.isActive() + (toServer ? "" : "");
        } catch (Exception e) {
            return "unknown (" + e.getMessage() + ")";
        }
    }

    @Override
    public boolean isConnected() {
        ChannelBase ch = channel();
        return ch != null && ch.isActive();
    }
}
