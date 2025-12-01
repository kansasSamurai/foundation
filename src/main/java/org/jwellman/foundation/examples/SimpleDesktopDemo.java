package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.swing.IWindow;

/**
 * Minimal demonstration of Foundation in desktop mode (JInternalFrame in JDesktopPane).
 *
 * This demonstrates:
 * - Basic Foundation lifecycle (init, useDesktop, showGUI)
 * - Single JPanel application
 * - Desktop mode deployment
 * - Same UI code as SimpleWindowDemo, different deployment
 *
 * @author Foundation Framework
 */
public class SimpleDesktopDemo {

    public static void main(String[] args) {
        // Step 1 - Initialize Foundation with no context (defaults)
        Foundation f = Foundation.init();

        // Step 2 - Create your UI in a JPanel
        JPanel ui = createUI();

        // Step 3 - Use Foundation to create a desktop window (internal frame)
        IWindow window = f.useDesktop(ui);
        window.setTitle("Foundation - Simple Desktop Demo");
        window.setResizable(true);
        window.setMaximizable(true);

        // Step 4 - Display the UI
        f.showGUI(window);
    }

    /**
     * Creates a simple UI with a label and button.
     * Note: This is identical to SimpleWindowDemo - demonstrating deployment-agnostic design.
     */
    private static JPanel createUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Foundation Framework - Desktop Mode");
        header.setHorizontalAlignment(JLabel.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This demonstrates a simple application running in desktop mode.<br>" +
            "Notice the UI code is identical to SimpleWindowDemo,<br>" +
            "only the deployment mode changed (useDesktop vs useWindow)." +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        panel.add(content, BorderLayout.CENTER);

        // Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton button = new JButton("Click Me");
        button.addActionListener(e -> {
            System.out.println("Button clicked in desktop mode!");
        });
        buttonPanel.add(button);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
}
