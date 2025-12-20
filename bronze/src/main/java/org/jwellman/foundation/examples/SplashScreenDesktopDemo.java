package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.model.PanelRegistration;
import org.jwellman.foundation.provider.DefaultSplashProvider;

/**
 * Demonstrates the splash screen functionality in desktop mode.
 * <p>
 * This example shows:<br>
 * 1. Desktop with splash internal frame on init()<br>
 * 2. Multiple panels registered (frames created but hidden)<br>
 * 3. First panel launched - splash closes, panel appears<br>
 * 4. Additional panels can be launched<br>
 * <p>
 * Run this demo: <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SplashScreenDesktopDemo"
 * </pre>
 * 
 * @author Foundation Framework
 */
public class SplashScreenDesktopDemo {

    public static void main(String[] args) {

        // Initialize Foundation in desktop mode - splash screen appears
        uiContext context = Foundation.createContext(SplashScreenDesktopDemo.class);
        context.setDesktopMode(true);
        context.setDesktopTitle("Splash Screen Desktop Demo");
        context.setSplashProvider(new DefaultSplashProvider());
        Foundation.init(context);

        // Register panels (frames created but not visible)
        PanelRegistration mainPanel = context.registerUI(
            "demo.app", 
            createMainPanel(),
            WindowPosition.center()
        );
        context.registerMasterPanel(mainPanel);

        PanelRegistration toolPanel = context.registerUI(
            "demo.tool",  
            createToolPanel("Calculator"),
            WindowPosition.cascade()
        );

        doMoreWork(context);

        // Launch first panel - splash closes, main panel appears
        Foundation.launch(context);

        // After a delay, launch the tool panel
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toolPanel.show();

    }

    private static void doMoreWork(uiContext context) {
        
        // Simulate more work
        final int total = Foundation.get() .logEnvironment(); // classpathEntries.length + fonts.length;
        final int delay = 4000 / total;
        int percent = 0;
        for (int i = 0; i <= total; i++) {

            // Do your work here (simulated)
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int current = (i*100)/total;
            if (current > percent) {
                percent = current;
                context.getSplashProvider().updateProgress(percent, null); // "In progress...");
            }
        }

        context.getSplashProvider().updateProgress(100, "Initialization complete");

        // Uncomment this if we need a delay to see splash "results"
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private static JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Desktop Splash Demo", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel descLabel = new JLabel("<html>" +
            "<h2>Desktop Mode Splash Screen</h2>" +
            "<p>The splash screen appeared as a JInternalFrame on the desktop.</p>" +
            "<p>When launch() was called:</p>" +
            "<ul>" +
            "<li>Splash internal frame was closed</li>" +
            "<li>This main panel became visible</li>" +
            "<li>Calculator tool will appear in 2 seconds</li>" +
            "</ul>" +
            "<p>Multiple launches are supported in desktop mode!</p>" +
            "</html>");

        centerPanel.add(descLabel, BorderLayout.CENTER);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exitButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createToolPanel(String toolName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(toolName + " Tool", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(18f));

        JLabel infoLabel = new JLabel("<html>" +
            "<p>This tool was:</p>" +
            "<ul>" +
            "<li>Created as an internal frame (but hidden)</li>" +
            "<li>Shown via RegistrationPanel.show() after a delay</li>" +
            "</ul>" +
            "</html>", SwingConstants.CENTER);

        panel.add(label, BorderLayout.NORTH);
        panel.add(infoLabel, BorderLayout.CENTER);

        return panel;
    }

}
