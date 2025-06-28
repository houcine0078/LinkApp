package Interfaces;

import UI.SplashScreen;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageReader;
import java.util.Iterator;
import UI.RoundedShadowBorder;
import UI.AnimatedBackground;
import UI.ModernShadowBorder;
import services.FirebaseAuthService;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.json.JSONObject;
import okhttp3.*;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class LoginInterface extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel forgotPasswordLabel;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private float alpha = 0.0f;
    private JPanel contentPanel;
    private JCheckBox rememberMeCheckbox;
    private JButton showPasswordButton;
    private boolean passwordVisible = false;
    private Preferences prefs;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private int loginAttempts = 0;
    private Timer lockoutTimer;
    private static final int LOCKOUT_DURATION = 30000; // 30 seconds
    private AnimatedBackground animatedBackground;
    private JButton createAccountButton;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(189, 195, 199);

    public LoginInterface() {
        prefs = Preferences.userNodeForPackage(LoginInterface.class);

        // Set up the frame
        setTitle("LinkApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Set application icon
        try (InputStream iconStream = getClass().getResourceAsStream("/logo_frame.png")) {
            if (iconStream != null) {
                setIconImage(ImageIO.read(iconStream));
            } else {
                System.err.println("Warning: Could not load application icon");
            }
        } catch (IOException e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }

        // Create and add animated background
        animatedBackground = new AnimatedBackground();
        setContentPane(animatedBackground);
        animatedBackground.setLayout(new BorderLayout());

        // Full screen configuration
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Create main container panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);  // Make panel transparent to show animation
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Create logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(0, 0, 40, 0));

        // Logo creation
        JPanel logoImagePanel = createLogoPanel();
        logoImagePanel.setPreferredSize(new Dimension(200, 200));
        logoImagePanel.setMaximumSize(new Dimension(200, 200));
        logoImagePanel.setOpaque(false);

        logoPanel.add(logoImagePanel, BorderLayout.CENTER);

        // Create content panel with modern shadow effect
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new ModernShadowBorder(15),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(400, 600));

        // Status label for animations and feedback
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(ACCENT_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setPreferredSize(new Dimension(300, 30));
        statusLabel.setMinimumSize(new Dimension(300, 30));
        statusLabel.setMaximumSize(new Dimension(300, 30));

        // Form panel with modern styling
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(300, 400));

        // Username field
        JLabel usernameLabel = new JLabel("Email");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field with modern styling
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Forgot password link with modern styling
        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(PRIMARY_COLOR);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotPasswordLabel.setBorder(new EmptyBorder(5, 0, 20, 0));

        // Add hover effect to the forgot password link
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setForeground(PRIMARY_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setForeground(PRIMARY_COLOR);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });

        // Button panel with modern styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(300, 120));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Login button with modern styling
        loginButton = createModernButton("Login", PRIMARY_COLOR, Color.WHITE);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add vertical space between buttons
        buttonPanel.add(Box.createVerticalStrut(20));

        // Register button
        registerButton = createModernButton("Register", SECONDARY_COLOR, Color.WHITE);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setBorder(new EmptyBorder(20, 0, 0, 0)); // Additional spacing

        // Add components to form panel
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(forgotPasswordLabel);

        // Add buttons to button panel
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createVerticalStrut(30)); // Add more vertical space
        buttonPanel.add(registerButton);

        // Create a wrapper panel to center the content panel
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapperPanel.add(contentPanel, gbc);

        // Add components to content panel
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(statusLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(buttonPanel);

        // Add all panels to main panel
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // ENHANCED LOGIN BUTTON ACTION LISTENER
        loginButton.addActionListener(e -> {
            animateButtonClick(loginButton);
            performLogin(); // Call the new enhanced login method
        });

        registerButton.addActionListener(e -> {
            animateButtonClick(registerButton);
            showRegistrationDialog();
        });

        // Add input validation
        addInputValidation();

        // Add fade-in animation when window opens
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                startFadeInAnimation();
            }
        });

        // Add window listener to stop animation when closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // No need to stop animation anymore
            }
        });
    }

    // ENHANCED LOGIN METHOD
    private void performLogin() {
        String email = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Clear any previous error states
        clearErrorStates();

        if (email.isEmpty()) {
            showFieldError(usernameField, "Email is required");
            return;
        }

        if (!isValidEmail(email)) {
            showFieldError(usernameField, "Please enter a valid email address");
            return;
        }

        if (password.isEmpty()) {
            showFieldError(passwordField, "Password is required");
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(PRIMARY_COLOR);

        // Perform login in background thread
        new Thread(() -> {
            try {
                // Check if user is trying to log in with temporary code
                if (isTemporaryPassword(password)) {
                    boolean codeValid = verifyTemporaryCode(email, password);
                    if (codeValid) {
                        SwingUtilities.invokeLater(() -> {
                            resetLoginButton();
                            // Show password change dialog first
                            showPasswordChangeDialog(email);
                        });
                        return;
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            resetLoginButton();
                            showLoginError("Invalid or expired access code. Please request a new one.");
                        });
                        return;
                    }
                }

                // Check if user exists in database first (for better error handling)
                String userResponse = FirebaseAuthService.getUserByEmail(email);
                JSONObject userJson = new JSONObject(userResponse);

                if (userJson.length() == 0) {
                    SwingUtilities.invokeLater(() -> {
                        resetLoginButton();
                        showLoginError("Email or password incorrect.");
                    });
                    return;
                }

                // Check password in database
                boolean passwordMatch = false;
                for (String key : userJson.keySet()) {
                    JSONObject userObj = userJson.getJSONObject(key);
                    String dbPassword = userObj.optString("password", "");
                    if (dbPassword.equals(password)) {
                        passwordMatch = true;
                        break;
                    }
                }

                if (passwordMatch) {
                    // Login successful
                    SwingUtilities.invokeLater(() -> {
                        resetLoginButton();
                        if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
                            saveCredentials(email, password);
                        } else {
                            clearSavedCredentials();
                        }
                        showSuccessAnimation();
                        // Show splash screen for loading chats in a background thread
                        new Thread(() -> {
                            UI.SplashScreen splash = new UI.SplashScreen("Loading chats", 0.4);
                            splash.showSplashWithProgress();
                            // After splash, open chat on EDT
                            SwingUtilities.invokeLater(() -> {
                                new ChatInterface(email).setVisible(true);
                                LoginInterface.this.setVisible(false);
                                LoginInterface.this.dispose();
                            });
                        }).start();
                    });
                } else {
                    // Wrong password
                    SwingUtilities.invokeLater(() -> {
                        resetLoginButton();
                        showLoginError("Email or password incorrect.");
                        loginAttempts++;
                        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                            startLockoutTimer();
                        }
                    });
                }

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resetLoginButton();
                    String errorMessage = parseExceptionError(ex);
                    showLoginError(errorMessage);
                    loginAttempts++;
                    if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                        startLockoutTimer();
                    }
                });
            }
        }).start();
    }

    // Helper methods for login error handling
    private void clearErrorStates() {
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private void showFieldError(JComponent field, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        showLoginError(message);
    }

    private void showLoginError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(ACCENT_COLOR);
        statusLabel.setOpaque(true);
        Color originalBg = statusLabel.getBackground();
        statusLabel.setBackground(new Color(255, 200, 200)); // light red flash
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setBackground(originalBg);
                    statusLabel.setOpaque(false);
                });
            }
        }, 300); // flash for 300ms
    }

    private void resetLoginButton() {
        loginButton.setEnabled(true);
        loginButton.setText("Login");
    }

    private String parseExceptionError(Exception ex) {
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("INVALID_LOGIN_CREDENTIALS") ||
                    message.contains("EMAIL_NOT_FOUND") ||
                    message.contains("INVALID_PASSWORD")) {
                return "Invalid email or password. Please check your credentials and try again.";
            } else if (message.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                return "Too many failed attempts. Please try again later.";
            } else if (message.contains("USER_DISABLED")) {
                return "This account has been disabled. Please contact support.";
            }
        }
        return "Login failed. Please check your internet connection and try again.";
    }

    // ENHANCED FORGOT PASSWORD DIALOG
    private void showForgotPasswordDialog() {
        JDialog forgotDialog = new JDialog(this, "Password Recovery", true);
        forgotDialog.setSize(480, 380);
        forgotDialog.setLocationRelativeTo(this);
        forgotDialog.setLayout(new BorderLayout());
        forgotDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        forgotDialog.setResizable(false);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Reset Your Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("We'll send you a temporary 6-digit access code");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        // Create form panel
        JPanel formPanel = new JPanel(new BorderLayout(0, 15));
        formPanel.setOpaque(false);

        // Email field with proper sizing
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_COLOR);

        // Fixed text field with proper height and visibility
        JTextField emailField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Draw border
                if (hasFocus()) {
                    g2.setColor(PRIMARY_COLOR);
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(TEXT_COLOR);
        emailField.setPreferredSize(new Dimension(400, 50)); // Fixed height for better visibility
        emailField.setMinimumSize(new Dimension(400, 50));
        emailField.setMaximumSize(new Dimension(400, 50));
        emailField.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18)); // Better padding

        // Add placeholder functionality
        final String PLACEHOLDER = "Enter your email address";
        final boolean[] showingPlaceholder = {true};

        emailField.setText(PLACEHOLDER);
        emailField.setForeground(new Color(150, 150, 150));

        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder[0]) {
                    emailField.setText("");
                    emailField.setForeground(TEXT_COLOR);
                    showingPlaceholder[0] = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (emailField.getText().trim().isEmpty()) {
                    emailField.setText(PLACEHOLDER);
                    emailField.setForeground(new Color(150, 150, 150));
                    showingPlaceholder[0] = true;
                }
            }
        });

        // Create status label for feedback
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton sendCodeButton = createStyledButton("Send Access Code", PRIMARY_COLOR, Color.WHITE);
        sendCodeButton.setPreferredSize(new Dimension(180, 45));

        JButton cancelButton = createStyledButton("Cancel", new Color(108, 117, 125), Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(120, 45));

        // Add action listeners
        sendCodeButton.addActionListener(e -> {
            String email = emailField.getText().trim();

            // Validate email
            if (showingPlaceholder[0] || email.isEmpty()) {
                statusLabel.setText("Please enter your email address");
                statusLabel.setForeground(Color.RED);
                return;
            }

            if (!isValidEmail(email)) {
                statusLabel.setText("Please enter a valid email address");
                statusLabel.setForeground(Color.RED);
                return;
            }

            // Check if email exists and send code
            checkEmailAndSendCode(email, sendCodeButton, statusLabel, forgotDialog);
        });

        cancelButton.addActionListener(e -> forgotDialog.dispose());

        // Allow Enter key to trigger send
        emailField.addActionListener(e -> sendCodeButton.doClick());

        buttonPanel.add(sendCodeButton);
        buttonPanel.add(cancelButton);

        // Add components to panels
        JPanel emailContainer = new JPanel(new BorderLayout(0, 8));
        emailContainer.setOpaque(false);
        emailContainer.add(emailLabel, BorderLayout.NORTH);
        emailContainer.add(emailField, BorderLayout.CENTER);

        formPanel.add(emailContainer, BorderLayout.NORTH);
        formPanel.add(statusLabel, BorderLayout.CENTER);

        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to dialog
        forgotDialog.add(mainPanel);
        forgotDialog.setVisible(true);
    }

    // Method to check email and send temporary code
    private void checkEmailAndSendCode(String email, JButton sendButton, JLabel statusLabel, JDialog dialog) {
        // Disable button and show loading state
        sendButton.setEnabled(false);
        sendButton.setText("Checking...");
        statusLabel.setText("Verifying email address...");
        statusLabel.setForeground(PRIMARY_COLOR);

        new Thread(() -> {
            try {
                // Check if email exists in database
                String response = FirebaseAuthService.getUserByEmail(email);
                JSONObject userResponse = new JSONObject(response);

                if (userResponse.length() == 0) {
                    // Email not found
                    SwingUtilities.invokeLater(() -> {
                        sendButton.setEnabled(true);
                        sendButton.setText("Send Access Code");
                        statusLabel.setText("No account found with this email address");
                        statusLabel.setForeground(Color.RED);
                    });
                    return;
                }

                // Generate temporary 6-digit code
                String tempCode = generateTemporaryCode();

                // Store temporary code in Firebase (with expiration)
                storeTemporaryCode(email, tempCode);

                // Simulate sending email with the code (you can implement actual email sending)
                sendCodeEmail(email, tempCode);

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Access code sent successfully!");
                    statusLabel.setForeground(new Color(0, 150, 0));

                    dialog.dispose();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    sendButton.setEnabled(true);
                    sendButton.setText("Send Access Code");
                    statusLabel.setText("Failed to send code. Please try again.");
                    statusLabel.setForeground(Color.RED);
                });
            }
        }).start();
    }

    // Generate 6-digit temporary code
    private String generateTemporaryCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    // Store temporary code in Firebase with expiration (1 hour)
    private void storeTemporaryCode(String email, String code) throws Exception {
        String url = FirebaseAuthService.getDatabaseUrl() + "/temp_codes/" +
                email.replace(".", "_").replace("@", "_") + ".json";

        long expirationTime = System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour

        JSONObject codeData = new JSONObject();
        codeData.put("code", code);
        codeData.put("email", email);
        codeData.put("expiresAt", expirationTime);
        codeData.put("used", false);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(codeData.toString(),
                MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).put(body).build();

        Response response = client.newCall(request).execute();
        response.close();
    }

    // Check if password is temporary code
    private boolean isTemporaryPassword(String password) {
        return password.matches("\\d{6}"); // 6 digits
    }

    // Verify temporary code during login
    private boolean verifyTemporaryCode(String email, String code) throws Exception {
        String url = FirebaseAuthService.getDatabaseUrl() + "/temp_codes/" +
                email.replace(".", "_").replace("@", "_") + ".json";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        if (responseBody.equals("null")) {
            return false;
        }

        JSONObject codeData = new JSONObject(responseBody);
        String storedCode = codeData.optString("code", "");
        long expirationTime = codeData.optLong("expiresAt", 0);
        boolean used = codeData.optBoolean("used", true);

        // Check if code matches, hasn't expired, and hasn't been used
        if (code.equals(storedCode) &&
                System.currentTimeMillis() < expirationTime &&
                !used) {

            // Mark code as used
            codeData.put("used", true);
            RequestBody body = RequestBody.create(codeData.toString(),
                    MediaType.parse("application/json; charset=utf-8"));
            Request updateRequest = new Request.Builder().url(url).put(body).build();
            client.newCall(updateRequest).execute();

            return true;
        }

        return false;
    }

    // Send email with temporary code (placeholder - implement with actual email service)
    private void sendCodeEmail(String email, String code) {

        final String username = "linkapp.java@gmail.com"; // your email
        final String password = "avdb xrdx xbse glav";    // your app password (not your Gmail password!)

        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        jakarta.mail.Session session = jakarta.mail.Session.getInstance(props,
            new jakarta.mail.Authenticator() {
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(username, password);
                }
            });

        try {
            jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(session);
            message.setFrom(new jakarta.mail.internet.InternetAddress(username));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(email));
            message.setSubject("Your LinkApp Access Code");
            message.setText("Your 6-digit access code is: " + code + "\n\nThis code will expire in 1 hour.");

            jakarta.mail.Transport.send(message);

            System.out.println("Access code email sent to " + email);

        } catch (jakarta.mail.MessagingException e) {
            e.printStackTrace();
            // Optionally, handle error (e.g., show a message to the user)
        }
    }

    // Password Change Dialog (shown after login with temporary code)
    private void showPasswordChangeDialog(String email) {
        JDialog changeDialog = new JDialog(this, "Set New Password", true);
        changeDialog.setSize(450, 400);
        changeDialog.setLocationRelativeTo(this);
        changeDialog.setLayout(new BorderLayout());
        changeDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        changeDialog.setResizable(false);
        changeDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Force password change

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Set Your New Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Please create a secure password for your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        formPanel.setOpaque(false);

        // New password field
        JPanel newPassPanel = new JPanel(new BorderLayout(0, 5));
        newPassPanel.setOpaque(false);
        JLabel newPassLabel = new JLabel("New Password");
        newPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        newPasswordField.setPreferredSize(new Dimension(350, 40));
        newPasswordField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        newPassPanel.add(newPassLabel, BorderLayout.NORTH);
        newPassPanel.add(newPasswordField, BorderLayout.CENTER);

        // Confirm password field
        JPanel confirmPassPanel = new JPanel(new BorderLayout(0, 5));
        confirmPassPanel.setOpaque(false);
        JLabel confirmPassLabel = new JLabel("Confirm Password");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        confirmPasswordField.setPreferredSize(new Dimension(350, 40));
        confirmPasswordField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        confirmPassPanel.add(confirmPassLabel, BorderLayout.NORTH);
        confirmPassPanel.add(confirmPasswordField, BorderLayout.CENTER);

        formPanel.add(newPassPanel);
        formPanel.add(confirmPassPanel);

        // Status label
        JLabel changeStatusLabel = new JLabel(" ");
        changeStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton updateButton = createStyledButton("Update Password", PRIMARY_COLOR, Color.WHITE);
        updateButton.setPreferredSize(new Dimension(200, 45));

        updateButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.length() < 6) {
                changeStatusLabel.setText("Password must be at least 6 characters long");
                changeStatusLabel.setForeground(Color.RED);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                changeStatusLabel.setText("Passwords do not match");
                changeStatusLabel.setForeground(Color.RED);
                return;
            }

            // Update password
            updateUserPassword(email, newPassword, updateButton, changeStatusLabel, changeDialog);
        });

        buttonPanel.add(updateButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(changeStatusLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        changeDialog.add(mainPanel);
        changeDialog.setVisible(true);
    }

    // Update user password in database
    private void updateUserPassword(String email, String newPassword, JButton button, JLabel statusLabel, JDialog dialog) {
        button.setEnabled(false);
        button.setText("Updating...");

        new Thread(() -> {
            try {
                // Get user data first
                String response = FirebaseAuthService.getUserByEmail(email);
                JSONObject userResponse = new JSONObject(response);

                if (userResponse.length() > 0) {
                    // Get the user key and update password
                    String userKey = userResponse.keys().next();
                    JSONObject userData = userResponse.getJSONObject(userKey);

                    // Update the password in the user data
                    userData.put("password", newPassword);

                    // Store updated user data back to Firebase
                    String updateUrl = FirebaseAuthService.getDatabaseUrl() + "/users/" + userKey + ".json";
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(userData.toString(),
                            MediaType.parse("application/json; charset=utf-8"));
                    Request request = new Request.Builder().url(updateUrl).put(body).build();

                    Response updateResponse = client.newCall(request).execute();

                    if (updateResponse.isSuccessful()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Password updated successfully!");
                            statusLabel.setForeground(new Color(0, 150, 0));

                            // Close dialog and proceed to chat after a short delay
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    SwingUtilities.invokeLater(() -> {
                                        dialog.dispose();
                                        new ChatInterface(email).setVisible(true);
                                        LoginInterface.this.setVisible(false);
                                        LoginInterface.this.dispose();
                                    });
                                }
                            }, 1500);
                        });
                    } else {
                        throw new Exception("Failed to update password");
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    button.setEnabled(true);
                    button.setText("Update Password");
                    statusLabel.setText("Failed to update password. Please try again.");
                    statusLabel.setForeground(Color.RED);
                });
            }
        }).start();
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Helper method to create styled buttons for dialogs
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient effect
                GradientPaint gp = new GradientPaint(
                        0, 0, bgColor.brighter(),
                        0, getHeight(), bgColor.darker()
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                super.paintComponent(g2);
                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    // EXISTING METHODS FROM YOUR ORIGINAL CODE (unchanged)

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BorderLayout());
        logoPanel.setPreferredSize(new Dimension(300, 300));
        logoPanel.setMaximumSize(new Dimension(300, 300));

        // Load image from resources folder
        try (InputStream imageStream = getClass().getResourceAsStream("/logo_LinkApp.png")) {
            if (imageStream == null) {
                throw new IOException("Logo image not found");
            }

            // Use ImageIO.read with better quality settings
            ImageInputStream iis = ImageIO.createImageInputStream(imageStream);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IOException("No image reader found");
            }

            ImageReader reader = readers.next();
            reader.setInput(iis);

            // Get the image dimensions
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            // Create a high-quality scaled image
            BufferedImage originalImage = reader.read(0);
            Image scaledImage = getHighQualityScaledImage(originalImage, 200, 200);

            // Create a high-quality ImageIcon
            ImageIcon logoIcon = new ImageIcon(scaledImage);

            // Create JLabel for the logo with high-quality rendering
            JLabel logoLabel = new JLabel(logoIcon) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    // Enable high-quality rendering
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                    super.paintComponent(g2d);
                    g2d.dispose();
                }
            };

            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            logoLabel.setVerticalAlignment(JLabel.CENTER);

            // Add to panel
            logoPanel.add(logoLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();

            // Fallback: error message
            JLabel errorLabel = new JLabel("Logo not found");
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            errorLabel.setForeground(PRIMARY_COLOR);
            errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            logoPanel.add(errorLabel, BorderLayout.CENTER);
        }

        return logoPanel;
    }

    private Image getHighQualityScaledImage(Image srcImg, int width, int height) {
        // Create a high-quality scaled image
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        // Set high-quality rendering hints
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Draw the image with high quality
        g2.drawImage(srcImg, 0, 0, width, height, null);
        g2.dispose();

        return resizedImg;
    }

    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw button background with gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, bgColor,
                        0, getHeight(), bgColor.darker()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Draw button text
                g2.setColor(fgColor);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(bgColor.darker());
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(bgColor);
                button.repaint();
            }
        });

        return button;
    }

    private void animateButtonClick(JButton button) {
        Color originalColor = button.getBackground();
        button.setBackground(originalColor.darker());
        button.repaint();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    button.setBackground(originalColor);
                    button.repaint();
                });
            }
        }, 100);
    }

    private void showErrorAnimation(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(ACCENT_COLOR);

        // Shake animation
        final Point originalLocation = contentPanel.getLocation();
        final int SHAKE_DISTANCE = 10;
        final int TOTAL_FRAMES = 8;
        final int DELAY = 50;

        Timer shakeTimer = new Timer();
        shakeTimer.schedule(new TimerTask() {
            int currentFrame = 0;

            @Override
            public void run() {
                if (currentFrame > TOTAL_FRAMES) {
                    shakeTimer.cancel();
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setLocation(originalLocation);
                    });
                    return;
                }

                int offset = (int)(SHAKE_DISTANCE * Math.sin(currentFrame * Math.PI) *
                        (TOTAL_FRAMES - currentFrame) / TOTAL_FRAMES);

                SwingUtilities.invokeLater(() -> {
                    contentPanel.setLocation(originalLocation.x + offset, originalLocation.y);
                });

                currentFrame++;
            }
        }, 0, DELAY);
    }

    private void startAuthenticatingAnimation() {
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(PRIMARY_COLOR);

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    private void stopAuthenticatingAnimation() {
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
    }

    private boolean authenticateUser(String email, String password) {
        try {
            String response = FirebaseAuthService.getUserByEmail(email);
            System.out.println("Firebase response: " + response);

            org.json.JSONObject json;
            try {
                json = new org.json.JSONObject(response);
            } catch (org.json.JSONException e) {
                showErrorAnimation("Erreur de réponse Firebase.");
                return false;
            }

            if (json.has("error")) {
                Object errorObj = json.get("error");
                showErrorAnimation("Erreur Firebase: " + errorObj.toString());
                return false;
            }

            if (json.length() == 0) {
                showErrorAnimation("Mot de passe ou email incorrecte");
                return false;
            }

            for (String key : json.keySet()) {
                org.json.JSONObject userObj = json.getJSONObject(key);
                String dbPassword = userObj.optString("password", "");
                if (dbPassword.equals(password)) {
                    SwingUtilities.invokeLater(() -> {
                        new ChatInterface(email).setVisible(true);
                        this.setVisible(false);
                        this.dispose();
                    });
                    return true;
                }
            }
            showErrorAnimation("Mot de passe ou email incorrecte");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAnimation("Erreur lors de la connexion");
            return false;
        }
    }

    private void showSuccessAnimation() {
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15, new Color(50, 205, 50, 80), 1, Color.WHITE),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        statusLabel.setText("Login successful!");
        statusLabel.setForeground(new Color(50, 205, 50));

        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
    }

    private void showFailureAnimation() {
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15, ACCENT_COLOR, 1, Color.WHITE),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        statusLabel.setText("Invalid email or password");
        statusLabel.setForeground(ACCENT_COLOR);

        final Point originalLocation = contentPanel.getLocation();
        final int SHAKE_DISTANCE = 10;
        final int TOTAL_FRAMES = 8;
        final int DELAY = 50;

        Timer shakeTimer = new Timer();
        shakeTimer.schedule(new TimerTask() {
            int currentFrame = 0;

            @Override
            public void run() {
                if (currentFrame > TOTAL_FRAMES) {
                    shakeTimer.cancel();
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setLocation(originalLocation);
                    });
                    return;
                }

                int offset = (int)(SHAKE_DISTANCE * Math.sin(currentFrame * Math.PI) *
                        (TOTAL_FRAMES - currentFrame) / TOTAL_FRAMES);

                SwingUtilities.invokeLater(() -> {
                    contentPanel.setLocation(originalLocation.x + offset, originalLocation.y);
                });

                currentFrame++;
            }
        }, 0, DELAY);
    }

    private void startFadeInAnimation() {
        alpha = 0.0f;
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(255, 255, 255, (int)(alpha * 255)));
        contentPanel.repaint();
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("👁");
        } else {
            passwordField.setEchoChar('•');
            showPasswordButton.setText("👁");
        }
    }

    private void loadSavedCredentials() {
        String savedEmail = prefs.get("email", "");
        String savedPassword = prefs.get("password", "");
        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            usernameField.setText(savedEmail);
            passwordField.setText(savedPassword);
            if (rememberMeCheckbox != null) {
                rememberMeCheckbox.setSelected(true);
            }
        }
    }

    private void saveCredentials(String email, String password) {
        prefs.put("email", email);
        prefs.put("password", password);
    }

    private void clearSavedCredentials() {
        prefs.remove("email");
        prefs.remove("password");
    }

    private void addInputValidation() {
        usernameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        });

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        });
    }

    private void validateInput() {
        String email = usernameField.getText();
        String password = new String(passwordField.getPassword());

        boolean isValid = !email.isEmpty() && !password.isEmpty();
        loginButton.setEnabled(isValid);

        if (isValid) {
            loginButton.setBackground(PRIMARY_COLOR);
        } else {
            loginButton.setBackground(Color.GRAY);
        }
    }

    private void startLockoutTimer() {
        if (lockoutTimer != null) {
            lockoutTimer.cancel();
        }

        lockoutTimer = new Timer();
        lockoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    loginAttempts = 0;
                    statusLabel.setText("");
                    statusLabel.setForeground(PRIMARY_COLOR);
                    loginButton.setEnabled(true);
                });
            }
        }, LOCKOUT_DURATION);
        showLockoutMessage();
    }

    private void showLockoutMessage() {
        long remainingTime = LOCKOUT_DURATION / 1000;
        statusLabel.setText("Too many failed attempts. Please wait " + remainingTime + " seconds.");
        statusLabel.setForeground(ACCENT_COLOR);
        loginButton.setEnabled(false);
    }

    private void showRegistrationDialog() {
        new RegisterInterface(this);
    }

    // Replace entire main method with:
    public static void main(String[] args) {
        UI.SplashScreen splash = new SplashScreen();
        splash.showSplashWithProgress();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            LoginInterface loginUI = new LoginInterface();
            loginUI.setVisible(true);
        });
    }
}