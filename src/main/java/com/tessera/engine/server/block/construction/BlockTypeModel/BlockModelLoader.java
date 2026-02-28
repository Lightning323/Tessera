package com.tessera.engine.server.block.construction.BlockTypeModel;

import java.io.*;

public class BlockModelLoader {

    public static BlockModel load(File inputFile, BlockModel.ShouldRenderSide shouldRenderSide) throws IOException {
        return load(new FileReader(inputFile), shouldRenderSide);
    }

    public static BlockModel load(InputStream inputFile, BlockModel.ShouldRenderSide shouldRenderSide) throws IOException {
        return load(new InputStreamReader(inputFile), shouldRenderSide);
    }

    public static BlockModel load(Reader inputFile, BlockModel.ShouldRenderSide shouldRenderSide) throws IOException {
        BlockModel model = new BlockModel(shouldRenderSide);
        ModelSide activeSide = null;
        int vertexIndex = 0;
        int activeSideIndex = 0;

        try (BufferedReader br = new BufferedReader(inputFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String splitLine[] = line.replaceAll("\\(.*?\\)", "").split("\\t");
//                System.out.println(Arrays.toString(splitLine));

                if (splitLine[0].equals("FACE")) {
                    activeSideIndex = sideStringToIndex(splitLine[1]); //side of the face
                    activeSide = new ModelSide(Integer.parseInt(splitLine[3]));//vertex count
                    activeSide.textureSide = sideStringToIndex(splitLine[2]);//side of the texture
                    vertexIndex = 0;
                    model.sides[activeSideIndex] = activeSide;
                } else if (splitLine[0].equals("VERT")) {
                    ModelVertex vert = new ModelVertex();
                    vert.position.x = Float.parseFloat(splitLine[1]);
                    vert.position.y = Float.parseFloat(splitLine[2]);
                    vert.position.z = Float.parseFloat(splitLine[3]);
                    vert.u = Float.parseFloat(splitLine[4]);
                    vert.v = Float.parseFloat(splitLine[5]);
                    vert.normal = (byte) Integer.parseInt(splitLine[6]);
                    activeSide.vertices[vertexIndex] = vert;
                    vertexIndex++;
                }
            }
        }
        return model;
    }

    private static int sideStringToIndex(String str) {
        int sideIndex;
        switch (str) {
            case "POS_X":
                sideIndex = BlockModel.SIDE_POS_X;
                break;
            case "NEG_X":
                sideIndex = BlockModel.SIDE_NEG_X;
                break;
            case "POS_Z":
                sideIndex = BlockModel.SIDE_POS_Z;
                break;
            case "NEG_Z":
                sideIndex = BlockModel.SIDE_NEG_Z;
                break;
            case "POS_Y":
                sideIndex = BlockModel.SIDE_POS_Y;
                break;
            case "NEG_Y":
                sideIndex = BlockModel.SIDE_NEG_Y;
                break;
            default:
                sideIndex = BlockModel.SIDE_CENTER;
                break;
        }
        return sideIndex;
    }


}
