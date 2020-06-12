package com.loottable.views;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ViewSetup {
    public static void launch(JPanel panel) {
        JFrame mainWindow = new JFrame("Test Frame");
        mainWindow.getContentPane().add(panel);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
}