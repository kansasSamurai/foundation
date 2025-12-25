package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiPanelLifecycleListener;
import org.jwellman.foundation.model.PanelRegistration;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XPanel;

/**
 * Comprehensive Bronze Tier showcase demonstrating three key features in one interactive demo:
 * <ol>
 * <li><b>Window Positioning</b> - CASCADE, CENTER, EXPLICIT positioning strategies</li>
 * <li><b>Panel Lifecycle</b> - onCreate, onShow, onHide, onClose event tracking</li>
 * <li><b>Dynamic Panel Management</b> - Runtime panel creation, removal, show/hide, registry queries</li>
 * </ol>
 * <p>
 * This interactive demo provides:
 * <ul>
 * <li>Control panel for creating panels with different positioning strategies</li>
 * <li>Real-time lifecycle event log visible in the UI</li>
 * <li>Registry statistics showing namespace and panel counts</li>
 * <li>Interactive panel management (show/hide/close)</li>
 * <li>Visual demonstration of positioning strategies</li>
 * <li>Panel list showing all registered panels with controls</li>
 * </ul>
 * <p>
 * Run with:
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.BronzeTierShowcaseDemo"
 * </pre>
 *
 * @author Foundation Framework
 */
public class BronzeTierShowcaseDemo {

    private static final AtomicInteger panelCounter = new AtomicInteger(1);
    private static DefaultListModel<String> eventLogModel;
    private static JLabel statsLabel;
    private static JList<String> panelListComponent;
    private static DefaultListModel<String> panelListModel;
    private static uiContext context;

    // Color palette for dynamic panels
    private static final Color[] PANEL_COLORS = {
        new Color(255, 250, 240), // Warm white
        new Color(240, 255, 240), // Light green
        new Color(240, 248, 255), // Alice blue
        new Color(255, 240, 245), // Lavender blush
        new Color(255, 255, 240), // Ivory
        new Color(245, 245, 245), // White smoke
    };

    private static class PALETTE {
        public static Color bkgControl = new Color(248, 248, 250);
    }

    public static void main(String[] args) {

        // Initialize context
        context = Foundation.createContext("showcase");
        context.setDesktopMode(true);
        context.setDesktopTitle("Bronze Tier Framework Showcase - Interactive Demo");

        Foundation.init(context);

        // Create the main control panel (left side)
        PanelRegistration controlPanel = context.registerUI(
            "control",
            createControlPanel(),
            createLifecycleListener("Control Panel"),
            WindowPosition.at(10, 10, 430, 700)
        );

        // Create the event log panel (right side)
        PanelRegistration eventLogPanel = context.registerUI(
            "eventlog",
            createEventLogPanel(),
            createLifecycleListener("Event Log"),
            WindowPosition.at(450, 10, 550, 350)
        );

        // Create the registry stats panel (right side, below event log)
        PanelRegistration statsPanel = context.registerUI(
            "stats",
            createStatsPanel(),
            createLifecycleListener("Registry Stats"),
            WindowPosition.at(450, 370, 550, 340)
        );

        // Set control panel as master
        context.registerMasterPanel(controlPanel);

        // Launch and show all panels
        Foundation.launch(context);
        controlPanel.show();
        eventLogPanel.show();
        statsPanel.show();

        // Update stats after initial setup
        updateStats();
    }

