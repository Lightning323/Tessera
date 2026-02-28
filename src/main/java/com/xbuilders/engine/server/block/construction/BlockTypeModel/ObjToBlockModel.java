/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.block.construction.BlockTypeModel;

import com.xbuilders.engine.common.utils.LoggingUtils;
import com.xbuilders.window.utils.obj.OBJ;
import com.xbuilders.window.utils.obj.OBJ.Face;
import com.xbuilders.window.utils.obj.OBJLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.xbuilders.Main.LOGGER;

/**
 * @author zipCoder933
 */
public class ObjToBlockModel {


    private static double round(double input) {
//        if (Math.abs(input - 1.0) < 0.001f) {
//            input = 1.0f;
//        } else if (Math.abs(input - 0.0) < 0.001f) {
//            input = 0.0f;
//        }
        input = (float) Math.round(input * 100) / 100;
        return input;
    }

    final static OBJLoader loader = new OBJLoader();

    public static abstract class VertexOperations {

        public abstract void applyOperations(Vector3f vertex);

        /**
         *
         * @param vertex
         * @param rotationIncrements the number of 90-degree increments to rotationIncrements
         */
        public final void rotateVerticiesY90DegreeIncrements(Vector3f vertex, int rotationIncrements) {
//        To rotationIncrements vertices on the Y-axis in 90-degree increments:
            float tmpx, tmpz;

            switch (rotationIncrements) {
                case 1 -> {
                    tmpx = 1 - vertex.z;
                    tmpz = vertex.x;
                    vertex.x = tmpx;
                    vertex.z = tmpz;
                }
                case 2 -> {
                    tmpx = 1 - vertex.x;
                    tmpz = 1 - vertex.z;
                    vertex.x = tmpx;
                    vertex.z = tmpz;
                }
                case 3 -> {
                    tmpx = vertex.z;
                    tmpz = 1 - vertex.x;
                    vertex.x = tmpx;
                    vertex.z = tmpz;
                }
            }

        }
    }



