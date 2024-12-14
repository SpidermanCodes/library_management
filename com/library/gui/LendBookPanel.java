package com.library.gui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LendBookPanel extends JPanel {
    private JTextField bookIdField;
    private JTextField borrowerIdField;
    private JTextField borrowerNameField;
    private JTextField borrowerEmailField;
    private JComboBox<String> returnDurationBox;
    private JButton lendButton;

    public LendBookPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Book ID
        JLabel bookIdLabel = new JLabel("Book ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(bookIdLabel, gbc);

        bookIdField = new JTextField(20);
        gbc.gridx = 1;
        add(bookIdField, gbc);

        // Borrower ID
        JLabel borrowerIdLabel = new JLabel("Borrower ID:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(borrowerIdLabel, gbc);

        borrowerIdField = new JTextField(20);
        gbc.gridx = 1;
        add(borrowerIdField, gbc);

        // Borrower Name
        JLabel borrowerNameLabel = new JLabel("Borrower Name:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(borrowerNameLabel, gbc);

        borrowerNameField = new JTextField(20);
        gbc.gridx = 1;
        add(borrowerNameField, gbc);

        // Borrower Email
        JLabel borrowerEmailLabel = new JLabel("Borrower Email:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(borrowerEmailLabel, gbc);

        borrowerEmailField = new JTextField(20);
        gbc.gridx = 1;
        add(borrowerEmailField, gbc);

        // Return Duration
        JLabel returnDurationLabel = new JLabel("Return Duration (days):");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(returnDurationLabel, gbc);

        returnDurationBox = new JComboBox<>(new String[]{"7", "15", "30"});
        gbc.gridx = 1;
        add(returnDurationBox, gbc);

        // Lend Book Button
        lendButton = new JButton("Lend Book");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        lendButton.addActionListener(e -> lendBook());
        add(lendButton, gbc);
    }

    private void lendBook() {
        String bookId = bookIdField.getText().trim();
        String borrowerId = borrowerIdField.getText().trim();
        String borrowerName = borrowerNameField.getText().trim();
        String borrowerEmail = borrowerEmailField.getText().trim();
        int returnDuration = Integer.parseInt((String) returnDurationBox.getSelectedItem());

        if (bookId.isEmpty() || borrowerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Book ID and Borrower ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Check if the book exists and has enough quantity
            String checkBookQuery = "SELECT quantity FROM books WHERE book_id = ?";
            PreparedStatement checkBookStmt = connection.prepareStatement(checkBookQuery);
            checkBookStmt.setString(1, bookId);
            ResultSet bookResult = checkBookStmt.executeQuery();

            if (!bookResult.next()) {
                JOptionPane.showMessageDialog(this, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            int quantity = bookResult.getInt("quantity");
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Book is out of stock.", "Error", JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            // Check if borrower exists
            String checkBorrowerQuery = "SELECT * FROM borrowers WHERE borrower_id = ?";
            PreparedStatement checkBorrowerStmt = connection.prepareStatement(checkBorrowerQuery);
            checkBorrowerStmt.setString(1, borrowerId);
            ResultSet borrowerResult = checkBorrowerStmt.executeQuery();

            if (!borrowerResult.next()) {
                // Register borrower if not found
                if (borrowerName.isEmpty() || borrowerEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Borrower not found. Please provide Name and Email to register.", "Registration Required", JOptionPane.WARNING_MESSAGE);
                    connection.rollback();
                    return;
                }

                String registerBorrowerQuery = "INSERT INTO borrowers (borrower_id, name, email, borrowed_book_id, return_date) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement registerBorrowerStmt = connection.prepareStatement(registerBorrowerQuery);

                LocalDate returnDate = LocalDate.now().plusDays(returnDuration);
                String formattedReturnDate = returnDate.format(DateTimeFormatter.ISO_DATE);

                registerBorrowerStmt.setString(1, borrowerId);
                registerBorrowerStmt.setString(2, borrowerName);
                registerBorrowerStmt.setString(3, borrowerEmail);
                registerBorrowerStmt.setString(4, bookId);
                registerBorrowerStmt.setString(5, formattedReturnDate);
                registerBorrowerStmt.executeUpdate();

            } else {
                // Update existing borrower with book info and return date
                String lendBookQuery = "UPDATE borrowers SET borrowed_book_id = ?, return_date = ? WHERE borrower_id = ?";
                PreparedStatement lendBookStmt = connection.prepareStatement(lendBookQuery);

                LocalDate returnDate = LocalDate.now().plusDays(returnDuration);
                String formattedReturnDate = returnDate.format(DateTimeFormatter.ISO_DATE);

                lendBookStmt.setString(1, bookId);
                lendBookStmt.setString(2, formattedReturnDate);
                lendBookStmt.setString(3, borrowerId);
                lendBookStmt.executeUpdate();
            }

            // Update book quantity
            String updateBookQuery = "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?";
            PreparedStatement updateBookStmt = connection.prepareStatement(updateBookQuery);
            updateBookStmt.setString(1, bookId);
            updateBookStmt.executeUpdate();

            connection.commit(); // Commit transaction

            JOptionPane.showMessageDialog(this, "Book lent successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            bookIdField.setText("");
            borrowerIdField.setText("");
            borrowerNameField.setText("");
            borrowerEmailField.setText("");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
