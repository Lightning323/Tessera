/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server.block;

import com.tessera.utils.FileUtils;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.utils.resource.ResourceLoader;
import com.tessera.window.utils.texture.Texture;
import com.tessera.window.utils.texture.TextureRequest;
import com.tessera.window.utils.texture.TextureUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

import static com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.CompactVertexSet.MAX_BLOCK_ANIMATION_LENGTH;

/**
 * @author zipCoder933
 */
public class BlockArrayTexture {

    private final HashMap<String, Integer> textureMap;
    private final HashMap<String, String> fileMap;
    private final HashMap<String, Integer> animationMap;
    public final int layerCount;
    private int textureSize;
    private TextureRequest[] filePaths;
    final Texture texture;


    public Texture getTexture() {
        return texture;
    }

    private static final String fileExtensionCode = "\\.(?i)(jpg|jpeg|png|gif|bmp|ico|tiff)$";

    public static String formatFilepath(String path) {
        if (path == null) return null;
        return path.replaceAll("\\\\", "/").replaceAll(fileExtensionCode, "");
    }

    public boolean fileIsImage(String file) {
        return file.toLowerCase().endsWith(".png")
                || file.toLowerCase().endsWith(".jpg")
                || file.toLowerCase().endsWith(".jpeg");
    }


    public int getTextureLayer(String name) {
        name = formatFilepath(name);
        Integer key = textureMap.get(name);

        if (key == null) {
            throw new NoSuchFieldError("Texture \"" + name + "\" not found! (Texture name: " + name + ")");
        }
        return key;
    }

    public int getAnimationLength(String name) {
        Integer value = animationMap.get(formatFilepath(name));
        return value == null ? 1 : value;
    }

    public String getTextureFile(String name) {
        return fileMap.get(formatFilepath(name));
    }


    AtomicInteger index = new AtomicInteger();
    ResourceLoader resourceLoader = new ResourceLoader();

    public BlockArrayTexture(String blockTexturesDir) throws IOException {
        textureMap = new HashMap<>();
        fileMap = new HashMap<>();
        animationMap = new HashMap<>();

        //load the textures
        String[] allFiles = ResourceLister.listSubResources(blockTexturesDir);
        if (allFiles.length == 0) {
            throw new IOException("No textures in directory");
        }

        //Read the first image and get the texture size
        BufferedImage img;
        for (String file : allFiles) {
            System.out.println("Testing file: " + file);
            if (fileIsImage(file)) {
                img = ImageIO.read(resourceLoader.getResourceAsStream(file));
                textureSize = img.getWidth();
                System.out.println("Texture size: " + textureSize);
                break;
            }
        }


        List<TextureRequest> imageFiles = new ArrayList<>();
        index.set(1);

        /**
         * Image files is the list that is given directly to the texture array
         * All other files are just for finding the texture
         * The index tells us where in the array the texture is
         */
        //Add some default textures first
        BuiltinTextures.addBuiltinTextures(imageFiles, index, textureSize);

        //Search all texture files within the base directory
        System.out.println("Searching for textures...");
        addAllTexturesFromResourceDir(blockTexturesDir, allFiles, imageFiles);
        System.out.println("Texture Files: " + imageFiles.size());


        filePaths = new TextureRequest[imageFiles.size()];
        for (int i = 0; i < filePaths.length; i++) {
            filePaths[i] = imageFiles.get(i);
        }
        layerCount = filePaths.length;
        System.out.println("Block Texture loaded; Layers: " + layerCount + "; Size: " + textureSize + " x " + textureSize);
        this.texture = TextureUtils.makeTextureArray(textureSize, textureSize, false, filePaths);
    }


    public int createNewArrayTexture() throws IOException {
        return TextureUtils.makeTextureArray(textureSize, textureSize, false, filePaths).id;
    }


    private void addAllTexturesFromResourceDir(
            String baseDir, String[] files, List<TextureRequest> imageFiles) throws IOException {
        for (String file : files) {
            if (fileIsImage(file)) {
                //Get the name of the file without the file extension
                String textureKey = FileUtils.removeBasePath(baseDir, file);
                textureKey = formatFilepath(textureKey);
//                    System.out.println("\tAdding texture: " + textureKey);

                textureMap.put(textureKey, index.get());
                fileMap.put(textureKey, file);


                BufferedImage image = ImageIO.read(resourceLoader.getResourceAsStream(file));
                if (image.getWidth() < image.getHeight()) {//if the image is not square, split it up
                    int lengthMultiplier = image.getHeight() / image.getWidth();
                    for (int j = 0; j < lengthMultiplier; j++) {
                        imageFiles.add(new TextureRequest(file, 0, j * image.getWidth(), image.getWidth(), image.getWidth()));
                        index.getAndAdd(1);
                    }
                    if (lengthMultiplier > MAX_BLOCK_ANIMATION_LENGTH)
                        lengthMultiplier = MAX_BLOCK_ANIMATION_LENGTH;
//                        System.out.println("Splitting " + name + " into " + lengthMultiplier + " pieces");
                    animationMap.put(textureKey, lengthMultiplier);
                } else {
                    imageFiles.add(new TextureRequest(file));
                    index.getAndAdd(1);
                }
            }
        }
    }

//    private void addTexturesFromDir(File baseDir,
//                                    File[] files, List<TextureFile> imageFiles) throws IOException {
//
//        //Search through the files
//        for (int i = 0; i < files.length; i++) {
//            //Recursively search through directories
//            if (files[i].isDirectory()) addTexturesFromDir(baseDir,
//                    files[i].listFiles(), imageFiles);
//            else {
//                String name = files[i].getName();
//                if (name.toLowerCase().endsWith(".png")
//                        || name.toLowerCase().endsWith(".jpg")
//                        || name.toLowerCase().endsWith(".jpeg")) {
//
//                    name = files[i].getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
//                    name = formatFilepath(name);
//
////                    System.out.println("\t"+name+" ("+index.get()+")");
//                    String path = files[i].getAbsolutePath();
//                    textureMap.put(name, index.get());
//                    fileMap.put(name, files[i]);
//
//                    BufferedImage image = ImageIO.read(files[i]);
//                    if (image.getWidth() < image.getHeight()) {//if the image is not square, split it up
//                        int lengthMultiplier = image.getHeight() / image.getWidth();
//                        for (int j = 0; j < lengthMultiplier; j++) {
//                            imageFiles.add(new TextureFile(path, 0, j * image.getWidth(), image.getWidth(), image.getWidth()));
//                            index.getAndAdd(1);
//                        }
//                        if (lengthMultiplier > MAX_BLOCK_ANIMATION_LENGTH)
//                            lengthMultiplier = MAX_BLOCK_ANIMATION_LENGTH;
////                        System.out.println("Splitting " + name + " into " + lengthMultiplier + " pieces");
//                        animationMap.put(name, lengthMultiplier);
//                    } else {
//                        imageFiles.add(new TextureFile(path));
//                        index.getAndAdd(1);
//                    }
//                }
//            }
//        }
//    }


}
