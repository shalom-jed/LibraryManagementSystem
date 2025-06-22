package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class DBConnection {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/library_db";
            String user = "root";
            String pass = "1234";  
            Connection conn = DriverManager.getConnection(url, user, pass);
            return conn;
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
            return null;
        }
    }
    
    public static void main(String[] args) {
        getConnection(); 
    }
}