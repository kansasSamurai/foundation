package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.swing.IWindow;

/**
 * Minimal demonstration of Foundation in window mode (standalone JFrame).
 *
 * This demonstrates:
 * - Basic Foundation lifecycle (init, useWindow, showGUI)
 * - Single JPanel application
 * - Window mode deployment
 *
 * @author Foundation Framework
 */
public class SimpleWindowDemo {

    public static void main(String[] args) {
        // Step 1 - Initialize Foundation with no context (defaults)
        Foundation f = Foundation.init();

        // Step 2 - Create your UI in a JPanel
        JPanel ui = createUI();

        // Step 3 - Use Foundation to create a window
        IWindow window = f.useWindow(ui);
        window.setTitle("Foundation - Simple Window Demo");
        window.setResizable(true);

        // Step 4 - Display the UI
        f.showGUI(window);
    }

    /**
     * Creates a simple UI with a label and button.
     */
    private static JPanel createUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Foundation Framework - Window Mode");
        header.setHorizontalAlignment(JLabel.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This demonstrates a simple application running in window mode.<br>" +
            "The same JPanel can be deployed in desktop mode without code changes." +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        panel.add(content, BorderLayout.CENTER);

        // Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton button = new JButton("Click Me");
        button.addActionListener(e -> {
            System.out.println("Button clicked in window mode!");
        });
        buttonPanel.add(button);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
}
