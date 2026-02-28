package com.tessera.content.vanilla.blockTools;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.window.utils.texture.TextureUtils;
import org.joml.Matrix4f;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;

public abstract class BlockTool {

    public final String name;
    public final BlockTools blockTools;
    public final CursorRay cursorRay;
    public boolean hasOptions = false;

    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
    }

    public static Block getSelectedBlock() {
        ItemStack selectedItem = Client.userPlayer.getSelectedItem();
        if (selectedItem == null || selectedItem.item == null) return BlockRegistry.BLOCK_AIR;
        if (selectedItem.item.getBlock() == null) return BlockRegistry.BLOCK_AIR;
        Block b = selectedItem.item.getBlock();
        if (b == null) return BlockRegistry.BLOCK_AIR;
        return b;
    }

    public static boolean hasBlock() {
        ItemStack selectedItem = Client.userPlayer.getSelectedItem();
        if (selectedItem == null || selectedItem.item == null) return false;
        if (selectedItem.item.getBlock() == null) return false;
        Block b = selectedItem.item.getBlock();
        return b != null;
    }

    public BlockTool(String name, BlockTools blockTools, CursorRay cursorRay) {
        this.name = name;
        this.blockTools = blockTools;
        this.cursorRay = cursorRay;
        NKicon = NkImage.create();
    }


    public String toolDescription() {
        return name;
    }

    public abstract boolean activationKey(int key, int scancode, int action, int mods);

    public void activate() {
    }

    public void changeMode() {
    }

    public void deactivate() {
    }

    public boolean drawCursor(CursorRay ray, Matrix4f proj, Matrix4f view) {
        return false;
    }


    public boolean setBlock(Block item, final CursorRay ray, boolean isCreationMode) {
        return false;
    }


    /**
     * @param scroll
     * @param xoffset
     * @param yoffset
     * @return true if the event was consumed
     */
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        return false;
    }

    /**
     * @param key
     * @param scancode
     * @param action
     * @param mods
     * @return true if the event was consumed
     */
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }

    public boolean mouseButtonEvent(int button, int action, int mods) {
        return false;
    }

    /**
     * Only activates when user does Shift+scroll for tool size
     *
     * @param scroll -1 for up, 1 for down
     * @return
     */
    public boolean mouseToolScrollEvent(int scroll) {
        return false;
    }


    private final NkImage NKicon;

    protected void setIcon(File file) throws IOException {
        int textureID = TextureUtils.loadTextureFromFile(file, false).id;
        NKicon.handle(it -> it.id(textureID));
    }

    public NkImage getNKIcon() {
        return NKicon;
    }
}





