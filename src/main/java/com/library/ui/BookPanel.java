package com.library.ui;

import com.library.dao.BookDAO;
import com.library.model.Book;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;



public class BookPanel extends JPanel {
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[]{"ID", "Title", "Author", "ISBN", "Quantity"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; 
        }
    };
    
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtSearch = new JTextField(20);
    
    public BookPanel() {
        setLayout(new BorderLayout());
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        loadBooks();
        initUI();
    }
    
    private void initUI() {
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Books"));
        
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(this::performSearch);
        
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            sorter.setRowFilter(null);
        });
        
        // Add search components
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Table setup
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnAdd = new JButton("Add Book");
        btnAdd.addActionListener(e -> showAddDialog());
        
        buttonPanel.add(btnAdd);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void performSearch(ActionEvent e) {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        
        // Create filter for title, author, and ISBN
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String title = entry.getStringValue(1).toLowerCase();
                String author = entry.getStringValue(2).toLowerCase();
                String isbn = entry.getStringValue(3).toLowerCase();
                
                return title.contains(query) || 
                       author.contains(query) || 
                       isbn.contains(query);
            }
        };
        
        sorter.setRowFilter(filter);
    }
    
    private void loadBooks() {
        try {
            List<Book> books = BookDAO.getAllBooks();
            tableModel.setRowCount(0); // Clear existing data
            
            for (Book book : books) {
                tableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getQuantity()
                });
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load books: " + e.getMessage());
        }
    }
    
    private void showAddDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Book");
        dialog.setSize(400, 250);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtTitle = new JTextField();
        JTextField txtAuthor = new JTextField();
        JTextField txtIsbn = new JTextField();
        JTextField txtQuantity = new JTextField();
        
        formPanel.add(new JLabel("Title:"));
        formPanel.add(txtTitle);
        formPanel.add(new JLabel("Author:"));
        formPanel.add(txtAuthor);
        formPanel.add(new JLabel("ISBN:"));
        formPanel.add(txtIsbn);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(txtQuantity);
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveBook(dialog, txtTitle, txtAuthor, txtIsbn, txtQuantity));
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void saveBook(JDialog dialog, JTextField... fields) {
        try {
            Book book = new Book();
            book.setTitle(fields[0].getText());
            book.setAuthor(fields[1].getText());
            book.setIsbn(fields[2].getText());
            book.setQuantity(Integer.parseInt(fields[3].getText()));
            
            BookDAO.addBook(book);
            loadBooks(); // Refresh table
            dialog.dispose();
        } catch (NumberFormatException | SQLException ex) {
            showError("Save Error", "Invalid input: " + ex.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}