package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.Main;
import com.tessera.content.vanilla.blocks.RenderType;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.content.vanilla.Blocks;
import org.joml.Vector3i;

import java.util.ArrayList;

public class BlockStraightTrack extends Block {

    public BlockStraightTrack(int id) {
        super(id, "straight_track", new BlockTexture("track.png", "track.png", "track.png"), RenderType.FLOOR);
        solid = false;
        opaque = false;

        localChangeEvent(false, ((history, changedPosition, thisPosition) -> {
            int x = thisPosition.x;
            int y = thisPosition.y;
            int z = thisPosition.z;
            changeBlock(x, y, z);
        }));
        setBlockEvent(false, (x, y, z) -> {
            changeBlock(x, y, z);
        });
    }

    private void addNeighbor(ArrayList<Vector3i> trackPositions, int x2, int y2, int z2) {
        Block b = Client.world.getBlock(x2, y2, z2);
        if (isTrack(b)) {
            trackPositions.add(new Vector3i(x2, y2, z2));
        }
    }

    public static boolean isTrack(Block block) {
        return block.id == Blocks.BLOCK_STRAIGHT_TRACK
                || block.id == Blocks.BLOCK_RAISED_TRACK
                || block.id == Blocks.BLOCK_CROSSTRACK
                || block.id == Blocks.BLOCK_CURVED_TRACK
                || block.id == Blocks.BLOCK_SWITCH_JUNCTION
                || block.id == Blocks.BLOCK_MERGE_TRACK
                || block.id == Blocks.BLOCK_TRACK_STOP;
    }

    private void changeBlock(int x, int y, int z) {
        ArrayList<Vector3i> trackNeighbors = new ArrayList<>();
        BlockData orientation = new BlockData(new byte[]{0, 0});
//        GameScene.world.setBlockData(orientation, x, y, z);

//        orientation.set(0, (byte) (Math.random() * 4));
//        GameScene.player.setBlock(this, x, y, z, orientation);

        addNeighbor(trackNeighbors, x + 1, y, z);
        addNeighbor(trackNeighbors, x - 1, y, z);
        addNeighbor(trackNeighbors, x, y, z + 1);
        addNeighbor(trackNeighbors, x, y, z - 1);

        addNeighbor(trackNeighbors, x + 1, y + 1, z);
        addNeighbor(trackNeighbors, x - 1, y + 1, z);
        addNeighbor(trackNeighbors, x, y + 1, z + 1);
        addNeighbor(trackNeighbors, x, y + 1, z - 1);

        addNeighbor(trackNeighbors, x + 1, y - 1, z);
        addNeighbor(trackNeighbors, x - 1, y - 1, z);
        addNeighbor(trackNeighbors, x, y - 1, z + 1);
        addNeighbor(trackNeighbors, x, y - 1, z - 1);

        boolean curvedTrack = false;
        boolean straightTrack = false;

        if (!trackNeighbors.isEmpty()) {
            if (isTrackAtPos2(trackNeighbors, x - 1, y, z) && isTrackAtPos2(trackNeighbors, x, y, z - 1)) {
                orientation.set(0, (byte) 3);
                Main.getServer().setBlock(Blocks.BLOCK_CURVED_TRACK, orientation, x, y, z);
                curvedTrack = true;
                return;
            } else if (isTrackAtPos2(trackNeighbors, x + 1, y, z) && isTrackAtPos2(trackNeighbors, x, y, z - 1)) {
                orientation.set(0, (byte) 0);
                Main.getServer().setBlock(Blocks.BLOCK_CURVED_TRACK, orientation, x, y, z);
                curvedTrack = true;
                return;
            } else if (isTrackAtPos2(trackNeighbors, x - 1, y, z) && isTrackAtPos2(trackNeighbors, x, y, z + 1)) {
                orientation.set(0, (byte) 2);
                Main.getServer().setBlock(Blocks.BLOCK_CURVED_TRACK, orientation, x, y, z);
                curvedTrack = true;
                return;
            } else if (isTrackAtPos2(trackNeighbors, x + 1, y, z) && isTrackAtPos2(trackNeighbors, x, y, z + 1)) {
                orientation.set(0, (byte) 1);
                Main.getServer().setBlock(Blocks.BLOCK_CURVED_TRACK, orientation, x, y, z);
                curvedTrack = true;
                return;
            } //=====================
            else if (isTrackAtPos2(trackNeighbors, x - 1, y, z) || isTrackAtPos2(trackNeighbors, x + 1, y, z)) {
                orientation.set(0, (byte) 2);
                straightTrack = true;
                Main.getServer().setBlock(this.id, orientation, x, y, z);
            } else if (isTrackAtPos2(trackNeighbors, x, y, z - 1) || isTrackAtPos2(trackNeighbors, x, y, z + 1)) {
                orientation.set(0, (byte) 1);
                Main.getServer().setBlock(this.id, orientation, x, y, z);
                straightTrack = true;
            }
        }
        if (!curvedTrack) {
            if (isTrackAtPos(trackNeighbors, x + 1, y - 1, z)) {
                orientation.set(0, (byte) 2);
                Main.getServer().setBlock(Blocks.BLOCK_RAISED_TRACK, orientation, x, y, z);
                return;
            } else if (isTrackAtPos(trackNeighbors, x - 1, y - 1, z)) {
                orientation.set(0, (byte) 0);
                Main.getServer().setBlock(Blocks.BLOCK_RAISED_TRACK, orientation, x, y, z);
                return;
            } else if (isTrackAtPos(trackNeighbors, x, y - 1, z + 1)) {
                orientation.set(0, (byte) 3);
                Main.getServer().setBlock(Blocks.BLOCK_RAISED_TRACK, orientation, x, y, z);
                return;
            } else if (isTrackAtPos(trackNeighbors, x, y - 1, z - 1)) {
                orientation.set(0, (byte) 1);
                Main.getServer().setBlock(Blocks.BLOCK_RAISED_TRACK, orientation, x, y, z);
                return;
            }
        }
    }


    private boolean isTrackAtPos(ArrayList<Vector3i> trackPositions, int x, int y, int z) {
        return trackPositions.contains(new Vector3i(x, y, z));
    }

    private boolean isTrackAtPos2(ArrayList<Vector3i> trackPositions, int x, int y, int z) {
        return trackPositions.contains(new Vector3i(x, y, z))
                || trackPositions.contains(new Vector3i(x, y + 1, z))
                || trackPositions.contains(new Vector3i(x, y - 1, z));
    }
}