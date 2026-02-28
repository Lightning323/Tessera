package com.tessera.engine.server.block.conversion;

public class Xb2ToXb3ConversionUtils {
//
//    public static void flipArray(String arrayName, Vector3f[] array) {
//        Vector3f[] temp = new Vector3f[array.length];
//        //Flip the order of every 3 elemtents, but keep the ordering of the groups of 3 the same
//        for (int i = 0; i < array.length; i += 3) {
//            temp[i] = array[i + 2];
//            temp[i + 1] = array[i + 1];
//            temp[i + 2] = array[i];
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("Vector3f[] " + arrayName + " = new Vector3f[]{\n");
//        for (int i = 0; i < temp.length; i++) {
//            sb.append("\tnew Vector3f(" + temp[i].x + "f," + temp[i].y + "f," + temp[i].z + "f), //" + i + "\n");
//        }
//        sb.append("};\n\n");
//
//        File file = ResourceUtils.localResource("flippedArrays.java");
//        try (FileWriter writer = new FileWriter(file, true)) {
//            writer.append(sb.toString());
//            writer.append(System.lineSeparator()); // To append a new line
//            System.out.println("Appended to file: " + file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void flipArray(String arrayName, Vector2f[] array) {
//        Vector2f[] temp = new Vector2f[array.length];
//        for (int i = 0; i < array.length; i++) {
//            temp[array.length - 1 - i] = array[i];
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("Vector2f[] " + arrayName + " = new Vector2f[]{\n");
//        for (int i = 0; i < temp.length; i++) {
//            sb.append("\tnew Vector2f(" + temp[i].x + "f," + temp[i].y + "f), //" + i + "\n");
//        }
//        sb.append("};\n\n");
//
//        File file = ResourceUtils.localResource("flippedArrays.java");
//        try (FileWriter writer = new FileWriter(file, true)) {
//            writer.append(sb.toString());
//            writer.append(System.lineSeparator()); // To append a new line
//            System.out.println("Appended to file: " + file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
