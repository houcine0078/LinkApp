package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class UserListRenderer extends DefaultListCellRenderer {
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(189, 195, 199);
    private final Color ONLINE_GREEN = new Color(52, 199, 89);
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // UserInfo is expected to be a public static class in ChatInterface or model.UserInfo
        Object userObj = value;
        String name = "";
        String email = "";
        String status = "offline";
        if (userObj != null) {
            try {
                java.lang.reflect.Field nameField = userObj.getClass().getDeclaredField("name");
                java.lang.reflect.Field emailField = userObj.getClass().getDeclaredField("email");
                java.lang.reflect.Field statusField = userObj.getClass().getDeclaredField("status");
                nameField.setAccessible(true);
                emailField.setAccessible(true);
                statusField.setAccessible(true);
                name = (String) nameField.get(userObj);
                email = (String) emailField.get(userObj);
                status = (String) statusField.get(userObj);
            } catch (Exception e) { /* fallback to empty */ }
        }
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(10, 18, 10, 18));
        panel.setOpaque(true);
        if (isSelected) {
            panel.setBackground(PRIMARY_COLOR);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(10, 18, 10, 18)
            ));
        } else {
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(10, 18, 10, 18));
        }
        final String finalName = name;
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_COLOR);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                String initial = finalName.isEmpty() ? "?" : finalName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(initial)) / 2;
                int y = (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(36, 36));
        avatarPanel.setOpaque(false);
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
        JLabel emailLabel = new JLabel(email);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(isSelected ? new Color(255,255,255,180) : BORDER_COLOR);
        userInfoPanel.add(nameLabel, BorderLayout.NORTH);
        userInfoPanel.add(emailLabel, BorderLayout.SOUTH);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("●");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(status.equals("online") ? ONLINE_GREEN : BORDER_COLOR);
        statusLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        rightPanel.add(statusLabel, BorderLayout.WEST);
        if (isSelected) {
            JLabel check = new JLabel("\u2714"); // Unicode heavy checkmark
            check.setFont(new Font("Segoe UI", Font.BOLD, 26));
            check.setForeground(Color.WHITE);
            rightPanel.add(check, BorderLayout.EAST);
        }
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(userInfoPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }
} 