package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.server.Difficulty;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Server -&gt; client snapshot of authoritative game state the client needs for
 * UI decisions (mining speed, reach, spectator lockout) without touching the
 * {@code Server} object directly.
 *
 * <p>Previously client code called {@code Main.getServer().getGameMode()}
 * everywhere, which NPEs on a remote client (no local server) and breaks the
 * client/server boundary. Clients must read {@link Client#cachedGameMode}
 * instead; this packet keeps it in sync.
 */
public class GameStatePacket extends Packet {

    public int gameModeOrdinal;
    public int difficultyOrdinal;

    public GameStatePacket() {
        super(AllPackets.GAME_STATE);
    }

    public GameStatePacket(GameMode mode) {
        super(AllPackets.GAME_STATE);
        this.gameModeOrdinal = mode == null ? GameMode.ADVENTURE.ordinal() : mode.ordinal();
        this.difficultyOrdinal = Difficulty.NORMAL.ordinal();
    }

    public GameStatePacket(GameMode mode, Difficulty difficulty) {
        super(AllPackets.GAME_STATE);
        this.gameModeOrdinal = mode == null ? GameMode.ADVENTURE.ordinal() : mode.ordinal();
        this.difficultyOrdinal = difficulty == null ? Difficulty.NORMAL.ordinal() : difficulty.ordinal();
    }

    public GameStatePacket(int gameModeOrdinal) {
        super(AllPackets.GAME_STATE);
        this.gameModeOrdinal = gameModeOrdinal;
        this.difficultyOrdinal = Difficulty.NORMAL.ordinal();
    }

    public GameStatePacket(int gameModeOrdinal, int difficultyOrdinal) {
        super(AllPackets.GAME_STATE);
        this.gameModeOrdinal = gameModeOrdinal;
        this.difficultyOrdinal = difficultyOrdinal;
    }

    public GameMode gameMode() {
        GameMode[] values = GameMode.values();
        if (gameModeOrdinal < 0 || gameModeOrdinal >= values.length) return GameMode.ADVENTURE;
        return values[gameModeOrdinal];
    }

    public Difficulty difficulty() {
        Difficulty[] values = Difficulty.values();
        if (difficultyOrdinal < 0 || difficultyOrdinal >= values.length) return Difficulty.NORMAL;
        return values[difficultyOrdinal];
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        GameStatePacket p = (GameStatePacket) packet;
        out.writeInt(p.gameModeOrdinal);
        out.writeInt(p.difficultyOrdinal);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int mode = in.readInt();
        // Difficulty was added later; old single-int payloads must still decode.
        int difficulty = in.isReadable() ? in.readInt() : Difficulty.NORMAL.ordinal();
        out.add(new GameStatePacket(mode, difficulty));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        GameStatePacket p = (GameStatePacket) packet;
        Client client = Main.getClient();
        if (client == null) return;
        // Volatile state is safe to set from any network thread. Everything
        // touching GL/player must run on the window thread (see Client queue):
        // gameModeChangedEvent -> setFlashlight -> glUseProgram with no
        // current context hard-aborts the JVM.
        client.cachedGameMode = p.gameMode();
        client.cachedDifficulty = p.difficulty();
        final com.tessera.engine.server.GameMode mode = p.gameMode();
        try {
            client.runOnMainThread(() -> {
                try {
                    if (Client.userPlayer != null) {
                        Client.userPlayer.gameModeChangedEvent(mode);
                    }
                } catch (Exception e) {
                    Main.LOGGER.warn("Failed to apply game mode " + mode, e);
                }
            });
        } catch (Exception e) {
            Main.LOGGER.warn("Failed to enqueue game mode " + mode, e);
        }
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        // Server is authoritative; it never accepts game state from clients.
    }

    /** Broadcast helper for server-side mode changes and join handshake. */
    public static void broadcast(Server server) {
        if (server == null) return;
        server.writeAndFlushToAllPlayers(new GameStatePacket(server.getGameMode(), server.getDifficulty()));
    }
}
