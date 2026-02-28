package com.tessera.utils.imageAtlas;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageAtlas {

    /**
     * @return the individualTextureSize
     */
    public int getIndividualTextureSize() {
        return individualTextureSize;
    }

    /**
     * @return the imageSize
     */
    public int getImageWidth() {
        return imageSize;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    private BufferedImage image;
    private int imageSize;
    private int individualTextureSize;

    /**
     *
     * @param applet the PApplet
     * @param imagePath the path to the image
     * @param atlasRows the number of rows in the atlas (note: The number of
     * columns must be the same as the number of rows.)
     * @throws IOException
     */
    public ImageAtlas(File imagePath, int atlasRows) throws IOException {
        //atlasRows=32 for a 512x512 image with 16x16 blocks each
        if (!imagePath.exists()) {
            throw new IOException("The image \"" + imagePath.getAbsolutePath() + "\" does not exist!");
        }
        image = ImageIO.read(imagePath);
        imageSize = image.getWidth();
        individualTextureSize = imageSize / atlasRows;//The individual texture size is the size of each atlas block.
    }
    
    public ImageAtlasPosition getImageIndex(int[] pos) {
        float texturePerRow = (float) getImageWidth() / (float) getIndividualTextureSize();
        float indvTexSize = 1.0f / texturePerRow;
        float pixelSize = 1.0f / (float) getImageWidth();

        float xMin = (pos[0] * indvTexSize) + 0.0f * pixelSize;
        float yMin = (pos[1] * indvTexSize) + 0.0f * pixelSize;

        float xMax = (xMin + indvTexSize) - 0.0f * pixelSize;
        float yMax = (yMin + indvTexSize) - 0.0f * pixelSize;
        return new ImageAtlasPosition(xMin, yMin, xMax, yMax);
    }
}
