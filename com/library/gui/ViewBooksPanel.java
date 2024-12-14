package com.library.gui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewBooksPanel extends JPanel {
    private JTable booksTable;
    private DefaultTableModel tableModel;

    public ViewBooksPanel() {
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel();
        booksTable = new JTable(tableModel);

        // Add columns to the table model
        tableModel.addColumn("Book ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Author");
        tableModel.addColumn("Quantity");

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes the book list by fetching updated data from the database.
     */
    public void refreshBookList() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM books";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Clear existing rows (if any)
            tableModel.setRowCount(0);

            // Populate the table with data
            while (resultSet.next()) {
                String bookId = resultSet.getString("book_id");
                String name = resultSet.getString("name");
                String author = resultSet.getString("author");
                int quantity = resultSet.getInt("quantity");

                tableModel.addRow(new Object[]{bookId, name, author, quantity});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
