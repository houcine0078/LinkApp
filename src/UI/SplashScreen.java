package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SplashScreen extends JWindow {

    private final int WIDTH = 400;
    private final int HEIGHT = 150;
    private int progress = 0;
    private float alpha = 0.0f; // For fade-in and fade-out

    // Enhanced attributes
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color PROGRESS_BG = new Color(230, 235, 240);
    private int animationFrame = 0;
    private boolean showDots = true;
    private String[] loadingMessages = {"Initializing...", "Loading components...", "Connecting...", "Almost ready..."};
    private int currentMessageIndex = 0;

    public SplashScreen() {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null); // center

        // Modern rounded window
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 20, 20));

        getContentPane().add(new LoadingPanel());
    }

    public void showSplashWithProgress() {
        setVisible(true);

        // Fade-in effect with bounce
        for (int i = 0; i <= 50; i++) {
            alpha = easeOutBounce(i / 50f);
            repaint();
            sleep(8);
        }

        // Progress loading with enhanced animations
        for (int i = 0; i <= 100; i++) {
            progress = i;
            animationFrame++;

            // Change loading message at certain progress points
            if (i == 25) currentMessageIndex = 1;
            else if (i == 50) currentMessageIndex = 2;
            else if (i == 80) currentMessageIndex = 3;

            repaint();
            sleep(25);
        }

        // Hold for a moment
        sleep(300);

        // Fade-out effect
        for (int i = 100; i >= 0; i--) {
            alpha = i / 100f;
            repaint();
            sleep(8);
        }

        setVisible(false);
        dispose();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Easing function for smooth bounce effect
    private float easeOutBounce(float t) {
        if (t < 1 / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75f) {
            return 7.5625f * (t -= 1.5f / 2.75f) * t + 0.75f;
        } else if (t < 2.5 / 2.75) {
            return 7.5625f * (t -= 2.25f / 2.75f) * t + 0.9375f;
        } else {
            return 7.5625f * (t -= 2.625f / 2.75f) * t + 0.984375f;
        }
    }

    private class LoadingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set transparency (alpha)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Modern gradient background
            GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_COLOR,
                    getWidth(), getHeight(), Color.WHITE);
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            // Subtle border
            g2.setColor(new Color(220, 220, 220, (int)(alpha * 255)));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

            // App logo/icon area with animated circle
            drawAnimatedLogo(g2);

            // Main title
            String text = "LinkApp";
            Font titleFont = new Font("Segoe UI", Font.BOLD, 32);
            g2.setFont(titleFont);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            int titleX = (getWidth() - textWidth) / 2;
            int titleY = 45;

            // Title shadow
            g2.setColor(new Color(0, 0, 0, (int)(alpha * 30)));
            g2.drawString(text, titleX + 2, titleY + 2);

            // Title with gradient effect
            GradientPaint titleGradient = new GradientPaint(titleX, titleY - textHeight, PRIMARY_COLOR,
                    titleX, titleY, SECONDARY_COLOR);
            g2.setPaint(titleGradient);
            g2.drawString(text, titleX, titleY);

            // Loading message with animated dots
            drawLoadingMessage(g2);

            // Enhanced progress bar
            drawModernProgressBar(g2);

            // Progress percentage
            drawProgressPercentage(g2);
        }

        private void drawAnimatedLogo(Graphics2D g2) {
            int centerX = getWidth() / 2 - 150;
            int centerY = 25;

            // Animated pulsing circle
            float pulseScale = 1.0f + 0.1f * (float) Math.sin(animationFrame * 0.1);
            int circleSize = (int)(16 * pulseScale);

            // Outer glow
            g2.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(),
                    PRIMARY_COLOR.getBlue(), (int)(alpha * 50)));
            g2.fillOval(centerX - circleSize/2 - 2, centerY - circleSize/2 - 2,
                    circleSize + 4, circleSize + 4);

            // Main circle
            g2.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(),
                    PRIMARY_COLOR.getBlue(), (int)(alpha * 255)));
            g2.fillOval(centerX - circleSize/2, centerY - circleSize/2, circleSize, circleSize);
        }

        private void drawLoadingMessage(Graphics2D g2) {
            String message = loadingMessages[currentMessageIndex];

            // Animated dots
            int dotCount = (animationFrame / 10) % 4;
            String dots = "";
            for (int i = 0; i < dotCount; i++) {
                dots += ".";
            }
            message += dots;

            Font messageFont = new Font("Segoe UI", Font.PLAIN, 12);
            g2.setFont(messageFont);
            FontMetrics fm = g2.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            int messageX = (getWidth() - messageWidth) / 2;
            int messageY = 70;

            g2.setColor(new Color(100, 100, 100, (int)(alpha * 255)));
            g2.drawString(message, messageX, messageY);
        }

        private void drawModernProgressBar(Graphics2D g2) {
            int barWidth = 300;
            int barHeight = 6;
            int barX = (getWidth() - barWidth) / 2;
            int barY = 90;

            // Progress bar background
            g2.setColor(new Color(PROGRESS_BG.getRed(), PROGRESS_BG.getGreen(),
                    PROGRESS_BG.getBlue(), (int)(alpha * 255)));
            g2.fillRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

            // Progress fill with gradient
            int fillWidth = (int) ((progress / 100.0) * barWidth);
            if (fillWidth > 0) {
                GradientPaint progressGradient = new GradientPaint(barX, barY, PRIMARY_COLOR,
                        barX + fillWidth, barY, SECONDARY_COLOR);
                g2.setPaint(progressGradient);
                g2.fillRoundRect(barX, barY, fillWidth, barHeight, barHeight, barHeight);

                // Animated highlight on progress bar
                if (progress < 100) {
                    int highlightX = barX + fillWidth - 20;
                    if (highlightX > barX) {
                        GradientPaint highlight = new GradientPaint(highlightX, barY,
                                new Color(255, 255, 255, (int)(alpha * 100)),
                                highlightX + 20, barY,
                                new Color(255, 255, 255, 0));
                        g2.setPaint(highlight);
                        g2.fillRoundRect(Math.max(barX, highlightX), barY,
                                Math.min(20, fillWidth - (highlightX - barX)),
                                barHeight, barHeight, barHeight);
                    }
                }
            }

            // Progress bar border
            g2.setColor(new Color(200, 200, 200, (int)(alpha * 255)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);
        }

        private void drawProgressPercentage(Graphics2D g2) {
            String percentage = progress + "%";
            Font percentFont = new Font("Segoe UI", Font.BOLD, 11);
            g2.setFont(percentFont);
            FontMetrics fm = g2.getFontMetrics();
            int percentWidth = fm.stringWidth(percentage);
            int percentX = (getWidth() - percentWidth) / 2;
            int percentY = 115;

            g2.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(),
                    PRIMARY_COLOR.getBlue(), (int)(alpha * 200)));
            g2.drawString(percentage, percentX, percentY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            SplashScreen splash = new SplashScreen();
            splash.showSplashWithProgress();
            // After splash: new MainApp().setVisible(true); // replace with your main window
        });
    }
}