package com.mycompany.car_rental_project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginSignUpPage extends JFrame {
    // Components
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signUpButton;

    // Constructor
    public LoginSignUpPage() {
        // Frame settings
        setTitle("Login or Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        // Initialize components
        emailLabel = new JLabel("Email:");
        passwordLabel = new JLabel("Password:");
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        signUpButton = new JButton("Sign Up");

        // Add components to the frame
        add(emailLabel);
        add(emailField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(signUpButton);

        // Center the frame on the screen
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        // Add action listeners
        loginButton.addActionListener(new LoginAction());
        signUpButton.addActionListener(new SignUpAction());
    }

    // Action for login button
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            // Check if admin credentials are entered
            if (email.equals("admin@admin.com") && password.equals("12345678")) {
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Welcome Admin!");
                dispose();
                return;
            }

            // Validate credentials
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {

                stmt.setString(1, email);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(LoginSignUpPage.this, "Login successful!");
                        dispose();
                        new vehicles(email).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginSignUpPage.this, "Invalid email or password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Action for sign-up button
    private class SignUpAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            // Validate input
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Password must be at least 8 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert new user into the database
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {

                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Sign Up successful! You can now log in.");
                dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LoginSignUpPage.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
