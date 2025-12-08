package org.jwellman.foundation.research;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test class for researching JDesktopPane and JInternalFrame behavior.
 * Does not use Foundation API - pure Swing components only.
 */
public class SimpleDesktopTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main frame
            JFrame frame = new JFrame("Simple Desktop Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create the desktop pane
            JDesktopPane desktopPane = new JDesktopPane();
            frame.setContentPane(desktopPane);

            // Create a simple panel with a button and label
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("Test Label", SwingConstants.CENTER), BorderLayout.NORTH);
            contentPanel.add(new JButton("Test Button"), BorderLayout.CENTER);

            // Create the internal frame
            JInternalFrame internalFrame = new JInternalFrame("Internal Frame", true, true, true, true);
            internalFrame.setContentPane(contentPanel);
            // internalFrame.setSize(400, 300);
            internalFrame.pack();
            internalFrame.setVisible(true);

            // Add internal frame to desktop pane
            desktopPane.add(internalFrame);

            // Center and display the main frame
            // frame.setSize(800, 600);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
