package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.framework.uContext;

/**
 * Minimal demonstration of Foundation Bronze tier in desktop mode (JInternalFrame in JDesktopPane).
 *
 * This demonstrates:
 * - Application extending JPanel (not JFrame - Foundation handles that)
 * - Bronze tier multi-panel API (registerUI, showPanel)
 * - Desktop mode deployment with namespace:panelId registration
 * - Setting custom window titles via windowTitle property
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
            "The panel is registered with Bronze's registerUI() API using namespace:panelId pattern." +
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

        // Step 1 - Create context and configure for desktop mode
        // Note: Desktop mode requires explicit sizing because JDesktopPane cannot
        //       calculate preferred size from internal frames (they are positioned
        //       absolutely, not laid out by a layout manager)
        uContext ctx = uContext.createContext(SimpleDesktopDemo.class);
        ctx.setDesktopMode(true);  // Enable desktop mode
        ctx.setDimension(1000, 600);  // Explicit size for desktop mode
        ctx.setDesktopTitle("Foundation Desktop Demo");

        // Step 2 - Initialize Foundation
        Foundation f = Foundation.init(ctx);

        // Step 3 - Register the panel using Bronze's registerUI() API
        // The panel is identified by namespace:panelId ("demo.simple:main")
        f.registerUI("demo.simple", "main", new SimpleDesktopDemo());

        // Step 4 - Set a custom window title using the windowTitle property
        f.getRegistration("demo.simple", "main").setWindowTitle("Foundation - Simple Desktop Demo");

        // Step 5 - Show the panel
        f.showPanel("demo.simple", "main");
    }

}
