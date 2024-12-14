package com.library.gui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnBookPanel extends JPanel {
    private JComboBox<String> borrowerComboBox;
    private JComboBox<String> bookComboBox;
    private JButton returnBookButton;

    public ReturnBookPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel borrowerLabel = new JLabel("Select Borrower:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(borrowerLabel, gbc);

        borrowerComboBox = new JComboBox<>();
        gbc.gridx = 1;
        add(borrowerComboBox, gbc);

        JLabel bookLabel = new JLabel("Select Book:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(bookLabel, gbc);

        bookComboBox = new JComboBox<>();
        gbc.gridx = 1;
        add(bookComboBox, gbc);

        returnBookButton = new JButton("Return Book");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(returnBookButton, gbc);

        // Load initial data into combo boxes
        loadBorrowerData();

        // Action listener for borrower selection to load books
        borrowerComboBox.addActionListener(e -> loadBookData());

        // Action listener for return book button
        returnBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });
    }

    private void loadBorrowerData() {
        borrowerComboBox.removeAllItems();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT borrower_id, name FROM borrowers");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String borrowerId = resultSet.getString("borrower_id");
                String borrowerName = resultSet.getString("name");
                borrowerComboBox.addItem(borrowerId + " - " + borrowerName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookData() {
        bookComboBox.removeAllItems();
        String selectedBorrower = (String) borrowerComboBox.getSelectedItem();
        if (selectedBorrower == null) return;

        String borrowerId = selectedBorrower.split(" - ")[0];

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT b.borrowed_book_id, bk.name AS book_name FROM borrowers b JOIN books bk ON b.borrowed_book_id = bk.book_id WHERE b.borrower_id = ?")) {
            statement.setString(1, borrowerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String bookId = resultSet.getString("borrowed_book_id");
                    String bookName = resultSet.getString("book_name");
                    bookComboBox.addItem(bookId + " - " + bookName);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        String selectedBorrower = (String) borrowerComboBox.getSelectedItem();
        String selectedBook = (String) bookComboBox.getSelectedItem();

        if (selectedBorrower == null || selectedBook == null) {
            JOptionPane.showMessageDialog(this, "Please select a borrower and a book.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String borrowerId = selectedBorrower.split(" - ")[0];
        String bookId = selectedBook.split(" - ")[0];

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Delete from borrowers table
            try (PreparedStatement deleteBorrowerStmt = connection.prepareStatement(
                    "DELETE FROM borrowers WHERE borrower_id = ? AND borrowed_book_id = ?")) {
                deleteBorrowerStmt.setString(1, borrowerId);
                deleteBorrowerStmt.setString(2, bookId);
                deleteBorrowerStmt.executeUpdate();
            }

            // Increment book quantity in books table
            try (PreparedStatement updateBookStmt = connection.prepareStatement(
                    "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?")) {
                updateBookStmt.setString(1, bookId);
                updateBookStmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Refresh the data
            loadBorrowerData();
            bookComboBox.removeAllItems();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error returning book: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
