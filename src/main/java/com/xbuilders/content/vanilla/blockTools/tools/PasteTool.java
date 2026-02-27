package com.xbuilders.content.vanilla.blockTools.tools;

import com.xbuilders.Main;
import com.xbuilders.content.vanilla.blockTools.BlockTool;
import com.xbuilders.content.vanilla.blockTools.BlockTools;
import com.xbuilders.content.vanilla.blockTools.PrefabUtils;
import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.EntityShader_ArrayTexture;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.block.BlockMeshBundle;
import com.xbuilders.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.entity.Entity;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.ChunkVoxels;
import com.xbuilders.engine.common.resource.ResourceUtils;
import com.xbuilders.window.render.MVP;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import static com.xbuilders.Main.LOGGER;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

public class PasteTool extends BlockTool {
    public PasteTool(BlockTools tools, CursorRay cursorRay) {
        super("Paste", tools, cursorRay);
        hasOptions = true;
        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\paste.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
        nk_layout_row_dynamic(ctx, 30, 2);
        if (Nuklear.nk_button_label(ctx, "Load Prefab")) {
            Main.getClient().window.gameScene.ui.fileDialog.show(ResourceUtils.appDataFile("prefabs"),
                    false, "xbprefab", (file) -> {
                        System.out.println("LOADING " + file.getAbsolutePath());
                        try {
                            PasteTool.clipboard = PrefabUtils.loadPrefabFromFile(file);
                        } catch (IOException e) {
                            LOGGER.log(Level.INFO,"error", e);
                        }
                        System.out.println(PasteTool.clipboard.toString());
                        PasteTool.updateMesh();
                    });
        }
        if (Nuklear.nk_button_label(ctx, (additionMode ? "Additive Paste" : "Paste"))) {
            additionMode = !additionMode;
        }
//        NKUtils.wrapText(ctx,"Help: \n" +
//                "Change mode: M \n" +
//                "Change offset point: mouse wheel \n" +
//                "Paste blocks: Enter\n" +
//                "Click to set the paste boundary and click again ro release it",windowSize.w()-20);
    }


    @Override
    public String toolDescription() {
        return (additionMode ? "Additive " : "") + "Paste (offset=" + (offsetMode + 1) + "/" + offsetMaxMode + ")";
    }

    MVP modelMatrix = new MVP();
    public static ChunkVoxels clipboard = new ChunkVoxels(16, 16, 16);


    public static ArrayList<Entity> clipboard_entities = new ArrayList<>(); //TODO: Add clipboard allEntities
    private final static EntityShader_ArrayTexture blockShader = new EntityShader_ArrayTexture();
    static BlockMeshBundle mesh = new BlockMeshBundle();
    static Box box = new Box();

    public static void updateMesh() {
        mesh.compute(clipboard);
        box.setPosAndSize(0, 0, 0, clipboard.size.x, clipboard.size.y, clipboard.size.z);
        mesh.sendToGPU();
    }

