package com.tessera.engine.client.visuals.gameScene.rendering.chunk.occlusionCulling;

import com.tessera.engine.common.math.AABB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class BoundingBoxMesh {
    private int vaoId;
    private int vboId;
    int verts;
    private float[] vertices; // 8 vertices of the box, each with 3 coordinates


    public BoundingBoxMesh() {
        // Create the VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create the VBO and buffer the vertices
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        // Define structure of the data
        GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL30.glEnableVertexAttribArray(0);

        // Unbind the VBO and VAO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void setBounds(AABB boundaries) {
        setBounds(boundaries.min.x, boundaries.min.y, boundaries.min.z,
                boundaries.max.x, boundaries.max.y, boundaries.max.z);
    }

    public void setBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        // Initialize the vertices of the bounding box
        this.vertices = new float[]{
                minX, minY, minZ, // triangle 1 : begin
                minX, minY, maxZ, //each line is a vertex
                minX, maxY, maxZ, // triangle 1 : end
                maxX, maxY, minZ, // triangle 2 : begin
                minX, minY, minZ,
                minX, maxY, minZ, // triangle 2 : end
                maxX, minY, maxZ,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ,
                minX, minY, minZ,
                minX, minY, minZ,
                minX, maxY, maxZ,
                minX, maxY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ,
                minX, minY, minZ,
                minX, maxY, maxZ,
                minX, minY, maxZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ,
                maxX, maxY, maxZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                minX, maxY, minZ,
                maxX, maxY, maxZ,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ,
                maxX, minY, maxZ
        };
        verts = vertices.length / 3;

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
    }

    public void render() {
        // Bind the VAO
        GL30.glBindVertexArray(vaoId);
        // Draw the bounding box as triangles
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, verts);
    }

    public void renderWireframe() {
        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, verts);
        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
    }

    public void cleanup() {
        // Delete the VBO and VAO
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
