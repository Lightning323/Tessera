package com.tessera.engine.common.players.localPlayer.camera;

import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;

public class FrustumCullingTester {

    private final Matrix4f prjViewMatrix;
    private final FrustumIntersection frustumIntersection;

    public FrustumCullingTester() {
        this.prjViewMatrix = new Matrix4f();
        this.frustumIntersection = new FrustumIntersection();
    }

    public boolean isChunkInside(int chunkXPos, int chunkYPos, int chunkZPos) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Vector3f minPoint = new Vector3f(stack.mallocFloat(3));
            Vector3f maxPoint = new Vector3f(stack.mallocFloat(3));

            minPoint.set(chunkXPos * Chunk.WIDTH,
                    chunkYPos * Chunk.WIDTH,
                    chunkZPos * Chunk.WIDTH);

            maxPoint.set(chunkXPos * Chunk.WIDTH + Chunk.WIDTH,
                    chunkYPos * Chunk.WIDTH + Chunk.WIDTH,
                    chunkZPos * Chunk.WIDTH + Chunk.WIDTH);

            return frustumIntersection.testAab(minPoint, maxPoint);
        }
    }

    // Do a wide frustum check for our chunk (put all neghbors into our AABB)
    public boolean isChunkPlusNeghborsInside(Vector3i chunkPos) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Vector3f minPoint = new Vector3f(stack.mallocFloat(3));
            Vector3f maxPoint = new Vector3f(stack.mallocFloat(3));

            minPoint.set(
                    chunkPos.x * Chunk.WIDTH - Chunk.WIDTH,
                    chunkPos.y * Chunk.WIDTH - Chunk.WIDTH,
                    chunkPos.z * Chunk.WIDTH - Chunk.WIDTH);

            maxPoint.set(
                    chunkPos.x * Chunk.WIDTH + Chunk.WIDTH * 3,
                    chunkPos.y * Chunk.WIDTH + Chunk.WIDTH * 3,
                    chunkPos.z * Chunk.WIDTH + Chunk.WIDTH * 3);

            return frustumIntersection.testAab(minPoint, maxPoint);
        }
    }

    public boolean isPillarChunkInside(int x,int z, int chunkMinY, int chunkMaxY) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Vector3f minPoint = new Vector3f(stack.mallocFloat(3));
            Vector3f maxPoint = new Vector3f(stack.mallocFloat(3));

            minPoint.set(
                    x * Chunk.WIDTH - Chunk.WIDTH,
                    (chunkMinY * Chunk.WIDTH),
                    z * Chunk.WIDTH - Chunk.WIDTH);

            maxPoint.set(
                    x * Chunk.WIDTH + Chunk.WIDTH * 3,
                    (chunkMaxY * Chunk.WIDTH) + Chunk.WIDTH,
                    z * Chunk.WIDTH + Chunk.WIDTH * 3);

            return frustumIntersection.testAab(minPoint, maxPoint);
        }
    }

    public boolean isAABBInside(AABB aabb) {
        return frustumIntersection.testAab(aabb.min, aabb.max);
    }

    public boolean isSphereInside(Vector3f center, float radius) {
        return frustumIntersection.testSphere(center, radius);
    }

    public void update(Matrix4f projMatrix, Matrix4f viewMatrix) {
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        frustumIntersection.set(prjViewMatrix);
    }
}
