package UI;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    private final int WIDTH = 400;
    private final int HEIGHT = 150;
    private int progress = 0;
    private float alpha = 0.0f; // For fade-in and fade-out

    public SplashScreen() {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null); // center
        getContentPane().add(new LoadingPanel());
    }

    public void showSplashWithProgress() {
        setVisible(true);

        // Fade-in effect
        for (int i = 0; i <= 50; i++) {
            alpha = i / 50f;
            repaint();
            sleep(5);
        }

        // Progress loading with filled text
        for (int i = 0; i <= 100; i++) {
            progress = i;
            repaint();
            sleep(20);
        }

        // Fade-out effect
        for (int i = 100; i >= 0; i--) {
            alpha = i / 100f;
            repaint();
            sleep(10);
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

    private class LoadingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set transparency (alpha)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            String text = "Loading LinkApp...";
            Font font = new Font("Segoe UI", Font.BOLD, 30);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 2 - 20;

            // Draw gray text as base
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(text, x, y);

            // Clip text area to show progress filling
            int clipWidth = (int) ((progress / 100.0) * textWidth);
            Shape oldClip = g2.getClip();
            g2.setClip(x, y - textHeight, clipWidth, textHeight + 5);

            // Draw filled text
            g2.setColor(new Color(41, 128, 185));
            g2.drawString(text, x, y);
            g2.setClip(oldClip);
        }
    }

    public static void main(String[] args) {
        SplashScreen splash = new SplashScreen();
        splash.showSplashWithProgress();
        // After splash: new MainApp().setVisible(true); // replace with your main window
    }
}
