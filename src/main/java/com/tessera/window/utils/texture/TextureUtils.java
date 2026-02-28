/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.texture;

import com.tessera.utils.resource.ResourceLoader;
import com.tessera.window.utils.IOUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_HEIGHT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_INTERNAL_FORMAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WIDTH;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glGetTexLevelParameteri;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.util.List;

import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * @author zipCoder933
 */
public class TextureUtils {

    static final ResourceLoader resourceLoader = new ResourceLoader();
    private static ArrayList<Integer> textures = new ArrayList<>();

    public static void saveTextureAsPNG(int textureID, File file) throws IOException {
        ImageIO.write(getTextureAsBufferedImage(textureID), "png", file);
    }

    /**
     * from
     * https://computergraphics.stackexchange.com/questions/4936/lwjgl-opengl-get-bufferedimage-from-texture-id
     *
     * @param textureID
     */
    public static BufferedImage getTextureAsBufferedImage(int textureID) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);    // Bind the texture you want to save

        int format = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT); //We can get all the information about the texture directly from opengl
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

//Using that information we again create a ByteBuffer as well as a BufferedImage. Reading the pixels from the ByteBuffer and placing them on the BufferedImage.
        int channels = 4;
        if (format == GL_RGB) {
            channels = 3;
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_UNSIGNED_BYTE, buffer);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
//                int i = (x + y * width) * channels;
                int i = (x + (height - 1 - y) * width) * channels; //Flip the image

                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = 255;
                if (channels == 4) {
                    a = buffer.get(i + 3) & 0xFF;
                }

                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    /**
     * Add texture to the list, so that the program can keep track of all
     * textures when it comes time to clean them all up.
     *
     * @param id the texture ID
     */
    public static void addTexture(int id) {
        textures.add(id);
    }

    public static Texture makeTextureArray(int imageWidth, int imageHeight, boolean linearFiltering, String... files) throws IOException {
        TextureRequest[] textureFiles = new TextureRequest[files.length];
        for (int i = 0; i < files.length; i++) {
            textureFiles[i] = new TextureRequest(files[i]);
        }
        return makeTextureArray(imageWidth, imageHeight, linearFiltering, textureFiles);
    }

    public static Texture makeTextureArray(int imageWidth, int imageHeight, boolean linearFiltering, List<TextureRequest> files) throws IOException {
        TextureRequest[] textureFiles = new TextureRequest[files.size()];
        for (int i = 0; i < files.size(); i++) {
            textureFiles[i] = files.get(i);
        }
        return makeTextureArray(imageWidth, imageHeight, linearFiltering, textureFiles);
    }

    /**
     * @param imageWidth
     * @param imageHeight
     * @param linearFiltering
     * @param files           the texture files
     * @return
     * @throws Exception
     */
    public static Texture makeTextureArray(int imageWidth, int imageHeight, boolean linearFiltering, TextureRequest... files) throws IOException {
        int id = glGenTextures();
        Texture texture = new Texture(id, imageWidth, imageHeight);
        addTexture(id);
        int layerCount = files.length; //the number of images we are making into one

        glBindTexture(GL_TEXTURE_2D_ARRAY, id);
//        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);

        if (linearFiltering) {
            GL11.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        } else {
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, imageWidth, imageHeight, layerCount, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        for (int i = 0; i < layerCount; i++) {
            ByteBuffer fullImage;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                TextureRequest file = files[i];
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                if (file == null) {
                    throw new IOException("Image file [" + file + "] is null");
                }

                fullImage = stbi_load_from_memory(file.image, w, h, channels, 4);
                if (fullImage == null) {
                    throw new IOException("Image file [" + file + "] not loaded: " + stbi_failure_reason());
                }
                if (file.regionX != -1) { //if we have a region, we need to crop the image
                    ByteBuffer section = makeRegionOfImage(fullImage, file, w.get(0), h.get(0));
                    MemoryUtil.memFree(fullImage);
                    fullImage = section;
                }
                texture.buffer = fullImage;

                GL30.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, //stacks another texure on top
                        0, 0, 0, i, //image id
                        imageWidth, imageHeight, //image size
                        1, GL_RGBA, GL_UNSIGNED_BYTE, fullImage);

                stbi_image_free(fullImage);
            }
        }

        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        if (id == 0) {
            return null;
        }
        return texture;
    }

    public static ByteBuffer makeRegionOfImage(ByteBuffer fullImage, TextureRequest file, int imageWidth, int imageHeight) {
        // Create a new buffer for the section
        ByteBuffer section = MemoryUtil.memAlloc(file.regionWidth * file.regionHeight * 4);

        // Copy pixels from the full image to the section buffer
        for (int y = 0; y < file.regionHeight; y++) {
            int fullImageOffset = ((file.regionY + y) * imageWidth + file.regionX) * 4;
            int sectionOffset = y * file.regionWidth * 4;
            MemoryUtil.memCopy(MemoryUtil.memAddress(fullImage) + fullImageOffset, MemoryUtil.memAddress(section) + sectionOffset, file.regionWidth * 4);
        }
        return section;
    }

    public static Texture loadTexture(ByteBuffer buffer,
                                      int width,
                                      int height,
                                      boolean linearFiltering) throws IOException {

        //<editor-fold defaultstate="collapsed" desc="load the bytes of the texture to memory">
        try {
            if (buffer == null) {
                throw new IOException("Can't load image:" + "\n" + STBImage.stbi_failure_reason());
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="load and configure the texture to opengl">
            int id = GL11.glGenTextures(); //Generate the texture ID
            Texture texture = new Texture(buffer, id, width, height);
            addTexture(id);

            /**
             * GL11.glBindTexture() Binds the texture to the 2D texture
             * target(gl_texture_2d). Now, any texture operations (referencing
             * gl_texture_2d) will affect the bound texture (textureID)
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

//Give the image to opengl:
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            if (linearFiltering) {
// When MAGnifying the image (no bigger mipmap available), use LINEAR filtering
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); //linear or nearest
// When MINifying the image, use a LINEAR blend of two mipmaps, each filtered LINEARLY too
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR); //linear or nearest
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            }

// Generate mipmaps:
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            STBImage.stbi_image_free(buffer);

            /**
             * unbinds (unbinds) any texture currently bound to the 2D texture
             * target. After this call, further texture operations for
             * GL_TEXTURE_2D will have no effect until a new texture is bound.
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//</editor-fold>

            if (id == 0) {
                return null;
            }
            return texture;
        } catch (Exception e) {
            throw new IOException("Unable to load texture: ", e);
        }
    }

    public static Texture loadTextureFromFile(File file, boolean linearFiltering) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="load the bytes of the texture to memory">
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            buffer = STBImage.stbi_load(file.getAbsolutePath(), w, h, channels, 4);
            if (buffer == null) {
                throw new IOException("Can't load image from " + file.getAbsolutePath() + ":" + "\n" + STBImage.stbi_failure_reason());
            }

            int width = w.get();
            int height = h.get();
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="load and configure the texture to opengl">
            int id = GL11.glGenTextures(); //Generate the texture ID
            Texture texture = new Texture(buffer, id, width, height);
            addTexture(id);

            /**
             * GL11.glBindTexture() Binds the texture to the 2D texture
             * target(gl_texture_2d). Now, any texture operations (referencing
             * gl_texture_2d) will affect the bound texture (textureID)
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

//Give the image to opengl:
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            if (linearFiltering) {
// When MAGnifying the image (no bigger mipmap available), use LINEAR filtering
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); //linear or nearest
// When MINifying the image, use a LINEAR blend of two mipmaps, each filtered LINEARLY too
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR); //linear or nearest
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            }

// Generate mipmaps:
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            STBImage.stbi_image_free(buffer);

            /**
             * unbinds (unbinds) any texture currently bound to the 2D texture
             * target. After this call, further texture operations for
             * GL_TEXTURE_2D will have no effect until a new texture is bound.
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//</editor-fold>

            if (id == 0) {
                return null;
            }
            return texture;
        } catch (Exception e) {
            throw new IOException("Unable to load texture: ", e);
        }
    }

    public static Texture loadTextureFromResource(String path, boolean linearFiltering) throws IOException {
        InputStream is = resourceLoader.getResourceAsStream(path);
        ByteBuffer buffer = IOUtil.inputStreamToByteBuffer(is, 512);
        return loadTexture(buffer, linearFiltering);
    }

    public static Texture loadTexture(ByteBuffer input, boolean linearFiltering) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="load the bytes of the texture to memory">
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            buffer = STBImage.stbi_load_from_memory(input, w, h, channels, 4);
            if (buffer == null) {
                throw new IOException("Can't load image from buffer:" + "\n" + STBImage.stbi_failure_reason());
            }

            int width = w.get();
            int height = h.get();
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="load and configure the texture to opengl">
            int id = GL11.glGenTextures(); //Generate the texture ID
            Texture texture = new Texture(buffer, id, width, height);
            addTexture(id);

            /**
             * GL11.glBindTexture() Binds the texture to the 2D texture
             * target(gl_texture_2d). Now, any texture operations (referencing
             * gl_texture_2d) will affect the bound texture (textureID)
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

//Give the image to opengl:
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            if (linearFiltering) {
// When MAGnifying the image (no bigger mipmap available), use LINEAR filtering
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); //linear or nearest
// When MINifying the image, use a LINEAR blend of two mipmaps, each filtered LINEARLY too
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR); //linear or nearest
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            }

// Generate mipmaps:
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            STBImage.stbi_image_free(buffer);

            /**
             * unbinds (unbinds) any texture currently bound to the 2D texture
             * target. After this call, further texture operations for
             * GL_TEXTURE_2D will have no effect until a new texture is bound.
             */
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//</editor-fold>

            if (id == 0) {
                return null;
            }
            return texture;
        } catch (Exception e) {
            throw new IOException("Unable to load texture: ", e);
        }
    }

    public static void deleteAllTextures() {
        for (Integer tex : textures) {
            glDeleteTextures(tex);
        }
        textures.clear();
    }

    /**
     * @param id
     */
    public void deleteTexture(int id) {
        glDeleteTextures(id);
    }

    //<editor-fold defaultstate="collapsed" desc="unused">
    //
//    public static int loadDDS(String filepath) {
//        ByteBuffer imageBuffer;
//        try {
//            imageBuffer = Utils.ioResourceToByteBuffer(filepath, 8 * 1024);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        IntBuffer width = BufferUtils.createIntBuffer(1);
//        IntBuffer height = BufferUtils.createIntBuffer(1);
//        IntBuffer components = BufferUtils.createIntBuffer(1);
//
//        if (!STBImage.stbi_info_from_memory(imageBuffer, width, height, components)) {
//            throw new RuntimeException("Failed to read image information: " + STBImage.stbi_failure_reason());
//        }
//
//        int textureID = glGenTextures();
//        glBindTexture(GL_TEXTURE_2D, textureID);
//
//        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
//
//        int format;
//        if (components.get(0) == 3) {
//            format = GL_RGB;
//        } else if (components.get(0) == 4) {
//            format = GL_RGBA;
//        } else {
//            throw new RuntimeException("Unsupported number of components: " + components.get(0));
//        }
//
//        glTexImage2D(GL_TEXTURE_2D, 0, format, width.get(0), height.get(0), 0, format, GL_UNSIGNED_BYTE, imageBuffer);
//
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//
//        glBindTexture(GL_TEXTURE_2D, 0);
//
//        return textureID;
//    }
//    public static int loadBMP_custom(String imagepath) {
//        byte[] header = new byte[54];
//        int dataPos;
//        int width, height;
//        int imageSize;
//
//        byte[] data;
//
//        try {
//            FileInputStream file = new FileInputStream(imagepath);
//            file.read(header, 0, 54);
//            if (header[0] != 'B' || header[1] != 'M') {
//                System.out.println("Not a correct BMP file");
//                return 0;
//            }
//
//            dataPos = ByteBuffer.wrap(header, 0x0A, 4).getInt();
//            imageSize = ByteBuffer.wrap(header, 0x22, 4).getInt();
//            width = ByteBuffer.wrap(header, 0x12, 4).getInt();
//            height = ByteBuffer.wrap(header, 0x16, 4).getInt();
//
//            if (imageSize == 0) {
//                imageSize = width * height * 3;
//            }
//            if (dataPos == 0) {
//                dataPos = 54;
//            }
//
//            data = new byte[imageSize];
//            file.read(data, 0, imageSize);
//
//            file.close();
//        } catch (IOException e) {
//            System.out.println("Image could not be opened");
//            return 0;
//        }
//
//        IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);
//        GL11.glGenTextures(textureIDBuffer);
//        int textureID = textureIDBuffer.get(0);
//
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
//        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data));
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//
//        return textureID;
//    }
//</editor-fold>
}
