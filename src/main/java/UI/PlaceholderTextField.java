package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderTextField extends JTextField {
    private String placeholder;
    private Color placeholderColor = new Color(150, 150, 150);
    private Color borderColor = new Color(189, 195, 199);
    private Color focusColor = new Color(41, 128, 185);

    public PlaceholderTextField(String placeholder) {
        super();
        this.placeholder = placeholder;
        setFont(new Font("Segoe UI", Font.PLAIN, 16));
        setBackground(Color.WHITE);
        setForeground(new Color(44, 62, 80));
        setPreferredSize(new Dimension(400, 50));
        setMinimumSize(new Dimension(400, 50));
        setMaximumSize(new Dimension(400, 50));
        setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        setText(placeholder);
        setForeground(placeholderColor);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(new Color(44, 62, 80));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().isEmpty()) {
                    setText(placeholder);
                    setForeground(placeholderColor);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        if (hasFocus()) {
            g2.setColor(focusColor);
            g2.setStroke(new BasicStroke(2));
        } else {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1));
        }
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
        super.paintComponent(g);
        g2.dispose();
    }

    public boolean isPlaceholderVisible() {
        return getText().equals(placeholder);
    }
} 