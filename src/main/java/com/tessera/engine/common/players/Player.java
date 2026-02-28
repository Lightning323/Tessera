/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.players;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.skin.Skin;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.worldInteraction.collision.EntityAABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * @author zipCoder933
 */
public class Player {
    public final EntityAABB aabb;
    public final Vector3f worldPosition;
    public float pan, tilt;
    private Skin skin;
    private int skinID = 0;
    boolean operator = false;
    private String name;//Every name MUST be unique

    public Skin getSkin() {
        return skin;
    }

    public int getSkinID() {
        return skinID;
    }

    public void setSkin(int id) {
        this.skin = Main.skins.get(id).get(this);
        this.skinID = id;
    }

    public ChannelBase channel;

    public Player(ChannelBase channel) {
        this();
        this.channel = channel;
    }

    public Player() {
        setName(System.getProperty("user.name"));
        aabb = new EntityAABB();
        initAABB();
        worldPosition = aabb.worldPosition;
        setSkin(0);
    }

    public final static float PLAYER_HEIGHT = 1.5f;
    public final static float PLAYER_WIDTH = 0.7f;

    private void initAABB() {
        aabb.size.set(PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_WIDTH);
        aabb.offset.set(-(PLAYER_WIDTH * 0.5f), -0.15f, -(PLAYER_WIDTH * 0.5f));
    }


    public void render(Matrix4f projection, Matrix4f view) {
        if (getSkin() != null) getSkin().super_render(projection, view);
    }

    @Override
    public String toString() {
        String n = "Unknown";
        if (getName() != null) {
            n = getName();
        } else if (channel != null) n = channel.remoteAddress().toString();
        if (this == Client.userPlayer) n += " (Me)";
        return n;
    }

    public String getConnectionStatus() {
        return "Connected: " + channel.isActive();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        //The name cannot be too long, the name is basically the ID for the server to find the player
        if (name == null || name.isBlank()) name = System.getProperty("user.name");
        if (name.length() > 16) name = name.substring(0, 16);
        this.name = name;
    }

    public boolean isOperator() {
        return operator;
    }

    public void setOperator(boolean operator) {
        this.operator = operator;
    }
}
