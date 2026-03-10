package com.musiccatalog;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.User;
import com.musiccatalog.ui.CatalogWindow;
import com.musiccatalog.ui.LoginDialog;

import javax.swing.*;

/**
 * Application entry point.
 */
public class Main {

    public static void main(String[] args) {
        // Use system look and feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default
        }

        // Initialize database (creates tables and seeds data on first run)
        DatabaseManager.getInstance().initializeDatabase();

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            User user = loginDialog.getAuthenticatedUser();
            if (user != null) {
                CatalogWindow window = new CatalogWindow(user);
                window.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
