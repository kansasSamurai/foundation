package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.swing.IWindow;

/**
 * Minimal demonstration of Foundation in desktop mode (JInternalFrame in JDesktopPane).
 *
 * This demonstrates:
 * - Application extending JPanel (not JFrame - Foundation handles that)
 * - Basic Foundation lifecycle (init, useDesktop, showGUI)
 * - Desktop mode deployment
 * - Deployment-agnostic design - same JPanel class works in window or desktop mode
 *
 * @author Foundation Framework
 */
public class SimpleDesktopDemo extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor builds the UI.
     * Note: This constructor is nearly identical to SimpleWindowDemo's constructor.
     * The only difference is the text describing the mode.
     * This demonstrates deployment-agnostic design.
     */
    public SimpleDesktopDemo() {
        super(new BorderLayout(10, 10));

        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Header
        JLabel header = new JLabel("Foundation Framework - Desktop Mode");
        header.setHorizontalAlignment(JLabel.CENTER);
        add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This demonstrates a simple application running in desktop mode.<br>" +
            "Foundation handles all the JFrame boilerplate.<br>" +
            "Notice: This class extends JPanel, not JFrame nor JInternalFrame.<br><br>" +
            "The only difference from SimpleWindowDemo is using useDesktop() instead of useWindow()." +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        add(content, BorderLayout.CENTER);

        // Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton button = new JButton("Click Me");
        button.addActionListener(e -> {
            System.out.println("Button clicked in desktop mode!");
        });
        buttonPanel.add(button);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        // Step 1 - Initialize Foundation with explicit desktop dimensions
        // Note: Desktop mode requires explicit sizing because JDesktopPane cannot
        //       calculate preferred size from internal frames (they are positioned
        //       absolutely, not laid out by a layout manager)
        org.jwellman.foundation.uContext ctx = org.jwellman.foundation.uContext.createContext();
        ctx.setDimension(1000, 600);  // Explicit size for desktop mode
        ctx.setDesktopTitle("Foundation Desktop Demo");

        Foundation f = Foundation.init(ctx);

        // Step 2 - Create your application (which is a JPanel)
        SimpleDesktopDemo app = new SimpleDesktopDemo();

        // Step 3 - Use Foundation to create a desktop window (internal frame)
        IWindow window = f.useDesktop(app);
        window.setTitle("Foundation - Simple Desktop Demo");
        window.setResizable(true);
        window.setMaximizable(true);

        // Step 4 - Display the UI
        f.showGUI(window);
    }

}
