package com.attendancetracker;

//import com.attendancetracker.db.DBConnection;
import com.attendancetracker.db.DatabaseManager;
import com.attendancetracker.ui.MainFrame;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        new File("data").mkdirs();
        new File("reports").mkdirs();

        DatabaseManager.initialize();

        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) { }
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}