package com.musiccatalog.ui;

import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login and Registration dialog.
 */
public class LoginDialog extends JDialog {

    private final UserDAO userDAO = new UserDAO();
    private User authenticatedUser = null;

    // Login panel components
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JLabel loginStatusLabel;

    // Register panel components
    private JTextField regUsernameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;
    private JLabel regStatusLabel;

    public LoginDialog(Frame parent) {
        super(parent, "Music Catalog — Login", true);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel header = new JLabel("🎵  Music Catalog", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(new Color(50, 120, 200));
        header.setBorder(new EmptyBorder(10, 0, 15, 0));
        root.add(header, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        loginUsernameField = new JTextField(20);
        panel.add(loginUsernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        loginPasswordField = new JPasswordField(20);
        panel.add(loginPasswordField, gbc);

        // Status label
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setForeground(Color.RED);
        loginStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(loginStatusLabel, gbc);

        // Login button
        gbc.gridy = 3;
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(50, 120, 200));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginBtn.setPreferredSize(new Dimension(200, 36));
        loginBtn.addActionListener(e -> doLogin());
        panel.add(loginBtn, gbc);

        // Hint
        gbc.gridy = 4;
        JLabel hint = new JLabel("Demo: admin / admin123  or  demo / demo");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(hint, gbc);

        // Enter key triggers login
        loginPasswordField.addActionListener(e -> doLogin());
        loginUsernameField.addActionListener(e -> loginPasswordField.requestFocus());

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        regUsernameField = new JTextField(20);
        panel.add(regUsernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        regEmailField = new JTextField(20);
        panel.add(regEmailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        regPasswordField = new JPasswordField(20);
        panel.add(regPasswordField, gbc);

        // Confirm password
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(new JLabel("Confirm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        regConfirmField = new JPasswordField(20);
        panel.add(regConfirmField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        regStatusLabel = new JLabel(" ");
        regStatusLabel.setForeground(Color.RED);
        regStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(regStatusLabel, gbc);

        // Register button
        gbc.gridy = 5;
        JButton regBtn = new JButton("Create Account");
        regBtn.setBackground(new Color(40, 160, 80));
        regBtn.setForeground(Color.BLACK);
        regBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        regBtn.setPreferredSize(new Dimension(200, 36));
        regBtn.addActionListener(e -> doRegister());
        panel.add(regBtn, gbc);

        return panel;
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            loginStatusLabel.setText("Please enter username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            authenticatedUser = user;
            dispose();
        } else {
            loginStatusLabel.setText("Invalid username or password.");
            loginPasswordField.setText("");
        }
    }

    private void doRegister() {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmField.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            regStatusLabel.setText("All fields are required.");
            return;
        }

        if (!email.contains("@")) {
            regStatusLabel.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 4) {
            regStatusLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            regStatusLabel.setText("Passwords do not match.");
            return;
        }

        boolean success = userDAO.register(username, password, email);
        if (success) {
            regStatusLabel.setForeground(new Color(0, 150, 0));
            regStatusLabel.setText("Account created! You can now log in.");
            regUsernameField.setText("");
            regEmailField.setText("");
            regPasswordField.setText("");
            regConfirmField.setText("");
        } else {
            regStatusLabel.setForeground(Color.RED);
            regStatusLabel.setText("Username already exists. Choose another.");
        }
    }

    /**
     * Returns the authenticated user, or null if login was cancelled.
     */
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
