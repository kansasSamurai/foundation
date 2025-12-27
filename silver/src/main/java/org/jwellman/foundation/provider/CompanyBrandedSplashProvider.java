package org.jwellman.foundation.provider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
 * Example custom splash provider demonstrating application-specific branding.
 * <p>
 * This implementation shows how to create a custom splash screen with:
 * <ul>
 * <li>Company/application branding and logo area</li>
 * <li>Custom color scheme (corporate green/blue instead of default gradient)</li>
 * <li>Professional bordered design instead of gradient background</li>
 * <li>Dual-purpose progress: overall percentage + current task description</li>
 * <li>Custom messaging and footer information</li>
 * </ul>
 * <p>
 * This demonstrates the provider pattern - applications can completely customize
 * the splash screen without modifying framework code by implementing uiSplashProvider.
 * <p>
 * Usage:
 * <pre>
 * uContext context = Foundation.createContext("myapp");
 * context.setSplashProvider(new CompanyBrandedSplashProvider());
 * Foundation.init(context);
 * </pre>
 *
 * @author Foundation Framework
 * @see org.jwellman.foundation.examples.CustomSplashProviderDemo
 */
public class CompanyBrandedSplashProvider implements uiSplashProvider {

    private JPanel splashPanel;
    private JLabel taskLabel;
    private JLabel percentLabel;
    private JProgressBar progressBar;

    // Corporate color scheme
    private static final Color CORPORATE_GREEN = new Color(34, 139, 34);
    private static final Color CORPORATE_BLUE = new Color(70, 130, 180);
    private static final Color BACKGROUND_COLOR = new Color(248, 248, 250);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    /**
     * Creates the custom branded splash screen content.
     *
     * @return The configured splash content panel
     */
    @Override
    public JPanel createSplashContent() {
        // Main panel with clean background (no gradient)
        splashPanel = new JPanel(new BorderLayout(15, 15));
        splashPanel.setBackground(BACKGROUND_COLOR);
        splashPanel.setPreferredSize(new Dimension(550, 320));
        splashPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 3),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // Create header with company branding
        JPanel headerPanel = createHeaderPanel();

        // Create progress area
        JPanel progressPanel = createProgressPanel();

        // Create footer with company info
        JPanel footerPanel = createFooterPanel();

        // Assemble the splash panel
        splashPanel.add(headerPanel, BorderLayout.NORTH);
        splashPanel.add(progressPanel, BorderLayout.CENTER);
        splashPanel.add(footerPanel, BorderLayout.SOUTH);

        return splashPanel;
    }

    /**
     * Creates the header panel with company branding and logo area.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Logo/Icon area (placeholder - in real app would load actual image)
        @SuppressWarnings("serial")
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw simple logo placeholder (rounded rectangle with company initials)
                int size = 60;
                int x = (getWidth() - size) / 2;
                g2d.setColor(CORPORATE_GREEN);
                g2d.fillRoundRect(x, 5, size, size, 15, 15);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
                String initials = "AB";
                int stringWidth = g2d.getFontMetrics().stringWidth(initials);
                g2d.drawString(initials, x + (size - stringWidth) / 2, 45);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(550, 70));
        logoPanel.setMaximumSize(new Dimension(550, 70));

        // Company name
        JLabel companyLabel = new JLabel("Acme Business Solutions");
        companyLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        companyLabel.setForeground(CORPORATE_BLUE);
        companyLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Application name
        JLabel appLabel = new JLabel("Enterprise Management Suite");
        appLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        appLabel.setForeground(new Color(100, 100, 100));
        appLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        headerPanel.add(logoPanel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(companyLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(appLabel);

        return headerPanel;
    }

    /**
     * Creates the progress panel with task label, percentage, and progress bar.
     */
    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        // Task description label
        taskLabel = new JLabel("Initializing application...", SwingConstants.CENTER);
        taskLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taskLabel.setForeground(new Color(80, 80, 80));
        taskLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Percentage label
        percentLabel = new JLabel("0%", SwingConstants.CENTER);
        percentLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        percentLabel.setForeground(CORPORATE_GREEN);
        percentLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false); // We're showing percentage separately
        progressBar.setPreferredSize(new Dimension(450, 20));
        progressBar.setMaximumSize(new Dimension(450, 20));
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        progressBar.setForeground(CORPORATE_GREEN);
        progressBar.setBackground(new Color(230, 230, 230));

        progressPanel.add(taskLabel);
        progressPanel.add(Box.createVerticalStrut(10));
        progressPanel.add(percentLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(progressBar);

        return progressPanel;
    }

    /**
     * Creates the footer panel with version and copyright information.
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);

        JLabel versionLabel = new JLabel("Version 2.5.1 (Build 2025.12)", SwingConstants.CENTER);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(120, 120, 120));
        versionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel("© 2025 Acme Business Solutions. All rights reserved.", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
        copyrightLabel.setForeground(new Color(140, 140, 140));
        copyrightLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        footerPanel.add(versionLabel);
        footerPanel.add(Box.createVerticalStrut(2));
        footerPanel.add(copyrightLabel);

        return footerPanel;
    }

    /**
     * Updates the progress bar and task message.
     * <p>
     * This method is thread-safe and can be called from any thread.
     * UI updates are automatically dispatched to the EDT.
     *
     * @param percent Progress percentage (0-100)
     * @param message Task description message to display
     */
    @Override
    public void updateProgress(int percent, String message) {
        if (progressBar != null && taskLabel != null && percentLabel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);
                percentLabel.setText(percent + "%");
                if (message != null) {
                    taskLabel.setText(message);
                }
            });
        }
    }

    /**
     * Called when the splash screen is closed.
     * <p>
     * No special cleanup needed for this implementation.
     */
    @Override
    public void onSplashClosed() {
        // No cleanup needed
    }

    /**
     * Returns the minimum display time for the splash screen.
     * <p>
     * Custom implementation returns 2000 milliseconds (2 seconds) to ensure
     * users have time to see the branding and initialization messages.
     *
     * @return 2000 milliseconds
     */
    @Override
    public int getMinimumDisplayTime() {
        return 2000;
    }

}
