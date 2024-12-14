package com.library.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ViewBooksPanel viewBooksPanel;
    private ViewBorrowersPanel viewBorrowersPanel;

    public MainFrame() {
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Instantiate panels for dynamic updates
        viewBooksPanel = new ViewBooksPanel();
        viewBorrowersPanel = new ViewBorrowersPanel();

        // Add panels
        mainPanel.add(new AddBookPanel(), "AddBook");
        mainPanel.add(new LendBookPanel(), "LendBook");
        mainPanel.add(viewBooksPanel, "ViewBooks");
        mainPanel.add(viewBorrowersPanel, "ViewBorrowers");
        mainPanel.add(new ReturnBookPanel(), "ReturnBook");

        add(mainPanel, BorderLayout.CENTER);

        // Navigation Panel
        JPanel navPanel = new JPanel();
        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> cardLayout.show(mainPanel, "AddBook"));
        navPanel.add(addBookButton);

        JButton lendBookButton = new JButton("Lend Book");
        lendBookButton.addActionListener(e -> cardLayout.show(mainPanel, "LendBook"));
        navPanel.add(lendBookButton);

        JButton viewBooksButton = new JButton("View Books");
        viewBooksButton.addActionListener(e -> {
            viewBooksPanel.refreshBookList();
            cardLayout.show(mainPanel, "ViewBooks");
        });
        navPanel.add(viewBooksButton);

        JButton viewBorrowersButton = new JButton("View Borrowers");
        viewBorrowersButton.addActionListener(e -> {
            viewBorrowersPanel.refreshBorrowersList();
            cardLayout.show(mainPanel, "ViewBorrowers");
        });
        navPanel.add(viewBorrowersButton);

        JButton returnBookButton = new JButton("Return Book");
        returnBookButton.addActionListener(e -> cardLayout.show(mainPanel, "ReturnBook"));
        navPanel.add(returnBookButton);

        add(navPanel, BorderLayout.NORTH);

        setVisible(true);
    }
}
