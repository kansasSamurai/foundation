package org.jwellman.foundation.plugins;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Simple Hello World plugin for demonstrating the Foundation plugin system.
 *
 * <p>This plugin can be packaged as a JAR and placed in the plugins directory.
 * It creates a simple Swing window with a greeting message.</p>
 *
 * <p><strong>To package as a plugin:</strong></p>
 * <pre>
 * 1. Compile: javac HelloWorldPlugin.java
 * 2. Create JAR with manifest:
 *    jar cfm hello-world-plugin.jar MANIFEST.MF org/jwellman/foundation/plugins/HelloWorldPlugin.class
 * 3. Place in plugins/hello-world/ directory
 * 4. Create plugin.properties (optional, can discover from manifest)
 * </pre>
 */
public class HelloWorldPlugin {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Hello World Plugin");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Hello from Foundation Plugin!", JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(24f));
        panel.add(header, BorderLayout.NORTH);

        // Message
        JLabel message = new JLabel(
                "<html><div style='text-align: center;'>" +
                "This is a simple plugin loaded by the Foundation framework.<br><br>" +
                "Plugins can be:<br>" +
                "- Loaded in isolated classloaders (ISOLATED_JVM)<br>" +
                "- Run in separate processes (EXTERNAL_PROCESS)<br>" +
                "- Shared with the desktop application (SHARED_JVM)<br>" +
                "</div></html>",
                JLabel.CENTER
        );
        panel.add(message, BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("Close Plugin");
        closeButton.addActionListener(e -> frame.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setSize(new Dimension(500, 300));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
