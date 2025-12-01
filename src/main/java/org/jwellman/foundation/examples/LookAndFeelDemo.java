package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.Stone;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.uContext;

/**
 * Demonstrates Look and Feel support in Foundation.
 *
 * NOTE: This demo shows the current hardcoded LAF approach which is TEMPORARY.
 *
 * Future Architecture Goal:
 * - LAF JARs will be discovered dynamically from a folder (e.g., ./lafs/)
 * - Framework will detect available LAFs at startup
 * - User can select LAF without recompiling
 * - LAF preference will be saved for future sessions
 *
 * This demo is useful for:
 * - Testing that all LAF dependencies are correctly configured
 * - Validating LAF integration
 * - Visualizing how Foundation UIs look across different LAFs
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

        // Initialize Foundation
        Foundation f = Foundation.init(context);

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
     * Prompts user to select a Look and Feel.
     */
    private static int promptForLAF() {
        String selection = (String) JOptionPane.showInputDialog(
            null,
            "Select a Look and Feel to test:\n\n" +
            "NOTE: This is a temporary approach.\n" +
            "Future versions will discover LAFs dynamically from a folder.",
            "Choose Look and Feel",
            JOptionPane.QUESTION_MESSAGE,
            null,
            LAF_NAMES,
            LAF_NAMES[0]
        );

        if (selection == null) {
            System.exit(0);
        }

        for (int i = 0; i < LAF_NAMES.length; i++) {
            if (LAF_NAMES[i].equals(selection)) {
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
