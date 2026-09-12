package com.tessera.engine.common.worldInteraction.block;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.window.nuklear.components.NumberBox;
import com.tessera.window.nuklear.components.TextBox;
import org.joml.Vector3i;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * In-game test panel for server-authoritative block breaking/placing.
 *
 * <p>One panel tests both transports: it sends through
 * {@link BlockInteractionService}, which uses the {@code FakeChannel} loopback
 * in singleplayer and Netty in hosted/joined multiplayer. The header always
 * shows which transport is active so a tester can confirm they exercised both.
 *
 * <p>Usage: press F8 in game to toggle, or call {@link #setOpen(boolean)}.
 * "Use cursor" fills XYZ from the current ray hit; Break/Place send requests;
 * Verify compares the client world against the (co-located) server world when
 * one exists, and always shows the client-side block.
 */
public class BlockInteractionTester {

    private final BlockInteractionService service = new ClientBlockInteractionService();
    private boolean open = false;

    private final NumberBox xBox = new NumberBox(8, 0);
    private final NumberBox yBox = new NumberBox(8, 0);
    private final NumberBox zBox = new NumberBox(8, 0);
    private final NumberBox blockIdBox = new NumberBox(8, 0);
    private final TextBox aliasBox = new TextBox(40);

    private final Deque<String> log = new ArrayDeque<>();
    private static final int MAX_LOG = 8;

    public BlockInteractionTester() {
        aliasBox.setValueAsString("");
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void toggle() {
        this.open = !this.open;
    }

    /** Fill XYZ from the cursor ray hit, if any. */
    public void useCursorTarget() {
        try {
            Vector3i hit = Client.userPlayer.camera.cursorRay.getHitPos();
            if (hit != null) {
                xBox.setValueAsNumber(hit.x);
                yBox.setValueAsNumber(hit.y);
                zBox.setValueAsNumber(hit.z);
                log("cursor -> " + hit.x + ", " + hit.y + ", " + hit.z);
            } else {
                log("cursor has no target");
            }
        } catch (Exception e) {
            log("cursor read failed: " + e.getMessage());
        }
    }

    private int ix() {
        return (int) xBox.getValueAsNumber();
    }

    private int iy() {
        return (int) yBox.getValueAsNumber();
    }

    private int iz() {
        return (int) zBox.getValueAsNumber();
    }

    private short resolvedBlockId() {
        String alias = aliasBox.getValueAsString().trim();
        if (!alias.isEmpty()) {
            try {
                Block b = Registrys.blocks.getBlock(alias);
                if (b != null) return b.id;
                log("unknown alias '" + alias + "', using numeric id");
            } catch (Exception e) {
                log("alias lookup failed: " + e.getMessage());
            }
        }
        return (short) blockIdBox.getValueAsNumber();
    }

    public void doBreak() {
        BlockInteractionService.Result r = service.requestBreak(ix(), iy(), iz());
        log((r.sent() ? "BREAK sent " : "BREAK rejected (" + r.reason() + ") ") + pos());
    }

    public void doPlace() {
        short id = resolvedBlockId();
        BlockInteractionService.Result r = service.requestPlace(ix(), iy(), iz(), id, null);
        Block b = Registrys.getBlock(id);
        log((r.sent() ? "PLACE sent " : "PLACE rejected (" + r.reason() + ") ")
                + pos() + " id=" + id + (b == null ? "" : " (" + b.alias + ")"));
    }

    public void doVerify() {
        int x = ix(), y = iy(), z = iz();
        String clientBlock = "?";
        try {
            Block b = Client.world.getBlock(x, y, z);
            BlockData d = Client.world.getBlockData(x, y, z);
            clientBlock = b == null ? "null" : b.alias + " id=" + b.id + (d == null ? "" : " data=" + d);
        } catch (Exception e) {
            clientBlock = "client read failed: " + e.getMessage();
        }
        if (Main.getServer() != null) {
            try {
                Block sb = Main.getServer().world.getBlock(x, y, z);
                String serverBlock = sb == null ? "null" : sb.alias + " id=" + sb.id;
                Block cb = Client.world.getBlock(x, y, z);
                boolean match = cb != null && sb != null && cb.id == sb.id;
                log("VERIFY " + pos() + " client=[" + clientBlock + "] server=[" + serverBlock + "] "
                        + (match ? "MATCH" : "MISMATCH (wait for BlockUpdate or check server log)"));
            } catch (Exception e) {
                log("VERIFY " + pos() + " client=[" + clientBlock + "] server read failed: " + e.getMessage());
            }
        } else {
            // Joined multiplayer: no local server, client view is authoritative display.
            log("VERIFY " + pos() + " client=[" + clientBlock + "] (remote server: no local copy to compare)");
        }
    }

    public void doSelfTest() {
        log(BlockPacketCodecTest.runSelfTest());
    }

    private String pos() {
        return "(" + ix() + ", " + iy() + ", " + iz() + ")";
    }

    private void log(String s) {
        System.out.println("[BlockTest] " + s);
        log.addLast(s);
        while (log.size() > MAX_LOG) log.removeFirst();
    }

    public void draw(MemoryStack stack, NkContext ctx, ClientWindow window) {
        if (!open) return;
        NkRect dims = NkRect.malloc(stack);
        int w = 380, h = 470;
        nk_rect((window.getWidth() - w) / 2, (window.getHeight() - h) / 2, w, h, dims);
        nk_style_set_font(ctx, Theme.font_10);
        if (nk_begin(ctx, "Block Interaction Test (F8 to close)", dims,
                NK_WINDOW_BORDER | NK_WINDOW_TITLE | NK_WINDOW_MOVABLE)) {
            // Transport header: the whole point is to see SP vs MP here.
            nk_layout_row_dynamic(ctx, 18, 1);
            nk_label(ctx, "Transport: " + service.connectionName(), NK_TEXT_ALIGN_LEFT);
            nk_layout_row_dynamic(ctx, 18, 1);
            String mode = Main.getServer() != null
                    ? (Main.getServer().runningLocally() ? "local server (singleplayer)" : "hosting (local + netty)")
                    : "no local server (joined multiplayer)";
            nk_label(ctx, "Server: " + mode, NK_TEXT_ALIGN_LEFT);
            nk_layout_row_dynamic(ctx, 18, 1);
            nk_label(ctx, "Client mode: " + Main.getClient().getGameMode(), NK_TEXT_ALIGN_LEFT);

            nk_layout_row_dynamic(ctx, 12, 1);
            nk_label(ctx, "Target block XYZ:", NK_TEXT_ALIGN_LEFT);
            nk_layout_row_dynamic(ctx, 28, 3);
            xBox.render(ctx);
            yBox.render(ctx);
            zBox.render(ctx);

            nk_layout_row_dynamic(ctx, 12, 1);
            nk_label(ctx, "Place: numeric id + optional alias (alias wins):", NK_TEXT_ALIGN_LEFT);
            nk_layout_row_dynamic(ctx, 28, 2);
            blockIdBox.render(ctx);
            aliasBox.render(ctx);

            nk_layout_row_dynamic(ctx, 32, 2);
            if (nk_button_label(ctx, "Use cursor")) useCursorTarget();
            if (nk_button_label(ctx, "Verify")) doVerify();

            nk_layout_row_dynamic(ctx, 32, 2);
            if (nk_button_label(ctx, "BREAK (del)")) doBreak();
            if (nk_button_label(ctx, "PLACE (ins)")) doPlace();

            nk_layout_row_dynamic(ctx, 32, 2);
            if (nk_button_label(ctx, "Codec self-test")) doSelfTest();
            if (nk_button_label(ctx, "Close")) setOpen(false);

            nk_layout_row_dynamic(ctx, 12, 1);
            nk_label(ctx, "Log (server applies, then BlockUpdate returns):", NK_TEXT_ALIGN_LEFT);
            for (String line : log) {
                nk_layout_row_dynamic(ctx, 16, 1);
                nk_label(ctx, line.length() > 62 ? line.substring(0, 62) : line, NK_TEXT_ALIGN_LEFT);
            }
        }
        nk_end(ctx);
        Theme.resetEntireButtonStyle(ctx);
    }
}
