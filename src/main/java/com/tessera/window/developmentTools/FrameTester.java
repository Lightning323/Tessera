package com.tessera.window.developmentTools;

import com.tessera.window.utils.preformance.Stopwatch;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FrameTester extends JFrame {
    private JEditorPane editorPane;
    private final Stopwatch processWatch = new Stopwatch();
    private final Stopwatch frameWatch = new Stopwatch();
    long lastUpdate = 0;
    boolean enabled = true;

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        setVisible(enabled);
    }

//    GraphPanel percentPanel = new GraphPanel();
    private int updateTimeMS = 500;

    public void setUpdateTimeMS(int updateTimeMS) {
        this.updateTimeMS = updateTimeMS;
    }

    private boolean started = false;
    private final Map<String, TimeList> processList = new LinkedHashMap<>();
    private final Map<String, Long> counterList = new LinkedHashMap<>();
    boolean frameStarted = false;

    //For the current period, a period ends when the updateStatus() method is called
    private long timeOfAllProcesses = 0;
    private int periodFrameCount = 0;


    public void setStarted(boolean started) {
        this.started = started;
        if (started) {
            startStopButton.setText("Stop");
        } else {
            startStopButton.setText("Start");
        }
    }

    static class TimeList {

        public long totalTime = 0;
    }

    long totalPeriodTime = 0;

    public void __startFrame() {
        if (started && enabled) {
            frameWatch.start();
            processWatch.start();
        }
    }

    public void __endFrame() {
        if (started && enabled) {
            frameWatch.calculateElapsedTime();
//            if(periodFrameCount % 10 == 0)System.out.println("Frame time: " + frameWatch.getElapsedMilliseconds());
            totalPeriodTime += frameWatch.getElapsedNanoseconds();
            periodFrameCount++;

            if (System.currentTimeMillis() - lastUpdate > updateTimeMS) {
                updateStatus(totalPeriodTime);
                lastUpdate = System.currentTimeMillis();
                timeOfAllProcesses = 0;
                periodFrameCount = 0;
                totalPeriodTime = 0;
            }
        } else if (frameStarted) {
            //Finish up if it was stopped
            processList.clear();
            counterList.clear();
        }
        frameStarted = started;
    }

    public void startProcess() {
        if (frameStarted) {
            processWatch.start();
        }
    }

    public void count(String name, int count) {
        if (frameStarted) {
            if (!counterList.containsKey(name)) {
                counterList.put(name, 0L);
            }
            counterList.put(name, counterList.get(name) + count);
        }
    }

    public void set(String name, int value) {
        if (frameStarted) {
            if (!counterList.containsKey(name)) {
                counterList.put(name, 0L);
            }
            counterList.put(name, (long) value);
        }
    }
    /**
     * @param name
     * @return elapsed milliseconds
     */
    public long endProcess(String name) {
        if (frameStarted) {
            if (!processList.containsKey(name)) {
                processList.put(name, new TimeList());
            }
            processWatch.calculateElapsedTime();
            processList.get(name).totalTime += processWatch.getElapsedNanoseconds();
            timeOfAllProcesses += processWatch.getElapsedNanoseconds();
            processWatch.start();
            return processWatch.getElapsedMilliseconds();
        }
        return 0;
    }

    public static String formatTime(long nanoseconds) {
        if (nanoseconds < 1_000) {
            return nanoseconds + " ns"; // nanoseconds
        } else if (nanoseconds < 1_000_000) {
            return String.format("%.0f μs", nanoseconds / 1_000.0); // microseconds with 3 decimal places
        } else if (nanoseconds < 1_000_000_000) {
            return String.format("%.0f ms", nanoseconds / 1_000_000.0); // milliseconds with 3 decimal places
        } else {
            return String.format("%.0f s", nanoseconds / 1_000_000_000.0); // seconds with 3 decimal places
        }
    }

    private final String startHtml = "<html><style>" +
            "table, th, td {border: 1px solid black;}" +
            ".bar{width: 80px; height: 10px;background-color: #ddd; }" +
            ".bar div{background-color: blue; height: 10px;}</style><body><p>This period:</p>";


    private void updateStatus(long totalPeriodTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(startHtml);
        sb.append("Period time: ").append(formatTime(totalPeriodTime)).append("<br>");
        sb.append("Process time: ").append(formatTime(timeOfAllProcesses)).append("<br>");
        sb.append("Frames: ").append(periodFrameCount).append("<br>");

        if (periodFrameCount > 0 && totalPeriodTime > 0) {
            long timeGap = totalPeriodTime - timeOfAllProcesses;
            double gapPercent = ((double) timeGap / totalPeriodTime) * 100;

            sb.append("<br>Un-measured time: ").append(Math.round(gapPercent)).append("%");
            sb.append("<br><b>Period time/frame: ").append(formatTime(totalPeriodTime / periodFrameCount));
            sb.append("<br>Process time/frame: ").append(formatTime(timeOfAllProcesses / periodFrameCount))
                    .append("</b>");
        }


        sb.append("<table><tr>" +
                "<th>Name</th>" +
                "<th>time/frame</th>" +
                "<th>total time</th>" +
                "<th>Total %</th></tr>");

        for (HashMap.Entry<String, TimeList> entry : processList.entrySet()) {
            String name = entry.getKey();
            String color = "blue";
            if (name.startsWith("red ")) {
                color = "red";
                name = name.replaceFirst("red", "");
            } else if (name.startsWith("green ")) {
                color = "green";
                name = name.replaceFirst("green", "");
            } else if (name.startsWith("black ")) {
                color = "black";
                name = name.replaceFirst("black", "");
            }


            long totalTime = entry.getValue().totalTime; //The total time this period
            long averageTime = entry.getValue().totalTime / periodFrameCount; //The average time per frame
            double usagePercent = (double) totalTime / timeOfAllProcesses; //The percentage of the total time used
            entry.getValue().totalTime = 0;

//            if(averageTime > LogThreshold)

            sb.append("<tr><td>")
                    .append(name).append("</td><td>")
                    .append(formatTime(averageTime)).append("/frame</td><td>")
                    .append(formatTime(totalTime)).append("</td><td>");

            //Usage bar
            sb.append("<div class=\"bar\">" +
                            "<div style=\"width: ")
                    .append(usagePercent * 100).append("%;")
                    .append("background-color: " + color + ";\"></div>" +
                            "</div>");

            sb.append("</td></tr>");
        }
        sb.append("</table>");

        //Tally the count
        if (counterList.size() > 0) {
            sb.append("<p>Counters/Values:</p><table><tr><th>Name</th><th>Value</th></tr>");
            for (HashMap.Entry<String, Long> entry : counterList.entrySet()) {
                String name = entry.getKey();
                long count = entry.getValue();
                sb.append("<tr><td>").append(name).append("</td><td>").append(count).append("</td></tr>");
            }
            sb.append("</table>");
        }

        sb.append("</body></html>");



        editorPane.setText(sb.toString());
    }


    JButton startStopButton;

    public FrameTester(String title) {
        // Create the JEditorPane
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        startStopButton = new JButton("Start");
        startStopButton.addActionListener(e -> {
            started = !started;
            setStarted(started);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startStopButton);
        add(buttonPanel, BorderLayout.NORTH);

        setTitle(title + " (Frame Tester)");

        // Initialize with some HTML content
        editorPane.setText("<html><body></body></html>");

        // Add the editor pane to a scroll pane (optional, but provides scrolling)
        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);

//        add(percentPanel, BorderLayout.SOUTH);

        // Set up the JFrame
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

//    public void updateMemoryPanel(){
//        double kb = MemoryProfiler.getMemoryUsed() / 1024; //In KB
//        double percent = MemoryProfiler.getMemoryUsagePercent();
//
//        percentPanel.addDataPoint(percent, 100);
////        usedPanel.addDataPoint(kb, 100);
////        usedPanel.update();
//        percentPanel.update();
//    }

    public static void main(String[] args) {
        FrameTester tester = new FrameTester("Test");

        while (true) {
            try {
                tester.__startFrame();
                Thread.sleep(1);
                tester.endProcess("1ms a");
                Thread.sleep(1);
                tester.endProcess("1ms b");
                Thread.sleep(20);
                tester.endProcess("2000ms");
                Thread.sleep(1);
                tester.endProcess("1ms c");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
