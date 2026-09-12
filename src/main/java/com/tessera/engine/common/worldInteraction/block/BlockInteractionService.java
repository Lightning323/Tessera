package com.tessera.engine.common.worldInteraction.block;

import com.tessera.engine.common.world.chunk.BlockData;

/**
 * Client-side entry point for block breaking and placing.
 *
 * <p>Implementations must be transport-agnostic: the same interface drives
 * singleplayer (loopback {@code FakeChannel}) and multiplayer (Netty), so a
 * single test panel exercises both. All methods are non-blocking: they enqueue
 * a request packet ({@code BlockBreakRequestPacket} /
 * {@code BlockPlaceRequestPacket}) and return whether the request was sent.
 * The authoritative result arrives later as a {@code BlockUpdatePacket}
 * broadcast from the server.
 *
 * <p>Separation contract:
 * <ul>
 *   <li>Client code calls this interface only. It never touches
 *       {@code Server}, {@code ServerWorld}, or server pipelines.</li>
 *   <li>Server code never calls this interface. It handles the request
 *       packets and broadcasts {@code BlockUpdatePacket}.</li>
 * </ul>
 */
public interface BlockInteractionService {

    /**
     * @return {@link Result} describing whether the request was sent and why not.
     */
    Result requestBreak(int x, int y, int z);

    /**
     * @param blockId        registry id of the block to place (never air).
     * @param blockData      optional orientation/data bytes, may be null to let
     *                       the server derive defaults.
     * @return {@link Result} describing whether the request was sent.
     */
    Result requestPlace(int x, int y, int z, short blockId, BlockData blockData);

    /** Connection the service is sending over, for test display. */
    String connectionName();

    /** Whether there is an active channel to send on. */
    boolean isConnected();

    /** Outcome of a non-blocking send attempt. */
    record Result(boolean sent, String reason) {
        public static Result ok() {
            return new Result(true, "sent");
        }

        public static Result rejected(String reason) {
            return new Result(false, reason);
        }
    }
}
