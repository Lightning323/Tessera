package com.tessera.engine.server.builtinMechanics.liquid;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.propagation.WaterPropagation;
import org.joml.Vector3i;

public class LiquidBlockType extends BlockType {

    final static Vector3i[] waterEraseNeighbors = new Vector3i[]{
            //edges
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 0),
            new Vector3i(0, 0, -1),
            new Vector3i(0, 0, 1),
            //corners
            new Vector3i(-1, 0, -1),
            new Vector3i(1, 0, -1),
            new Vector3i(-1, 0, 1),
            new Vector3i(1, 0, 1)
    };

    public int getGreedyMesherPermissions() {
        /**
         * We can decide if we want to allow greedy meshering or not
         */
//        return MainWindow.settings.game_fixLiquidMesh ? DENY_GM : PERMIT_GM;
        return PERMIT_GM;
    }


    public LiquidBlockType() {
        super();
        replaceOnSet = true;
        initializationCallback = (b) -> {

            b.opaque = false;
            b.solid = false;
            b.liquidMaxFlow = 7;

            b.initialBlockData = (existingData) -> {
                BlockData bd = new BlockData(1);
                //The source block is block max flow + 1
                bd.set(0, (byte) b.getLiquidSourceValue());
                return bd;
            };
            //TODO: The user should not be allowed to delete water, just replace blocks over it
            b.removeBlockEvent(false, ((x, y, z, history) -> {
                //If we are a source block, put us back if we are surrounded by water
                int defaultFlow = b.liquidMaxFlow + 1;
//                System.out.println("Remove " + x + " " + y + " " + z);
                if (history.newBlock == BlockRegistry.BLOCK_AIR &&
                        WaterPropagation.getFlow(history.previousBlockData, defaultFlow) == defaultFlow) {

                    //If there is no water above us
                    if (Client.world.getBlockID(x, y - 1, z) != b.id) {
                        //If there is at least X neighboring source water, put us back
                        int neighboringWater = 0;
                        for (Vector3i n : waterEraseNeighbors) {
                            if (Client.world.getBlockID(x + n.x, y + n.y, z + n.z) == b.id &&
                                    WaterPropagation.getFlow(history.previousBlockData, defaultFlow) == defaultFlow) {
                                neighboringWater++;
                                if (neighboringWater > 3) {
                                    ClientWindow.printlnDev("replacing liquid: " + x + " " + y + " " + z);
                                    Main.getServer().setBlock(b.id, history.previousBlockData, x, y, z);
                                    return;
                                }
                            }
                        }
                    }
                }
            }));
        };
    }


    private float getHeightOfFlow(int flow, float liquidMaxFlow, int y) {
        float val = 1.0f + y - (flow / (liquidMaxFlow + 1)) - (1 / liquidMaxFlow);
        return val;
    }


    private float getHeightOfFlow(BlockData data, int liquidMaxFlow, int y) {
        return getHeightOfFlow(WaterPropagation.getFlow(data, liquidMaxFlow), liquidMaxFlow, y);
    }

    static final private int TEX_FLOW_STATIC = 0;
    static final private int TEX_FLOW_NEG_X = 1;
    static final private int TEX_FLOW_POS_X = 2;
    static final private int TEX_FLOW_NEG_Z = 3;
    static final private int TEX_FLOW_POS_Z = 4;
    static final private int TEX_FLOW_NEG_X_NEG_Z = 5;
    static final private int TEX_FLOW_POS_X_NEG_Z = 6;
    static final private int TEX_FLOW_NEG_X_POS_Z = 7;
    static final private int TEX_FLOW_POS_X_POS_Z = 8;

    final static float[] topFaceUV_posZ = { //Original
            0.0f, 0.0f,//0,1
            1.0f, 0.0f,//2.3
            1.0f, 1.0f,//4,5
            0.0f, 1.0f//6,7
    };
    final static float[] topFaceUV_negZ = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };
    final static float[] topFaceUV_posX = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
    final static float[] topFaceUV_negX = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    final static float[] topFaceUV_negX_posZ = { //Original
            0.0f, 0.5f,
            0.5f, 0.0f,
            1.0f, 0.5f,
            0.5f, 1.0f
    };


    final static float[] topFaceUV_negX_negZ = {
            0.5f, 1.0f,
            0.0f, 0.5f,
            0.5f, 0.0f,
            1.0f, 0.5f
    };

    final static float[] topFaceUV_posX_negZ = {
            1.0f, 0.5f,
            0.5f, 1.0f,
            0.0f, 0.5f,
            0.5f, 0.0f
    };

    final static float[] topFaceUV_posX_posZ = {
            0.5f, 0.0f,
            1.0f, 0.5f,
            0.5f, 1.0f,
            0.0f, 0.5f
    };


    public boolean determineIfUsingGreedyMesher(
            Block block, BlockData data,
            Block[] neighbors, BlockData[] neighborData, byte[] lightValues,
            Chunk chunk, int chunkX, int chunkY, int chunkZ) {

        return constructBlock(null,
                block, data, neighbors, neighborData, lightValues,
                chunk, chunkX, chunkY, chunkZ,
                true);
    }

    @Override
    public boolean constructBlock(VertexSet buffer,
                                  Block block, BlockData data,
                                  Block[] neighbors,
                                  BlockData[] neighborData,
                                  byte[] light,
                                  Chunk chunk, int chunkX, int chunkY, int chunkZ,
                                  boolean isUsingGreedyMesher) {


        BlockTexture.FaceTexture texLayer;

        final float yFloor = 1.0f + chunkY;
        float y00 = chunkY;
        float y10 = chunkY;
        float y01 = chunkY;
        float y11 = chunkY;
        int topTextureFlowMode = TEX_FLOW_STATIC;
        float[] topFaceUV = topFaceUV_posZ;
        boolean onSurface = neighbors[POS_Y] != block;

        if (getGreedyMesherPermissions() == DENY_GM) isUsingGreedyMesher = false;
        int flow = WaterPropagation.getFlow(data, block.liquidMaxFlow + 1);

        if (isUsingGreedyMesher &&
                (
                        !onSurface ||
                                chunk == null ||
                                (flow > block.liquidMaxFlow)
                )) {
            return true; //If we are not on top, or we are at max flow, we use the GM
        }

        if (onSurface && chunk != null) {
            float zeroFlowHeight = getHeightOfFlow(0, block.liquidMaxFlow, chunkY);
            float fullFlowHeight = getHeightOfFlow(block.liquidMaxFlow, block.liquidMaxFlow, chunkY);
            float centerFlowHeight = getHeightOfFlow(flow, block.liquidMaxFlow, chunkY);

            int worldX = chunkX + chunk.position.x * Chunk.WIDTH;
            int worldY = chunkY + chunk.position.y * Chunk.HEIGHT;
            int worldZ = chunkZ + chunk.position.z * Chunk.WIDTH;


            float negXFlow = zeroFlowHeight;
            float posXFlow = zeroFlowHeight;
            float negZFlow = zeroFlowHeight;
            float posZFlow = zeroFlowHeight;
            int y00WithLiquid = 0;
            int y10WithLiquid = 0;
            int y01WithLiquid = 0;
            int y11WithLiquid = 0;

            if (neighbors[NEG_X] == block) {
                //If the block above this one is also a liquid block, than the flow should be maxFlow
                if (Client.world.getBlockID(worldX - 1, worldY - 1, worldZ) == block.id) {
                    negXFlow = fullFlowHeight;
                } else negXFlow = getHeightOfFlow(neighborData[NEG_X], block.liquidMaxFlow, chunkY);

                y00WithLiquid++;
                y01WithLiquid++;
            }
            if (neighbors[POS_X] == block) {
                if (Client.world.getBlockID(worldX + 1, worldY - 1, worldZ) == block.id) {
                    posXFlow = fullFlowHeight;
                } else posXFlow = getHeightOfFlow(neighborData[POS_X], block.liquidMaxFlow, chunkY);

                y10WithLiquid++;
                y11WithLiquid++;
            }
            if (neighbors[NEG_Z] == block) {
                if (Client.world.getBlockID(worldX, worldY - 1, worldZ - 1) == block.id) {
                    negZFlow = fullFlowHeight;
                } else negZFlow = getHeightOfFlow(neighborData[NEG_Z], block.liquidMaxFlow, chunkY);

                y00WithLiquid++;
                y10WithLiquid++;
            }
            if (neighbors[POS_Z] == block) {
                if (Client.world.getBlockID(worldX, worldY - 1, worldZ + 1) == block.id) {
                    posZFlow = fullFlowHeight;
                } else posZFlow = getHeightOfFlow(neighborData[POS_Z], block.liquidMaxFlow, chunkY);

                y01WithLiquid++;
                y11WithLiquid++;
            }

            float negXnegZFlow = zeroFlowHeight;
            float negXposZFlow = zeroFlowHeight;
            float posXnegZFlow = zeroFlowHeight;
            float posXposZFlow = zeroFlowHeight;


            if (Client.world.getBlockID(worldX - 1, worldY, worldZ - 1) == block.id) {
                if (Client.world.getBlockID(worldX - 1, worldY - 1, worldZ - 1) == block.id) {
                    negXnegZFlow = fullFlowHeight;
                } else
                    negXnegZFlow = getHeightOfFlow(Client.world.getBlockData(worldX - 1, worldY, worldZ - 1), block.liquidMaxFlow, chunkY);

                y00WithLiquid++;
            }
            if (Client.world.getBlockID(worldX - 1, worldY, worldZ + 1) == block.id) {
                if (Client.world.getBlockID(worldX - 1, worldY - 1, worldZ + 1) == block.id) {
                    negXposZFlow = fullFlowHeight;
                } else
                    negXposZFlow = getHeightOfFlow(Client.world.getBlockData(worldX - 1, worldY, worldZ + 1), block.liquidMaxFlow, chunkY);

                y01WithLiquid++;
            }
            if (Client.world.getBlockID(worldX + 1, worldY, worldZ - 1) == block.id) {
                if (Client.world.getBlockID(worldX + 1, worldY - 1, worldZ - 1) == block.id) {
                    posXnegZFlow = fullFlowHeight;
                } else
                    posXnegZFlow = getHeightOfFlow(Client.world.getBlockData(worldX + 1, worldY, worldZ - 1), block.liquidMaxFlow, chunkY);

                y10WithLiquid++;
            }
            if (Client.world.getBlockID(worldX + 1, worldY, worldZ + 1) == block.id) {
                if (Client.world.getBlockID(worldX + 1, worldY - 1, worldZ + 1) == block.id) {
                    posXposZFlow = fullFlowHeight;
                } else
                    posXposZFlow = getHeightOfFlow(Client.world.getBlockData(worldX + 1, worldY, worldZ + 1), block.liquidMaxFlow, chunkY);

                y11WithLiquid++;
            }

            //Each corner is the average of its four neighbors
            if (y00WithLiquid > 0)
                y00 = Math.min(Math.min(negXnegZFlow, negXFlow), Math.min(negZFlow, centerFlowHeight));
            else y00 = zeroFlowHeight;

            if (y10WithLiquid > 0)
                y10 = Math.min(Math.min(posXnegZFlow, posXFlow), Math.min(negZFlow, centerFlowHeight));
            else y10 = zeroFlowHeight;

            if (y01WithLiquid > 0)
                y01 = Math.min(Math.min(negXposZFlow, negXFlow), Math.min(posZFlow, centerFlowHeight));
            else y01 = zeroFlowHeight;

            if (y11WithLiquid > 0)
                y11 = Math.min(Math.min(posXposZFlow, posXFlow), Math.min(posZFlow, centerFlowHeight));
            else y11 = zeroFlowHeight;

            if (y00 == fullFlowHeight && y10 == fullFlowHeight && y01 == fullFlowHeight && y11 == fullFlowHeight) {
                if (isUsingGreedyMesher) return true;
            }

            //Clamp to chunk
            if (y00 < chunkY) y00 = chunkY;
            if (y10 < chunkY) y10 = chunkY;
            if (y01 < chunkY) y01 = chunkY;
            if (y11 < chunkY) y11 = chunkY;

            //Determine top texture flow
            if (y00 > y10
                    && y00 > y01
                    && y00 > y11) {
                topTextureFlowMode = TEX_FLOW_NEG_X_NEG_Z;
            } else if (y01 > y00
                    && y01 > y10
                    && y01 > y11) {
                topTextureFlowMode = TEX_FLOW_NEG_X_POS_Z;
            } else if (y10 > y00
                    && y10 > y01
                    && y10 > y11) {
                topTextureFlowMode = TEX_FLOW_POS_X_NEG_Z;
            } else if (y11 > y00
                    && y11 > y01
                    && y11 > y10) {
                topTextureFlowMode = TEX_FLOW_POS_X_POS_Z;
            } else if (y00 + y01 < y10 + y11) {
                topTextureFlowMode = TEX_FLOW_POS_X;
            } else if (y00 + y01 > y10 + y11) {
                topTextureFlowMode = TEX_FLOW_NEG_X;
            } else if (y00 + y10 < y01 + y11) {
                topTextureFlowMode = TEX_FLOW_POS_Z;
            } else if (y00 + y10 > y01 + y11) {
                topTextureFlowMode = TEX_FLOW_NEG_Z;
            }

            switch (topTextureFlowMode) {
                case TEX_FLOW_NEG_Z -> topFaceUV = topFaceUV_negZ;
                case TEX_FLOW_POS_X -> topFaceUV = topFaceUV_posX;
                case TEX_FLOW_NEG_X -> topFaceUV = topFaceUV_negX;
                case TEX_FLOW_NEG_X_POS_Z -> topFaceUV = topFaceUV_negX_posZ;
                case TEX_FLOW_POS_X_POS_Z -> topFaceUV = topFaceUV_posX_posZ;
                case TEX_FLOW_NEG_X_NEG_Z -> topFaceUV = topFaceUV_negX_negZ;
                case TEX_FLOW_POS_X_NEG_Z -> topFaceUV = topFaceUV_posX_negZ;
            }
        }


        //We have to do this because we use this method to check if we should use the greedy mesher
        if (buffer == null) return false;

        if (sideIsVisibleXZ(block, data, neighborData[NEG_X], neighbors[NEG_X], onSurface)) {
            texLayer = (block.texture.getNEG_X());
            //NEG_X FACE:
            buffer.vertex(chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, y00, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, y01, 1.0f + chunkZ, /* uvs */ 1.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);

            buffer.vertex(chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, yFloor, chunkZ, /* uvs */ 0.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, y00, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);
        }

        if (sideIsVisibleXZ(block, data, neighborData[POS_X], neighbors[POS_X], onSurface)) {
            texLayer = (block.texture.getPOS_X());
            //POS_X FACE:
            buffer.vertex(1.0f + chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, y11, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, y10, chunkZ, /* uvs */ 1.0f, 0.0f, POS_X, texLayer, light[POS_X]);

            buffer.vertex(1.0f + chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, y11, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_X, texLayer, light[POS_X]);
        }

        if (sideIsVisibleY(block, neighbors[POS_Y])) {
            if (topTextureFlowMode == TEX_FLOW_STATIC) texLayer = (block.texture.getPOS_Y());
            else texLayer = (block.texture.getPOS_X());

            //POS_Y FACE:
            buffer.vertex(1.0f + chunkX, y10, chunkZ,           /* uvs */ topFaceUV[2], topFaceUV[3], POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, y01, 1.0f + chunkZ,           /* uvs */ topFaceUV[6], topFaceUV[7], POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, y00, chunkZ,                     /* uvs */ topFaceUV[0], topFaceUV[1], POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(1.0f + chunkX, y10, chunkZ,           /* uvs */ topFaceUV[2], topFaceUV[3], POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(1.0f + chunkX, y11, 1.0f + chunkZ, /* uvs */ topFaceUV[4], topFaceUV[5], POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, y01, 1.0f + chunkZ,           /* uvs */ topFaceUV[6], topFaceUV[7], POS_Y, texLayer, light[POS_Y]);
        }

        if (sideIsVisibleXZ(block, data, neighborData[NEG_Z], neighbors[NEG_Z], onSurface)) {
            texLayer = (block.texture.getNEG_Z());
            //NEG_Z FACE:
            buffer.vertex(chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, y10, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(chunkX, y00, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);

            buffer.vertex(chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, yFloor, chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, y10, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);
        }

        if (sideIsVisibleY(block, neighbors[NEG_Y])) {
            texLayer = (block.texture.getNEG_Y());
            //NEG_Y FACE:
            buffer.vertex(chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, yFloor, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);

            buffer.vertex(chunkX, yFloor, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
        }

        if (sideIsVisibleXZ(block, data, neighborData[POS_Z], neighbors[POS_Z], onSurface)) {
            texLayer = (block.texture.getPOS_Z());
            //POS_Z FACE:
            buffer.vertex(1.0f + chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, y01, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(1.0f + chunkX, y11, 1.0f + chunkZ, /* uvs */ 1.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);

            buffer.vertex(1.0f + chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, yFloor, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, y01, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);
        }

        return false;
    }


    public boolean sideIsVisibleXZ(Block block, BlockData data, BlockData neighborData, Block neighbor,
                                   boolean isTopLiquid) {
        if (neighbor.isAir()) return true;
        return false;
    }

    public boolean sideIsVisibleY(Block block, Block neighbor) {
        if (neighbor.isAir()) return true;
        return false;
    }
}
