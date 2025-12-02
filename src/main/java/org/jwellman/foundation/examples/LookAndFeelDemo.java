package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.Stone;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.uContext;

/**
 * Demonstrates Look and Feel support in Foundation. <br>
 * <em>NOTE: This demo shows the current hardcoded LAF approach which is TEMPORARY.</em>
 * <p>
 * Future Architecture Goal:<br>
 * - LAF JARs will be discovered dynamically from a folder (e.g., ./lafs/)<br>
 * - Framework will detect available LAFs at startup<br>
 * - User can select LAF without recompiling<br>
 * - LAF preference will be saved for future sessions<br>
 *<br>
 * This demo is useful for:<br>
 * - Testing that all LAF dependencies are correctly configured<br>
 * - Validating LAF integration<br>
 * - Visualizing how Foundation UIs look across different LAFs<br>
 *
 * @author Foundation Framework
 */
public class LookAndFeelDemo {

    private static final String[] LAF_NAMES = {
        "System Default",
        "Nimbus",
        "WebLAF",
        "NapkinLAF",
        "Nimrod",
        "JTattoo",
        "Darcula"
    };

    private static final int[] LAF_CONSTANTS = {
        Stone.LAF_SYSTEM,
        Stone.LAF_NIMBUS,
        Stone.LAF_WEB,
        Stone.LAF_NAPKIN,
        Stone.LAF_NIMROD,
        Stone.LAF_JTATTOO,
        Stone.LAF_DARCULA
    };

    public static void main(String[] args) {
        // Let user choose LAF before Foundation init
        int selectedLAF = promptForLAF();

        // Create context with selected LAF
        uContext context = uContext.createContext();
        // Note: LAF selection via context not yet implemented
        // This demonstrates current limitation and future direction

        // Initialize Foundation (this will set LAF to system default)
        Foundation f = Foundation.init(context);

        // Now manually set the selected LAF
        // This is necessary because Foundation currently hardcodes LAF_SYSTEM
        setLookAndFeel(selectedLAF);

        // Create UI
        JPanel ui = createUI(selectedLAF);

        // Use window mode
        IWindow window = f.useWindow(ui);
        window.setTitle("Foundation - Look and Feel Demo [" + LAF_NAMES[selectedLAF] + "]");
        window.setResizable(true);

        // Display
        f.showGUI(window);
    }

    /**
     * Sets the Look and Feel based on the selected index.
     * This mirrors the logic in Stone.java but allows runtime selection.
     */
    private static void setLookAndFeel(int selectedLAF) {
        try {
            switch (LAF_CONSTANTS[selectedLAF]) {
                case Stone.LAF_NIMBUS:
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                    break;
                case Stone.LAF_WEB:
                    UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
                    break;
                case Stone.LAF_NAPKIN:
                    net.sourceforge.napkinlaf.NapkinTheme.Manager.setCurrentTheme("blueprint");
                    UIManager.setLookAndFeel(new net.sourceforge.napkinlaf.NapkinLookAndFeel());
                    break;
                case Stone.LAF_SYSTEM:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                case Stone.LAF_NIMROD:
                    UIManager.setLookAndFeel("com.nilo.plaf.nimrod.NimRODLookAndFeel");
                    break;
                case Stone.LAF_JTATTOO:
                    UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    break;
                case Stone.LAF_DARCULA:
                    UIManager.setLookAndFeel("com.bulenkov.darcula.DarculaLaf");
                    break;
            }
            System.out.println("LAF set to: " + UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            System.err.println("Error setting LAF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prompts user to select a Look and Feel using radio buttons.
     */
    private static int promptForLAF() {
        // Create panel with radio buttons
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Add instruction label
        JLabel instruction = new JLabel(
            "<html>Select a Look and Feel to test:<br><br>" +
            "<i>NOTE: This is a temporary approach.<br>" +
            "Future versions will discover LAFs dynamically from a folder.</i></html>"
        );
        instruction.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(instruction, BorderLayout.NORTH);

        // Create radio button panel
        JPanel radioPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        radioPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radioButtons = new JRadioButton[LAF_NAMES.length];

        for (int i = 0; i < LAF_NAMES.length; i++) {
            radioButtons[i] = new JRadioButton(LAF_NAMES[i]);
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
            "Choose Look and Feel",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        // Find which radio button is selected
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                return i;
            }
        }

        return 0; // Default to system
    }

    /**
     * Creates the demo UI.
     */
    private static JPanel createUI(int selectedLAF) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Look and Feel: " + LAF_NAMES[selectedLAF]);
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
            "<html><center>" +
            "<b>Current LAF Selection Method: Hardcoded Constants</b><br><br>" +
            "<i>Future Goal: Dynamic LAF Discovery</i><br>" +
            "- Drop LAF .jar files into ./lafs/ folder<br>" +
            "- Framework auto-detects available LAFs<br>" +
            "- User selects from discovered LAFs<br>" +
            "- Preference saved for future sessions<br>" +
            "</center></html>"
        );
        info.setHorizontalAlignment(JLabel.CENTER);
        panel.add(info, BorderLayout.SOUTH);

        return panel;
    }
}
