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
 * - Application extending JPanel (not JFrame - Foundation handles that)
 * - Basic Foundation lifecycle (init, useWindow, showGUI)
 * - Window mode deployment
 * - Minimal boilerplate in main() method
 *
 * @author Foundation Framework
 */
public class SimpleWindowDemo extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor builds the UI.
     * This is the Foundation way - your application IS a JPanel.
     */
    public SimpleWindowDemo() {
        super(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Foundation Framework - Window Mode");
        header.setHorizontalAlignment(JLabel.CENTER);
        add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This demonstrates a simple application running in window mode.<br>" +
            "Notice: This class extends JPanel, not JFrame.<br>" +
            "Foundation handles all the JFrame boilerplate." +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        add(content, BorderLayout.CENTER);

        // Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton button = new JButton("Click Me");
        button.addActionListener(e -> {
            System.out.println("Button clicked in window mode!");
        });
        buttonPanel.add(button);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        // Step 1 - Initialize Foundation
        Foundation f = Foundation.init();

        // Step 2 - Create your application (which is a JPanel)
        SimpleWindowDemo app = new SimpleWindowDemo();

        // Step 3 - Use Foundation to create a window
        IWindow window = f.useWindow(app);
        window.setTitle("Foundation - Simple Window Demo");
        window.setResizable(true);

        // Step 4 - Display the UI
        f.showGUI(window);
    }

}
