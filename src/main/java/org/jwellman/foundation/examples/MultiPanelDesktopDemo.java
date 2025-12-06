package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.WindowPosition;
import org.jwellman.foundation.interfaces.PanelLifecycleListener;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XPanel;
import org.jwellman.foundation.uContext;

/**
 * Demonstrates Foundation's enhanced multi-panel desktop capabilities.
 *
 * NEW in this demo (Bronze tier enhancements):
 * - Foundation.init() now automatically displays a window (desktop with empty JDesktopPane)
 * - Multiple panels per namespace (tool.calculator has "main" and "history")
 * - Panels registered after init() are immediately added to the visible desktop
 * - Panel lifecycle events (onCreate, onShow, onHide, onClose)
 * - Window positioning strategies (CASCADE, CENTER, EXPLICIT)
 * - Dynamic panel visibility management (show/hide)
 * - Panel registry queries (get panels by namespace)
 *
 * This demonstrates how a multi-tool desktop environment would work:
 * - Foundation.init() shows the desktop immediately
 * - Each tool can register multiple panels that appear as internal frames
 * - Tools can show/hide their windows dynamically
 * - Framework manages positioning and lifecycle automatically
 *
 * @author Foundation Framework
 */
public class MultiPanelDesktopDemo {

    private static Foundation foundation;

    public static void main(String[] args) {

        // Step 1 - Initialize Foundation with desktop mode
        uContext context = uContext.createContext(MultiPanelDesktopDemo.class);
        context.setDesktopMode(true);
        context.setDesktopTitle("Multi-Tool Desktop - Bronze Tier Demo");
        foundation = Foundation.init(context);

        // Step 2 - Register Calculator tool with main and history panels
        System.out.println("=== Registering Calculator Tool ===");
        XPanel calcMainPanel = foundation.registerUI(
                "tool.calculator",
                "main",
                createCalculatorPanel(),
                createLifecycleListener("Calculator Main"),
                WindowPosition.cascade()
        );

        XPanel calcHistoryPanel = foundation.registerUI(
                "tool.calculator",
                "history",
                createHistoryPanel(),
                createLifecycleListener("Calculator History"),
                WindowPosition.at(300, 50)  // Explicit positioning
        );

        // Step 3 - Register Text Editor tool
        System.out.println("=== Registering Text Editor Tool ===");
        XPanel editorPanel = foundation.registerUI(
                "tool.editor",
                "main",
                createToolPanel("Text Editor", Color.WHITE),
                createLifecycleListener("Text Editor"),
                WindowPosition.center()
        );

        // Step 4 - Register File Browser tool
        System.out.println("=== Registering File Browser Tool ===");
        XPanel browserPanel = foundation.registerUI(
                "tool.browser",
                "main",
                createToolPanel("File Browser", new Color(230, 240, 255)),
                createLifecycleListener("File Browser"),
                WindowPosition.cascade()
        );

        // Step 5 - Create control panel for managing panels
        XPanel controlPanel = foundation.registerUI(
                "system",
                "control",
                createControlPanel(),
                createLifecycleListener("Control Panel"),
                WindowPosition.at(10, 10, 250, 200)
        );

        // Step 6 - Demonstrate querying the registry
        System.out.println("\n=== Panel Registry Query ===");
        System.out.println("Calculator panels: " + foundation.getPanels("tool.calculator").size());
        System.out.println("All namespaces: " + foundation.getNamespaces());

        // Note: The desktop window was already created and shown by Foundation.init()
        // All registered panels have been automatically added as internal frames
    }

    /**
     * Creates a control panel with buttons to demonstrate panel management.
     */
    private static JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Panel Controls");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(14f).deriveFont(java.awt.Font.BOLD));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        // Toggle calculator history
        JButton toggleHistoryBtn = new JButton("Toggle Calc History");
        toggleHistoryBtn.addActionListener(e -> {
            foundation.togglePanel("tool.calculator", "history");
            System.out.println("Calculator history visible: " +
                    foundation.isPanelVisible("tool.calculator", "history"));
        });
        buttonPanel.add(toggleHistoryBtn);

        // Toggle editor
        JButton toggleEditorBtn = new JButton("Toggle Editor");
        toggleEditorBtn.addActionListener(e -> {
            foundation.togglePanel("tool.editor", "main");
            System.out.println("Editor visible: " +
                    foundation.isPanelVisible("tool.editor", "main"));
        });
        buttonPanel.add(toggleEditorBtn);

        // Toggle file browser
        JButton toggleBrowserBtn = new JButton("Toggle File Browser");
        toggleBrowserBtn.addActionListener(e -> {
            foundation.togglePanel("tool.browser", "main");
            System.out.println("File browser visible: " +
                    foundation.isPanelVisible("tool.browser", "main"));
        });
        buttonPanel.add(toggleBrowserBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a calculator panel with action buttons.
     */
    private static JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Calculator");
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(16f));
        panel.add(header, BorderLayout.NORTH);

        JLabel display = new JLabel("0");
        display.setHorizontalAlignment(JLabel.RIGHT);
        display.setFont(display.getFont().deriveFont(24f));
        display.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        display.setOpaque(true);
        display.setBackground(Color.WHITE);
        panel.add(display, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());
        JButton showHistoryBtn = new JButton("Show History");
        showHistoryBtn.addActionListener(e -> {
            foundation.showPanel("tool.calculator", "history");
        });
        buttons.add(showHistoryBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a history panel for the calculator.
     */
    private static JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Calculation History");
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(14f));
        panel.add(header, BorderLayout.NORTH);

        JLabel content = new JLabel(
            "<html>" +
            "1 + 1 = 2<br>" +
            "5 * 3 = 15<br>" +
            "10 / 2 = 5<br>" +
            "</html>"
        );
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a simple tool panel to simulate a tool/application.
     */
    private static JPanel createToolPanel(String toolName, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel(toolName);
        header.setHorizontalAlignment(JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(16f));
        panel.add(header, BorderLayout.NORTH);

        JLabel content = new JLabel(
            "<html><center>" +
            "This simulates a tool/application in a desktop environment.<br>" +
            "Each tool is a simple JPanel, wrapped in a JInternalFrame.<br><br>" +
            "Bronze tier features:<br>" +
            "- Multiple panels per namespace<br>" +
            "- Lifecycle events<br>" +
            "- Window positioning<br>" +
            "- Dynamic show/hide" +
            "</center></html>"
        );
        content.setHorizontalAlignment(JLabel.CENTER);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a lifecycle listener for demonstration purposes.
     */
    private static PanelLifecycleListener createLifecycleListener(final String panelName) {
        return new PanelLifecycleListener() {
            @Override
            public void onCreate(IWindow window) {
                System.out.println("[" + panelName + "] onCreate - window created");
            }

            @Override
            public void onShow(IWindow window) {
                System.out.println("[" + panelName + "] onShow - panel shown");
            }

            @Override
            public void onHide(IWindow window) {
                System.out.println("[" + panelName + "] onHide - panel hidden");
            }

            @Override
            public void onClose(IWindow window) {
                System.out.println("[" + panelName + "] onClose - panel closed");
            }
        };
    }
}
