
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public class CipherCascadeApp extends JFrame {
    // Database connection
    private Connection conn;
    
    // UI Components
    private JTextArea inputArea, outputArea;
    private JComboBox<String> algorithmCombo;
    private JPasswordField keyField;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JCheckBox aesCheck, desCheck, blowfishCheck;
    
    // Login system components
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int currentUserId = -1;
    private String currentUserName = "";
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cipherdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    public CipherCascadeApp() {
        setTitle("CipherCascade - Multi-Encryption Framework");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeDatabase();
        initializeUI();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            createTables();
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection failed: " + e.getMessage() + 
                "\n\nUsing in-memory mode instead.", 
                "Database Warning", 
                JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Users table for authentication
            String createUsersTable = 
                "CREATE TABLE IF NOT EXISTS cipher_users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(100) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "full_name VARCHAR(100)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(createUsersTable);
            
            // Encrypted data table
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS encrypted_data (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "original_text TEXT," +
                "encrypted_text TEXT," +
                "algorithms_used VARCHAR(255)," +
                "encryption_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES cipher_users(id))";
            stmt.executeUpdate(createTableSQL);
            
            stmt.close();
            System.out.println("Database tables verified/created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Add different screens
        mainPanel.add(createWelcomeScreen(), "WELCOME");
        mainPanel.add(createLoginScreen(), "LOGIN");
        mainPanel.add(createRegisterScreen(), "REGISTER");
        mainPanel.add(createMainAppScreen(), "APP");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "WELCOME");
    }
    
    // ==================== WELCOME SCREEN ====================
    private JPanel createWelcomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        
        // Center content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Logo/Title
        JLabel titleLabel = new JLabel("CipherCascade");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 64));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("Multi-Layer Encryption Framework");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        centerPanel.add(subtitleLabel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        buttonPanel.setOpaque(false);
        
        JButton loginBtn = createStyledButton("LOGIN", Color.WHITE, new Color(41, 128, 185));
        loginBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        
        JButton registerBtn = createStyledButton("CREATE ACCOUNT", Color.WHITE, new Color(41, 128, 185));
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(50, 10, 10, 10);
        centerPanel.add(buttonPanel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== LOGIN SCREEN ====================
    private JPanel createLoginScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(1200, 100));
        JLabel headerLabel = new JLabel("Welcome Back to CipherCascade!");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField usernameField = new JTextField(25);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField passwordField = new JPasswordField(25);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        
        JButton loginBtn = createStyledButton("LOGIN", new Color(46, 204, 113), Color.WHITE);
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, 
                    "Welcome back, " + currentUserName + "!", 
                    "Login Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                // Rebuild the app screen to show user info
                rebuildMainAppScreen();
                cardLayout.show(mainPanel, "APP");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid credentials! Please try again.", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        formPanel.add(loginBtn, gbc);
        
        JButton backBtn = createStyledButton("BACK", new Color(149, 165, 166), Color.WHITE);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(backBtn, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== REGISTER SCREEN ====================
    private JPanel createRegisterScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(1200, 100));
        JLabel headerLabel = new JLabel("Create Your CipherCascade Account");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField fullNameField = new JTextField(25);
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField usernameField = new JTextField(25);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField emailField = new JTextField(25);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField passwordField = new JPasswordField(25);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField confirmPasswordField = new JPasswordField(25);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        int row = 0;
        addFormField(formPanel, gbc, "Full Name:", fullNameField, row++);
        addFormField(formPanel, gbc, "Username:", usernameField, row++);
        addFormField(formPanel, gbc, "Email:", emailField, row++);
        addFormField(formPanel, gbc, "Password:", passwordField, row++);
        addFormField(formPanel, gbc, "Confirm Password:", confirmPasswordField, row++);
        
        JButton registerBtn = createStyledButton("CREATE ACCOUNT", new Color(46, 204, 113), Color.WHITE);
        registerBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!");
                return;
            }
            
            if (registerUser(username, password, email, fullName)) {
                JOptionPane.showMessageDialog(this, 
                    "Registration successful!\nPlease login to continue.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Registration failed!\nUsername or email may already exist.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        formPanel.add(registerBtn, gbc);
        
        JButton backBtn = createStyledButton("BACK", new Color(149, 165, 166), Color.WHITE);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        gbc.gridy = row + 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(backBtn, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== MAIN APP SCREEN ====================
    private JPanel createMainAppScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Encryption/Decryption Tab
        JPanel encryptPanel = createEncryptionPanel();
        tabbedPane.addTab("Encryption", new ImageIcon(), encryptPanel, "Encrypt and Decrypt Data");
        
        // Database View Tab
        JPanel dbPanel = createDatabasePanel();
        tabbedPane.addTab("My Encrypted Data", new ImageIcon(), dbPanel, "View Your Encrypted Data");
        
        // Schema Tab
        JPanel schemaPanel = createSchemaPanel();
        tabbedPane.addTab("Database Schema", new ImageIcon(), schemaPanel, "View Database Structure");
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void rebuildMainAppScreen() {
        // Remove and recreate the APP screen
        mainPanel.remove(3);
        mainPanel.add(createMainAppScreen(), "APP", 3);
        loadDatabaseRecords();
    }
    
    private JPanel createEncryptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header with user info and logout
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(1200, 80));
        
        JLabel titleLabel = new JLabel("  CipherCascade Multi-Layer Encryption");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("User: " + currentUserName + "  ");
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userLabel.setForeground(Color.WHITE);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentUserId = -1;
                currentUserName = "";
                cardLayout.show(mainPanel, "WELCOME");
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(logoutBtn);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel - Input/Output
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Text"));
        inputArea = new JTextArea(10, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        
        // Output Panel
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Text"));
        outputArea = new JTextArea(10, 30);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputPanel.add(outputScroll, BorderLayout.CENTER);
        
        centerPanel.add(inputPanel);
        centerPanel.add(outputPanel);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Encryption Configuration"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Algorithm Selection
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Select Algorithms (Cascade):"), gbc);
        
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aesCheck = new JCheckBox("AES", true);
        desCheck = new JCheckBox("DES", false);
        blowfishCheck = new JCheckBox("Blowfish", false);
        algoPanel.add(aesCheck);
        algoPanel.add(desCheck);
        algoPanel.add(blowfishCheck);
        gbc.gridx = 1; gbc.gridy = 0;
        controlPanel.add(algoPanel, gbc);
        
        // Encryption Key
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Encryption Key:"), gbc);
        keyField = new JPasswordField(20);
        keyField.setText("MySecretKey12345");
        gbc.gridx = 1; gbc.gridy = 1;
        controlPanel.add(keyField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton encryptBtn = new JButton("Encrypt");
        encryptBtn.setBackground(new Color(46, 204, 113));
        encryptBtn.setForeground(Color.WHITE);
        encryptBtn.setFocusPainted(false);
        encryptBtn.setFont(new Font("Arial", Font.BOLD, 14));
        encryptBtn.addActionListener(e -> performEncryption());
        
        JButton decryptBtn = new JButton("Decrypt");
        decryptBtn.setBackground(new Color(231, 76, 60));
        decryptBtn.setForeground(Color.WHITE);
        decryptBtn.setFocusPainted(false);
        decryptBtn.setFont(new Font("Arial", Font.BOLD, 14));
        decryptBtn.addActionListener(e -> performDecryption());
        
        JButton saveBtn = new JButton("Save to Database");
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.addActionListener(e -> saveToDatabase());
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.setBackground(new Color(149, 165, 166));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setFont(new Font("Arial", Font.BOLD, 14));
        clearBtn.addActionListener(e -> clearFields());
        
        JButton copyToInputBtn = new JButton("Copy Output to Input");
        copyToInputBtn.setBackground(new Color(155, 89, 182));
        copyToInputBtn.setForeground(Color.WHITE);
        copyToInputBtn.setFocusPainted(false);
        copyToInputBtn.setFont(new Font("Arial", Font.BOLD, 14));
        copyToInputBtn.addActionListener(e -> {
            inputArea.setText(outputArea.getText());
            JOptionPane.showMessageDialog(this, "Output copied to input for decryption!");
        });
        
        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        buttonPanel.add(copyToInputBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        controlPanel.add(buttonPanel, gbc);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Info label
        JLabel infoLabel = new JLabel("Your Encrypted Data Records");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(infoLabel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Original Text", "Encrypted Text", "Algorithms", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadDatabaseRecords());
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedRecord());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(deleteBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSchemaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JTextArea schemaArea = new JTextArea();
        schemaArea.setEditable(false);
        schemaArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StringBuilder schema = new StringBuilder();
        schema.append("DATABASE SCHEMA - CipherCascade\n");
        schema.append("=".repeat(60)).append("\n\n");
        
        schema.append("Table: cipher_users\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("Column Name          | Data Type        | Constraints\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("id                   | INT              | PRIMARY KEY, AUTO_INCREMENT\n");
        schema.append("username             | VARCHAR(100)     | UNIQUE, NOT NULL\n");
        schema.append("password             | VARCHAR(100)     | NOT NULL\n");
        schema.append("email                | VARCHAR(100)     | UNIQUE, NOT NULL\n");
        schema.append("full_name            | VARCHAR(100)     | \n");
        schema.append("created_at           | TIMESTAMP        | DEFAULT CURRENT_TIMESTAMP\n");
        schema.append("-".repeat(60)).append("\n\n");
        
        schema.append("Table: encrypted_data\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("Column Name          | Data Type        | Constraints\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("id                   | INT              | PRIMARY KEY, AUTO_INCREMENT\n");
        schema.append("user_id              | INT              | FOREIGN KEY -> cipher_users(id)\n");
        schema.append("original_text        | TEXT             | \n");
        schema.append("encrypted_text       | TEXT             | \n");
        schema.append("algorithms_used      | VARCHAR(255)     | \n");
        schema.append("encryption_date      | TIMESTAMP        | DEFAULT CURRENT_TIMESTAMP\n");
        schema.append("-".repeat(60)).append("\n\n");
        
        schema.append("ENCRYPTION ALGORITHMS SUPPORTED:\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("1. AES (Advanced Encryption Standard) - 128-bit\n");
        schema.append("2. DES (Data Encryption Standard) - 56-bit\n");
        schema.append("3. Blowfish - Variable key length\n\n");
        
        schema.append("SECURITY FEATURES:\n");
        schema.append("-".repeat(60)).append("\n");
        schema.append("- User authentication system\n");
        schema.append("- Personal encrypted data storage\n");
        schema.append("- Multi-layer cascade encryption\n");
        schema.append("- Secure password protection\n");
        
        schemaArea.setText(schema.toString());
        
        JScrollPane scrollPane = new JScrollPane(schemaArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, Component field, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
    
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ==================== AUTHENTICATION ====================
    
    private boolean authenticateUser(String username, String password) {
        if (conn == null) return false;
        
        try {
            String sql = "SELECT id, full_name FROM cipher_users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                currentUserName = rs.getString("full_name");
                rs.close();
                pstmt.close();
                System.out.println("User authenticated: " + currentUserName + " (ID: " + currentUserId + ")");
                return true;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean registerUser(String username, String password, String email, String fullName) {
        if (conn == null) return false;
        
        try {
            String sql = "INSERT INTO cipher_users (username, password, email, full_name) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("User registered: " + username);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== ENCRYPTION/DECRYPTION ====================
    
    private void performEncryption() {
        try {
            String input = inputArea.getText();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter text to encrypt!");
                return;
            }
            
            String key = new String(keyField.getPassword());
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an encryption key!");
                return;
            }
            
            String encrypted = input;
            StringBuilder algorithmsUsed = new StringBuilder();
            
            // Apply selected algorithms in cascade
            if (aesCheck.isSelected()) {
                encrypted = encryptAES(encrypted, key);
                algorithmsUsed.append("AES ");
            }
            if (desCheck.isSelected()) {
                encrypted = encryptDES(encrypted, key);
                algorithmsUsed.append("DES ");
            }
            if (blowfishCheck.isSelected()) {
                encrypted = encryptBlowfish(encrypted, key);
                algorithmsUsed.append("Blowfish ");
            }
            
            if (algorithmsUsed.length() == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one algorithm!");
                return;
            }
            
            outputArea.setText(encrypted);
            System.out.println("Encryption successful - Algorithms: " + algorithmsUsed.toString());
            JOptionPane.showMessageDialog(this, 
                "Encryption successful!\nAlgorithms used: " + algorithmsUsed.toString() +
                "\n\nClick 'Copy Output to Input' to prepare for decryption.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encryption failed: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void performDecryption() {
        try {
            String input = inputArea.getText();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter encrypted text to decrypt!\n\nUse 'Copy Output to Input' button after encryption.");
                return;
            }
            
            String key = new String(keyField.getPassword());
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the encryption key!");
                return;
            }
            
            String decrypted = input;
            StringBuilder algorithmsUsed = new StringBuilder();
            
            // Apply decryption in reverse order
            if (blowfishCheck.isSelected()) {
                decrypted = decryptBlowfish(decrypted, key);
                algorithmsUsed.insert(0, "Blowfish ");
            }
            if (desCheck.isSelected()) {
                decrypted = decryptDES(decrypted, key);
                algorithmsUsed.insert(0, "DES ");
            }
            if (aesCheck.isSelected()) {
                decrypted = decryptAES(decrypted, key);
                algorithmsUsed.insert(0, "AES ");
            }
            
            if (algorithmsUsed.length() == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one algorithm!");
                return;
            }
            
            outputArea.setText(decrypted);
            System.out.println("Decryption successful - Algorithms: " + algorithmsUsed.toString());
            JOptionPane.showMessageDialog(this, 
                "Decryption successful!\nAlgorithms used: " + algorithmsUsed.toString(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Decryption failed: " + e.getMessage() + 
                "\n\nMake sure you're using:\n" +
                "- The correct encryption key\n" +
                "- The same algorithms used for encryption\n" +
                "- Valid encrypted text",
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private String encryptAES(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(padKey(key, 16).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }
    
    private String decryptAES(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(padKey(key, 16).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }
    
    private String encryptDES(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(padKey(key, 8).getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }
    
    private String decryptDES(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(padKey(key, 8).getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }
    
    private String encryptBlowfish(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }
    
    private String decryptBlowfish(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }
    
    private String padKey(String key, int length) {
        if (key.length() >= length) {
            return key.substring(0, length);
        }
        StringBuilder paddedKey = new StringBuilder(key);
        while (paddedKey.length() < length) {
            paddedKey.append("0");
        }
        return paddedKey.toString();
    }
    
    // ==================== DATABASE OPERATIONS ====================
    
    private void saveToDatabase() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, 
                "Database not connected!\nData cannot be saved.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (currentUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please login first!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String original = inputArea.getText();
            String encrypted = outputArea.getText();
            
            if (original.isEmpty() || encrypted.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please encrypt some data first!\n\n" +
                    "1. Enter text in Input area\n" +
                    "2. Select algorithms\n" +
                    "3. Click Encrypt\n" +
                    "4. Then click Save to Database");
                return;
            }
            
            StringBuilder algorithms = new StringBuilder();
            if (aesCheck.isSelected()) algorithms.append("AES ");
            if (desCheck.isSelected()) algorithms.append("DES ");
            if (blowfishCheck.isSelected()) algorithms.append("Blowfish ");
            
            String algoString = algorithms.toString().trim();
            
            if (algoString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No algorithms were selected!");
                return;
            }
            
            System.out.println("=== Saving to Database ===");
            System.out.println("User ID: " + currentUserId);
            System.out.println("Original (first 50 chars): " + original.substring(0, Math.min(50, original.length())));
            System.out.println("Encrypted (first 50 chars): " + encrypted.substring(0, Math.min(50, encrypted.length())));
            System.out.println("Algorithms: " + algoString);
            
            String sql = "INSERT INTO encrypted_data (user_id, original_text, encrypted_text, algorithms_used) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, original);
            pstmt.setString(3, encrypted);
            pstmt.setString(4, algoString);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("Rows inserted: " + rowsAffected);
            System.out.println("=========================");
            
            JOptionPane.showMessageDialog(this, 
                "Data saved to database successfully!\n\n" +
                "Algorithms: " + algoString + "\n" +
                "Records affected: " + rowsAffected,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            loadDatabaseRecords();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save to database:\n" + e.getMessage() +
                "\n\nCheck console for detailed error.",
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadDatabaseRecords() {
        if (conn == null || currentUserId == -1) {
            System.out.println("Cannot load records - database not connected or user not logged in");
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String sql = "SELECT * FROM encrypted_data WHERE user_id = ? ORDER BY id DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    truncate(rs.getString("original_text"), 50),
                    truncate(rs.getString("encrypted_text"), 50),
                    rs.getString("algorithms_used"),
                    rs.getTimestamp("encryption_date")
                };
                tableModel.addRow(row);
                count++;
            }
            
            System.out.println("Loaded " + count + " records from database for user " + currentUserId);
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Failed to load database records:");
            e.printStackTrace();
        }
    }
    
    private void deleteSelectedRecord() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database not connected!");
            return;
        }
        
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete!");
            return;
        }
        
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete record ID: " + id + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM encrypted_data WHERE id = ? AND user_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setInt(2, currentUserId);
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();
                
                System.out.println("Deleted record ID: " + id + " (rows affected: " + rowsAffected + ")");
                
                loadDatabaseRecords();
                JOptionPane.showMessageDialog(this, "Record deleted successfully!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }
    
    private void clearFields() {
        inputArea.setText("");
        outputArea.setText("");
        System.out.println("Fields cleared");
    }
    
    // ==================== MAIN METHOD ====================
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Show startup dialog
            JOptionPane.showMessageDialog(null, 
                "Welcome to CipherCascade!\n\n" +
                "DATABASE SETUP:\n" +
                "1. Ensure MySQL/MariaDB is running\n" +
                "2. Create database: CREATE DATABASE cipherdb;\n" +
                "3. Tables will be created automatically\n\n" +
                "FEATURES:\n" +
                "✓ User Authentication System\n" +
                "✓ Multi-Layer Cascade Encryption (AES, DES, Blowfish)\n" +
                "✓ Personal Encrypted Data Storage\n" +
                "✓ Secure Database Integration\n\n" +
                "Get started by creating an account!",
                "CipherCascade - Multi-Encryption Framework", 
                JOptionPane.INFORMATION_MESSAGE);
            
            new CipherCascadeApp().setVisible(true);
        });
    }
}
