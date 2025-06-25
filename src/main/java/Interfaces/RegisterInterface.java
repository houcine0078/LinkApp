package Interfaces;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Add this import for JSON parsing
import services.FirebaseAuthService;

public class RegisterInterface extends JDialog {
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(189, 195, 199);
    private JButton createAccountButton;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStrengthLabel;

    public RegisterInterface(Frame parent) {
        super(parent, "Register New Account", true);
        setSize(500, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

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

        nameField = new JTextField(20);
        emailField = new JTextField(20);
        newUsernameField = new JTextField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setForeground(TEXT_COLOR);

        // Password strength indicator
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

        createAccountButton = createModernButton("Create Account", PRIMARY_COLOR, Color.WHITE);
        createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        createAccountButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = newUsernameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
        
            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill all fields",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            int strength = calculatePasswordStrength(password);
            if (strength < 3) {
                JOptionPane.showMessageDialog(this,
                        "Password too weak! Please choose a stronger password.",
                        "Weak Password", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            try {
                String registerResponse = FirebaseAuthService.register(email, password);
                System.out.println("Firebase register response: " + registerResponse);
        
                org.json.JSONObject json = new org.json.JSONObject(registerResponse);
        
                if (json.has("error")) {
                    Object errorObj = json.get("error");
                    String errorMessage = errorObj.toString();
                    JOptionPane.showMessageDialog(this,
                            "Registration failed: " + errorMessage,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                String localId = json.optString("localId", null);
                if (localId == null) {
                    JOptionPane.showMessageDialog(this,
                            "Registration failed. Could not get user ID.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                // Store user data in Firebase Realtime Database
                String storeResponse = FirebaseAuthService.storeUserData(localId, name, username, email, password);
                System.out.println("Store user response: " + storeResponse);
                if (storeResponse != null && storeResponse.contains(email)) {
                    JOptionPane.showMessageDialog(this,
                            "Account created successfully!\nPlease check your email to activate your account.",
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to store user data.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        regPanel.add(titleLabel);
        regPanel.add(Box.createVerticalStrut(25));
        regPanel.add(formPanel);
        regPanel.add(Box.createVerticalStrut(25));
        regPanel.add(createAccountButton);

        add(regPanel);
        setVisible(true);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[@#$%^&+=].*")) strength++;
        return strength;
    }

    private void updatePasswordStrength(JPasswordField passwordField, JLabel strengthLabel) {
        String password = new String(passwordField.getPassword());
        int strength = calculatePasswordStrength(password);

        String strengthText;
        Color strengthColor;

        switch (strength) {
            case 0:
            case 1:
                strengthText = "Very Weak";
                strengthColor = new Color(231, 76, 60);
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

    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, bgColor,
                        0, getHeight(), bgColor.darker()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

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

        return button;
    }
}