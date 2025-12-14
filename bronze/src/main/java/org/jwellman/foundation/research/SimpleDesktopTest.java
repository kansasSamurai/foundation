package org.jwellman.foundation.research;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test class for researching JDesktopPane and JInternalFrame behavior.
 * <em>Does not use Foundation API - pure Swing components only.</em>
 * Besides demonstrating the sizing behavior of a JDesktopPane, this demo
 * also shows that you can build the entire user interface before making it
 * visible using JFrame.setVisible().  This includes the fact that you can
 * add JInternalFrame objects to JDesktopPane even before JFrame.setVisible().
 * However, you must ensure to call JInternalFrame.setVisible(true).
 */
public class SimpleDesktopTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            /* A flag to control behavior of this simple app.
             * When true, sizes will be set as necessary and the JFrame
             * will appear as you would expect in a normal app.
             * When false, it demonstrates the fact that even when an
             * internal frame has a size (setSize()/setBounds()/pack()), then
             * the desktop pane will not calculate a size during frame.pack() 
             * and all you get when the app starts up is a minimally sized 
             * JFrame without any visible content - in which case 
             * you must manually resize the frame to see the content.
             */
            boolean fixSizes = true;

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
            internalFrame.pack(); // or, // internalFrame.setSize(400, 300);
            internalFrame.setVisible(true); // ✅ Critical! won't actually be visible until the frame is visible
            // NOTE:  Because we do not use setLocation(), it will be
            // shown in the upper left corner of the desktop.

            // Add internal frame to desktop pane
            desktopPane.add(internalFrame);

            // Center and display the main frame
            if (fixSizes) {
                frame.setSize(800, 600);
            } else {
                frame.pack();
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
