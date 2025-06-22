package com.library.ui;

import javax.swing.*;

public class DashboardFrame extends JFrame {
    public DashboardFrame() {
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Books", new BookPanel());
        tabbedPane.addTab("Students", new StudentPanel());
        tabbedPane.addTab("Transactions", new TransactionPanel());
        tabbedPane.addTab("Reports", new ReportPanel());
        
        add(tabbedPane);
    }
}