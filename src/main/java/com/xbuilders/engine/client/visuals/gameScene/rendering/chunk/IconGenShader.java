/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.gameScene.rendering.chunk;

import com.xbuilders.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.CompactVertexSet;
import com.xbuilders.utils.resource.ResourceUtils;
import com.xbuilders.window.render.Shader;

import java.io.IOException;

import static com.xbuilders.engine.client.visuals.gameScene.rendering.chunk.ChunkShader.CHUNK_SHADER_DIR;

/**
 * @author zipCoder933
 */
public class IconGenShader extends Shader {

    public final int mvpUniform, maxMult12bitsUniform, maxMult10bitsUniform, textureLayerCountUniform;

    public IconGenShader(int textureLayers) throws IOException {
        init(
                ResourceUtils.localFile(CHUNK_SHADER_DIR+"/vertex.glsl"),
                ResourceUtils.localFile(CHUNK_SHADER_DIR+"/frag_icon.glsl"));
        mvpUniform = getUniformLocation("MVP");
        maxMult12bitsUniform = getUniformLocation("maxMult12bits");
        maxMult10bitsUniform = getUniformLocation("maxMult10bits");
        textureLayerCountUniform = getUniformLocation("textureLayerCount");
        loadFloat(maxMult10bitsUniform, CompactVertexSet.maxMult10bits);
        loadFloat(maxMult12bitsUniform, CompactVertexSet.maxMult12bits);
        loadInt(textureLayerCountUniform, textureLayers - 1);
    }

    @Override
    public void bindAttributes() {
    }

}
