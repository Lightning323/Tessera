/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.utils;

import com.xbuilders.utils.resource.PathHandler;

import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zipCoder933
 */
public class LoggingUtils {

//    public static class SevereErrorHandler extends Handler {
//
//        ClientWindow window;
//
//        public SevereErrorHandler(ClientWindow window) {
//            this.window = window;
//        }
//
//        @Override
//        public void publish(LogRecord record) {
//            if (record.getLevel().equals(Level.SEVERE)) {
//                window.minimizeWindow();
//                createPopupWindow("Severe Error", record.getMessage());
//                System.out.println(record.getMessage());
//            } else if (record.getLevel().equals(Level.WARNING)) {
//                if (record.getThrown() != null) {
//                    String stack = Arrays.toString(record.getThrown().getStackTrace())
//                            .replace(",", "\n");
//                    window.popupMessage.message(
//                            "Error",
//                            record.getMessage() + "\n" + stack);
//                } else {
//                    window.popupMessage.message(
//                            "Error",
//                            record.getMessage());
//                }
//            }
//        }
//
//        @Override
//        public void flush() {
//            // Not needed in this example
//        }
//
//        @Override
//        public void close() throws SecurityException {
//            // Not needed in this example
//        }
//    }

//    public static void createPopupWindow(String title, String str) {
//        final JFrame parent = new JFrame();
//        JLabel label = new JLabel("");
//        label.setText("<html><body style='padding:5px;'>" + str.replace("\n", "<br>") + "</body></html>");
////        label.setFont(new Font("Arial", 0, 12));
//        label.setVerticalAlignment(JLabel.TOP);
//        parent.add(label);
//        parent.pack();
//        parent.getContentPane().setBackground(Color.white);
//        parent.setVisible(true);
//        parent.pack();
//
//        parent.setIconImage(popupWindowIcon.getImage());
//        parent.setTitle(title);
//        parent.setLocationRelativeTo(null);
//        parent.setAlwaysOnTop(true);
//        parent.setVisible(true);
//        parent.setSize(380, 240);
//    }

    private final static ImageIcon popupWindowIcon = new ImageIcon(PathHandler.resourcePath("logo.png").getAbsolutePath());
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");

    public static String getFileDate() {
        return dateFormat.format(new Date()).replace(":", "_");
    }

//    public static void report(Throwable ex) {
//        report("error", ex);
//    }
//
//    public static void report(String title, String details) {
//        if (title == null || title.isBlank()) title = "Error";
//        ClientWindow.popupMessage.message(title, details);
//    }

//    public static void report(String userMsg, Throwable ex) {
//        String errMessage = (ex.getMessage() != null ? " \n(" + ex.getMessage() + ")" : "");
//        if (userMsg == null || userMsg.isBlank()) userMsg = "Runtime Error!";
//
//        ClientWindow.popupMessage.message(userMsg, errMessage + "\n(Content saved to clipboard)");
//        log(ex, userMsg);
//    }
//
//
//    /**
//     * Prints the stack trace and saves the error to log file.
//     *
//     * @param ex         the throwable
//     * @param devMessage
//     */
//    public static void log(Throwable ex, String devMessage) {
//        String errorStr;
//        if (ex != null) {
//            if (ex.getMessage() == null) {
//                errorStr = "Message: \t" + devMessage + "\n"
//                        + "Class: \t" + ex.getClass() + "\n\n"
//                        + "Stack trace:\n" + Arrays.toString(ex.getStackTrace()).replace(",", "\n");
//            } else {
//                errorStr = "Message: \t" + devMessage + "\n"
//                        + "Error: \t" + ex.getMessage() + "\n"
//                        + "Class: \t" + ex.getClass() + "\n\n"
//                        + "Stack trace:\n" + Arrays.toString(ex.getStackTrace()).replace(",", "\n");
//            }
//            System.out.println(errorStr);
//            try {
//                //Create log file directory if it doesn't exist
//                saveLogToFile(devMessage, errorStr);
//                //Copy to clipboard
//                MiscUtils.setClipboard(errorStr);
//            } catch (IOException ex1) {
//            }
//        }
//    }
//
//    private static File saveLogToFile(String devMessage, String errorStr) throws IOException {
//        String date = dateFormat.format(new Date()).replace(":", "_");
//        File logFile = ResourceUtils.localFile("error logs\\" + date + ".txt");
//        if (!logFile.getParentFile().exists()) logFile.getParentFile().mkdirs();
//
//        if (devMessage != null) errorStr = "Message: \t" + devMessage + "\n" + errorStr;
//        Files.writeString(logFile.toPath(), errorStr);
//        return logFile;
//    }
//
//    public static void log(Throwable throwable) {
//        log(throwable, "unnamed exception");
//    }
}
