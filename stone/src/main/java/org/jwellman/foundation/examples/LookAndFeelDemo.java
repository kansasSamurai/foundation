package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.framework.LAFDiscovery;
import org.jwellman.foundation.framework.LAFDiscovery.LAFInfo;
import org.jwellman.foundation.interfaces.uiContext;

/**
 * Demonstrates Look and Feel support in Foundation using the new LAFDiscovery system.
 * <p>
 * This demo showcases Foundation's dynamic LAF discovery architecture:
 * - LAFs are discovered from built-in Java LAFs, classpath, and ./lafs/ directory
 * - User can select from all discovered LAFs at runtime
 * - LAF selection can be configured via uContext or ./lafs/foundation.properties
 * - No recompilation needed to add new LAFs (just drop JAR in ./lafs/ folder)
 * <p>
 * This demo is useful for:
 * - Testing that all LAF dependencies are correctly configured
 * - Validating LAF integration and discovery
 * - Visualizing how Foundation UIs look across different LAFs
 *
 * @author Foundation Framework
 */
public class LookAndFeelDemo {

    public static void main(String[] args) {

        // Discover all available LAFs
        List<LAFInfo> discoveredLAFs = LAFDiscovery.discoverLookAndFeels();
        if (discoveredLAFs.isEmpty()) {
            System.err.println("ERROR: No Look and Feels discovered!");
            return;
        }

        // Let user choose LAF before Foundation init
        LAFInfo selectedLAF = promptForLAF(discoveredLAFs);
        if (selectedLAF == null) {
            System.exit(0);
        }

        // Create context with selected LAF
        uiContext context = Foundation.createContext(LookAndFeelDemo.class);
        context.setLookAndFeel(selectedLAF.getClassName());

        // Initialize Foundation (LAFDiscovery will apply the LAF from context)
        Foundation.init(context);

        // Create UI
        context.registerMasterPanel("master", createUI(selectedLAF));

//        new Thread(() -> {
//            // Simulate more work
//            final int total = foundation.logEnvironment(); // classpathEntries.length + fonts.length;
//            final int delay = 5000 / total;
//            int percent = 0;
//            for (int i = 0; i <= total; i++) {
//
//                // Do your work here (simulated)
//                try {
//                    Thread.sleep(delay);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                int current = (i*100)/total;
//                if (current > percent) {
//                    percent = current;
//                    foundation.getSplashProvider().updateProgress(percent, null); // "In progress...");
//                }
//            }
//
//            foundation.getSplashProvider().updateProgress(100, "Initialization complete");
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }).start();

        // Launch the application
        Foundation.launch(context);

    }

    /**
     * Prompts user to select a Look and Feel using radio buttons.
     */
    private static LAFInfo promptForLAF(List<LAFInfo> lafs) {
        // Create panel with radio buttons
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Add prompt
        JLabel prompt = new JLabel(
            "<html>Select a Look and Feel to test:</html>"
        );
        // prompt.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(prompt, BorderLayout.NORTH);

        // Add instruction label
        JLabel instruction = new JLabel(
            "<html> <br>" +
            "<i>Foundation discovered " + lafs.size() + " Look and Feel(s).<br>" +
            "To add more LAFs, place JAR files in the ./lafs/ folder.</i></html>"
        );
        // instruction.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(instruction, BorderLayout.SOUTH);

        // Create radio button panel
        JPanel radioPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        // radioPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radioButtons = new JRadioButton[lafs.size()];

        for (int i = 0; i < lafs.size(); i++) {
            LAFInfo laf = lafs.get(i);
            String label = laf.getName();
            // Add description if available and not too long
            if (laf.getDescription() != null && !laf.getDescription().isEmpty()
                && !laf.getDescription().equals(laf.getName())) {
                label += " - " + laf.getDescription();
            }
            radioButtons[i] = new JRadioButton(label);
            group.add(radioButtons[i]);
            radioPanel.add(radioButtons[i]);
        }

        // Select first option by default
        radioButtons[0].setSelected(true);

        panel.add(radioPanel, BorderLayout.CENTER);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Choose Look and Feel - Foundation Discovery Demo",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        // Find which radio button is selected
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                return lafs.get(i);
            }
        }

        return lafs.get(0); // Default to first
    }

    /**
     * Creates the demo UI.
     */
    private static JPanel createUI(LAFInfo selectedLAF) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel header = new JLabel("Look and Feel: " + selectedLAF.getName());
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(18f));
        panel.add(header, BorderLayout.NORTH);

        // Content - show various components
        JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        content.add(new JLabel("Sample Label"));
        content.add(new JButton("Sample Button"));

        JComboBox<String> combo = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3"});
        content.add(combo);

        panel.add(content, BorderLayout.CENTER);

        // Info
        JLabel info = new JLabel(
                "<html>" // "<html><center>" +
                + "<b>LAF Selection: Dynamic Discovery System</b><br><br>" 
                + "<i>Current LAF:</i><br>"
                + selectedLAF.getClassName() + "<br><br>"
                + "<b>How to add more LAFs:</b><br>" 
                + "1. Drop LAF .jar files into ./lafs/ folder<br>"
                + "2. Framework auto-detects available LAFs<br>"
                + "3. Configure default via ./lafs/foundation.properties<br>"
                + "4. Or specify via uContext.setLookAndFeel(className)<br>" 
                + "</html>" // + "</center></html>"
        );
        info.setHorizontalAlignment(JLabel.CENTER);
        panel.add(info, BorderLayout.SOUTH);

        return panel;
    }
}
