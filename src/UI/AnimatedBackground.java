package UI;

import javax.swing.*;
import java.awt.*;

public class AnimatedBackground extends JPanel {
    public AnimatedBackground() {
        setOpaque(true);
        setBackground(new Color(240, 242, 245)); // Light grayish background
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}
