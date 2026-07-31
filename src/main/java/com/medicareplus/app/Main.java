package com.medicareplus.app;

import com.medicareplus.db.DBInitializer;
import com.medicareplus.ui.DashboardFrame;
import com.medicareplus.ui.UITheme;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        UITheme.install();

        // Prepare the local schema before the Swing event thread begins painting.
        DBInitializer.createTables();

        SwingUtilities.invokeLater(
                () -> new DashboardFrame().setVisible(true)
        );
    }
}
