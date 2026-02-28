package com.xbuilders.utils;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileDialog {
    public static void fileDialog(Consumer<java.awt.FileDialog> setupConsumer, Consumer<File> chosenFile) {
        (new Thread(() -> {
            JFrame frame = new JFrame();
            java.awt.FileDialog fd = new java.awt.FileDialog(frame, "Choose a file", java.awt.FileDialog.LOAD);
            setupConsumer.accept(fd);
            fd.setVisible(true);
            //Set the file dialog to the front
            frame.toFront();
            fd.toFront();
            //Set always on top
            frame.setAlwaysOnTop(true);
            fd.setAlwaysOnTop(true);

            File f = null;
            if (fd.getDirectory() != null && fd.getFile() != null) f = new File(fd.getDirectory(), fd.getFile());
            chosenFile.accept(f);
            frame.dispose();
        })).start();
    }

    private static class FileWrapper {
        public File file;
    }

    public static File fileDialog(Consumer<java.awt.FileDialog> setupConsumer) {
        AtomicBoolean done = new AtomicBoolean(false);
        FileWrapper file = new FileWrapper();

        fileDialog(setupConsumer, f -> {
            done.set(true);
            file.file = f;
        });

        while (!done.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return file.file;
    }
}
