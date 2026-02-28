package com.xbuilders.window.utils.texture;

import com.xbuilders.utils.resource.ResourceLoader;
import com.xbuilders.window.utils.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TextureRequest {
    ByteBuffer image;
    int regionX;
    int regionY;
    int regionWidth;
    int regionHeight;

    public TextureRequest(ByteBuffer buffer,
                          int regionX, int regionY, int regionWidth, int regionHeight) {
        image = buffer;
        this.regionX = regionX;
        this.regionY = regionY;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
    }

    public TextureRequest(ByteBuffer buffer) {
        image = buffer;
        regionX = -1;
        regionY = -1;
        regionWidth = -1;
        regionHeight = -1;
    }

    private final static ResourceLoader resourceLoader = new ResourceLoader();

    public TextureRequest(String resource,
                          int regionX, int regionY, int regionWidth, int regionHeight) throws IOException {
        image = IOUtil.inputStreamToByteBuffer(resourceLoader.getResourceAsStream(resource),512);
        this.regionX = regionX;
        this.regionY = regionY;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
    }

    public TextureRequest(String resource) throws IOException {
        image = IOUtil.inputStreamToByteBuffer(resourceLoader.getResourceAsStream(resource),512);
        regionX = -1;
        regionY = -1;
        regionWidth = -1;
        regionHeight = -1;
    }


}
