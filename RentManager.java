package com.mycompany.car_rental_project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RentManager extends JFrame {
    private JTable rentalsTable;
    private JTextField modelField, yearField, startDateField, endDateField, totalPriceField;
    private JButton calculateButton, confirmButton, cancelButton, refreshButton;
    private String userEmail;

    public RentManager(String userEmail) {
        this.userEmail = userEmail;

        // Frame setup
        setTitle("Manage Rentals");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Rentals table
        rentalsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(rentalsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Rental inputs panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        JLabel modelLabel = new JLabel("Model:");
        modelField = new JTextField();
        JLabel yearLabel = new JLabel("Year:");
        yearField = new JTextField();
        JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        startDateField = new JTextField();
        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        endDateField = new JTextField();
        JLabel totalPriceLabel = new JLabel("Total Price:");
        totalPriceField = new JTextField();
        totalPriceField.setEditable(false);

        inputPanel.add(modelLabel);
        inputPanel.add(modelField);
        inputPanel.add(yearLabel);
        inputPanel.add(yearField);
        inputPanel.add(startDateLabel);
        inputPanel.add(startDateField);
        inputPanel.add(endDateLabel);
        inputPanel.add(endDateField);
        inputPanel.add(totalPriceLabel);
        inputPanel.add(totalPriceField);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        calculateButton = new JButton("Calculate Price");
        confirmButton = new JButton("Confirm Rental");
        cancelButton = new JButton("Cancel Rental");
        refreshButton = new JButton("Refresh Rentals");

        buttonPanel.add(calculateButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        // Add panels to the frame
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.add(inputPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // Add action listeners
        calculateButton.addActionListener(new CalculateAction());
        confirmButton.addActionListener(new ConfirmAction());
        cancelButton.addActionListener(new CancelAction());
        refreshButton.addActionListener(new RefreshAction());

        // Load rentals initially
        loadRentals();

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void loadRentals() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "Vehicle Model", "Year", "Start Date", "End Date", "Total Price", "Status", "Overdue Days", "Fee"}, 0
        );

        // Check pending rentals and update their status to active if the start date matches today
        String updatePendingToActive = "UPDATE rent SET status = 'active' WHERE status = 'pending' AND start_date = CURDATE()";

        // Load rentals
        String query = "SELECT r.rent_id, v.model, v.year, r.start_date, r.end_date, r.total_price, r.status, r.overdue_days, r.fee " +
                       "FROM rent r " +
                       "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                       "WHERE r.user_email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updatePendingToActive);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Update pending rentals to active
            updateStmt.executeUpdate();

            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("rent_id"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            rs.getDouble("total_price"),
                            rs.getString("status"),
                            rs.getInt("overdue_days"),
                            rs.getDouble("fee")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage());
        }
        rentalsTable.setModel(tableModel);
    }

    private class CalculateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            calculatePrice();
        }
    }

    private void calculatePrice() {
        String model = modelField.getText().trim();
        String year = yearField.getText().trim();
        String startDateText = startDateField.getText().trim();
        String endDateText = endDateField.getText().trim();

        if (model.isEmpty() || year.isEmpty() || startDateText.isEmpty() || endDateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);
            long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);

            if (rentalDays <= 0) {
                JOptionPane.showMessageDialog(this, "End date must be after start date.");
                return;
            }

            String query = "SELECT price_per_day, availability_status FROM vehicles WHERE model = ? AND year = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, model);
                stmt.setString(2, year);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean available = rs.getBoolean("availability_status");
                        if (available) {
                            double pricePerDay = rs.getDouble("price_per_day");
                            double totalPrice = pricePerDay * rentalDays;
                            totalPriceField.setText(String.format("%.2f", totalPrice));
                        } else {
                            JOptionPane.showMessageDialog(this, "Car not available.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Car not found.");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private class ConfirmAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            confirmRental();
        }
    }

    private void confirmRental() {
    String model = modelField.getText().trim();
    String year = yearField.getText().trim();
    String startDateText = startDateField.getText().trim();
    String endDateText = endDateField.getText().trim();
    String totalPriceText = totalPriceField.getText().trim();

    if (model.isEmpty() || year.isEmpty() || startDateText.isEmpty() || endDateText.isEmpty() || totalPriceText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all fields.");
        return;
    }

    String message = "By confirming this rental, you agree to the following terms:\n" +
                     "- You are responsible for returning the vehicle on time.\n" +
                     "- Late returns may incur additional fees.\n" +
                     "- You will handle the vehicle with care and follow all safety regulations.\n\n" +
                     "Do you accept these terms and wish to proceed with the rental?";

    int result = JOptionPane.showConfirmDialog(this, message, "Rental Agreement", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.NO_OPTION) {
        JOptionPane.showMessageDialog(this, "Rental process canceled.");
        return;  // If user declines, exit the method
    }

    try {
        LocalDate startDate = LocalDate.parse(startDateText);
        LocalDate currentDate = LocalDate.now();

        // Determine rental status (pending or active)
        String status = startDate.isAfter(currentDate) ? "pending" : "active";

        // Insert the rental data into the database
        String query = "INSERT INTO rent (user_email, vehicle_id, start_date, end_date, total_price, status, overdue_days, fee) " +
                       "SELECT ?, vehicle_id, ?, ?, ?, ?, 0, 0 FROM vehicles WHERE model = ? AND year = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userEmail);
            stmt.setString(2, startDateText);
            stmt.setString(3, endDateText);
            stmt.setDouble(4, Double.parseDouble(totalPriceText));
            stmt.setString(5, status);
            stmt.setString(6, model);
            stmt.setString(7, year);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Rental confirmed!");
                loadRentals();  // Refresh rental list
            } else {
                JOptionPane.showMessageDialog(this, "Car not available or already rented.");
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error confirming rental: " + e.getMessage());
    }
}


    private class CancelAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelRental();
        }
    }

    private void cancelRental() {
        int selectedRow = rentalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a rental to cancel.");
            return;
        }

        int rentId = (int) rentalsTable.getValueAt(selectedRow, 0);
        String status = (String) rentalsTable.getValueAt(selectedRow, 6);

        // Allow cancellation only if the rental is pending
        if (!"pending".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only pending rentals can be canceled.");
            return;
        }

        String query = "DELETE FROM rent WHERE rent_id = ? AND status = 'pending'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, rentId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Rental canceled successfully.");
                loadRentals();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel the rental.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error canceling rental: " + ex.getMessage());
        }
    }

    private class RefreshAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadRentals();
        }
    }
}
