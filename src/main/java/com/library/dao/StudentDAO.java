package com.library.dao;

import com.library.db.DBConnection;
import com.library.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StudentDAO {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    public static void addStudent(Student student) throws SQLException, IllegalArgumentException {
        // Validation
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty");
        }
        
        if (student.getName().length() > 255) {
            throw new IllegalArgumentException("Student name cannot exceed 255 characters");
        }
        
        if (student.getGrade() != null && student.getGrade().length() > 10) {
            throw new IllegalArgumentException("Grade cannot exceed 10 characters");
        }
        
        if (student.getEmail() != null) {
            if (student.getEmail().length() > 100) {
                throw new IllegalArgumentException("Email cannot exceed 100 characters");
            }
            if (!EMAIL_PATTERN.matcher(student.getEmail()).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        String sql = "INSERT INTO students (name, grade, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName().trim());
            stmt.setString(2, student.getGrade() != null ? student.getGrade().trim() : null);
            stmt.setString(3, student.getEmail() != null ? student.getEmail().trim() : null);
            stmt.executeUpdate();
        }
    }
    
    public static List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("student_id"));
                student.setName(rs.getString("name"));
                student.setGrade(rs.getString("grade"));
                student.setEmail(rs.getString("email"));
                students.add(student);
            }
        }
        return students;
    }
    
    public static void updateStudent(Student student) throws SQLException, IllegalArgumentException {
        // Validation 
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty");
        }
        
        if (student.getName().length() > 255) {
            throw new IllegalArgumentException("Student name cannot exceed 255 characters");
        }
        
        if (student.getGrade() != null && student.getGrade().length() > 10) {
            throw new IllegalArgumentException("Grade cannot exceed 10 characters");
        }
        
        if (student.getEmail() != null) {
            if (student.getEmail().length() > 100) {
                throw new IllegalArgumentException("Email cannot exceed 100 characters");
            }
            if (!EMAIL_PATTERN.matcher(student.getEmail()).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        String sql = "UPDATE students SET name = ?, grade = ?, email = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName().trim());
            stmt.setString(2, student.getGrade() != null ? student.getGrade().trim() : null);
            stmt.setString(3, student.getEmail() != null ? student.getEmail().trim() : null);
            stmt.setInt(4, student.getId());
            stmt.executeUpdate();
        }
    }
    
    public static void deleteStudent(int id) throws SQLException, IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No student found with ID: " + id);
            }
        }
    }
}