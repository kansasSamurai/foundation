package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.model.FrameDescriptor;
import org.jwellman.foundation.provider.CompanyBrandedSplashProvider;

/**
 * Demonstrates using a custom splash provider with application-specific branding.
 * <p>
 * This example shows:
 * <ul>
 * <li>How to implement a custom uiSplashProvider (CompanyBrandedSplashProvider)</li>
 * <li>Custom branding with company logo, colors, and messaging</li>
 * <li>Professional design alternative to DefaultSplashProvider's gradient</li>
 * <li>Realistic initialization sequence with task-specific progress messages</li>
 * <li>Easy provider swap - just change one line of code</li>
 * </ul>
 * <p>
 * Key Takeaway: Applications can completely customize the splash screen without
 * modifying framework code by implementing the uiSplashProvider interface.
 * <p>
 * Run with:
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.CustomSplashProviderDemo"
 * </pre>
 *
 * @author Foundation Framework
 */
public class CustomSplashProviderDemo {

    public static void main(String[] args) {

        // Create context for the application
        uiContext context = Foundation.createContext(CustomSplashProviderDemo.class);
        context.setDesktopTitle("Custom Splash Provider Demo");

        // Set custom splash provider instead of default
        // This is the ONLY change needed to use custom branding!
        context.setSplashProvider(new CompanyBrandedSplashProvider());

        // Initialize Foundation (shows splash screen with custom provider)
        Foundation.init(context);

        // Create the main application UI
        JPanel mainUI = createMainUI();

        // Register the main panel
        FrameDescriptor mainPanel = context.registerUI("main", mainUI, WindowPosition.center());
        context.registerMasterPanel(mainPanel);

        // Simulate realistic application initialization with progress updates
        performInitialization(context.getSplashProvider());

        // Launch the application (closes splash, shows main UI)
        Foundation.launch(context);
    }

    /**
     * Creates the main application UI.
     */
    private static JPanel createMainUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 248, 250));

        // Header
        JLabel headerLabel = new JLabel("Custom Splash Provider Demo");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(new Color(70, 130, 180));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Content
        JLabel contentLabel = new JLabel(
            "<html><div style='text-align: center; padding: 20px;'>" +
            "<p style='font-size: 14px; color: #555;'><b>Application Initialized Successfully!</b></p>" +
            "<br>" +
            "<p style='font-size: 12px; color: #666;'>" +
            "This demo shows how to create a custom splash screen<br>" +
            "with application-specific branding by implementing<br>" +
            "the <b>uiSplashProvider</b> interface." +
            "</p>" +
            "<br>" +
            "<p style='font-size: 11px; color: #888;'>" +
            "<b>Key Points:</b><br>" +
            "• Custom logo and company branding<br>" +
            "• Corporate color scheme (green/blue)<br>" +
            "• Professional bordered design<br>" +
            "• Task-specific progress messages<br>" +
            "• Easy to swap providers (one line of code!)<br>" +
            "</p>" +
            "<br>" +
            "<p style='font-size: 10px; color: #999;'>" +
            "Compare this to <b>SplashScreenDemo</b> which uses DefaultSplashProvider<br>" +
            "to see how easy it is to customize the splash screen." +
            "</p>" +
            "</div></html>"
        );
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Simulates realistic application initialization with progress updates.
     * <p>
     * This demonstrates how to provide meaningful task messages during startup,
     * which works with any uiSplashProvider implementation.
     */
    private static void performInitialization(uiSplashProvider splasher) {
        // Realistic initialization tasks with progress updates
        String[] tasks = {
            "Loading configuration...",
            "Initializing security subsystem...",
            "Connecting to database...",
            "Loading user preferences...",
            "Initializing UI components...",
            "Loading plugins...",
            "Checking for updates...",
            "Preparing workspace...",
            "Finalizing initialization..."
        };

        int totalTasks = tasks.length;
        int taskDuration = 10000 / totalTasks; // Total 10 seconds

        for (int i = 0; i < totalTasks; i++) {
            // Update progress with task message
            int percent = ((i + 1) * 100) / totalTasks;
            splasher.updateProgress(percent, tasks[i]);

            // Simulate work
            try {
                Thread.sleep(taskDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Final completion message
        splasher.updateProgress(100, "Initialization complete!");

        // Brief pause to show completion
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
