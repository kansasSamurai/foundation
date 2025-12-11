package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.interfaces.uiContext;

/**
 * Minimal demonstration of Foundation in desktop mode (JInternalFrame in JDesktopPane).
 * <p>
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
        uiContext app = Foundation.createContext(SimpleDesktopDemo.class);
        app.setDesktopMode(true);
        app.setDesktopTitle("Foundation Desktop Demo");
        app.setDimension(1000, 600);  // Explicit size for desktop mode
        Foundation.init(app);

        // Step 2 - Create your application (which is a JPanel)
        app.registerMasterPanel("master", new SimpleDesktopDemo());
        app.getMasterPanel().setWindowTitle("Main Tool Window");

        // Step 3 - Display the UI
        Foundation.launch(app);

    }

}
