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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import java.util.Random;

public class LoginInterface extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel forgotPasswordLabel;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private Timer animationTimer;
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
    private List<ChatBubble> particles;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(189, 195, 199);

    // Password strength requirements
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    // Custom rounded shadow border
    private class RoundedShadowBorder extends AbstractBorder {
        private int radius;
        private Color shadowColor;

        public RoundedShadowBorder(int radius) {
            this(radius, new Color(0, 0, 0, 80));
        }

        public RoundedShadowBorder(int radius, Color shadowColor) {
            this.radius = radius;
            this.shadowColor = shadowColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw shadow
            g2d.setColor(shadowColor);
            g2d.fill(new RoundRectangle2D.Double(x + 2, y + 2, width - 4, height - 4, radius, radius));

            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.fill(new RoundRectangle2D.Double(x, y, width - 2, height - 2, radius, radius));

            g2d.dispose();
        }
    }

    // Inner class for animated background
    private class AnimatedBackground extends JPanel {
        private final List<Wave> waves;
        private final List<ChatBubble> chatBubbles;
        private Timer animationTimer;
        private final int WAVE_COUNT = 3;
        private final Random random;

        public AnimatedBackground() {
            waves = new ArrayList<>();
            chatBubbles = new ArrayList<>();
            random = new Random();
            setOpaque(false);
            initializeWaves();
            startAnimation();
        }

        protected static abstract class Particle {
            protected Point position;
            protected Dimension size;
            protected Color color;

            public Particle(Point position, Dimension size, Color color) {
                this.position = position;
                this.size = size;
                this.color = color;
            }

            public abstract void update();
            public abstract void draw(Graphics2D g2d);
            public abstract boolean isExpired();
        }

        private void initializeWaves() {
            // Create waves with different properties
            Color[] waveColors = {
                    new Color(41, 128, 185, 30),   // Light blue
                    new Color(52, 152, 219, 25),   // Medium blue
                    new Color(31, 97, 141, 20),    // Dark blue
                    new Color(36, 113, 163, 15),   // Deep blue
                    new Color(46, 134, 193, 20)    // Bright blue
            };

            for (int i = 0; i < WAVE_COUNT; i++) {
                waves.add(new Wave(
                        waveColors[i],
                        0.3 + i * 0.15,
                        30 + i * 15,
                        150 + i * 80,
                        i * Math.PI / WAVE_COUNT
                ));
            }

            // Create chat bubbles
            for (int i = 0; i < 15; i++) {
                chatBubbles.add(new ChatBubble());
            }
        }

        private void startAnimation() {
            animationTimer = new Timer();
            animationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (Wave wave : waves) {
                        wave.update();
                    }
                    for (ChatBubble bubble : chatBubbles) {
                        bubble.update();
                    }
                    repaint();
                }
            }, 0, 16);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            // Create gradient background
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(236, 240, 241),
                    0, getHeight(), new Color(214, 234, 248)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw chat bubbles
            // Draw particles behind waves
            for (Particle particle : particles) {
                particle.draw(g2d);
            }

            // Draw waves with composite blending
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            for (Wave wave : waves) {
                wave.draw(g2d, getWidth(), getHeight());
            }

            g2d.dispose();
        }

        private static class Wave {
            private final Color color;
            private final double speed;
            private final double amplitude;
            private final double wavelength;
            private final double phaseOffset;
            private double offset = 0;
            private List<Disturbance> disturbances;
            private double time = 0;

            public Wave(Color color, double speed, double amplitude, double wavelength, double phaseOffset) {
                this.color = color;
                this.speed = speed;
                this.amplitude = amplitude;
                this.wavelength = wavelength;
                this.phaseOffset = phaseOffset;
                this.disturbances = new ArrayList<>();
            }

            public void addDisturbance(double x) {
                disturbances.add(new Disturbance(x));
            }

            public void update() {
                time += 0.05;
                offset += speed;
                if (offset > wavelength) {
                    offset = 0;
                }

                // Update disturbances
                for (int i = disturbances.size() - 1; i >= 0; i--) {
                    Disturbance d = disturbances.get(i);
                    d.update();
                    if (d.amplitude <= 0.1) {
                        disturbances.remove(i);
                    }
                }
            }

            public void draw(Graphics2D g2d, int width, int height) {
                g2d.setColor(color);
                Path2D path = new Path2D.Double();
                path.moveTo(0, height);

                for (double x = 0; x < width + wavelength; x += 2) {
                    double y = height - amplitude * Math.sin((x + offset) * 2 * Math.PI / wavelength + phaseOffset + time);

                    // Add disturbance effects
                    for (Disturbance d : disturbances) {
                        double distance = Math.abs(x - d.x);
                        if (distance < d.radius) {
                            double factor = (1 - distance / d.radius) * d.amplitude;
                            y += Math.sin(distance * 0.5) * factor * 20;
                        }
                    }

                    if (x == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }

                path.lineTo(width, height);
                path.closePath();
                g2d.fill(path);
            }
        }

        private static class Disturbance {
            private double x;
            private double radius;
            private double amplitude;

            public Disturbance(double x) {
                this.x = x;
                this.radius = 50;
                this.amplitude = 1.0;
            }

            public void update() {
                radius += 5;
                amplitude *= 0.95;
            }
        }

        public void stopAnimation() {
            if (animationTimer != null) {
                animationTimer.cancel();
            }
        }
    }

    private static class ChatBubble extends AnimatedBackground.Particle {
        private double alpha;
        private double speed;

        public ChatBubble() {
            super(new Point(0, 0), new Dimension(10, 10), Color.WHITE);
            this.alpha = 1.0;
            this.speed = Math.random() * 2 + 1;
        }

        public ChatBubble(Point position, Dimension size, Color color) {
            super(position, size, color);
            this.alpha = 1.0;
            this.speed = Math.random() * 2 + 1;
        }

        @Override
        public void update() {
            position.y -= speed;
            alpha -= 0.01;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha));
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(position.x, position.y, size.width, size.height));
        }

        @Override
        public boolean isExpired() {
            return alpha <= 0;
        }
    }

    public LoginInterface() {
        particles = new ArrayList<>();
        prefs = Preferences.userNodeForPackage(LoginInterface.class);

        // Set up the frame
        setTitle("LinkApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Set application icon
        try {
            InputStream iconStream = getClass().getResourceAsStream("/ressources/logo_frame.png");
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
        formPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(300, 400));

        // Username field with modern styling
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        usernameField.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Password field with modern styling
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        passwordField.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Forgot password link with modern styling
        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(PRIMARY_COLOR);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
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
        buttonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(300, 120));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Login button with modern styling
        loginButton = createModernButton("Login", PRIMARY_COLOR, Color.WHITE);
        loginButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Register button with modern styling
        registerButton = createModernButton("Register", SECONDARY_COLOR, Color.WHITE);
        registerButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        registerButton.setBorder(new EmptyBorder(15, 0, 0, 0));

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
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                animateButtonClick(loginButton);

                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    showErrorAnimation("Please enter both username and password");
                    return;
                }

                // Show "authenticating" animation
                startAuthenticatingAnimation();

                // Simulate network delay for authentication
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopAuthenticatingAnimation();

                        if (authenticateUser(username, password)) {
                            if (rememberMeCheckbox.isSelected()) {
                                saveCredentials(username, password);
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
                    }
                }, 1500);
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                animateButtonClick(registerButton);
                showRegistrationDialog();
            }
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
                animatedBackground.stopAnimation();
            }
        });
    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BorderLayout());
        logoPanel.setPreferredSize(new Dimension(200, 200));
        logoPanel.setMaximumSize(new Dimension(200, 200));

        try {
            // Load image from resources folder
            InputStream imageStream = getClass().getResourceAsStream("/ressources/logo_LinkApp.png");
            if (imageStream == null) {
                throw new IOException("Logo image not found");
            }

            BufferedImage originalImage = ImageIO.read(imageStream);



            // High-quality scaling with bicubic interpolation
            Image scaledImage = getHighQualityScaledImage(originalImage, 200, 200);

            // Create a high-quality ImageIcon
            ImageIcon logoIcon = new ImageIcon(scaledImage);

            // Create JLabel for the logo with high-quality rendering
            JLabel logoLabel = new JLabel(logoIcon) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
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
        cancelButton.setEnabled(false);

        final int[] dots = {0};
        animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    dots[0] = (dots[0] + 1) % 4;
                    StringBuilder text = new StringBuilder("Authenticating");
                    for (int i = 0; i < dots[0]; i++) {
                        text.append(".");
                    }
                    statusLabel.setText(text.toString());
                });
            }
        }, 0, 300);
    }

    private void stopAuthenticatingAnimation() {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    private boolean authenticateUser(String username, String password) {
        // This is a placeholder for actual authentication logic
        return username.equals("admin") && password.equals("password");
    }

    private void showSuccessAnimation() {
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15, new Color(50, 205, 50)),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        statusLabel.setText("Login successful!");
        statusLabel.setForeground(new Color(50, 205, 50));

        final int TOTAL_FRAMES = 10;
        final int DELAY = 50;
        final int[] currentFrame = {0};
        final float[] scaleFactor = {1.0f};

        animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentFrame[0]++;
                if (currentFrame[0] > TOTAL_FRAMES * 2) {
                    animationTimer.cancel();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LoginInterface.this,
                                "Welcome to LinkApp!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                                new RoundedShadowBorder(15),
                                BorderFactory.createEmptyBorder(40, 40, 40, 40)
                        ));
                        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
                    });
                    return;
                }

                scaleFactor[0] = 1.0f + 0.1f * (float)Math.sin(currentFrame[0] * Math.PI / TOTAL_FRAMES);

                SwingUtilities.invokeLater(() -> {
                    Font originalFont = statusLabel.getFont();
                    float newSize = 18 * scaleFactor[0];
                    statusLabel.setFont(originalFont.deriveFont(newSize));
                    statusLabel.repaint();
                });
            }
        }, 0, DELAY);
    }

    private void showFailureAnimation() {
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(15, ACCENT_COLOR),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        statusLabel.setText("Invalid username or password");
        statusLabel.setForeground(ACCENT_COLOR);

        final int SHAKE_DISTANCE = 10;
        final int TOTAL_FRAMES = 8;
        final int DELAY = 50;
        final int[] currentFrame = {0};

        final Point originalLocation = contentPanel.getLocation();

        animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentFrame[0]++;
                if (currentFrame[0] > TOTAL_FRAMES) {
                    animationTimer.cancel();
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setLocation(originalLocation);
                        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                                new RoundedShadowBorder(15),
                                BorderFactory.createEmptyBorder(40, 40, 40, 40)
                        ));
                    });
                    return;
                }

                final int offset = (int)(SHAKE_DISTANCE * Math.sin(currentFrame[0] * Math.PI) *
                        (TOTAL_FRAMES - currentFrame[0]) / TOTAL_FRAMES);

                SwingUtilities.invokeLater(() -> {
                    contentPanel.setLocation(originalLocation.x + offset, originalLocation.y);
                });
            }
        }, 0, DELAY);
    }

    private void startFadeInAnimation() {
        alpha = 0.0f;
        contentPanel.setOpaque(false);

        animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    animationTimer.cancel();
                }
                contentPanel.setBackground(new Color(255, 255, 255, (int)(alpha * 255)));
                contentPanel.repaint();
            }
        }, 0, 30);
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
        String savedUsername = prefs.get("username", "");
        String savedPassword = prefs.get("password", "");
        if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
            usernameField.setText(savedUsername);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);
        }
    }

    private void saveCredentials(String username, String password) {
        prefs.put("username", username);
        prefs.put("password", password);
    }

    private void clearSavedCredentials() {
        prefs.remove("username");
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
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        boolean isValid = !username.isEmpty() && !password.isEmpty();
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
        emailField.addFocusListener(new FocusListener() {
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
        JDialog registerDialog = new JDialog(this, "Register New Account", true);
        registerDialog.setSize(500, 500);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout());

        JPanel regPanel = new JPanel();
        regPanel.setLayout(new BoxLayout(regPanel, BoxLayout.Y_AXIS));
        regPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        regPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Create a LinkApp Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(450, 250));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField newUsernameField = new JTextField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        JLabel passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setForeground(TEXT_COLOR);

        // Add password strength indicator
        newPasswordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePasswordStrength(newPasswordField, passwordStrengthLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePasswordStrength(newPasswordField, passwordStrengthLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePasswordStrength(newPasswordField, passwordStrengthLabel);
            }
        });

        // Add form components
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(newUsernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(newPasswordField);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(confirmPasswordField);
        formPanel.add(new JLabel("Password Strength:"));
        formPanel.add(passwordStrengthLabel);

        JButton createAccountButton = createModernButton("Create Account", PRIMARY_COLOR, Color.WHITE);
        createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createAccountButton.addActionListener(e -> {
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerDialog,
                        "Passwords do not match!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                JOptionPane.showMessageDialog(registerDialog,
                        "Password must be at least 8 characters long and contain:\n" +
                                "- At least one uppercase letter\n" +
                                "- At least one lowercase letter\n" +
                                "- At least one number\n" +
                                "- At least one special character (@#$%^&+=)",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(registerDialog,
                    "Account created successfully!\nPlease check your email to activate your account.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            registerDialog.dispose();
        });

        regPanel.add(titleLabel);
        regPanel.add(Box.createVerticalStrut(25));
        regPanel.add(formPanel);
        regPanel.add(Box.createVerticalStrut(25));
        regPanel.add(createAccountButton);

        registerDialog.add(regPanel);
        registerDialog.setVisible(true);
    }

    private void updatePasswordStrength(JPasswordField passwordField, JLabel strengthLabel) {
        String password = new String(passwordField.getPassword());
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[@#$%^&+=].*")) strength++;

        String strengthText;
        Color strengthColor;

        switch (strength) {
            case 0:
            case 1:
                strengthText = "Very Weak";
                strengthColor = ACCENT_COLOR;
                break;
            case 2:
                strengthText = "Weak";
                strengthColor = new Color(255, 140, 0);
                break;
            case 3:
                strengthText = "Medium";
                strengthColor = new Color(255, 215, 0);
                break;
            case 4:
                strengthText = "Strong";
                strengthColor = new Color(50, 205, 50);
                break;
            default:
                strengthText = "Very Strong";
                strengthColor = new Color(50, 205, 50);
                break;
        }

        strengthLabel.setText(strengthText);
        strengthLabel.setForeground(strengthColor);
    }

    // Modern shadow border
    class ModernShadowBorder extends EmptyBorder {
        private int cornerRadius;

        public ModernShadowBorder(int radius) {
            super(5, 5, 5, 5);
            this.cornerRadius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw shadow
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(0, 0, 0, 10 - i * 3));
                g2d.fillRoundRect(x + i, y + i, width - 1 - 2*i, height - 1 - 2*i, cornerRadius, cornerRadius);
            }

            g2d.dispose();
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginInterface loginUI = new LoginInterface();
                loginUI.setVisible(true);
            }
        });
    }
}

