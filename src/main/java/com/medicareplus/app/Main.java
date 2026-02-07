package com.medicareplus.app;

import com.medicareplus.db.DBInitializer;
import com.medicareplus.ui.DashboardFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create required tables
            DBInitializer.createTables();

            // Launch dashboard
            new DashboardFrame().setVisible(true);
        });
    }
}
