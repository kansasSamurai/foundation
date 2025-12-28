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
import org.jwellman.foundation.interfaces.uiPanelLifecycleListener;
import org.jwellman.foundation.model.FrameDescriptor;
import org.jwellman.foundation.provider.CompanyBrandedDesktopProvider;
import org.jwellman.foundation.swing.IWindow;

/**
 * Demonstrates using a custom desktop provider with application-specific branding.
 * <p>
 * This example shows:
 * <ul>
 * <li>How to implement a custom uiDesktopProvider (CompanyBrandedDesktopProvider)</li>
 * <li>Custom desktop background with gradient painting</li>
 * <li>Desktop-level menu bar with File, Window, and Help menus</li>
 * <li>Post-initialization hook (onDesktopInitialized) for logging and setup</li>
 * <li>Professional desktop environment for enterprise applications</li>
 * <li>Easy provider swap - just change one line of code</li>
 * </ul>
 * <p>
 * Key Takeaway: Applications can completely customize the desktop environment
 * without modifying framework code by implementing the uiDesktopProvider interface.
 * <p>
 * Compare to DefaultDesktopProvider (used in SimpleDesktopDemo and MultiPanelDesktopDemo)
 * to see how easy it is to customize the desktop environment with the provider pattern.
 * <p>
 * Run with:
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.CustomDesktopProviderDemo"
 * </pre>
 *
 * @author Foundation Framework
 */
public class CustomDesktopProviderDemo {

    public static void main(String[] args) {

        // Create context for the application
        uiContext context = Foundation.createContext("demo.customdesktop");
        context.setDesktopMode(true);
        context.setDesktopTitle("Custom Desktop Provider Demo - Acme Business Solutions");

        // Set custom desktop provider instead of default
        // This is the ONLY change needed to use custom desktop environment!
        // Comment out this line to see DefaultDesktopProvider (plain gray background, no menu bar)
        context.setDesktopProvider(new CompanyBrandedDesktopProvider());

        // Initialize Foundation (creates desktop with custom provider)
        Foundation.init(context);

        // Create and register several panels to demonstrate the custom desktop
        System.out.println("\n=== Registering Application Panels ===");

        // Main application panel
        FrameDescriptor mainPanel = context.registerUI(
            "main",
            createApplicationPanel("Main Application", Color.WHITE,
                "This is the main application window.\n\n" +
                "Notice the custom desktop features:\n" +
                "• Gradient background (blue tones)\n" +
                "• Company watermark in lower left\n" +
                "• Menu bar with File, Window, Help\n" +
                "• LIVE_DRAG_MODE (smooth window dragging)"),
            createLifecycleListener("Main Application"),
            WindowPosition.at(20, 20, 500, 350)
        );

        // Tool panel 1
        FrameDescriptor toolPanel1 = context.registerUI(
            "tool1",
            createApplicationPanel("Calculator Tool", new Color(255, 250, 240),
                "This simulates a calculator tool.\n\n" +
                "All panels share the same custom desktop.\n\n" +
                "Try the menu bar:\n" +
                "• File > Exit to close application\n" +
                "• Window > Minimize All to minimize windows\n" +
                "• Help > About for application info"),
            createLifecycleListener("Calculator Tool"),
            WindowPosition.at(540, 20, 400, 300)
        );

        // Tool panel 2
        FrameDescriptor toolPanel2 = context.registerUI(
            "tool2",
            createApplicationPanel("Editor Tool", new Color(240, 255, 240),
                "This simulates an editor tool.\n\n" +
                "Key Points:\n" +
                "• Custom desktop provider is set once in context\n" +
                "• All panels automatically use the custom desktop\n" +
                "• No framework modification needed\n" +
                "• Provider pattern enables flexibility"),
            createLifecycleListener("Editor Tool"),
            WindowPosition.at(20, 390, 450, 280)
        );

        // Info panel
        FrameDescriptor infoPanel = context.registerUI(
            "info",
            createInfoPanel(),
            createLifecycleListener("Info Panel"),
            WindowPosition.at(540, 340, 400, 330)
        );

        // Set main panel as master
        context.registerMasterPanel(mainPanel);

        // Launch the application (shows all panels on custom desktop)
        System.out.println("=== Launching Application ===\n");
        Foundation.launch(context);

        // Show all panels
        mainPanel.show();
        toolPanel1.show();
        toolPanel2.show();
        infoPanel.show();
    }

    /**
     * Creates an application panel with specified title, background, and content.
     */
    private static JPanel createApplicationPanel(String title, Color bgColor, String content) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(new Color(70, 130, 180));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Content
        JLabel contentLabel = new JLabel(
            "<html><div style='padding: 10px;'>" +
            content.replace("\n", "<br>") +
            "</div></html>"
        );
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contentLabel.setVerticalAlignment(SwingConstants.TOP);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates an informational panel explaining the custom desktop provider pattern.
     */
    private static JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 248, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header
        JLabel headerLabel = new JLabel("Custom Desktop Provider");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(new Color(34, 139, 34));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Content
        JLabel contentLabel = new JLabel(
            "<html><div style='font-size: 11px; padding: 5px;'>" +
            "<p><b>How it works:</b></p>" +
            "<ol style='margin-left: 15px;'>" +
            "<li>Implement <b>uiDesktopProvider</b> interface</li>" +
            "<li>Override <b>createDesktop()</b> for custom desktop pane</li>" +
            "<li>Override <b>createMenuBar()</b> for custom menu bar</li>" +
            "<li>Override <b>onDesktopInitialized()</b> for post-setup</li>" +
            "<li>Set provider in context: <code>context.setDesktopProvider(...)</code></li>" +
            "</ol>" +
            "<br>" +
            "<p><b>Benefits:</b></p>" +
            "<ul style='margin-left: 15px;'>" +
            "<li>No framework modification needed</li>" +
            "<li>Easy to swap providers (one line of code)</li>" +
            "<li>Strategy pattern for desktop customization</li>" +
            "<li>Parameter-free methods support nested desktops</li>" +
            "<li>Professional appearance for enterprise apps</li>" +
            "</ul>" +
            "<br>" +
            "<p><b>Compare to DefaultDesktopProvider:</b></p>" +
            "<ul style='margin-left: 15px;'>" +
            "<li>Default: Plain gray background</li>" +
            "<li>Custom: Gradient with company watermark</li>" +
            "<li>Default: No menu bar</li>" +
            "<li>Custom: Full menu bar (File/Window/Help)</li>" +
            "<li>Default: OUTLINE_DRAG_MODE</li>" +
            "<li>Custom: LIVE_DRAG_MODE</li>" +
            "</ul>" +
            "</div></html>"
        );

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a lifecycle listener for demonstration purposes.
     */
    private static uiPanelLifecycleListener createLifecycleListener(final String panelName) {
        return new uiPanelLifecycleListener() {
            @Override
            public void onCreate(IWindow window) {
                System.out.println("[" + panelName + "] onCreate - window created on custom desktop");
            }

            @Override
            public void onShow(IWindow window) {
                System.out.println("[" + panelName + "] onShow - panel shown");
            }

            @Override
            public void onHide(IWindow window) {
                System.out.println("[" + panelName + "] onHide - panel hidden");
            }

            @Override
            public void onClose(IWindow window) {
                System.out.println("[" + panelName + "] onClose - panel closed");
            }
        };
    }

}
