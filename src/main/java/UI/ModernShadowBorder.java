package UI;


import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernShadowBorder extends EmptyBorder {
    private final int cornerRadius;

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
            g2d.fillRoundRect(x + i, y + i, width - 1 - 2 * i, height - 1 - 2 * i, cornerRadius, cornerRadius);
        }
        g2d.dispose();
    }
}