    static {
        box.setLineWidth(2);
        box.setColor(new Vector4f(1, 0, 0, 1));
    }


    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        return key == GLFW.GLFW_KEY_V && (mods & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    public void activate() {
        cursorRay.disableBoundaryMode();
        if (PasteTool.clipboard == null) {
            PasteTool.clipboard = new ChunkVoxels(0, 0, 0);
        }
        PasteTool.updateMesh();
        offsetMode = 1;
        positioningMode = true;
        additionMode = true;
    }

    public Vector3i getOffset() {
        if (placeOnHit) offset.set(cursorRay.getHitPos());
        else offset.set(cursorRay.getHitPosPlusNormal());

        switch (offsetMode) {
            //Up
            case 1 -> {
                offset.y -= clipboard.size.y - 1;
            }
            case 2 -> {
                offset.y -= clipboard.size.y - 1;
                offset.x -= clipboard.size.x - 1;
            }
            case 3 -> {
                offset.y -= clipboard.size.y - 1;
                offset.z -= clipboard.size.z - 1;
            }
            case 4 -> {
                offset.x -= clipboard.size.x - 1;
                offset.y -= clipboard.size.y - 1;
                offset.z -= clipboard.size.z - 1;
            }
            //Down (0 included)
            case 5 -> {
                offset.x -= clipboard.size.x - 1;
            }
            case 6 -> {
                offset.z -= clipboard.size.z - 1;
            }
            case 7 -> {
                offset.x -= clipboard.size.x - 1;
                offset.z -= clipboard.size.z - 1;
            }

        }
        return offset;
    }

    @Override
    public boolean drawCursor(CursorRay ray, Matrix4f proj, Matrix4f view) {
        if (positioningMode) {
            offset = getOffset();
            if (!ray.hitTarget()) return false;
        }

        blockShader.bind();
        blockShader.updateProjectionViewMatrix(proj, view);
        modelMatrix.identity().translate(offset.x, offset.y, offset.z);
        modelMatrix.updateAndSendToShader(blockShader.getID(), blockShader.uniform_modelMatrix);
        mesh.draw();

        box.setPosition(offset.x, offset.y, offset.z);
        box.draw(proj, view);
        return true;
    }

    public boolean setBlock(Block item, final CursorRay ray, boolean isCreationMode) {
        positioningMode = !positioningMode;
        return true;
    }

    private void paste() {
        for (int x = 0; x < clipboard.size.x; x++) {
            for (int y = 0; y < clipboard.size.y; y++) {
                for (int z = 0; z < clipboard.size.z; z++) {
                    if (clipboard.getBlock(x, y, z) != BlockRegistry.BLOCK_AIR.id || !additionMode) {
                        Main.getServer().setBlock(clipboard.getBlock(x, y, z), clipboard.getBlockData(x, y, z), x + offset.x, y + offset.y, z + offset.z);
                    }
                }
            }
        }
//        positioningMode = true;
    }

    public static void rotatePasteBox() {
        if (clipboard != null) {
            clipboard_entities.clear();
            ChunkVoxels newClipboard = new ChunkVoxels(clipboard.size.z, clipboard.size.y, clipboard.size.x);
            for (int x = 0; x < clipboard.size.x; x++) {
                for (int y = 0; y < clipboard.size.y; y++) {
                    for (int z = 0; z < clipboard.size.z; z++) {
                        int newX = (clipboard.size.z - 1) - z;
                        int newZ = x;
                        int newY = y;

                        Block block = Registrys.getBlock(clipboard.getBlock(x, y, z));
                        newClipboard.setBlock(newX, newY, newZ, block.id);

                        BlockData oldData = clipboard.getBlockData(x, y, z);
                        if (oldData != null) {
                            BlockData newData = new BlockData(oldData); //Create a copy
                            block.getType().rotateBlockData(newData, false);
                            newClipboard.setBlockData(newX, newY, newZ, newData);
                        }

                    }
                }
            }
            clipboard = newClipboard;
        }
        updateMesh();
    }

    boolean additionMode = false;
    Vector3i offset = new Vector3i();
    int offsetMaxMode = 8;
    int offsetMode = 1;
    boolean positioningMode = false;

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (positioningMode) {
            offsetMode = (offsetMode + ((int) yoffset)) % offsetMaxMode;
        } else offset.y += (int) yoffset;
        return true;
    }

    boolean placeOnHit = false;

    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_K) {
                placeOnHit = true;
                return true;
            } else if (!positioningMode) {
                if (key == GLFW.GLFW_KEY_LEFT) {
                    offset.x--;
                    return true;
                } else if (key == GLFW.GLFW_KEY_RIGHT) {
                    offset.x++;
                    return true;
                } else if (key == GLFW.GLFW_KEY_UP) {
                    if (mods == GLFW.GLFW_MOD_SHIFT) {
                        offset.y--;
                    } else {
                        offset.z--;
                    }
                    return true;
                } else if (key == GLFW.GLFW_KEY_DOWN) {
                    if (mods == GLFW.GLFW_MOD_SHIFT) {
                        offset.y++;
                    } else {
                        offset.z++;
                    }
                    return true;
                }
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_O) {
                offsetMode = (offsetMode + 1) % offsetMaxMode;
                System.out.println("Offset mode: " + offsetMode);
                return true;
            } else if (key == GLFW.GLFW_KEY_M) {
                additionMode = !additionMode;
                return true;
            } else if (key == GLFW.GLFW_KEY_R) {
                rotatePasteBox();
                return true;
            } else if (key == GLFW.GLFW_KEY_K) {
                placeOnHit = false;
                return true;
            } else if (!positioningMode && key == GLFW.GLFW_KEY_ENTER) {
                paste();

                return true;
            }
        }
        return false;
    }
}
