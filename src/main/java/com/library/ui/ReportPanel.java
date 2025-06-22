package com.library.ui;

import com.library.db.DBConnection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ReportPanel extends JPanel {
    public ReportPanel() {
        setLayout(new BorderLayout());
        createUI();
    }
    
    private void createUI() {
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       
        JLabel titleLabel = new JLabel("Library Reports");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        headerPanel.add(titleLabel);
        
        // Create report buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 1, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 100, 100));
        
        // Overdue Books Report
        JButton btnOverdue = createReportButton("Overdue Books");
        btnOverdue.addActionListener(this::generateOverdueReport);
        
        // Student Report
        JButton btnStudents = createReportButton("Student Report");
        btnStudents.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Student Report coming soon!", "Information", JOptionPane.INFORMATION_MESSAGE));
        
        // Transaction History
        JButton btnTransactions = createReportButton("Transaction History");
        btnTransactions.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Transaction History Report coming soon!", "Information", JOptionPane.INFORMATION_MESSAGE));
        
        buttonPanel.add(btnOverdue);
        buttonPanel.add(btnStudents);
        buttonPanel.add(btnTransactions);
        
        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }
    
    private JButton createReportButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setPreferredSize(new Dimension(75, 100));
        button.setBackground(Color.WHITE);
        return button;
    }
    
    private void generateOverdueReport(ActionEvent e) {
        try {
            // Load the JRXML file
            String reportPath = "/reports/overdue.jrxml";
            java.io.InputStream reportStream = getClass().getResourceAsStream(reportPath);
            
            if (reportStream == null) {
                throw new RuntimeException("Report template not found: " + reportPath);
            }
            
            // Compile report
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            
            // Fill report with data
            HashMap<String, Object> parameters = new HashMap<>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, 
                parameters, 
                DBConnection.getConnection()
            );
            
            // Display report
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Overdue Books Report");
            viewer.setVisible(true);
            
        } catch (JRException ex) {
            showError("Report Generation Error", "Failed to generate report: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error", ex.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}