package org.jwellman.foundation.examples;

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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiSplashProvider;

/**
 * Custom splash provider for the Silver Tier Showcase Demo.
 * <p>
 * This splash screen displays:
 * <ul>
 * <li>Framework branding with modern gradient design</li>
 * <li>Silver tier logo/badge</li>
 * <li>Progress bar for initialization phases</li>
 * <li>Real-time status messages during plugin discovery</li>
 * <li>User-dismissable via button (demonstrates card-based view management)</li>
 * <li>Showcase demo color scheme (blues and greens)</li>
 * </ul>
 *
 * @author Foundation Framework
 */
public class ShowcaseSplashProvider implements uiSplashProvider {

    private final uiContext context;
    private JPanel splashPanel;
    private JLabel messageLabel;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton dismissButton;

    // Showcase color scheme
    private static final Color GRADIENT_START = new Color(25, 45, 85);
    private static final Color GRADIENT_END = new Color(50, 90, 160);
    private static final Color ACCENT_GREEN = new Color(34, 139, 34);
    private static final Color ACCENT_BLUE = new Color(70, 130, 180);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(200, 220, 255);

    /**
     * Creates a new ShowcaseSplashProvider.
     *
     * @param context The application context (needed to access view provider for dismissal)
     */
    public ShowcaseSplashProvider(uiContext context) {
        this.context = context;
    }

    /**
     * Creates the showcase splash screen content with custom branding.
     *
     * @return The configured splash content panel
     */
    @Override
    @SuppressWarnings("serial")
    public JPanel createSplashContent() {
        // Create main panel with gradient background
        splashPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Create diagonal gradient
                int width = getWidth();
                int height = getHeight();
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    width, height, GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };

        splashPanel.setPreferredSize(new Dimension(600, 350));
        splashPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(20, 40, 70), 3),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Create header with branding
        JPanel headerPanel = createHeaderPanel();

        // Create progress area
        JPanel progressPanel = createProgressPanel();

        // Create footer
        JPanel footerPanel = createFooterPanel();

        // Assemble the splash panel
        splashPanel.add(headerPanel, BorderLayout.NORTH);
        splashPanel.add(progressPanel, BorderLayout.CENTER);
        splashPanel.add(footerPanel, BorderLayout.SOUTH);

        return splashPanel;
    }

    /**
     * Creates the header panel with showcase branding.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Main title
        JLabel titleLabel = new JLabel("Foundation Framework");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Silver tier badge
        JLabel tierLabel = new JLabel("◆ SILVER TIER ◆");
        tierLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        tierLabel.setForeground(new Color(230, 230, 240)); // Almost white with slight blue tint
        tierLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Interactive Showcase Demo");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Feature highlights
        JLabel featuresLabel = new JLabel("Multi-Panel Registry • Plugin System • Desktop Management");
        featuresLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        featuresLabel.setForeground(new Color(180, 200, 240));
        featuresLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(tierLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(featuresLabel);

        return headerPanel;
    }

    /**
     * Creates the progress panel with status messages, progress bar, and dismiss button.
     */
    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        // Main status message
        messageLabel = new JLabel("Initializing framework...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Detailed status (for plugin discovery, etc.)
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(500, 28));
        progressBar.setMaximumSize(new Dimension(500, 28));
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        progressBar.setForeground(ACCENT_GREEN);
        progressBar.setBackground(new Color(30, 50, 90));

        // Dismiss button (initially disabled until initialization completes)
        dismissButton = new JButton("Click to Continue");
        dismissButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        dismissButton.setPreferredSize(new Dimension(200, 35));
        dismissButton.setMaximumSize(new Dimension(200, 35));
        dismissButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        dismissButton.setForeground(Color.WHITE);
        dismissButton.setBackground(ACCENT_GREEN);
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setOpaque(true);
        dismissButton.setEnabled(false); // Disabled until progress reaches 100%

        // Wire button to dismiss splash by showing "main" card
        dismissButton.addActionListener(e -> {
            if (context instanceof org.jwellman.foundation.uContext) {
                org.jwellman.foundation.uContext ctx = (org.jwellman.foundation.uContext) context;
                if (ctx.getViewProvider() != null) {
                    ctx.getViewProvider().showCard("main");
                }
            }
        });

        progressPanel.add(messageLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(statusLabel);
        progressPanel.add(Box.createVerticalStrut(12));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(dismissButton);

        return progressPanel;
    }

    /**
     * Creates the footer panel with version and framework info.
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);

        JLabel versionLabel = new JLabel("Version 1.0.1-SNAPSHOT", SwingConstants.CENTER);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(150, 180, 220));
        versionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel("Foundation Framework • Interface-First Design Philosophy", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
        copyrightLabel.setForeground(new Color(130, 160, 210));
        copyrightLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        footerPanel.add(versionLabel);
        footerPanel.add(Box.createVerticalStrut(3));
        footerPanel.add(copyrightLabel);

        return footerPanel;
    }

    /**
     * Updates the progress bar and status messages.
     * <p>
     * Message format can include multiple parts separated by " | " for
     * main message and detailed status:
     * <pre>
     * updateProgress(50, "Discovering plugins | Found 3 plugins")
     * </pre>
     * <p>
     * When progress reaches 100%, the dismiss button is enabled.
     *
     * @param percent Progress percentage (0-100)
     * @param message Status message (can include " | " separator for dual-line status)
     */
    @Override
    public void updateProgress(int percent, String message) {
        if (progressBar != null && messageLabel != null && statusLabel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);

                if (message != null) {
                    // Support dual-line messages with " | " separator
                    if (message.contains(" | ")) {
                        String[] parts = message.split(" \\| ", 2);
                        messageLabel.setText(parts[0]);
                        statusLabel.setText(parts[1]);
                    } else {
                        messageLabel.setText(message);
                        statusLabel.setText(" ");
                    }
                }

                // Enable dismiss button when initialization completes
                if (percent >= 100 && dismissButton != null) {
                    dismissButton.setEnabled(true);
                    dismissButton.requestFocusInWindow(); // Give button focus for keyboard accessibility
                }
            });
        }
    }

    /**
     * Called when the splash screen is closed.
     */
    @Override
    public void onSplashClosed() {
        // No special cleanup needed
    }

    /**
     * Returns the minimum display time for the splash screen.
     * <p>
     * Set to 0 milliseconds because this splash is user-dismissable.
     * The user controls when to dismiss the splash by clicking the "Click to Continue" button.
     *
     * @return 0 milliseconds (user-controlled dismissal)
     */
    @Override
    public int getMinimumDisplayTime() {
        return 0;
    }

}
