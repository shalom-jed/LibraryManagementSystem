package com.library.ui;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public StudentPanel() {
        setLayout(new BorderLayout());
        createUI();
        loadStudents();
    }
    
    private void createUI() {
        // Create table model and table
        String[] columnNames = {"ID", "Name", "Grade", "Email"};
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
        
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Students"));
        
        searchField = new JTextField(25);
        JButton searchButton = new JButton("Search");
        JButton clearSearchButton = new JButton("Clear");
        
        searchButton.addActionListener(e -> searchStudents());
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });
        
        searchPanel.add(new JLabel("Name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("Add Student");

        
        addButton.addActionListener(e -> showAddEditDialog(null));
        
        buttonPanel.add(addButton);
        
        // Add components to panel
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadStudents() {
        try {
            List<Student> students = StudentDAO.getAllStudents();
            updateTable(students);
        } catch (SQLException e) {
            handleDatabaseError("Failed to load students", e);
        }
    }
    
    private void searchStudents() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            loadStudents();
            return;
        }
        
        try {
            List<Student> allStudents = StudentDAO.getAllStudents();
            List<Student> filteredStudents = allStudents.stream()
                .filter(student -> student.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
            
            updateTable(filteredStudents);
        } catch (SQLException e) {
            handleDatabaseError("Failed to search students", e);
        }
    }
    
    private void updateTable(List<Student> students) {
        tableModel.setRowCount(0);
        
        for (Student student : students) {
            Object[] rowData = {
                student.getId(),
                student.getName(),
                student.getGrade() != null ? student.getGrade() : "",
                student.getEmail() != null ? student.getEmail() : ""
            };
            tableModel.addRow(rowData);
        }
        
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showAddEditDialog(Student student) {
        boolean isEdit = (student != null);
        String title = isEdit ? "Edit Student" : "Add Student";
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField nameField = new JTextField();
        JTextField gradeField = new JTextField();
        JTextField emailField = new JTextField();
        
        if (isEdit) {
            nameField.setText(student.getName());
            gradeField.setText(student.getGrade() != null ? student.getGrade() : "");
            emailField.setText(student.getEmail() != null ? student.getEmail() : "");
        }
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Grade:"));
        formPanel.add(gradeField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String grade = gradeField.getText().trim();
                String email = emailField.getText().trim();
                
                // UI-level validation
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Name cannot be empty");
                }
                
                Student updatedStudent = new Student();
                updatedStudent.setName(name);
                updatedStudent.setGrade(grade.isEmpty() ? null : grade);
                updatedStudent.setEmail(email.isEmpty() ? null : email);
                
                if (isEdit) {
                    updatedStudent.setId(student.getId());
                    StudentDAO.updateStudent(updatedStudent);
                } else {
                    StudentDAO.addStudent(updatedStudent);
                }
                
                loadStudents();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                showError("Validation Error", ex.getMessage());
            } catch (SQLException ex) {
                handleDatabaseError("Failed to save student", ex);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Selection Error", "Please select a student to edit");
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String grade = (String) tableModel.getValueAt(selectedRow, 2);
            String email = (String) tableModel.getValueAt(selectedRow, 3);
            
            Student student = new Student();
            student.setId(id);
            student.setName(name);
            student.setGrade(grade);
            student.setEmail(email);
            
            showAddEditDialog(student);
        } catch (Exception e) {
            showError("Error", "Failed to edit student: " + e.getMessage());
        }
    }
    
    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Selection Error", "Please select a student to delete");
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Are you sure you want to delete student: " + name + "?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                StudentDAO.deleteStudent(id);
                loadStudents();
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to delete student", e);
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        }
    }
    
    private void handleDatabaseError(String action, SQLException e) {
        String errorMessage = action + ":\n" + e.getMessage();
        
        // Check for common SQL error states
        if (e.getSQLState() != null) {
            switch (e.getSQLState()) {
                case "23000": // Integrity constraint violation
                    errorMessage += "\n\nThis student has related records in transactions.";
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