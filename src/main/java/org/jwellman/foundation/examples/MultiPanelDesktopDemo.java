package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XPanel;

/**
 * Demonstrates Foundation's multi-panel desktop capability.
 *
 * This shows:
 * - Multiple panels registered with Foundation
 * - Desktop mode with multiple internal frames
 * - How Bronze tier manages multiple UIs
 *
 * This is where Foundation's architecture shines - imagine these panels
 * as separate tool JARs loaded dynamically into a multi-tool desktop environment.
 *
 * @author Foundation Framework
 */
public class MultiPanelDesktopDemo {

    public static void main(String[] args) {
        // Step 1 - Initialize Foundation
        Foundation f = Foundation.init();

        // Step 2 - Create and register multiple UIs
        XPanel toolPanel1 = f.registerUI("tool.calculator", createToolPanel("Calculator", Color.LIGHT_GRAY));
        XPanel toolPanel2 = f.registerUI("tool.editor", createToolPanel("Text Editor", Color.WHITE));
        XPanel toolPanel3 = f.registerUI("tool.browser", createToolPanel("File Browser", new Color(230, 240, 255)));

        // Step 3 - Create the main desktop window with the first tool
        IWindow mainWindow = f.useDesktop(toolPanel1);
        mainWindow.setTitle("Multi-Tool Desktop");
        mainWindow.setResizable(true);
        mainWindow.setMaximizable(true);

        // Step 4 - Display the desktop
        // Note: Foundation will automatically create internal frames for toolPanel2 and toolPanel3
        // due to the registerUI() calls and the Bronze tier's initializeOtherWindows() logic
        f.showGUI(mainWindow);
    }

    /**
     * Creates a simple tool panel to simulate a tool/application.
     */
    private static JPanel createToolPanel(String toolName, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JLabel header = new JLabel(toolName);
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(16f));
        panel.add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This simulates a tool/application in a desktop environment.<br>" +
            "Each tool is a simple JPanel, wrapped in a JInternalFrame." +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        panel.add(content, BorderLayout.CENTER);

        // Action button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton actionButton = new JButton("Tool Action");
        actionButton.addActionListener(e -> {
            System.out.println(toolName + " action executed!");
        });
        buttonPanel.add(actionButton);
        buttonPanel.setOpaque(false);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
}
