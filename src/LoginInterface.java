import UI.SplashScreen;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
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
        try (InputStream iconStream = getClass().getResourceAsStream("/ressources/logo_frame.png")) {
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

        // Form panel with modern styling
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(300, 120));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Login button with modern styling
        loginButton = createModernButton("Login", PRIMARY_COLOR, Color.WHITE);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add vertical space between buttons
        buttonPanel.add(Box.createVerticalStrut(20));

        // Register button with modern styling
        registerButton = createModernButton("Register", SECONDARY_COLOR, Color.WHITE);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setBorder(new EmptyBorder(20, 0, 0, 0)); // Additional spacing

        // Add components to form panel
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
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
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        // Add all panels to main panel
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        loginButton.addActionListener(e -> {
            animateButtonClick(loginButton);

            String email = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                showErrorAnimation("Please enter both email and password");
                return;
            }

            // Show "authenticating" animation
            startAuthenticatingAnimation();

            // Simulate network delay for authentication
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        stopAuthenticatingAnimation();
                        if (authenticateUser(email, password)) {
                            if (rememberMeCheckbox.isSelected()) {
                                saveCredentials(email, password);
                            } else {
                                clearSavedCredentials();
                            }
                            showSuccessAnimation();
                        } else {
                            loginAttempts++;
                            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                                startLockoutTimer();
                            }
                            showFailureAnimation();
                        }
                    });
                }
            }, 1500);
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

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BorderLayout());
        logoPanel.setPreferredSize(new Dimension(300, 300));
        logoPanel.setMaximumSize(new Dimension(300, 300));


        // Load image from resources folder
        try (InputStream imageStream = getClass().getResourceAsStream("/ressources/logo_LinkApp.png")) {
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

        JOptionPane.showMessageDialog(LoginInterface.this,
                "Welcome to LinkApp!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            rememberMeCheckbox.setSelected(true);
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

    private void showForgotPasswordDialog() {
        JDialog forgotDialog = new JDialog(this, "Password Recovery", true);
        forgotDialog.setSize(450, 300);
        forgotDialog.setLocationRelativeTo(this);
        forgotDialog.setLayout(new BorderLayout());
        forgotDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        forgotDialog.setResizable(false);

        // Create main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Reset Your Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Enter your email address to receive a password reset link");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        // Create form panel
        JPanel formPanel = new JPanel(new BorderLayout(0, 8));
        formPanel.setOpaque(false);

        // Email field with modern styling
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(TEXT_COLOR);

        // Custom text field
        JTextField emailField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw border
                if (hasFocus()) {
                    g2.setColor(PRIMARY_COLOR);
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(TEXT_COLOR);
        emailField.setPreferredSize(new Dimension(350, 40));
        emailField.setMaximumSize(new Dimension(350, 40));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        // Add placeholder text
        emailField.addFocusListener(new FocusAdapter() {
            private boolean showingPlaceholder = true;
            private final String PLACEHOLDER = "Enter your email address";

            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    emailField.setText("");
                    emailField.setForeground(TEXT_COLOR);
                    showingPlaceholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (emailField.getText().isEmpty()) {
                    emailField.setText(PLACEHOLDER);
                    emailField.setForeground(new Color(150, 150, 150));
                    showingPlaceholder = true;
                }
            }
        });

        // Set initial placeholder
        emailField.setText("Enter your email address");
        emailField.setForeground(new Color(150, 150, 150));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton resetButton = createModernButton("Send Reset Link", PRIMARY_COLOR, Color.WHITE);
        resetButton.setPreferredSize(new Dimension(200, 45));

        JButton cancelButton = createModernButton("Cancel", SECONDARY_COLOR, Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(200, 45));

        // Add action listeners
        resetButton.addActionListener(e -> {
            if (!emailField.getText().isEmpty() && !emailField.getText().equals("Enter your email address")) {
                JOptionPane.showMessageDialog(forgotDialog,
                        "Password reset link has been sent to your email address.",
                        "Link Sent", JOptionPane.INFORMATION_MESSAGE);
                forgotDialog.dispose();
            } else {
                showErrorAnimation("Please enter your email address");
            }
        });

        cancelButton.addActionListener(e -> forgotDialog.dispose());

        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);

        // Add components to panels
        formPanel.add(emailLabel, BorderLayout.NORTH);
        formPanel.add(emailField, BorderLayout.CENTER);

        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to dialog
        forgotDialog.add(mainPanel);
        forgotDialog.setVisible(true);
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

