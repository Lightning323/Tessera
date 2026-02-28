package com.tessera.engine.client.skin;

import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.tessera.engine.common.players.Player;
import org.joml.Vector3i;
import org.joml.Vector4f;

public class BoxSkin extends Skin {
    Box box;
    private Vector3i color = new Vector3i();

    public BoxSkin(Player player, int r, int g, int b) {
        super("Box", player);
        color.set(r, g, b);
    }

    public BoxSkin(String name, Player player, int r, int g, int b) {
        super(name, player);
        color.set(r, g, b);
    }

    @Override
    public void init() {
        box = new Box();
        box.setColor(new Vector4f(color.x, color.y, color.z, 1));
        box.setLineWidth(3);
    }

    @Override
    public void render() {
        box.draw(GameScene.projection, GameScene.view);
    }
}
