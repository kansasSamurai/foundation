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

        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Header
        JLabel header = new JLabel("Foundation Framework - Window Mode");
        header.setHorizontalAlignment(JLabel.CENTER);
        add(header, BorderLayout.NORTH);

        // Content
        JLabel content = new JLabel(
            "<html><center>" +
            "This demonstrates a simple application running in window mode.<br>" +
            "Foundation handles all the JFrame boilerplate.<br>" +
            "Notice: This class extends JPanel, not JFrame.<br><br>" +
            "The only difference from SimpleDesktopDemo is using useWindow() instead of useDesktop()." +
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

        // Demonstrate how to startup an application using Foundation.
        // For quick and simple use cases, the demo() method can be used.
        boolean demo = true;
        if (demo) {
            Foundation.demo(SimpleWindowDemo.class);
        } else {

            /* The use of the demo method is equivalent to the following 
             * required minimal startup sequence.  However, you will note that 
             * the demo() method takes a class as a parameter instead of an actual object.  
             * This is because init() MUST be called BEFORE any Swing components 
             * are created. [because init() eventually calls UIManager.setLookAndFeel()] 
             * Therefore, the demo method takes the class object to construct 
             * the object AFTER it calls init().  It is worth noting that not
             * all LookAndFeel's exhibit anomalies if initialized incorrectly, 
             * which can make this a difficult situation to diagnose.
             */

            // Step 1 - Create an application context
            uiContext ctx = Foundation.createContext(SimpleWindowDemo.class);

            // Step 2 - Initialize Foundation
            Foundation.init(ctx);

            // Step 3 - Create your application (which is a JPanel)
            ctx.registerMasterPanel("master", new SimpleWindowDemo());

            // Step 4 - Display the UI - this occurs properly on the EDT
            Foundation.launch(ctx);

        }

    }

}
