package com.library.gui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ViewBorrowersPanel extends JPanel {
    private JTable borrowersTable;
    private DefaultTableModel tableModel;

    public ViewBorrowersPanel() {
        setLayout(new BorderLayout());

        // Header Label
        JLabel headerLabel = new JLabel("View Borrowers", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(headerLabel, BorderLayout.NORTH);

        // Table for Borrower Details
        tableModel = new DefaultTableModel();
        borrowersTable = new JTable(tableModel);
        tableModel.addColumn("Borrower ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Email");
        tableModel.addColumn("Book ID");
        tableModel.addColumn("Book Title");
        tableModel.addColumn("Return Date");

        JScrollPane tableScrollPane = new JScrollPane(borrowersTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Load Borrower Data Initially
        loadBorrowersData();
    }

    // Public method to refresh data dynamically
    public void refreshBorrowersList() {
        loadBorrowersData();
    }

    private void loadBorrowersData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Database Connection
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
                SELECT 
                    b.borrower_id,
                    b.name,
                    b.email,
                    bk.book_id,
                    bk.name AS book_title,
                    b.return_date
                FROM 
                    borrowers b
                JOIN 
                    books bk ON b.borrowed_book_id = bk.book_id;
            """;

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                // Populate the table with database rows
                while (resultSet.next()) {
                    Vector<String> row = new Vector<>();
                    row.add(resultSet.getString("borrower_id"));
                    row.add(resultSet.getString("name"));
                    row.add(resultSet.getString("email"));
                    row.add(resultSet.getString("book_id"));
                    row.add(resultSet.getString("book_title"));
                    row.add(resultSet.getDate("return_date").toString());
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
