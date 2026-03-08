package com.tessera.engine.server.builtinMechanics.gravityBlock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh_ArrayTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.block.BlockVertexSet;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.block.meshers.Block_NaiveMesher;
import com.tessera.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.tessera.engine.common.worldInteraction.collision.PositionHandler;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;

public class GravityBlockEntity extends Entity {
    private final BlockVertexSet buffer = new BlockVertexSet();
    private final ChunkVoxels voxels = new ChunkVoxels(1, 1, 1);
    private EntityMesh_ArrayTexture mesh;


    Box box;
    PositionHandler positionHandler;
    Block block;
    long startTime;

    public GravityBlockEntity(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier);
        positionHandler = new PositionHandler(window, Client.world, aabb, null);
        aabb.setOffsetAndSize(0, 0, 0, 1, 1, 1);
        frustumSphereRadius = 1;
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) {
        box = new Box();
        box.setColor(1, 0, 0, 1);
        box.setLineWidth(4);
        mesh = new EntityMesh_ArrayTexture();

        buffer.reset();
        voxels.setBlock(0, 0, 0, block.id);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Block_NaiveMesher mesher = new Block_NaiveMesher(voxels, new Vector3i(0, 0, 0), true);
            mesher.compute(buffer, buffer, stack, 1, false);
            buffer.makeVertexSet();
            buffer.sendToMesh(mesh);
        }
        startTime = System.currentTimeMillis();
    }

    public static final int WAIT_TIME = 300;


    public void server_update() {
    }

    @Override
    public void client_draw() {
        if (positionHandler.isFrozen() || positionHandler.onGround ||
                positionHandler.collisionHandler.collisionData.block_penPerAxes.y < 0) {
            Main.getServer().setBlock(block.id, (int) worldPosition.x, (int) worldPosition.y, (int) worldPosition.z);
            destroy();
        } else {
            //There is actually something in the buffer
            arrayTextureShader.bind();//TODO: Allow better integration with an arrayTextureShader in Entity class
            arrayTextureShader.setSunAndTorch(sunValue, torchValue);
            arrayTextureShader.updateProjectionViewMatrix(GameScene.projection, GameScene.view);
            modelMatrix.update();
            modelMatrix.sendToShader(arrayTextureShader.getID(), arrayTextureShader.uniform_modelMatrix);
            mesh.draw(false, Registrys.blocks.textures.getTexture().id);

            //            box.set(aabb.box);
            //            box.draw(GameScene.projection, GameScene.view);
            if (System.currentTimeMillis() - startTime > WAIT_TIME) positionHandler.update(1);
        }
    }
}

