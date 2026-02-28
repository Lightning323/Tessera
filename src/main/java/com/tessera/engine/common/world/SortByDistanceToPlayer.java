/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.world;

import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3f;

import java.util.Comparator;

/**
 * @author zipCoder933
 */
class SortByDistanceToPlayer implements Comparator<Chunk> {

    final Vector3f playerPos;

    public SortByDistanceToPlayer(Vector3f playerPos) {
        this.playerPos = playerPos;
    }

    @Override
    public int compare(Chunk chunk, Chunk chunk1) {
        float d1 = playerPos.distance(
                (chunk.position.x * Chunk.WIDTH) + Chunk.HALF_WIDTH,
                (chunk.position.y * Chunk.WIDTH) + Chunk.HALF_WIDTH,
                (chunk.position.z * Chunk.WIDTH) + Chunk.HALF_WIDTH);
        float d2 = playerPos.distance(
                (chunk1.position.x * Chunk.WIDTH) + Chunk.HALF_WIDTH,
                (chunk1.position.y * Chunk.WIDTH) + Chunk.HALF_WIDTH,
                (chunk1.position.z * Chunk.WIDTH) + Chunk.HALF_WIDTH);
        return Float.compare(d1, d2);
    }

}
