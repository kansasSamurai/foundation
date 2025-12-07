package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.uContext;

/**
 * Demonstrates the splash screen functionality in Foundation framework.
 *
 * This example shows:
 * 1. Splash screen appears when init() is called
 * 2. Application panel is shown when launch() is called
 * 3. Splash screen is automatically closed
 *
 * Run this demo:
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SplashScreenDemo"
 *
 * @author Foundation Framework
 */
public class SplashScreenDemo extends JPanel {

    private static final long serialVersionUID = 1L;

    public SplashScreenDemo() {
        super(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Splash Screen Demo", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));

        // Description
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel descLabel = new JLabel("<html>" +
            "<h2>Welcome to the Splash Screen Demo!</h2>" +
            "<p>The splash screen you just saw was created using the DefaultSplashProvider.</p>" +
            "<p>Features demonstrated:</p>" +
            "<ul>" +
            "<li>Automatic splash screen display on init()</li>" +
            "<li>Progress bar showing initialization</li>" +
            "<li>Gradient background with branding</li>" +
            "<li>Automatic splash closure on launch()</li>" +
            "</ul>" +
            "<p>This same pattern works in both window and desktop modes!</p>" +
            "</html>");

        centerPanel.add(descLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(okButton);

        // Layout
        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {

        // Initialize Foundation - splash screen appears here
        uContext context = uContext.createContext(SplashScreenDemo.class);
        Foundation foundation = Foundation.init(context);

        new Thread(() -> {
            // Simulate more work
            final int total = foundation.logEnvironment(); // classpathEntries.length + fonts.length;
            final int delay = 4000 / total;
            int percent = 0;
            for (int i = 0; i <= total; i++) {

                // Do your work here (simulated)
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int current = (i*100)/total;
                if (current > percent) {
                    percent = current;
                    foundation.getSplashProvider().updateProgress(percent, null); // "In progress...");
                }
            }

            foundation.getSplashProvider().updateProgress(100, "Initialization complete");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Launch the application - splash screen closes, app appears
            foundation.launch(new SplashScreenDemo());

        }).start();

    }

}
