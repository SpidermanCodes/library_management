package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.library.database.DatabaseConnection;
//import com.library.util.DatabaseConnection;

public class AddBookPanel extends JPanel {
    // Components
    private JTextField txtBookId, txtBookName, txtAuthorName, txtQuantity;
    private JButton btnAddBook;

    public AddBookPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel titleLabel = new JLabel("Add Book");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Reset Gridwidth
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Book ID
        JLabel lblBookId = new JLabel("Book ID:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblBookId, gbc);

        txtBookId = new JTextField(20);
        gbc.gridx = 1;
        add(txtBookId, gbc);

        // Book Name
        JLabel lblBookName = new JLabel("Book Name:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(lblBookName, gbc);

        txtBookName = new JTextField(20);
        gbc.gridx = 1;
        add(txtBookName, gbc);

        // Author Name
        JLabel lblAuthorName = new JLabel("Author Name:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(lblAuthorName, gbc);

        txtAuthorName = new JTextField(20);
        gbc.gridx = 1;
        add(txtAuthorName, gbc);

        // Quantity
        JLabel lblQuantity = new JLabel("Quantity:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(lblQuantity, gbc);

        txtQuantity = new JTextField(20);
        gbc.gridx = 1;
        add(txtQuantity, gbc);

        // Add Book Button
        btnAddBook = new JButton("Add Book");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnAddBook, gbc);

        // Button Action Listener
        btnAddBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBookToDatabase();
            }
        });
    }

    private void addBookToDatabase() {
        // Retrieve input values
        String bookId = txtBookId.getText().trim();
        String bookName = txtBookName.getText().trim();
        String authorName = txtAuthorName.getText().trim();
        String quantityStr = txtQuantity.getText().trim();

        // Input validation
        if (bookId.isEmpty() || bookName.isEmpty() || authorName.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert book into database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO books (book_id, name, author, quantity) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, bookId);
            stmt.setString(2, bookName);
            stmt.setString(3, authorName);
            stmt.setInt(4, quantity);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add book. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtBookId.setText("");
        txtBookName.setText("");
        txtAuthorName.setText("");
        txtQuantity.setText("");
    }
}
