package com.tessera.engine.server.block;

import com.tessera.window.utils.texture.TextureRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BuiltinTextures {
    protected static void addBuiltinTextures(List<TextureRequest> imageFiles,
                                             AtomicInteger index, int textureSize) throws IOException {

        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_0.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_1.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_2.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_3.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_4.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_5.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_6.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_7.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_8.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
        imageFiles.add(new TextureRequest("/assets/tessera/textures/block/builtin/destroy_stage_9.png", 0, 0, textureSize, textureSize));
        index.incrementAndGet();
    }
}
