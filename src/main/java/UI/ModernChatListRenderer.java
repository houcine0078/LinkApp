package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernChatListRenderer extends DefaultListCellRenderer {
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color GROUP_COLOR = new Color(155, 89, 182);
    private final Color ONLINE_GREEN = new Color(52, 199, 89);
    private final Color BORDER_COLOR = new Color(189, 195, 199);
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // ChatItem is expected to be a public static class in ChatInterface or model.ChatItem
        Object chatItem = value;
        boolean isGroup = false;
        String name = "";
        String status = "";
        Color statusColor = TEXT_COLOR;
        try {
            java.lang.reflect.Field isGroupField = chatItem.getClass().getDeclaredField("isGroup");
            isGroupField.setAccessible(true);
            isGroup = isGroupField.getBoolean(chatItem);
            if (isGroup) {
                Object group = chatItem.getClass().getDeclaredField("chatInfo").get(chatItem);
                java.lang.reflect.Field nameField = group.getClass().getDeclaredField("name");
                java.lang.reflect.Field membersField = group.getClass().getDeclaredField("members");
                nameField.setAccessible(true);
                membersField.setAccessible(true);
                name = (String) nameField.get(group);
                java.util.List<?> members = (java.util.List<?>) membersField.get(group);
                status = members.size() + " members";
                statusColor = isSelected ? new Color(255, 255, 255, 180) : TEXT_COLOR;
            } else {
                Object user = chatItem.getClass().getDeclaredField("chatInfo").get(chatItem);
                java.lang.reflect.Field nameField = user.getClass().getDeclaredField("name");
                java.lang.reflect.Field statusField = user.getClass().getDeclaredField("status");
                nameField.setAccessible(true);
                statusField.setAccessible(true);
                name = (String) nameField.get(user);
                status = ((String) statusField.get(user)).equals("online") ? "Online" : "Last seen recently";
                statusColor = isSelected ? new Color(255, 255, 255, 180) : TEXT_COLOR;
            }
        } catch (Exception e) { /* fallback to empty */ }
        final String finalName = name;
        final boolean finalIsGroup = isGroup;
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setOpaque(true);
        panel.setBackground(isSelected ? PRIMARY_COLOR : Color.WHITE);
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(finalIsGroup ? GROUP_COLOR : PRIMARY_COLOR);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String initial = finalName.isEmpty() ? "?" : finalName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(40, 40));
        avatarPanel.setOpaque(false);
        JPanel chatInfoPanel = new JPanel(new BorderLayout());
        chatInfoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(statusColor);
        chatInfoPanel.add(nameLabel, BorderLayout.NORTH);
        chatInfoPanel.add(statusLabel, BorderLayout.SOUTH);
        JLabel indicator = new JLabel();
        if (isGroup) {
            indicator.setText("●");
            indicator.setForeground(GROUP_COLOR);
        } else {
            try {
                Object user = chatItem.getClass().getDeclaredField("chatInfo").get(chatItem);
                java.lang.reflect.Field statusField = user.getClass().getDeclaredField("status");
                statusField.setAccessible(true);
                String userStatus = (String) statusField.get(user);
                indicator.setText("●");
                indicator.setForeground(userStatus.equals("online") ? ONLINE_GREEN : Color.WHITE);
            } catch (Exception e) {
                indicator.setText("");
            }
        }
        indicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        indicator.setBorder(new EmptyBorder(0, 0, 0, 8));
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(chatInfoPanel, BorderLayout.CENTER);
        panel.add(indicator, BorderLayout.EAST);
        return panel;
    }
} 