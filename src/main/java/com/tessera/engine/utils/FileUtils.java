package com.tessera.engine.utils;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;



public class FileUtils {
    public final static boolean canRecycleFiles = Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH);
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile(".*\\.[\\w]+$");

    public static boolean hasFileExtension(String resourcePath) {
        return FILE_EXTENSION_PATTERN.matcher(resourcePath).matches();
    }

    /**
     * Removes the base path from a full path, returning a relative path.
     * This method is platform independent and works with any file type.
     *
     * @param basePath The base path to remove.
     * @param fullPath The full file path.
     * @return The relative path with the base removed.
     * @throws IllegalArgumentException if the base path is not a prefix of the full path.
     */
    public static String removeBasePath(String basePath, String fullPath) {
        // Convert both strings to Path objects
        Path base = Paths.get(basePath).normalize();
        Path full = Paths.get(fullPath).normalize();

        if (!full.startsWith(base)) {
            throw new IllegalArgumentException("The full path does not start with the base path.");
        }

        // Use relativize to compute the relative path
        Path relative = base.relativize(full);
        return relative.toString();
    }

    public static ByteBuffer fileToByteBuffer(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip(); // Prepare for reading
            return buffer;
        }
    }

    public static void moveDirectoryToTrash(File directory) throws IOException {
        if (!directory.exists()) return;

        // 1. Try Desktop API
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            try {
                if (Desktop.getDesktop().moveToTrash(directory)) return;
            } catch (Exception ignored) {}
        }

        // 2. Try OS-Specific CLI
        if (tryOsTrash(directory)) return;

        // 3. FINAL FALLBACK: Permanent Delete
        System.out.println("Trash failed. Deleting permanently: " + directory.getAbsolutePath());
        deleteRecursively(directory.toPath());
    }

    private static boolean tryOsTrash(File directory) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;

        if (os.contains("win")) {
            pb = new ProcessBuilder("powershell.exe", "-Command",
                    "Add-Type -AssemblyName Microsoft.VisualBasic; " +
                            "[Microsoft.VisualBasic.FileIO.FileSystem]::DeleteDirectory('" +
                            directory.getAbsolutePath() + "', 'OnlyErrorDialogs', 'SendToRecycleBin')");
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("osascript", "-e",
                    "tell app \"Finder\" to move POSIX file \"" + directory.getAbsolutePath() + "\" to trash");
        } else {
            pb = new ProcessBuilder("gio", "trash", directory.getAbsolutePath());
        }

        try {
            return pb.start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static boolean fileIsInUse(File file) {
        boolean used;
        Channel channel = null;
        try {
            channel = new RandomAccessFile(file, "rw").getChannel();
            used = false;
        } catch (FileNotFoundException ex) {
            used = true;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    // exception handling
                }
            }
        }
        return used;
    }

}