    public static void parseDirectory(
            VertexOperations vertOperations,
            boolean flipOrderOfIndicies,
            float modelScale,
            File folder) {
        System.out.println("PARSING FOLDER \"" + folder.getName() + "\" TO BLOCK RENDERER...");
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".obj")) {
                parseFile(vertOperations, flipOrderOfIndicies, modelScale, file);
            }
        }
    }

    public static void parseFileWithYRotations(boolean flipOrderOfIndicies, float modelScale, File inputFile) {
        parseFile(null, flipOrderOfIndicies, modelScale, inputFile, inputFile.getName().replace(".obj", "0.blockType"));
        parseFile(new VertexOperations() {
            @Override
            public void applyOperations(Vector3f vertex) {
                rotateVerticiesY90DegreeIncrements(vertex, 1);
            }
        }, flipOrderOfIndicies, modelScale, inputFile, inputFile.getName().replace(".obj", "1.blockType"));
        parseFile(new VertexOperations() {
            @Override
            public void applyOperations(Vector3f vertex) {
                rotateVerticiesY90DegreeIncrements(vertex, 2);
            }
        }, flipOrderOfIndicies, modelScale, inputFile, inputFile.getName().replace(".obj", "2.blockType"));
        parseFile(new VertexOperations() {
            @Override
            public void applyOperations(Vector3f vertex) {
                rotateVerticiesY90DegreeIncrements(vertex, 3);
            }
        }, flipOrderOfIndicies, modelScale, inputFile, inputFile.getName().replace(".obj", "3.blockType"));
    }


    public static void parseFile(
            VertexOperations vertOperations,
            boolean flipOrderOfIndicies,
            float modelScale,
            File inputFile) {
        parseFile(vertOperations, flipOrderOfIndicies, modelScale, inputFile,
                inputFile.getName().replace(".obj", ".blockType"));
    }

    public static void parseFile(
            VertexOperations vertOperations,
            boolean flipOrderOfIndicies,
            float modelScale,
            File inputFile, String outputFilename) {
        try {
            System.out.println("PARSING FILE \"" + inputFile.getName() + "\" TO BLOCK RENDERER...");
            StringBuffer str = new StringBuffer();
            System.out.println("\tProcessing " + inputFile.getName() + "...");
            OBJ model = loader.loadModel(inputFile);
            for (int v = 0; v < model.getVertexCoordinates().size(); v++) {
                Vector3f vertex = model.getVertexCoordinates().get(v);
                vertex.x = (float) round((double) vertex.x / modelScale);
                vertex.y = (float) round((double) vertex.y / modelScale);
                vertex.z = (float) round((double) vertex.z / modelScale);
                if (vertOperations != null) {
                    vertOperations.applyOperations(vertex);
                }
                model.getVertexCoordinates().set(v, vertex);
            }
            HashMap<Integer, String> faces = new HashMap<>();
            HashMap<Integer, Integer> vertCounts = new HashMap<>();

            for (Face face : model.getFaces()) {
                FaceResults fResults = makeFace(model, face, flipOrderOfIndicies);
                if (faces.containsKey(fResults.dim)) {
                    //We append to the string
                    faces.put(fResults.dim, faces.get(fResults.dim) + "\n" + fResults.string);
                    vertCounts.put(fResults.dim, vertCounts.get(fResults.dim) + fResults.vertexCount);
                } else {
                    vertCounts.put(fResults.dim, fResults.vertexCount);
                    faces.put(fResults.dim, fResults.string);
                }
            }

            for (Map.Entry<Integer, String> set : faces.entrySet()) {
                int dim = set.getKey();
                if (dim == 0) {
                    str.append("\n\nFACE\tCENTER\t(texture)NEG-Y\t(vert-count)" + vertCounts.get(dim) + "\n")
                            .append(set.getValue());
                } else {
                    str.append("\n\nFACE\t" + dimToStr(dim) + "\t(texture)" + dimToStr(dim) + "\t(vert-count)" + vertCounts.get(dim) + "\n"
                            + set.getValue());
                }
            }

            //Write to file
            File outputFile = new File(inputFile.getParentFile(), outputFilename);
            outputFile.getParentFile().mkdirs();
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            Files.writeString(outputFile.toPath(), str);
            System.out.println("\tSaved to: " + outputFile.toString());
        } catch (Exception ex) {
            LOGGER.info("Error", ex);
        }
    }

    private static String dimToStr(int dim) {
        switch (dim) {
            case 1 -> {
                return "POS_X";
            }
            case 2 -> {
                return "POS_Y";
            }
            case 3 -> {
                return "POS_Z";
            }
            case -1 -> {
                return "NEG_X";
            }
            case -2 -> {
                return "NEG_Y";
            }
            case -3 -> {
                return "NEG_Z";
            }
        }
        ;
        return null;
    }

    private static boolean checkIfIsSideFace(int predictedDim, OBJ model, Face face) {
        for (int i = 0; i < face.getVertexCoordinates().length; i++) {
            Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);

            switch (predictedDim) {
                case 1 -> {
                    if (vertex.x != 1.0) {
                        return false;
                    }
                }
                case 2 -> {
                    if (vertex.y != 0.0) {
                        return false;
                    }
                }
                case 3 -> {
                    if (vertex.z != 1.0) {
                        return false;
                    }
                }
                case -1 -> {
                    if (vertex.x != 0.0) {
                        return false;
                    }
                }
                case -2 -> {
                    if (vertex.y != 1.0) {
                        return false;
                    }
                }
                case -3 -> {
                    if (vertex.z != 0.0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static class FaceResults {

        public int dim;
        public String string;
        public int vertexCount;

        public FaceResults(String string, int dim, int vertexCount) {
            this.string = string;
            this.dim = dim;
            this.vertexCount = vertexCount;
        }

    }

    private static FaceResults makeFace(OBJ model, Face face, boolean flipOrderOfIndicies) {
        int dim = 0;
        if (checkIfIsSideFace(1, model, face)) {
            dim = 1;
        } else if (checkIfIsSideFace(2, model, face)) {
            dim = 2;
        } else if (checkIfIsSideFace(3, model, face)) {
            dim = 3;
        } else if (checkIfIsSideFace(-1, model, face)) {
            dim = -1;
        } else if (checkIfIsSideFace(-2, model, face)) {
            dim = -2;
        } else if (checkIfIsSideFace(-3, model, face)) {
            dim = -3;
        }
        int vertexCount = 0;
        StringBuilder sb = new StringBuilder();
        if (flipOrderOfIndicies) {
            for (int i = face.getVertexCoordinates().length - 1; i >= 0; i--) {
                Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);
                Vector3f normal = model.getVertexCoordinates().get(face.getNormals()[i] - 1);
                 
                 
                Vector2f uv = model.getTextureCoordinates().get(face.getTextureCoords()[i] - 1);
                vertex(vertex, uv, dim, sb);
                vertexCount++;
            }
        } else {
            for (int i = 0; i < face.getVertexCoordinates().length; i++) {
                Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);
                Vector2f uv = model.getTextureCoordinates().get(face.getTextureCoords()[i] - 1);
                vertex(vertex, uv, dim, sb);
                vertexCount++;
            }
        }
        return new FaceResults(sb.toString(), dim, vertexCount);
    }

    private static void vertex(Vector3f vertex, Vector2f uv, int normal, StringBuilder sb) {
        sb.append("VERT\t(vert)")
                .append(vertex.x).append("\t")
                .append(vertex.y).append("\t")
                .append(vertex.z).append("\t"
                        + "(uv)").append(uv.x).append("\t").append(uv.y)
                .append("\t(normal)" + normal + "\n");
    }

}
