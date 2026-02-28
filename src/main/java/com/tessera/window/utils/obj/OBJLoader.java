
/*
* The MIT License (MIT)
*
* Copyright (c) 2014 Matthew 'siD' Van der Bijl
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
 */
package com.tessera.window.utils.obj;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * OBJloader class. Loads in Wavefront .obj file in to the program.
 *
 * @author Matthew Van der Bijl
 */
public class OBJLoader {

    /**
     * Constructs a new <code>OBJLoader</code>.
     */
    public OBJLoader() {
        super();
    }

    /**
     * @param file the file to be loaded
     * @return the loaded <code>OBJ</code>
     * @throws java.io.FileNotFoundException thrown if the OBJ file is not found
     */
    public static OBJ loadModel(File file) throws FileNotFoundException {
        return loadModel(new Scanner(file));
    }

    /**
     * @param stream the stream to be loaded
     * @return the loaded <code>OBJ</code>
     */
    public static OBJ loadModel(InputStream stream) {
        return loadModel(new Scanner(stream));
    }

    /**
     * @param sc the <code>OBJ</code> to be loaded
     * @return the loaded <code>OBJ</code>
     */
    public static OBJ loadModel(Scanner sc) {
        OBJ model = new OBJ();
        while (sc.hasNextLine()) {
            String ln = sc.nextLine();
            if (ln == null || ln.equals("") || ln.startsWith("#")) {
            } else {
                String[] split = ln.split(" ");
                switch (split[0]) {
                    case "v":
                        model.getVertexCoordinates().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vn":
                        model.getNormals().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vt":
                        model.getTextureCoordinates().add(new Vector2f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2])
                        ));
                        break;
                    case "f":
                        if (split[1].split("/").length == 1) {
                            model.getFaces().add(new OBJ.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[0]),
                                        Integer.parseInt(split[2].split("/")[0]),
                                        Integer.parseInt(split[3].split("/")[0])
                                    },
                                    null, null
                            ));
                        } else if (split[1].split("/").length == 2) {
                            model.getFaces().add(new OBJ.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[0]),
                                        Integer.parseInt(split[2].split("/")[0]),
                                        Integer.parseInt(split[3].split("/")[0])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[1]),
                                        Integer.parseInt(split[2].split("/")[1]),
                                        Integer.parseInt(split[3].split("/")[1])
                                    }, null
                            ));
                        } else {
                            model.getFaces().add(new OBJ.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[0]),
                                        Integer.parseInt(split[2].split("/")[0]),
                                        Integer.parseInt(split[3].split("/")[0])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[1]),
                                        Integer.parseInt(split[2].split("/")[1]),
                                        Integer.parseInt(split[3].split("/")[1])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split("/")[2]),
                                        Integer.parseInt(split[2].split("/")[2]),
                                        Integer.parseInt(split[3].split("/")[2])
                                    }
                            ));
                        }
                        break;
                    case "s":
                        model.setSmoothShadingEnabled(!ln.contains("off"));
                        break;
                    default:
//                        System.err.println("[OBJ] Unknown Line: " + ln);
                }
            }
        }
        sc.close();
        return model;
    }
}
