package com.library.dao;

import com.library.db.DBConnection;
import com.library.model.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {
    public static void borrowBook(Transaction transaction) throws SQLException, IllegalArgumentException {
        // Validation
        if (transaction.getBookId() <= 0) {
            throw new IllegalArgumentException("Invalid book selection");
        }
        
        if (transaction.getStudentId() <= 0) {
            throw new IllegalArgumentException("Invalid student selection");
        }
        
        if (transaction.getBorrowDate() == null) {
            throw new IllegalArgumentException("Borrow date is required");
        }
        
        if (transaction.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        
        if (transaction.getDueDate().before(transaction.getBorrowDate())) {
            throw new IllegalArgumentException("Due date must be after borrow date");
        }
        
        // Check book availability
        int availableQuantity = getBookQuantity(transaction.getBookId());
        if (availableQuantity <= 0) {
            throw new IllegalArgumentException("This book is currently unavailable");
        }
        
        String sql = "INSERT INTO transactions (book_id, student_id, borrow_date, due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transaction.getBookId());
            stmt.setInt(2, transaction.getStudentId());
            stmt.setDate(3, new java.sql.Date(transaction.getBorrowDate().getTime()));
            stmt.setDate(4, new java.sql.Date(transaction.getDueDate().getTime()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to create transaction");
            }
            
            // Update book quantity
            updateBookQuantity(transaction.getBookId(), -1);
        }
    }
    
    public static void returnBook(int transactionId) throws SQLException, IllegalArgumentException {
        if (transactionId <= 0) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }
        
        String sql = "UPDATE transactions SET return_date = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, new java.sql.Date(new Date().getTime()));
            stmt.setInt(2, transactionId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No transaction found with ID: " + transactionId);
            }
            
            // Get book ID and update quantity
            int bookId = getBookIdForTransaction(transactionId);
            updateBookQuantity(bookId, 1);
        }
    }
    
    public static List<Transaction> getActiveTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title, s.name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN students s ON t.student_id = s.student_id " +
                     "WHERE t.return_date IS NULL";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("transaction_id"));
                transaction.setBookId(rs.getInt("book_id"));
                transaction.setStudentId(rs.getInt("student_id"));
                transaction.setBorrowDate(rs.getDate("borrow_date"));
                transaction.setDueDate(rs.getDate("due_date"));
                
                // Additional info for display
                transaction.setBookTitle(rs.getString("title"));
                transaction.setStudentName(rs.getString("name"));
                
                transactions.add(transaction);
            }
        }
        return transactions;
    }
    
    public static List<Transaction> getOverdueTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title, s.name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN students s ON t.student_id = s.student_id " +
                     "WHERE t.return_date IS NULL AND t.due_date < CURDATE()";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("transaction_id"));
                transaction.setBookId(rs.getInt("book_id"));
                transaction.setStudentId(rs.getInt("student_id"));
                transaction.setBorrowDate(rs.getDate("borrow_date"));
                transaction.setDueDate(rs.getDate("due_date"));
                
                // Additional info for display
                transaction.setBookTitle(rs.getString("title"));
                transaction.setStudentName(rs.getString("name"));
                
                transactions.add(transaction);
            }
        }
        return transactions;
    }
    
    private static int getBookQuantity(int bookId) throws SQLException {
        String sql = "SELECT quantity FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        return 0;
    }
    
    private static void updateBookQuantity(int bookId, int change) throws SQLException {
        String sql = "UPDATE books SET quantity = quantity + ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, change);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }
    
    private static int getBookIdForTransaction(int transactionId) throws SQLException {
        String sql = "SELECT book_id FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("book_id");
                }
            }
        }
        throw new SQLException("Book not found for transaction: " + transactionId);
    }
}