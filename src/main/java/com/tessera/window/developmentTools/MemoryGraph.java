package com.tessera.window.developmentTools;

import javax.swing.*;
import java.awt.*;

public class MemoryGraph extends JPanel {
    GraphPanel percentPanel;

    public MemoryGraph() {
//        usedPanel = new GraphPanel();
        percentPanel = new GraphPanel();
//        usedPanel.setPreferredSize(new Dimension(400, 200));
        JFrame frame = new JFrame("Memory Percent");
//        usedPanel.setVisible(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 4));
        frame.add(panel);

//        //used panel
//        JLabel lab = new JLabel("Memory Usage (KB)");
//        //Set the height of this row
//        lab.setPreferredSize(new Dimension(400, 200));
//        panel.add(lab);
//        panel.add(usedPanel);

        //Add a label for the percent panel
        JLabel percentLabel = new JLabel("Memory Usage Percent");
        panel.add(percentLabel, BorderLayout.NORTH);
        percentPanel.setPreferredSize(new Dimension(500, 200));
        percentPanel.setYBounds(0, 1);
        panel.add(percentPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setAlwaysOnTop(true);
        //Move to the top right corner
        //Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //Get the width and height of this frame
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        frame.setLocation(screenSize.width - frameWidth, 50);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void update() {
        double kb = MemoryProfiler.getMemoryUsed() / 1024; //In KB
        double percent = MemoryProfiler.getMemoryUsagePercent();
        percentPanel.addDataPoint(percent, 2000);
        percentPanel.update();
    }
}
