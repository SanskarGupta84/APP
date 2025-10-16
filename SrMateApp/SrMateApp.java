
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class SrMateApp extends JFrame {
    private Connection conn;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int currentUserId = -1;
    private String currentUserName = "";
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/srmatedb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    // Profile card components
    private JLabel profileNameLabel, profileAgeLabel, profileInterestsLabel;
    private JLabel profileLookingForLabel, profileBioLabel;
    private JPanel profileImagePanel;
    private List<UserProfile> suggestedProfiles;
    private int currentProfileIndex = 0;
    
    public SrMateApp() {
        setTitle("srMATE - Find Your College Connection");
        setSize(900, 700);
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
                "\n\nPlease ensure MySQL is running and database 'srmatedb' exists.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Users table
            String usersTable = 
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "registration_number VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "age INT," +
                "gender VARCHAR(20)," +
                "college VARCHAR(100)," +
                "department VARCHAR(100)," +
                "year_of_study INT," +
                "interests TEXT," +
                "hobbies TEXT," +
                "looking_for VARCHAR(100)," +
                "bio TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(usersTable);
            
            // Matches table
            String matchesTable = 
                "CREATE TABLE IF NOT EXISTS matches (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "matched_user_id INT," +
                "action VARCHAR(20)," +
                "matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id)," +
                "FOREIGN KEY (matched_user_id) REFERENCES users(id))";
            stmt.executeUpdate(matchesTable);
            
            // Messages table
            String messagesTable = 
                "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender_id INT," +
                "receiver_id INT," +
                "message TEXT," +
                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (sender_id) REFERENCES users(id)," +
                "FOREIGN KEY (receiver_id) REFERENCES users(id))";
            stmt.executeUpdate(messagesTable);
            
            stmt.close();
            System.out.println("Database tables created successfully!");
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
        mainPanel.add(createProfileScreen(), "PROFILE");
        mainPanel.add(createMatchesScreen(), "MATCHES");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "WELCOME");
    }
    
    // ==================== WELCOME SCREEN ====================
    private JPanel createWelcomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 87, 87));
        
        // Center content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Logo/Title
        JLabel titleLabel = new JLabel("srMATE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("Find Your Perfect College Connection");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        centerPanel.add(subtitleLabel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        buttonPanel.setOpaque(false);
        
        JButton loginBtn = createStyledButton("LOGIN", new Color(255, 255, 255), new Color(255, 87, 87));
        loginBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        
        JButton registerBtn = createStyledButton("CREATE ACCOUNT", new Color(255, 255, 255), new Color(255, 87, 87));
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
        headerPanel.setBackground(new Color(255, 87, 87));
        headerPanel.setPreferredSize(new Dimension(900, 80));
        JLabel headerLabel = new JLabel("Welcome Back!");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField emailField = new JTextField(25);
        JPasswordField passwordField = new JPasswordField(25);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("College Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        
        JButton loginBtn = createStyledButton("LOGIN", new Color(255, 87, 87), Color.WHITE);
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            if (authenticateUser(email, password)) {
                loadSuggestedProfiles();
                cardLayout.show(mainPanel, "PROFILE");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        formPanel.add(loginBtn, gbc);
        
        JButton backBtn = createStyledButton("BACK", new Color(200, 200, 200), Color.WHITE);
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
        headerPanel.setBackground(new Color(255, 87, 87));
        headerPanel.setPreferredSize(new Dimension(900, 80));
        JLabel headerLabel = new JLabel("Create Your Profile");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Form with scroll
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField regNumberField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField ageField = new JTextField(20);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField collegeField = new JTextField(20);
        JTextField deptField = new JTextField(20);
        JComboBox<String> yearCombo = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        JTextField interestsField = new JTextField(20);
        JTextField hobbiesField = new JTextField(20);
        JComboBox<String> lookingForCombo = new JComboBox<>(new String[]{
            "Study Partner", "Friend", "Dating", "Networking", "Any"
        });
        JTextArea bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        
        int row = 0;
        addFormField(formPanel, gbc, "Name:", nameField, row++);
        addFormField(formPanel, gbc, "College Email:", emailField, row++);
        addFormField(formPanel, gbc, "Registration Number:", regNumberField, row++);
        addFormField(formPanel, gbc, "Password:", passwordField, row++);
        addFormField(formPanel, gbc, "Age:", ageField, row++);
        addFormField(formPanel, gbc, "Gender:", genderCombo, row++);
        addFormField(formPanel, gbc, "College:", collegeField, row++);
        addFormField(formPanel, gbc, "Department:", deptField, row++);
        addFormField(formPanel, gbc, "Year of Study:", yearCombo, row++);
        addFormField(formPanel, gbc, "Interests (comma-separated):", interestsField, row++);
        addFormField(formPanel, gbc, "Hobbies (comma-separated):", hobbiesField, row++);
        addFormField(formPanel, gbc, "Looking For:", lookingForCombo, row++);
        addFormField(formPanel, gbc, "Bio:", bioScroll, row++);
        
        JButton registerBtn = createStyledButton("CREATE ACCOUNT", new Color(255, 87, 87), Color.WHITE);
        registerBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String regNumber = regNumberField.getText().trim();
                String password = new String(passwordField.getPassword());
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = (String) genderCombo.getSelectedItem();
                String college = collegeField.getText().trim();
                String dept = deptField.getText().trim();
                int year = Integer.parseInt((String) yearCombo.getSelectedItem());
                String interests = interestsField.getText().trim();
                String hobbies = hobbiesField.getText().trim();
                String lookingFor = (String) lookingForCombo.getSelectedItem();
                String bio = bioArea.getText().trim();
                
                if (name.isEmpty() || email.isEmpty() || regNumber.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all required fields!");
                    return;
                }
                
                if (registerUser(name, email, regNumber, password, age, gender, college, dept, 
                                year, interests, hobbies, lookingFor, bio)) {
                    JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                    cardLayout.show(mainPanel, "LOGIN");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed! Email or Registration Number may already exist.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid age and year!");
            }
        });
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(registerBtn, gbc);
        
        JButton backBtn = createStyledButton("BACK", new Color(200, 200, 200), Color.WHITE);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        gbc.gridy = row + 1;
        gbc.insets = new Insets(10, 10, 20, 10);
        formPanel.add(backBtn, gbc);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== PROFILE SWIPE SCREEN ====================
    private JPanel createProfileScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(255, 87, 87));
        topBar.setPreferredSize(new Dimension(900, 70));
        
        JLabel logoLabel = new JLabel("  srMATE");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        topBar.add(logoLabel, BorderLayout.WEST);
        
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtonPanel.setOpaque(false);
        
        JButton matchesBtn = createIconButton("💬 Matches");
        matchesBtn.addActionListener(e -> {
            loadMatchesScreen();
            cardLayout.show(mainPanel, "MATCHES");
        });
        
        JButton logoutBtn = createIconButton("🚪 Logout");
        logoutBtn.addActionListener(e -> {
            currentUserId = -1;
            currentUserName = "";
            cardLayout.show(mainPanel, "WELCOME");
        });
        
        topButtonPanel.add(matchesBtn);
        topButtonPanel.add(logoutBtn);
        topBar.add(topButtonPanel, BorderLayout.EAST);
        
        panel.add(topBar, BorderLayout.NORTH);
        
        // Profile card
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new CompoundBorder(
            new EmptyBorder(30, 50, 30, 50),
            new LineBorder(new Color(230, 230, 230), 2, true)
        ));
        
        // Profile image placeholder
        profileImagePanel = new JPanel();
        profileImagePanel.setBackground(new Color(255, 87, 87));
        profileImagePanel.setPreferredSize(new Dimension(300, 300));
        JLabel imageLabel = new JLabel("👤", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Arial", Font.PLAIN, 120));
        imageLabel.setForeground(Color.WHITE);
        profileImagePanel.add(imageLabel);
        
        // Profile info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        profileNameLabel = new JLabel("Name");
        profileNameLabel.setFont(new Font("Arial", Font.BOLD, 32));
        profileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        profileAgeLabel = new JLabel("Age");
        profileAgeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        profileAgeLabel.setForeground(Color.GRAY);
        profileAgeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        profileLookingForLabel = new JLabel("Looking for");
        profileLookingForLabel.setFont(new Font("Arial", Font.BOLD, 16));
        profileLookingForLabel.setForeground(new Color(255, 87, 87));
        profileLookingForLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        profileInterestsLabel = new JLabel("Interests");
        profileInterestsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profileInterestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        profileBioLabel = new JLabel("<html>Bio</html>");
        profileBioLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profileBioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(profileNameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(profileAgeLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(profileLookingForLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("Interests:"));
        infoPanel.add(profileInterestsLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("About:"));
        infoPanel.add(profileBioLabel);
        
        JPanel cardContentPanel = new JPanel(new BorderLayout());
        cardContentPanel.setBackground(Color.WHITE);
        cardContentPanel.add(profileImagePanel, BorderLayout.NORTH);
        cardContentPanel.add(infoPanel, BorderLayout.CENTER);
        
        cardPanel.add(cardContentPanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        actionPanel.setBackground(new Color(245, 245, 245));
        
        JButton rejectBtn = createRoundButton("❌", new Color(255, 100, 100));
        rejectBtn.setPreferredSize(new Dimension(80, 80));
        rejectBtn.addActionListener(e -> handleSwipe("REJECT"));
        
        JButton likeBtn = createRoundButton("❤️", new Color(100, 255, 100));
        likeBtn.setPreferredSize(new Dimension(80, 80));
        likeBtn.addActionListener(e -> handleSwipe("LIKE"));
        
        actionPanel.add(rejectBtn);
        actionPanel.add(likeBtn);
        
        panel.add(cardPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ==================== MATCHES SCREEN ====================
    private JPanel createMatchesScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 87, 87));
        headerPanel.setPreferredSize(new Dimension(900, 70));
        
        JButton backBtn = createIconButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "PROFILE"));
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel headerLabel = new JLabel("Your Matches");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Matches list
        JPanel matchesPanel = new JPanel();
        matchesPanel.setLayout(new BoxLayout(matchesPanel, BoxLayout.Y_AXIS));
        matchesPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(matchesPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, Component field, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
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
    
    private JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 87, 87));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(10, 20, 10, 20));
        return btn;
    }
    
    private JButton createRoundButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 32));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ==================== DATABASE OPERATIONS ====================
    
    private boolean authenticateUser(String email, String password) {
        if (conn == null) return false;
        
        try {
            String sql = "SELECT id, name FROM users WHERE email = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                currentUserName = rs.getString("name");
                rs.close();
                pstmt.close();
                return true;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean registerUser(String name, String email, String regNumber, String password,
                                 int age, String gender, String college, String dept, int year,
                                 String interests, String hobbies, String lookingFor, String bio) {
        if (conn == null) return false;
        
        try {
            String sql = "INSERT INTO users (name, email, registration_number, password, age, gender, " +
                        "college, department, year_of_study, interests, hobbies, looking_for, bio) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, regNumber);
            pstmt.setString(4, password);
            pstmt.setInt(5, age);
            pstmt.setString(6, gender);
            pstmt.setString(7, college);
            pstmt.setString(8, dept);
            pstmt.setInt(9, year);
            pstmt.setString(10, interests);
            pstmt.setString(11, hobbies);
            pstmt.setString(12, lookingFor);
            pstmt.setString(13, bio);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void loadSuggestedProfiles() {
        if (conn == null) return;
        
        suggestedProfiles = new ArrayList<>();
        try {
            // Get profiles that user hasn't interacted with yet
            String sql = "SELECT * FROM users WHERE id != ? AND id NOT IN " +
                        "(SELECT matched_user_id FROM matches WHERE user_id = ?) LIMIT 50";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                UserProfile profile = new UserProfile();
                profile.id = rs.getInt("id");
                profile.name = rs.getString("name");
                profile.age = rs.getInt("age");
                profile.gender = rs.getString("gender");
                profile.interests = rs.getString("interests");
                profile.hobbies = rs.getString("hobbies");
                profile.lookingFor = rs.getString("looking_for");
                profile.bio = rs.getString("bio");
                profile.college = rs.getString("college");
                profile.department = rs.getString("department");
                suggestedProfiles.add(profile);
            }
            
            rs.close();
            pstmt.close();
            
            currentProfileIndex = 0;
            if (!suggestedProfiles.isEmpty()) {
                displayProfile(suggestedProfiles.get(0));
            } else {
                displayNoMoreProfiles();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void displayProfile(UserProfile profile) {
        profileNameLabel.setText(profile.name + ", " + profile.age);
        profileAgeLabel.setText(profile.college + " • " + profile.department);
        profileLookingForLabel.setText("🎯 Looking for: " + profile.lookingFor);
        profileInterestsLabel.setText(profile.interests != null ? profile.interests : "Not specified");
        profileBioLabel.setText("<html>" + (profile.bio != null ? profile.bio : "No bio available") + "</html>");
    }
    
    private void displayNoMoreProfiles() {
        profileNameLabel.setText("No more profiles!");
        profileAgeLabel.setText("Check back later for new matches");
        profileLookingForLabel.setText("");
        profileInterestsLabel.setText("");
        profileBioLabel.setText("<html>You've seen all available profiles. Check your matches!</html>");
    }
    
    private void handleSwipe(String action) {
        if (suggestedProfiles == null || suggestedProfiles.isEmpty() || 
            currentProfileIndex >= suggestedProfiles.size()) {
            return;
        }
        
        UserProfile currentProfile = suggestedProfiles.get(currentProfileIndex);
        
        // Save the action to database
        saveMatch(currentUserId, currentProfile.id, action);
        
        // Check if it's a mutual match
        if (action.equals("LIKE") && checkMutualMatch(currentProfile.id, currentUserId)) {
            JOptionPane.showMessageDialog(this, 
                "🎉 It's a Match! You both liked each other!\n\nYou can now chat with " + currentProfile.name,
                "New Match!", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Move to next profile
        currentProfileIndex++;
        if (currentProfileIndex < suggestedProfiles.size()) {
            displayProfile(suggestedProfiles.get(currentProfileIndex));
        } else {
            displayNoMoreProfiles();
        }
    }
    
    private void saveMatch(int userId, int matchedUserId, String action) {
        if (conn == null) return;
        
        try {
            String sql = "INSERT INTO matches (user_id, matched_user_id, action) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, matchedUserId);
            pstmt.setString(3, action);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean checkMutualMatch(int userId, int currentUserId) {
        if (conn == null) return false;
        
        try {
            String sql = "SELECT * FROM matches WHERE user_id = ? AND matched_user_id = ? AND action = 'LIKE'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            boolean hasMatch = rs.next();
            rs.close();
            pstmt.close();
            return hasMatch;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void loadMatchesScreen() {
        if (conn == null) return;
        
        // Get the matches panel
        JPanel matchesScreenPanel = (JPanel) mainPanel.getComponent(4); // MATCHES screen
        JScrollPane scrollPane = (JScrollPane) matchesScreenPanel.getComponent(1);
        JPanel matchesPanel = (JPanel) scrollPane.getViewport().getView();
        matchesPanel.removeAll();
        
        try {
            // Get mutual matches
            String sql = "SELECT u.* FROM users u " +
                        "INNER JOIN matches m1 ON u.id = m1.matched_user_id " +
                        "INNER JOIN matches m2 ON u.id = m2.user_id " +
                        "WHERE m1.user_id = ? AND m1.action = 'LIKE' " +
                        "AND m2.matched_user_id = ? AND m2.action = 'LIKE'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            boolean hasMatches = false;
            while (rs.next()) {
                hasMatches = true;
                JPanel matchCard = createMatchCard(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("college"),
                    rs.getString("interests"),
                    rs.getString("bio")
                );
                matchesPanel.add(matchCard);
                matchesPanel.add(Box.createVerticalStrut(10));
            }
            
            if (!hasMatches) {
                JLabel noMatchesLabel = new JLabel("No matches yet! Keep swiping!");
                noMatchesLabel.setFont(new Font("Arial", Font.BOLD, 20));
                noMatchesLabel.setForeground(Color.GRAY);
                noMatchesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                matchesPanel.add(Box.createVerticalStrut(100));
                matchesPanel.add(noMatchesLabel);
            }
            
            rs.close();
            pstmt.close();
            
            matchesPanel.revalidate();
            matchesPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createMatchCard(int userId, String name, int age, String college, 
                                   String interests, String bio) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(800, 150));
        
        // Profile icon
        JPanel iconPanel = new JPanel();
        iconPanel.setBackground(new Color(255, 87, 87));
        iconPanel.setPreferredSize(new Dimension(80, 80));
        JLabel iconLabel = new JLabel("👤", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(name + ", " + age);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel collegeLabel = new JLabel(college);
        collegeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        collegeLabel.setForeground(Color.GRAY);
        
        JLabel interestsLabel = new JLabel("Interests: " + (interests != null ? interests : "Not specified"));
        interestsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(collegeLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(interestsLabel);
        
        // Chat button
        JButton chatBtn = new JButton("💬 Chat");
        chatBtn.setFont(new Font("Arial", Font.BOLD, 14));
        chatBtn.setBackground(new Color(255, 87, 87));
        chatBtn.setForeground(Color.WHITE);
        chatBtn.setFocusPainted(false);
        chatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chatBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Chat feature coming soon!\n\nYou matched with " + name + "!",
                "Chat", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(chatBtn);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);
        
        return card;
    }
    
    // ==================== INNER CLASS ====================
    
    class UserProfile {
        int id;
        String name;
        int age;
        String gender;
        String college;
        String department;
        String interests;
        String hobbies;
        String lookingFor;
        String bio;
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
                "Welcome to srMATE!\n\n" +
                "DATABASE SETUP INSTRUCTIONS:\n" +
                "1. Ensure MySQL is running\n" +
                "2. Create database: CREATE DATABASE srmatedb;\n" +
                "3. Tables will be created automatically\n\n" +
                "Features:\n" +
                "✓ User Registration with College Email\n" +
                "✓ Tinder-style Swipe Interface\n" +
                "✓ Interest-based Matching\n" +
                "✓ Mutual Match Notifications\n" +
                "✓ View Your Matches\n\n" +
                "Ready to find your perfect college connection?",
                "srMATE Setup", 
                JOptionPane.INFORMATION_MESSAGE);
            
            new SrMateApp().setVisible(true);
        });
    }
}
