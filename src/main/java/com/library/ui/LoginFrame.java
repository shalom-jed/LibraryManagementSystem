package com.library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

public class LoginFrame extends JFrame {
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private Image backgroundImage;

    public LoginFrame() {
        configureFrame();
        loadBackgroundImage();
        createUI();
    }
    
    private void configureFrame() {
        setTitle("St. Mary's College - Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void loadBackgroundImage() {
        try (InputStream is = getClass().getResourceAsStream("/library_bg.jpg")) {
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load background image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
private void createUI() {
    
    JPanel mainPanel = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    };

    
    JPanel titlePanel = new JPanel();
    titlePanel.setOpaque(false);
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
    titlePanel.setBorder(BorderFactory.createEmptyBorder(75, 0, 30, 0)); 
    
    JLabel title1Label = new JLabel("St. Mary's College, Kegalle");
    title1Label.setFont(new Font("Serif", Font.BOLD, 32));
    title1Label.setForeground(Color.BLACK);
    title1Label.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel title2Label = new JLabel("Library Management System");
    title2Label.setFont(new Font("Serif", Font.BOLD, 28));
    title2Label.setForeground(Color.BLACK);
    title2Label.setAlignmentX(Component.CENTER_ALIGNMENT);

    titlePanel.add(title1Label);
    titlePanel.add(title2Label);

    
    JPanel formPanel = new JPanel();
    formPanel.setOpaque(false);
    formPanel.setLayout(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 130, 100)); 
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;

    
    JLabel lblUsername = new JLabel("Username:");
    lblUsername.setForeground(Color.BLACK);
    lblUsername.setFont(new Font("SansSerif", Font.BOLD, 14));
    formPanel.add(lblUsername, gbc);

    gbc.gridy++;
    txtUsername.setPreferredSize(new Dimension(200, 30));
    formPanel.add(txtUsername, gbc);

    
    gbc.gridy++;
    JLabel lblPassword = new JLabel("Password:");
    lblPassword.setForeground(Color.BLACK);
    lblPassword.setFont(new Font("SansSerif", Font.BOLD, 14));
    formPanel.add(lblPassword, gbc);

    gbc.gridy++;
    txtPassword.setPreferredSize(new Dimension(200, 30));
    formPanel.add(txtPassword, gbc);

   
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.NONE;
    JButton btnLogin = new JButton("Login");
    btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
    btnLogin.setPreferredSize(new Dimension(100, 30));
    btnLogin.addActionListener(this::performLogin);
    formPanel.add(btnLogin, gbc);

   
    mainPanel.add(titlePanel, BorderLayout.NORTH);
    
    
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setOpaque(false);
    bottomPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER); 
    bottomPanel.add(formPanel, BorderLayout.SOUTH);
    
    mainPanel.add(bottomPanel, BorderLayout.CENTER);
    
    setContentPane(mainPanel);
}
    
    private void performLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        

        if ("admin".equals(username) && "1234".equals(password)) {
            openDashboard();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openDashboard() {
        new DashboardFrame().setVisible(true);
        dispose(); 
    }
}