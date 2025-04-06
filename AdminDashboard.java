package com.mycompany.car_rental_project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private JTable vehicleTable, rentalTable;
    private JTextArea notificationArea;
    private JButton refreshVehiclesButton, refreshRentalsButton, markResolvedButton, generateInvoiceButton, markAsReturnedButton, generateReportButton;
    private JButton addCarButton, editCarButton, deleteCarButton, toggleAvailabilityButton;


    public AdminDashboard() {
        // Frame setup
        setTitle("Admin Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Vehicle Management Panel
        JPanel vehiclePanel = new JPanel(new BorderLayout(10, 10));
        vehicleTable = new JTable();
        JScrollPane vehicleScrollPane = new JScrollPane(vehicleTable);

        JPanel vehicleButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshVehiclesButton = new JButton("Refresh Vehicles");
        addCarButton = new JButton("Add Car");
        editCarButton = new JButton("Edit Car");
        deleteCarButton = new JButton("Delete Car");
        toggleAvailabilityButton = new JButton("Toggle Availability");

        vehicleButtonsPanel.add(refreshVehiclesButton);
        vehicleButtonsPanel.add(addCarButton);
        vehicleButtonsPanel.add(editCarButton);
        vehicleButtonsPanel.add(deleteCarButton);
        vehicleButtonsPanel.add(toggleAvailabilityButton);

        vehiclePanel.add(new JLabel("Vehicle Inventory", JLabel.CENTER), BorderLayout.NORTH);
        vehiclePanel.add(vehicleScrollPane, BorderLayout.CENTER);
        vehiclePanel.add(vehicleButtonsPanel, BorderLayout.SOUTH);

        // Rental Management Panel
        JPanel rentalPanel = new JPanel(new BorderLayout(10, 10));
        rentalTable = new JTable();
        JScrollPane rentalScrollPane = new JScrollPane(rentalTable);

        JPanel rentalButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshRentalsButton = new JButton("Refresh Rentals");
        generateInvoiceButton = new JButton("Generate Invoice");
        markAsReturnedButton = new JButton("Mark as Returned");
        generateReportButton = new JButton("Generate Report");

        rentalButtonsPanel.add(refreshRentalsButton);
        rentalButtonsPanel.add(generateInvoiceButton);
        rentalButtonsPanel.add(markAsReturnedButton);
        rentalButtonsPanel.add(generateReportButton);

        rentalPanel.add(new JLabel("Rental History", JLabel.CENTER), BorderLayout.NORTH);
        rentalPanel.add(rentalScrollPane, BorderLayout.CENTER);
        rentalPanel.add(rentalButtonsPanel, BorderLayout.SOUTH);

        // Notification Panel
        JPanel notificationPanel = new JPanel(new BorderLayout(10, 10));
        notificationArea = new JTextArea(10, 30);
        notificationArea.setEditable(false);
        JScrollPane notificationScrollPane = new JScrollPane(notificationArea);

        markResolvedButton = new JButton("Mark Resolved");
        notificationPanel.add(new JLabel("Notifications", JLabel.CENTER), BorderLayout.NORTH);
        notificationPanel.add(notificationScrollPane, BorderLayout.CENTER);
        notificationPanel.add(markResolvedButton, BorderLayout.SOUTH);

        // Add panels to frame
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, vehiclePanel, rentalPanel);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);
        add(notificationPanel, BorderLayout.EAST);

        // Add action listeners
        refreshVehiclesButton.addActionListener(new RefreshVehiclesAction());
        refreshRentalsButton.addActionListener(new RefreshRentalsAction());
        markResolvedButton.addActionListener(new MarkResolvedAction());
        generateInvoiceButton.addActionListener(new GenerateInvoiceAction());
        markAsReturnedButton.addActionListener(new MarkAsReturnedAction());
        generateReportButton.addActionListener(new GenerateReportAction());
        addCarButton.addActionListener(new AddCarAction());
        editCarButton.addActionListener(new EditCarAction());
        deleteCarButton.addActionListener(new DeleteCarAction());
        toggleAvailabilityButton.addActionListener(new ToggleAvailabilityAction());

        loadVehicles();
        loadRentals();
        loadNotifications();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadVehicles() {
        DefaultTableModel vehicleModel = new DefaultTableModel(
                new String[]{"ID", "Make", "Model", "Year", "Type", "Price/Day", "Available"}, 0
        );
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM vehicles")) {

            while (rs.next()) {
                vehicleModel.addRow(new Object[]{
                        rs.getInt("vehicle_id"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("type"),
                        rs.getDouble("price_per_day"),
                        rs.getBoolean("availability_status") ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + e.getMessage());
        }
        vehicleTable.setModel(vehicleModel);
    }

    private void loadRentals() {
    DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "User Email", "Vehicle ID", "Start Date", "End Date", "Status", "Total Price", "Overdue Days", "Fee"}, 0
    );

    String updateQuery = "UPDATE rent r " +
            "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
            "SET r.overdue_days = DATEDIFF(CURDATE(), r.end_date), " +
            "r.fee = DATEDIFF(CURDATE(), r.end_date) * v.price_per_day " +
            "WHERE r.status = 'active' AND CURDATE() > r.end_date";

    String selectQuery = "SELECT r.rent_id, r.user_email, r.vehicle_id, r.start_date, r.end_date, r.status, r.total_price, " +
            "r.overdue_days, r.fee " +
            "FROM rent r JOIN vehicles v ON r.vehicle_id = v.vehicle_id";

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement()) {

        // Update overdue days and fees for active rentals
        stmt.executeUpdate(updateQuery);

        // Fetch updated rental data
        try (ResultSet rs = stmt.executeQuery(selectQuery)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("rent_id"),
                        rs.getString("user_email"),
                        rs.getInt("vehicle_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("status"),
                        rs.getDouble("total_price"),
                        rs.getInt("overdue_days"),
                        rs.getDouble("fee")
                });
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage());
    }
    rentalTable.setModel(tableModel);
}

    private void loadNotifications() {
        notificationArea.setText(""); 

        String overdueQuery = "SELECT r.user_email, v.model, v.year, r.end_date " +
                "FROM rent r JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.status = 'active' AND CURDATE() > r.end_date";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet overdueRs = stmt.executeQuery(overdueQuery)) {
            notificationArea.append("Overdue Rentals:\n");
            while (overdueRs.next()) {
                notificationArea.append("User: " + overdueRs.getString("user_email") +
                        ", Vehicle: " + overdueRs.getString("model") +
                        ", Due Date: " + overdueRs.getDate("end_date") + "\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage());
        }
    }

    private class RefreshVehiclesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadVehicles();
        }
    }

    private class RefreshRentalsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadRentals();
        }
    }

    private class MarkResolvedAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            notificationArea.setText("");
            JOptionPane.showMessageDialog(AdminDashboard.this, "All notifications marked as resolved!");
        }
    }

    private class GenerateInvoiceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = rentalTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a rental to generate an invoice.");
                return;
            }

            int rentalId = (int) rentalTable.getValueAt(selectedRow, 0);
            String userEmail = (String) rentalTable.getValueAt(selectedRow, 1);
            int vehicleId = (int) rentalTable.getValueAt(selectedRow, 2);
            String startDate = rentalTable.getValueAt(selectedRow, 3).toString();
            String endDate = rentalTable.getValueAt(selectedRow, 4).toString();
            double fee = (double) rentalTable.getValueAt(selectedRow, 8);
            double totalPrice = (double) rentalTable.getValueAt(selectedRow, 6);

            String invoiceContent = "Rental Invoice\n"
                    + "--------------\n"
                    + "Rental ID: " + rentalId + "\n"
                    + "User Email: " + userEmail + "\n"
                    + "Vehicle ID: " + vehicleId + "\n"
                    + "Rental Period: " + startDate + " to " + endDate + "\n"
                    + "Total Price: $" + totalPrice + "\n"
                    + "Fee (Overdue): $" + fee + "\n"
                    + "Final Price: $" + (totalPrice + fee) + "\n";

            try (FileWriter writer = new FileWriter("Invoice_" + rentalId + ".txt")) {
                writer.write(invoiceContent);
                JOptionPane.showMessageDialog(AdminDashboard.this, "Invoice saved as: Invoice_" + rentalId + ".txt");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error saving invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     private class MarkAsReturnedAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = rentalTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a rental to mark as returned.");
                return;
            }

            int rentalId = (int) rentalTable.getValueAt(selectedRow, 0);
            String query = "UPDATE rent SET status = 'returned' WHERE rent_id = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, rentalId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Rental marked as returned.");
                    loadRentals();
                } else {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to mark rental as returned.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error updating status: " + ex.getMessage());
            }
        }
    }

    private class GenerateReportAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String totalBookingsQuery = "SELECT COUNT(*) AS total_bookings FROM rent";
        String totalCustomersQuery = "SELECT COUNT(DISTINCT user_email) AS total_customers FROM rent";
        String totalRevenueQuery = "SELECT SUM(total_price + fee) AS total_revenue FROM rent";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             FileWriter writer = new FileWriter("Report.txt")) {

            writer.write("Rental Report\n\n");
            // Total Bookings
            try (ResultSet rs = stmt.executeQuery(totalBookingsQuery)) {
                if (rs.next()) {
                    writer.write("Total Bookings: " + rs.getInt("total_bookings") + "\n");
                }
            }

            // Total Customers
            try (ResultSet rs = stmt.executeQuery(totalCustomersQuery)) {
                if (rs.next()) {
                    writer.write("Total Customers: " + rs.getInt("total_customers") + "\n");
                }
            }

            // Total Revenue (Sum of Total Prices)
            try (ResultSet rs = stmt.executeQuery(totalRevenueQuery)) {
                if (rs.next()) {
                    writer.write("Total Sum of Total Prices: $" + rs.getDouble("total_revenue") + "\n");
                }
            }

            writer.close();
            JOptionPane.showMessageDialog(AdminDashboard.this, "Report saved as Report.txt");

        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(AdminDashboard.this, "Error generating report: " + ex.getMessage());
        }
    }
}


    private class AddCarAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String make = JOptionPane.showInputDialog("Enter Car Make:");
            String model = JOptionPane.showInputDialog("Enter Car Model:");
            String year = JOptionPane.showInputDialog("Enter Car Year:");
            String type = JOptionPane.showInputDialog("Enter Car Type:");
            String price = JOptionPane.showInputDialog("Enter Price/Day:");

            if (make == null || model == null || year == null || type == null || price == null) {
                JOptionPane.showMessageDialog(null, "All fields are required.");
                return;
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO vehicles (make, model, year, type, price_per_day, availability_status) VALUES (?, ?, ?, ?, ?, true)")) {

                stmt.setString(1, make);
                stmt.setString(2, model);
                stmt.setInt(3, Integer.parseInt(year));
                stmt.setString(4, type);
                stmt.setDouble(5, Double.parseDouble(price));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Car added successfully!");
                loadVehicles();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error adding car: " + ex.getMessage());
            }
        }
    }

    private class EditCarAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = vehicleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a car to edit.");
                return;
            }

            int vehicleId = (int) vehicleTable.getValueAt(selectedRow, 0);

            String make = JOptionPane.showInputDialog("Enter new Make:", vehicleTable.getValueAt(selectedRow, 1));
            String model = JOptionPane.showInputDialog("Enter new Model:", vehicleTable.getValueAt(selectedRow, 2));
            String year = JOptionPane.showInputDialog("Enter new Year:", vehicleTable.getValueAt(selectedRow, 3));
            String type = JOptionPane.showInputDialog("Enter new Type:", vehicleTable.getValueAt(selectedRow, 4));
            String price = JOptionPane.showInputDialog("Enter new Price/Day:", vehicleTable.getValueAt(selectedRow, 5));

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE vehicles SET make = ?, model = ?, year = ?, type = ?, price_per_day = ? WHERE vehicle_id = ?")) {

                stmt.setString(1, make);
                stmt.setString(2, model);
                stmt.setInt(3, Integer.parseInt(year));
                stmt.setString(4, type);
                stmt.setDouble(5, Double.parseDouble(price));
                stmt.setInt(6, vehicleId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Car details updated successfully!");
                loadVehicles();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error editing car: " + ex.getMessage());
            }
        }
    }

    private class DeleteCarAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = vehicleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a car to delete.");
                return;
            }

            int vehicleId = (int) vehicleTable.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this car?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM vehicles WHERE vehicle_id = ?")) {

                stmt.setInt(1, vehicleId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Car deleted successfully!");
                loadVehicles();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error deleting car: " + ex.getMessage());
            }
        }
    }

    private class ToggleAvailabilityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = vehicleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a car to toggle availability.");
                return;
            }

            int vehicleId = (int) vehicleTable.getValueAt(selectedRow, 0);
            String query = "UPDATE vehicles SET availability_status = NOT availability_status WHERE vehicle_id = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, vehicleId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Car availability toggled successfully!");
                loadVehicles();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error toggling availability: " + ex.getMessage());
            }
        }
    }
}