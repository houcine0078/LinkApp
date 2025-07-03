package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color bgColor;
    private Color fgColor;
    private Color hoverColor;
    private Color pressColor;
    private boolean hovered = false;
    private boolean pressed = false;

    public ModernButton(String text, Color bgColor, Color fgColor) {
        super(text);
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.hoverColor = bgColor.brighter();
        this.pressColor = bgColor.darker();
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setForeground(fgColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOpaque(false);
        setPreferredSize(new Dimension(300, 50));
        setMaximumSize(new Dimension(300, 50));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color paintColor = bgColor;
        if (pressed) paintColor = pressColor;
        else if (hovered) paintColor = hoverColor;
        GradientPaint gradient = new GradientPaint(0, 0, paintColor, 0, getHeight(), paintColor.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.setColor(fgColor);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
} 