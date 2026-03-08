package com.tessera.content.vanilla.skins;

import com.tessera.engine.common.players.Player;
import com.tessera.engine.client.skin.Skin;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.utils.resource.PathHandler;
import com.tessera.window.utils.texture.TextureUtils;

import java.io.IOException;

import static com.tessera.Main.LOGGER;

public class FoxSkin extends Skin {

    EntityMesh mesh;
    String texture;
    int textureID;

    public FoxSkin(Player position, String texture) {
        super("fox (" + texture + ")", position);
        this.texture = texture;
    }

    public void init() {
        mesh = new EntityMesh();
        try {
            mesh.loadFromOBJ(PathHandler.resourcePath("skins\\fox\\body.obj"));
            textureID =
                    TextureUtils.loadTextureFromFile(
                            PathHandler.resourcePath("skins\\fox\\" + texture + ".png"),
                    false).id;
        } catch (IOException e) {
            LOGGER.info("Error", e);
        }
    }

    @Override
    public void render() {
        modelMatrix.translate(0, player.aabb.size.y, 0);
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        mesh.draw(false, textureID);
    }
}
