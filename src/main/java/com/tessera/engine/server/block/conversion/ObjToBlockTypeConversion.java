///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.xbuilders.engine.game.model.items.block.conversion;
//
//import com.xbuilders.engine.utils.ErrorHandler;
//import com.xbuilders.engine.utils.resource.ResourceUtils;
//import com.xbuilders.window.utils.obj.OBJ;
//import com.xbuilders.window.utils.obj.OBJ.Face;
//import com.xbuilders.window.utils.obj.OBJLoader;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.joml.Vector2f;
//import org.joml.Vector3f;
//
///**
// * @author zipCoder933
// */
////TODO: Load obj files directly into the render class instead of converting them into raw code
//public class ObjToBlockTypeConversion {
//
//    private static final float ONE_32th = (float) 1 / 32;
//
//    private static float round(double input) {
//        float i = (float) input;
//        if (Math.abs(input - 1.0) < ONE_32th) {
//            i = 1.0f;
//        } else if (Math.abs(input - 0.0) < ONE_32th) {
//            i = 0.0f;
//        }
//        return (float) Math.round(i * 25) / 25;
//    }
//
//    final static OBJLoader loader = new OBJLoader();
//
//    public static abstract class VertexOperations {
//
//        public abstract Vector3f applyOperations(Vector3f vertex);
//
//        public final void rotateVerticiesY(Vector3f vertex, int rotate) {
////        To rotate vertices on the Y-axis in 90-degree increments:
//            float tmpx, tmpz;
//
//            switch (rotate) {
//                case 1 -> {
//                    tmpx = 1 - vertex.z;
//                    tmpz = vertex.x;
//                    vertex.x = tmpx;
//                    vertex.z = tmpz;
//                }
//                case 2 -> {
//                    tmpx = 1 - vertex.x;
//                    tmpz = 1 - vertex.z;
//                    vertex.x = tmpx;
//                    vertex.z = tmpz;
//                }
//                case 3 -> {
//                    tmpx = vertex.z;
//                    tmpz = 1 - vertex.x;
//                    vertex.x = tmpx;
//                    vertex.z = tmpz;
//                }
//            }
//
//        }
//    }
//
//    public static void parseFolderToRendererClass(
//            VertexOperations vertOperations,
//            boolean flipOrderOfIndicies,
//            File folder) {
//
//        String filename = folder.getName().replace(".obj", "") + ".java";
//        System.out.println("PARSING FOLDER \"" + folder.getName() + "\" TO BLOCK RENDERER...");
//        parseOBJToRendererClass(new File(folder, filename),
//                vertOperations, flipOrderOfIndicies, folder.listFiles());
//    }
//
//    public static void parseOBJToRendererClass(
//            File outputFile,
//            VertexOperations vertOperations,
//            boolean flipOrderOfIndicies,
//            File... inputFiles) {
//        try {
//            System.out.println("PARSING FILE \"" + outputFile.getName() + "\" TO BLOCK RENDERER...");
//            outputFile.getParentFile().mkdirs();
//            if (!outputFile.exists()) {
//                outputFile.createNewFile();
//            }
//
//            StringBuffer str = new StringBuffer();
//
//            for (File inputFile : inputFiles) {
//                if (!inputFile.getName().endsWith(".obj")) {
//                    continue;
//                }
//                System.out.println("\tProcessing " + inputFile.getName() + "...");
//                OBJ model = loader.loadModel(inputFile);
//                for (int v = 0; v < model.getVertexCoordinates().size(); v++) {
//                    Vector3f vertex = model.getVertexCoordinates().get(v);
//                    vertex.x = round(vertex.x);
//                    vertex.y = round(vertex.y);
//                    vertex.z = round(vertex.z);
//                    if (vertOperations != null) {
//                        vertex = vertOperations.applyOperations(vertex);
//                    }
//                    model.getVertexCoordinates().set(v, vertex);
//                }
//                String namePrefix = inputFile.getName().replace(".obj", "").replace(" ", "_");
//                str.append("\n//<editor-fold defaultstate=\"collapsed\" desc=\"make " + inputFile.getName() + "\">\n"
//                        + "private void make_" + namePrefix + "(BufferSet buffer,\n"
//                        + "            Block block, BlockData data, Block[] neighbors,\n"
//                        + "            int x, int y, int z) {");
//                str.append("\nBlockTexture.FaceTexture texture;\n");
//
//                HashMap<Integer, String> faces = new HashMap<>();
//
//                for (Face face : model.getFaces()) {
//                    FaceResults fResults = makeFace(model, face, flipOrderOfIndicies);
//                    if (faces.containsKey(fResults.dim)) {
//                        faces.put(fResults.dim, faces.get(fResults.dim) + "\n" + fResults.string);
//                    } else {
//                        faces.put(fResults.dim, fResults.string);
//                    }
//                }
//
//                for (Map.Entry<Integer, String> set : faces.entrySet()) {
//                    int dim = set.getKey();
//                    if (dim == 0) {
//                        str.append("\n\n"
//                                        + "\n\ttexture = block.texture.getPOS_Y();\n")
//                                .append(set.getValue());
//                    } else {
//                        str.append("\n\n"
//                                        + "if(sideIsVisible(block, neighbors[" + dimToStr(dim) + "])){"
//                                        + "\n\ttexture = block.texture.get" + dimToStr(dim) + "();"
//                                        + "\n\t//" + dimToStr(dim) + " FACE:\n"
//                                        + set.getValue())
//                                .append("}");
//                    }
//                }
//
//                str.append("}\n"
//                        + "//</editor-fold>\n");
//            }
//
//            str.append("""
//                    \n\n
//                    private static boolean sideIsVisible(Block thisBlock, Block side){
//                        return side.opaque != thisBlock.opaque;
//                    }""");
//
//            Files.writeString(outputFile.toPath(), str);
//            System.out.println("\tSaved to: " + outputFile.toString());
//        } catch (Exception ex) {
//            ErrorHandler.handleFatalError(ex);
//        }
//    }
//
//    private static String dimToStr(int dim) {
//        switch (dim) {
//            case 1 -> {
//                return "POS_X";
//            }
//            case 2 -> {
//                return "POS_Y";
//            }
//            case 3 -> {
//                return "POS_Z";
//            }
//            case -1 -> {
//                return "NEG_X";
//            }
//            case -2 -> {
//                return "NEG_Y";
//            }
//            case -3 -> {
//                return "NEG_Z";
//            }
//        }
//        ;
//        return null;
//    }
//
//    private static boolean checkIfIsSideFace(int predictedDim, OBJ model, Face face) {
//        for (int i = 0; i < face.getVertexCoordinates().length; i++) {
//            Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);
//
//            switch (predictedDim) {
//                case 1 -> {
//                    if (vertex.x != 1.0) {
//                        return false;
//                    }
//                }
//                case 2 -> {
//                    if (vertex.y != 1.0) {
//                        return false;
//                    }
//                }
//                case 3 -> {
//                    if (vertex.z != 1.0) {
//                        return false;
//                    }
//                }
//                case -1 -> {
//                    if (vertex.x != 0.0) {
//                        return false;
//                    }
//                }
//                case -2 -> {
//                    if (vertex.y != 0.0) {
//                        return false;
//                    }
//                }
//                case -3 -> {
//                    if (vertex.z != 0.0) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    static class FaceResults {
//
//        public int dim;
//        public String string;
//
//        public FaceResults(String string, int dim) {
//            this.string = string;
//            this.dim = dim;
//        }
//
//    }
//
//    private static FaceResults makeFace(OBJ model, Face face, boolean flipOrderOfIndicies) {
//        int dim = 0;
//        if (checkIfIsSideFace(1, model, face)) {
//            dim = 1;
//        } else if (checkIfIsSideFace(2, model, face)) {
//            dim = 2;
//        } else if (checkIfIsSideFace(3, model, face)) {
//            dim = 3;
//        } else if (checkIfIsSideFace(-1, model, face)) {
//            dim = -1;
//        } else if (checkIfIsSideFace(-2, model, face)) {
//            dim = -2;
//        } else if (checkIfIsSideFace(-3, model, face)) {
//            dim = -3;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        if (flipOrderOfIndicies) {
//            for (int i = face.getVertexCoordinates().length - 1; i >= 0; i--) {
//                Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);
//                Vector2f uv = model.getTextureCoordinates().get(face.getTextureCoords()[i] - 1);
//                vertex(vertex, uv, dim, sb);
//            }
//        } else {
//            for (int i = 0; i < face.getVertexCoordinates().length; i++) {
//                Vector3f vertex = model.getVertexCoordinates().get(face.getVertexCoordinates()[i] - 1);
//                Vector2f uv = model.getTextureCoordinates().get(face.getTextureCoords()[i] - 1);
//                vertex(vertex, uv, dim, sb);
//            }
//        }
//        return new FaceResults(sb.toString(), dim);
//    }
//
//    private static void vertex(Vector3f vertex, Vector2f uv, int dim, StringBuilder sb) {
//        sb.append("\tbuffer.vertex(")
//                .append((vertex.x == 0 ? "x" : vertex.x + "f + x")).append(",\t")
//                .append((vertex.y == 0 ? "y" : vertex.y + "f + y")).append(",\t")
//                .append((vertex.z == 0 ? "z" : vertex.z + "f + z")).append(",\t"
//                        + " /* uvs */ ").append(uv.x).append("f,").append(uv.y)
//                .append("f, /* normal */ (byte)" + dim + ", texture);\n");
//    }
//
//}
