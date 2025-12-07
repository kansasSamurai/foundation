package org.jwellman.foundation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jwellman.foundation.interfaces.uiSplashProvider;

/**
 * Default implementation of uiSplashProvider.
 *
 * This is the framework-provided default splash screen that creates
 * a modern-looking splash panel with gradient background, progress bar,
 * and status messages. It is used automatically when no custom splash
 * provider is specified in uContext.
 *
 * Default Configuration:
 * - Gradient background (dark blue to lighter blue)
 * - "Foundation Framework" branding
 * - Animated progress bar
 * - Status message area
 * - Modern, clean appearance
 *
 * Custom Splash Providers:
 * Applications can create their own implementations of uiSplashProvider
 * to provide custom splash screens with:
 * - Company logos and branding
 * - Custom colors and themes
 * - Application-specific messaging
 * - Custom layouts and components
 *
 * Example Custom Provider:
 * <pre>
 * public class MyAppSplashProvider implements uiSplashProvider {
 *     private JProgressBar progressBar;
 *     private JLabel messageLabel;
 *
 *     public JPanel createSplashContent() {
 *         JPanel panel = new JPanel();
 *         // Add company logo, custom design...
 *         progressBar = new JProgressBar(0, 100);
 *         messageLabel = new JLabel("Loading...");
 *         // Layout and return panel
 *         return panel;
 *     }
 *
 *     public void updateProgress(int percent, String message) {
 *         progressBar.setValue(percent);
 *         messageLabel.setText(message);
 *     }
 *
 *     public void onSplashClosed() {
 *         System.out.println("Splash closed");
 *     }
 * }
 *
 * // Use in context:
 * uContext context = uContext.createContext("myapp");
 * context.setSplashProvider(new MyAppSplashProvider());
 * </pre>
 *
 * @author Foundation Framework
 */
public class DefaultSplashProvider implements uiSplashProvider {

    private JPanel splashPanel;
    private JLabel messageLabel;
    private JProgressBar progressBar;

    /**
     * Creates the default splash screen content with gradient background,
     * branding, progress bar, and status message area.
     *
     * @return The configured splash content panel
     */
    @Override
    @SuppressWarnings("serial")
    public JPanel createSplashContent() {
        // Create main panel with custom painting for gradient
        splashPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Create gradient from dark blue to lighter blue
                int width = getWidth();
                int height = getHeight();
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 50, 80),
                    0, height, new Color(60, 100, 160)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };

        splashPanel.setPreferredSize(new Dimension(500, 300));
        splashPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(20, 40, 70), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Create header with title and subtitle
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Foundation Framework");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("A Micro-Framework for Java Swing Applications");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        subtitleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        // Create progress area
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);

        messageLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setMaximumSize(new Dimension(400, 25));
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);

        progressPanel.add(messageLabel);
        progressPanel.add(Box.createVerticalStrut(10));
        progressPanel.add(progressBar);

        // Create footer with version info
        JLabel versionLabel = new JLabel("Version 1.0.1-SNAPSHOT", SwingConstants.CENTER);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(150, 180, 220));

        // Assemble the splash panel
        splashPanel.add(headerPanel, BorderLayout.NORTH);
        splashPanel.add(progressPanel, BorderLayout.CENTER);
        splashPanel.add(versionLabel, BorderLayout.SOUTH);

        return splashPanel;
    }

    /**
     * Updates the progress bar and status message.
     * <p>
     * This method is thread-safe and can be called from any thread.
     * UI updates are automatically dispatched to the EDT.
     *
     * @param percent Progress percentage (0-100)
     * @param message Status message to display
     */
    @Override
    public void updateProgress(int percent, String message) {
        if (progressBar != null && messageLabel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);
                if (message != null) {
                    messageLabel.setText(message);
                }
            });
        }
    }

    /**
     * Called when the splash screen is closed.
     * <p>
     * No special cleanup needed for the default implementation.
     */
    @Override
    public void onSplashClosed() {
        // No cleanup needed for default implementation
    }

    /**
     * Returns the minimum display time for the splash screen.
     * <p>
     * Default implementation returns 3000 milliseconds (3 seconds) to ensure
     * the splash screen is visible long enough to read.
     *
     * @return 3000 milliseconds
     */
    @Override
    public int getMinimumDisplayTime() {
        return 1500;
    }

}
