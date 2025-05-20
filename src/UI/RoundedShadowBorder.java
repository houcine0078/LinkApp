package UI;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedShadowBorder extends AbstractBorder {
    private final int radius;
    private final Color shadowColor;
    private final int shadowLayers;
    private final Color baseColor;

    /**
     * @param radius        Corner radius
     * @param shadowColor   Main shadow color (can include alpha)
     * @param shadowLayers  Number of shadow blur layers (set to 1 for a sharp shadow)
     * @param baseColor     Border's base color (e.g. Color.WHITE)
     */
    public RoundedShadowBorder(int radius, Color shadowColor, int shadowLayers, Color baseColor) {
        this.radius = radius;
        this.shadowColor = shadowColor;
        this.shadowLayers = Math.max(1, shadowLayers);
        this.baseColor = baseColor;
    }

    // Simple, backwards-compatible constructor
    public RoundedShadowBorder(int radius) {
        this(radius, new Color(0, 0, 0, 80), 1, Color.WHITE);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw multiple shadow layers for a blurred effect
        if (shadowLayers > 1) {
            for (int i = shadowLayers - 1; i >= 0; i--) {
                int offset = i;
                int alpha = Math.max(0, shadowColor.getAlpha() - i * 15);
                Color layerColor = new Color(
                        shadowColor.getRed(),
                        shadowColor.getGreen(),
                        shadowColor.getBlue(),
                        alpha
                );
                g2d.setColor(layerColor);
                g2d.fillRoundRect(x + offset, y + offset, width - 2 * offset - 1, height - 2 * offset - 1, radius, radius);
            }
        } else {
            g2d.setColor(shadowColor);
            g2d.fill(new RoundRectangle2D.Double(x + 2, y + 2, width - 4, height - 4, radius, radius));
        }

        // Draw main border
        g2d.setColor(baseColor);
        g2d.fill(new RoundRectangle2D.Double(x, y, width - 2, height - 2, radius, radius));

        g2d.dispose();
    }
}
