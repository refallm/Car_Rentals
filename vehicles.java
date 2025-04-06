package com.mycompany.car_rental_project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class vehicles extends JFrame {
    private JTable vehicleTable;
    private JTextArea notificationArea;
    private JTextField modelField, typeField;
    private JButton searchButton, manageRentalsButton, clearNotificationsButton;
    private String userEmail;

    public vehicles(String userEmail) {
        this.userEmail = userEmail;

        // Frame setup
        setTitle("Vehicle Browser");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JLabel modelLabel = new JLabel("Model:");
        modelField = new JTextField();
        JLabel typeLabel = new JLabel("Type:");
        typeField = new JTextField();
        searchButton = new JButton("Search");
        manageRentalsButton = new JButton("Manage Rentals");

        searchPanel.add(modelLabel);
        searchPanel.add(modelField);
        searchPanel.add(typeLabel);
        searchPanel.add(typeField);
        searchPanel.add(searchButton);
        searchPanel.add(manageRentalsButton);

        add(searchPanel, BorderLayout.NORTH);

        // Table area
        vehicleTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        add(scrollPane, BorderLayout.CENTER);

        // Notifications area
        notificationArea = new JTextArea();
        notificationArea.setEditable(false);
        JScrollPane notificationScrollPane = new JScrollPane(notificationArea);
        notificationScrollPane.setBorder(BorderFactory.createTitledBorder("Notifications"));

        // Clear Notifications Button
        clearNotificationsButton = new JButton("Clear Notifications");
        clearNotificationsButton.addActionListener(new ClearNotificationsAction());

        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.add(notificationScrollPane, BorderLayout.CENTER);
        notificationPanel.add(clearNotificationsButton, BorderLayout.SOUTH);

        add(notificationPanel, BorderLayout.EAST);

        // Load all vehicles and notifications initially
        loadVehicles("", "");
        loadNotifications(); // Load notifications on startup

        // Add action listeners
        searchButton.addActionListener(new SearchAction());
        manageRentalsButton.addActionListener(new ManageRentalsAction());

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void loadVehicles(String model, String type) {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Make", "Model", "Year", "Type", "Price/Day", "Available"}, 0);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vehicles WHERE model LIKE ? AND type LIKE ?")) {

            stmt.setString(1, "%" + model + "%");
            stmt.setString(2, "%" + type + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("vehicle_id"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getString("type"),
                            rs.getDouble("price_per_day"),
                            rs.getBoolean("availability_status") ? "Yes" : "No"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + e.getMessage());
        }
        vehicleTable.setModel(tableModel);
    }

    private void loadNotifications() {
        notificationArea.setText(""); // Clear the notification area

        // Fetch notifications for new rent, upcoming rent, and reminders
        addNewRentNotifications();
        addUpcomingRentNotifications();
        addReminderNotifications();
    }

    private void addNewRentNotifications() {
        String query = "SELECT v.model, v.make, r.end_date FROM rent r JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.user_email = ? AND DATE(r.start_date) = CURDATE()";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificationArea.append("New Rent:\n");
                    notificationArea.append("Car: " + rs.getString("make") + " " + rs.getString("model") + "\n");
                    notificationArea.append("Due Date: " + rs.getDate("end_date") + "\n\n");
                }
            }
        } catch (SQLException e) {
            notificationArea.append("Error fetching new rent notifications: " + e.getMessage() + "\n");
        }
    }

    private void addUpcomingRentNotifications() {
        String query = "SELECT v.model, v.make, r.start_date FROM rent r JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.user_email = ? AND DATE(r.start_date) > CURDATE() AND DATE(r.start_date) <= CURDATE() + INTERVAL 3 DAY";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificationArea.append("Upcoming Rent:\n");
                    notificationArea.append("Car: " + rs.getString("make") + " " + rs.getString("model") + "\n");
                    notificationArea.append("Start Date: " + rs.getDate("start_date") + "\n\n");
                }
            }
        } catch (SQLException e) {
            notificationArea.append("Error fetching upcoming rent notifications: " + e.getMessage() + "\n");
        }
    }

    private void addReminderNotifications() {
        String query = "SELECT v.model, v.make, r.end_date, DATEDIFF(r.end_date, CURDATE()) AS days_left " +
                "FROM rent r JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.user_email = ? AND DATE(r.end_date) > CURDATE() AND DATEDIFF(r.end_date, CURDATE()) <= 3";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificationArea.append("Reminder:\n");
                    notificationArea.append("Car: " + rs.getString("make") + " " + rs.getString("model") + "\n");
                    notificationArea.append("Ends in: " + rs.getInt("days_left") + " day(s)\n\n");
                }
            }
        } catch (SQLException e) {
            notificationArea.append("Error fetching reminders: " + e.getMessage() + "\n");
        }
    }

    // ActionListener for Clear Notifications button
    private class ClearNotificationsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            notificationArea.setText(""); // Clear the notification area
        }
    }

    // ActionListener for Search button
    private class SearchAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String model = modelField.getText();
            String type = typeField.getText();
            loadVehicles(model, type);
        }
    }

    // ActionListener for Manage Rentals button
    private class ManageRentalsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new RentManager(userEmail).setVisible(true);
        }
    }
}