    /**
     * Creates the main control panel with interactive controls.
     */
    private static JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PALETTE.bkgControl);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PALETTE.bkgControl, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header
        JLabel headerLabel = new JLabel("Framework Showcase Controls");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        // headerLabel.setForeground(PALETTE.bkgControl);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Main content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        // contentPanel.setOpaque(false);
        contentPanel.setBackground(PALETTE.bkgControl);

        // Section 1: Create New Panel
        contentPanel.add(createSectionHeader("1. Window Positioning"));
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createNewPanelSection());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        // Section 2: Panel Management
        contentPanel.add(createSectionHeader("2. Dynamic Panel Management"));
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createPanelManagementSection());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        // Section 3: Registered Panels List
        contentPanel.add(createSectionHeader("3. Panel Registry"));
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createPanelListSection());

        // Scroll pane for content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a section header label.
     */
    private static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(34, 139, 34));
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates the "Create New Panel" section with positioning strategy selector.
     */
    private static JPanel createNewPanelSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><i>Create panels with different positioning strategies:</i></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        section.add(infoLabel);
        section.add(Box.createVerticalStrut(8));

        // Positioning strategy selector - radio buttons
        JRadioButton cascadeRadio = new JRadioButton("CASCADE (diagonal offset)", true);
        JRadioButton centerRadio = new JRadioButton("CENTER (centered)");
        JRadioButton explicitPosRadio = new JRadioButton("EXPLICIT (100,100)");
        JRadioButton explicitSizeRadio = new JRadioButton("EXPLICIT (with size 200,150,400,300)");

        ButtonGroup strategyGroup = new ButtonGroup();
        strategyGroup.add(cascadeRadio);
        strategyGroup.add(centerRadio);
        strategyGroup.add(explicitPosRadio);
        strategyGroup.add(explicitSizeRadio);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setOpaque(false);
        radioPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        cascadeRadio.setOpaque(false);
        centerRadio.setOpaque(false);
        explicitPosRadio.setOpaque(false);
        explicitSizeRadio.setOpaque(false);
        radioPanel.add(cascadeRadio);
        radioPanel.add(centerRadio);
        radioPanel.add(explicitPosRadio);
        radioPanel.add(explicitSizeRadio);

        section.add(radioPanel);
        section.add(Box.createVerticalStrut(8));

        // Create button
        JButton createButton = new JButton("Create Panel with Selected Strategy");
        createButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        createButton.addActionListener(e -> {
            WindowPosition position;
            String strategyName;

            if (cascadeRadio.isSelected()) {
                position = WindowPosition.cascade();
                strategyName = "CASCADE";
            } else if (centerRadio.isSelected()) {
                position = WindowPosition.center();
                strategyName = "CENTER";
            } else if (explicitPosRadio.isSelected()) {
                position = WindowPosition.at(100, 100);
                strategyName = "EXPLICIT(100,100)";
            } else if (explicitSizeRadio.isSelected()) {
                position = WindowPosition.at(200, 150, 400, 300);
                strategyName = "EXPLICIT(200,150,400,300)";
            } else {
                position = WindowPosition.cascade();
                strategyName = "CASCADE";
            }

            createDynamicPanel(strategyName, position);
        });
        section.add(createButton);

        return section;
    }

    /**
     * Creates the panel management section with show/hide/close buttons.
     */
    private static JPanel createPanelManagementSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><i>Manage panel visibility and lifecycle:</i></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        section.add(infoLabel);
        section.add(Box.createVerticalStrut(8));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(350, 90));
        buttonsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JButton showAllButton = new JButton("Show All Panels");
        showAllButton.addActionListener(e -> showAllPanels());
        buttonsPanel.add(showAllButton);

        JButton hideAllButton = new JButton("Hide All Dynamic Panels");
        hideAllButton.addActionListener(e -> hideAllDynamicPanels());
        buttonsPanel.add(hideAllButton);

        JButton closeAllButton = new JButton("Close All Dynamic Panels");
        closeAllButton.addActionListener(e -> closeAllDynamicPanels());
        buttonsPanel.add(closeAllButton);

        section.add(buttonsPanel);

        return section;
    }

    /**
     * Creates the panel list section showing all registered panels.
     */
    private static JPanel createPanelListSection() {
        JPanel section = new JPanel(new BorderLayout(5, 5));
        section.setOpaque(false);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(350, 250));

        JLabel infoLabel = new JLabel("<html><i>Registered panels (click to toggle visibility):</i></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        panelListModel = new DefaultListModel<>();
        panelListComponent = new JList<>(panelListModel);
        panelListComponent.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panelListComponent.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = panelListComponent.getSelectedIndex();
                if (index >= 0) {
                    String item = panelListModel.get(index);
                    togglePanelFromList(item);
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(panelListComponent);
        listScrollPane.setPreferredSize(new Dimension(340, 200));

        section.add(infoLabel, BorderLayout.NORTH);
        section.add(listScrollPane, BorderLayout.CENTER);

        return section;
    }

    /**
     * Creates the event log panel showing lifecycle events.
     */
    private static JPanel createEventLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 248, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header
        JLabel headerLabel = new JLabel("Panel Lifecycle Events");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(new Color(34, 139, 34));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Event log list
        eventLogModel = new DefaultListModel<>();
        JList<String> eventList = new JList<>(eventLogModel);
        eventList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        eventList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(eventList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Info label
        JLabel infoLabel = new JLabel("<html><i>Events: onCreate → onShow → onHide → onClose</i></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the registry statistics panel.
     */
    private static JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 248, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header
        JLabel headerLabel = new JLabel("Registry Statistics");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(new Color(139, 69, 19));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Stats text area
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsLabel.setVerticalAlignment(SwingConstants.TOP);

        JScrollPane scrollPane = new JScrollPane(statsLabel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Refresh button
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> updateStats());

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a dynamic panel with the specified positioning strategy.
     */
    private static void createDynamicPanel(String strategyName, WindowPosition position) {
        int panelNum = panelCounter.getAndIncrement();
        String panelId = "panel" + panelNum;

        // Create panel content
        JPanel content = createDynamicPanelContent(panelNum, strategyName);

        // Register panel
        PanelRegistration registration = context.registerUI(
            panelId,
            content,
            createLifecycleListener("Dynamic Panel #" + panelNum),
            position
        );

        // Show the panel
        registration.show();

        // Update stats and list
        updateStats();
        updatePanelList();

        logEvent("CREATED", "Panel #" + panelNum + " with " + strategyName + " positioning");
    }

    /**
     * Creates content for a dynamic panel.
     */
    private static JPanel createDynamicPanelContent(int panelNum, String strategyName) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        Color bgColor = PANEL_COLORS[panelNum % PANEL_COLORS.length];
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Dynamic Panel #" + panelNum);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Content
        JTextArea contentArea = new JTextArea(
            "Positioning Strategy: " + strategyName + "\n\n" +
            "This panel was created dynamically at runtime.\n\n" +
            "Panel ID: panel" + panelNum + "\n" +
            "Namespace: showcase\n" +
            "Full ID: showcase:panel" + panelNum + "\n\n" +
            "Try creating more panels with different\n" +
            "positioning strategies to see the differences!"
        );
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        // Close button
        JButton closeButton = new JButton("Close This Panel");
        closeButton.addActionListener(e -> {
            // TODO ensure the following works
            Foundation.get().closePanel("showcase", "panel" + panelNum);
            updateStats();
            updatePanelList();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a lifecycle listener that logs events.
     */
    private static uiPanelLifecycleListener createLifecycleListener(final String panelName) {
        return new uiPanelLifecycleListener() {
            @Override
            public void onCreate(IWindow window) {
                logEvent("onCreate", panelName);
            }

            @Override
            public void onShow(IWindow window) {
                logEvent("onShow", panelName);
                updatePanelList();
            }

            @Override
            public void onHide(IWindow window) {
                logEvent("onHide", panelName);
                updatePanelList();
            }

            @Override
            public void onClose(IWindow window) {
                logEvent("onClose", panelName);
                updateStats();
                updatePanelList();
            }
        };
    }

    /**
     * Logs an event to the event log panel.
     */
    private static void logEvent(String eventType, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        String logEntry = String.format("[%s] %-10s | %s", timestamp, eventType, message);

        javax.swing.SwingUtilities.invokeLater(() -> {
            eventLogModel.add(0, logEntry); // Add to top
            if (eventLogModel.size() > 100) {
                eventLogModel.remove(eventLogModel.size() - 1); // Keep max 100 entries
            }
        });
    }

    /**
     * Updates the registry statistics display.
     */
    private static void updateStats() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            List<String> namespaces = Foundation.get().getNamespaces();

            int visibleCount = 0;
            int hiddenCount = 0;
            List<PanelRegistration> allPanels = Foundation.get().getRegistrations("showcase");
            for (PanelRegistration reg : allPanels ) {
                if (reg != null && reg.isVisible()) {
                    visibleCount++;
                } else {
                    hiddenCount++;
                }
            }

            StringBuilder stats = new StringBuilder("<html><body style='padding: 10px;'>");
            stats.append("<b>Registry Overview:</b><br>");
            stats.append("Namespaces: ").append(namespaces.size()).append("<br>");
            stats.append("Total Panels: ").append(allPanels.size()).append("<br>");
            stats.append("Visible Panels: ").append(visibleCount).append("<br>");
            stats.append("Hidden Panels: ").append(hiddenCount).append("<br><br>");

            stats.append("<b>Namespaces:</b><br>");
            for (String ns : namespaces) {
                List<XPanel> nsPanels = Foundation.get().getPanels(ns);
                stats.append("• ").append(ns).append(": ").append(nsPanels.size()).append(" panels<br>");
            }

            stats.append("<br><b>All Panels:</b><br>");
            for (PanelRegistration reg : allPanels ) {
                String visibility = (reg != null && reg.isVisible()) ? "VISIBLE" : "HIDDEN";
                stats.append("• showcase:").append(reg.getPanel().getName()).append(" [").append(visibility).append("]<br>");
            }
            stats.append("</body></html>");

            statsLabel.setText(stats.toString());
        });
    }

    /**
     * Updates the panel list display.
     */
    private static void updatePanelList() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            panelListModel.clear();
            List<PanelRegistration> panelList = Foundation.get().getRegistrations("showcase");
            for (PanelRegistration reg : panelList ) {
                String visibility = (reg != null && reg.isVisible()) ? "●" : "○";
                String entry = String.format("%s showcase:%s", visibility, reg.getPanel().getName());
                panelListModel.addElement(entry);
            }
        });
    }

    /**
     * Shows all panels.
     */
    private static void showAllPanels() {
        List<PanelRegistration> panelList = Foundation.get().getRegistrations("showcase");
        for (PanelRegistration panel : panelList ) {
            panel.show();
        }
        updateStats();
        updatePanelList();
    }

    /**
     * Hides all dynamic panels (keeps control, eventlog, stats visible).
     */
    private static void hideAllDynamicPanels() {
        List<PanelRegistration> allPanels = Foundation.get().getRegistrations("showcase");
        for (PanelRegistration reg : allPanels ) {
            if (isPanelDynamic(reg)) {
                reg.hide();
            } 
        }
        updateStats();
        updatePanelList();
    }

    /**
     * Closes all dynamic panels (keeps control, eventlog, stats).
     */
    private static void closeAllDynamicPanels() {
        List<PanelRegistration> allPanels = Foundation.get().getRegistrations("showcase");
        for (PanelRegistration reg : allPanels ) {
            if (isPanelDynamic(reg)) reg.getWindow().close();
            // This does not actual remove from uiContext but close enough for demo app
        }
        updateStats();
        updatePanelList();
    }

    private static boolean isPanelDynamic(PanelRegistration reg) {
        String staticPanels = "control:eventlog:stats";
        return ! staticPanels.contains(reg.getPanel().getName().split(":")[1]);
    }

    /**
     * Toggles panel visibility from list selection.
     */
    private static void togglePanelFromList(String listItem) {
        // Parse "● showcase:panel1" or "○ showcase:panel1"
        String[] parts = listItem.split(":");
        if (parts.length == 3) {
            String panelId = parts[2].trim();
            Foundation.togglePanel("showcase", panelId);
        }
    }

}
