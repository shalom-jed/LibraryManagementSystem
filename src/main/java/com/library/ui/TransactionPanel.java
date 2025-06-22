package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.StudentDAO;
import com.library.dao.TransactionDAO;
import com.library.model.Book;
import com.library.model.Student;
import com.library.model.Transaction;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class TransactionPanel extends JPanel {
    private JComboBox<Student> studentComboBox;
    private JComboBox<Book> bookComboBox;
    private JDateChooser borrowDateChooser;
    private JDateChooser dueDateChooser;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    
    public TransactionPanel() {
        setLayout(new BorderLayout(10, 10));
        createUI();
        loadActiveTransactions();
    }
    
    private void createUI() {
        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Borrow Book"));
        
        // Initialize components
        studentComboBox = new JComboBox<>();
        bookComboBox = new JComboBox<>();
        borrowDateChooser = new JDateChooser();
        dueDateChooser = new JDateChooser();
        
        // Set default dates
        borrowDateChooser.setDate(new Date());
        dueDateChooser.setDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        
        // Populate combo boxes
        populateComboBoxes();
        
        // Add components to form
        formPanel.add(new JLabel("Student:"));
        formPanel.add(studentComboBox);
        formPanel.add(new JLabel("Book:"));
        formPanel.add(bookComboBox);
        formPanel.add(new JLabel("Borrow Date:"));
        formPanel.add(borrowDateChooser);
        formPanel.add(new JLabel("Due Date:"));
        formPanel.add(dueDateChooser);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton borrowButton = new JButton("Borrow Book");
        
        borrowButton.addActionListener(this::borrowBook);
        buttonPanel.add(borrowButton);
        
        // Create transaction table
        String[] columnNames = {"ID", "Student", "Book", "Borrow Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        
        // Create return panel
        JPanel returnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton returnButton = new JButton("Return Selected Book");
        returnButton.addActionListener(this::returnBook);
        returnPanel.add(returnButton);
        

        
        // Create main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(returnPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }
    
    private void populateComboBoxes() {
        try {
            // Populate students
            List<Student> students = StudentDAO.getAllStudents();
            studentComboBox.removeAllItems();
            for (Student student : students) {
                studentComboBox.addItem(student);
            }
            
            // Populate available books
            List<Book> books = BookDAO.getAllBooks();
            bookComboBox.removeAllItems();
            for (Book book : books) {
                if (book.getQuantity() > 0) {
                    bookComboBox.addItem(book);
                }
            }
            
            if (bookComboBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No books available for borrowing", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load data: " + e.getMessage());
        }
    }
    
    private void borrowBook(ActionEvent e) {
        try {
            // Get selected student and book
            Student student = (Student) studentComboBox.getSelectedItem();
            Book book = (Book) bookComboBox.getSelectedItem();
            
            // Validate selection
            if (student == null) {
                throw new IllegalArgumentException("Please select a student");
            }
            
            if (book == null) {
                throw new IllegalArgumentException("Please select a book");
            }
            
            // Get dates
            Date borrowDate = borrowDateChooser.getDate();
            Date dueDate = dueDateChooser.getDate();
            
            // Validate dates
            if (borrowDate == null) {
                throw new IllegalArgumentException("Borrow date is required");
            }
            
            if (dueDate == null) {
                throw new IllegalArgumentException("Due date is required");
            }
            
            if (dueDate.before(borrowDate)) {
                throw new IllegalArgumentException("Due date must be after borrow date");
            }
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setBookId(book.getId());
            transaction.setStudentId(student.getId());
            transaction.setBorrowDate(borrowDate);
            transaction.setDueDate(dueDate);
            
            // Process transaction
            TransactionDAO.borrowBook(transaction);
            
            // Refresh UI
            JOptionPane.showMessageDialog(this, 
                "Book borrowed successfully!\n" +
                "Student: " + student.getName() + "\n" +
                "Book: " + book.getTitle() + "\n" +
                "Due Date: " + dueDate,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            populateComboBoxes();
            loadActiveTransactions();
            
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        } catch (SQLException ex) {
            handleDatabaseError("Failed to borrow book", ex);
        }
    }
    
    private void returnBook(ActionEvent e) {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Selection Error", "Please select a transaction to return");
            return;
        }
        
        try {
            int transactionId = (int) tableModel.getValueAt(selectedRow, 0);
            String studentName = (String) tableModel.getValueAt(selectedRow, 1);
            String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Return book: " + bookTitle + "\n" +
                "Borrowed by: " + studentName + "?", 
                "Confirm Return", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                TransactionDAO.returnBook(transactionId);
                JOptionPane.showMessageDialog(this, 
                    "Book returned successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                loadActiveTransactions();
                populateComboBoxes();
            }
        } catch (SQLException ex) {
            handleDatabaseError("Failed to return book", ex);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }
    
    private void loadActiveTransactions() {
        try {
            List<Transaction> transactions = TransactionDAO.getActiveTransactions();
            updateTable(transactions);
        } catch (SQLException e) {
            handleDatabaseError("Failed to load transactions", e);
        }
    }
    
    private void loadOverdueTransactions() {
        try {
            List<Transaction> transactions = TransactionDAO.getOverdueTransactions();
            updateTable(transactions);
            
            if (transactions.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No overdue books found", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to load overdue transactions", e);
        }
    }
    
    private void updateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        
        for (Transaction transaction : transactions) {
            String status = transaction.isOverdue() ? "Overdue" : "Active";
            String statusDisplay = transaction.isOverdue() 
                ? "<html><font color='red'>" + status + "</font></html>" 
                : status;
            
            Object[] rowData = {
                transaction.getId(),
                transaction.getStudentName(),
                transaction.getBookTitle(),
                transaction.getBorrowDate(),
                transaction.getDueDate(),
                statusDisplay
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void handleDatabaseError(String action, SQLException e) {
        String errorMessage = action + ":\n" + e.getMessage();
        
        // Check for common SQL error states
        if (e.getSQLState() != null) {
            switch (e.getSQLState()) {
                case "23000": // Integrity constraint violation
                    errorMessage += "\n\nData integrity violation. Related records might be missing.";
                    break;
                case "08001": // Unable to connect
                    errorMessage += "\n\nPlease check your database connection.";
                    break;
                case "42S02": // Table doesn't exist
                    errorMessage += "\n\nDatabase table might be missing.";
                    break;
            }
        }
        
        showError("Database Error", errorMessage);
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}