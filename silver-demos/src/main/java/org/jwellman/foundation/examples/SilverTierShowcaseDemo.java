package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import org.jwellman.foundation.interfaces.uiPluginManager;
import org.jwellman.foundation.listener.PanelLifecycleListener;
import org.jwellman.foundation.model.FrameDescriptor;
import org.jwellman.foundation.plugin.LaunchMode;
import org.jwellman.foundation.plugin.PluginActionRegistry;
import org.jwellman.foundation.plugin.PluginRegistration;
import org.jwellman.foundation.plugin.UnregisteredPlugin;
import org.jwellman.foundation.swing.IWindow;

/**
 * Comprehensive Silver Tier showcase demonstrating key features in one interactive demo:
 * <ol>
 * <li><b>Window Positioning</b> - CASCADE, CENTER, EXPLICIT positioning strategies</li>
 * <li><b>Panel Lifecycle</b> - onCreate, onShow, onHide, onClose event tracking</li>
 * <li><b>Dynamic Panel Management</b> - Runtime panel creation, removal, show/hide, registry queries</li>
 * <li><b>Panel Detach/Attach</b> - Toggle panels between internal frames (desktop) and external frames (standalone windows)</li>
 * <li><b>Menu Bar Support</b> - Optional menu bars on panels that transfer during detach/attach operations</li>
 * <li><b>Advanced Frame Manager</b> - Rich UI for managing all frames with visibility toggle, attach/detach, and editable titles</li>
 * <li><b>Plugin System</b> - Plugin discovery, registration, and launching with action registry integration</li>
 * </ol>
 * <p>
 * This interactive demo provides:
 * <ul>
 * <li>Control panel for creating panels with different positioning strategies</li>
 * <li>Optional menu bar creation for dynamic panels</li>
 * <li>Real-time lifecycle event log visible in the UI</li>
 * <li>Registry statistics showing namespace and panel counts</li>
 * <li>Interactive panel management (show/hide/close/detach)</li>
 * <li>Visual demonstration of positioning strategies</li>
 * <li>Panel list showing all registered panels with controls (simple taskbar)</li>
 * <li>Advanced Frame Manager with per-frame controls and editable titles</li>
 * <li>IDE-like panel detaching - "pop out" panels to standalone windows or dock them back (menu bars transfer automatically)</li>
 * </ul>
 * <p>
 * Run with:
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SilverTierShowcaseDemo"
 * </pre>
 *
 * @author Foundation Framework
 */
public class SilverTierShowcaseDemo {

    private static final AtomicInteger panelCounter = new AtomicInteger(1);
    private static DefaultListModel<String> eventLogModel;
    private static JLabel statsLabel;
    private static JList<String> panelListComponent;
    private static DefaultListModel<String> panelListModel;
    private static uiContext context;
    private static AdvancedFrameManagerPanel advancedFrameManager;
    private static PluginManagementPanel pluginManagementPanel;

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
        context.setDesktopTitle("Silver Tier Framework Showcase - Interactive Demo");

        Foundation.init(context);

        // Initialize plugin system (Silver tier feature)
        try {
            Foundation.initPlugins();
            logEvent("PLUGIN", "Plugin system initialized successfully");

            // Log discovered plugins
            uiPluginManager pluginManager = Foundation.getPluginManager();
            if (pluginManager != null) {
                List<UnregisteredPlugin> discovered = pluginManager.getDiscoveredPlugins();
                logEvent("PLUGIN", "Discovered " + discovered.size() + " new plugin(s)");

                for (UnregisteredPlugin plugin : discovered) {
                    logEvent("PLUGIN", "Found: " + plugin.getName() + " v" + plugin.getVersion());
                }

                logEvent("PLUGIN", "Registered plugins: " + pluginManager.getRegisteredPluginCount());
            }
        } catch (Exception e) {
            logEvent("PLUGIN", "Plugin system initialization failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Register registry change listener (Silver tier feature)
        // This automatically updates all UI components when the registry changes
        context.addRegistryChangeListener(() -> {
            updateStats();
            updatePanelList();
            if (advancedFrameManager != null) {
                advancedFrameManager.refresh();
            }
        });

        // Create the main control panel (left side)
        FrameDescriptor controlPanel = context.registerUI(
            "control",
            createControlPanel(),
            createLifecycleListener("Control Panel"),
            WindowPosition.at(10, 10, 430, 700)
        );
        controlPanel.setAttribute("permanent", true);

        // Create the event log panel (right side)
        FrameDescriptor eventLogPanel = context.registerUI(
            "eventlog",
            createEventLogPanel(),
            createLifecycleListener("Event Log"),
            WindowPosition.at(450, 10, 550, 350)
        );
        eventLogPanel.setAttribute("permanent", true);

        // Create the registry stats panel (right side, below event log)
        FrameDescriptor statsPanel = context.registerUI(
            "stats",
            createStatsPanel(),
            createLifecycleListener("Registry Stats"),
            WindowPosition.at(450, 370, 550, 340)
        );
        statsPanel.setAttribute("permanent", true);

        // Create the advanced frame manager panel (hidden by default)
        advancedFrameManager = new AdvancedFrameManagerPanel(context);
        FrameDescriptor advancedManagerPanel = context.registerUI(
            "advancedmanager",
            advancedFrameManager,
            createLifecycleListener("Advanced Frame Manager"),
            WindowPosition.at(100, 100, 600, 500)
        );
        advancedManagerPanel.setWindowTitle("Advanced Frame Manager");
        advancedManagerPanel.setAttribute("utility", true);

        // Create the plugin management panel (hidden by default)
        pluginManagementPanel = new PluginManagementPanel(
            () -> { showPluginRegistrationWizard(); refreshPluginPanel(); },
            () -> { registerAllPluginActions(); refreshPluginPanel(); },
            () -> { rescanPlugins(); refreshPluginPanel(); },
            () -> refreshPluginPanel()
        );
        FrameDescriptor pluginMenuPanel = context.registerUI(
            "pluginmenu",
            pluginManagementPanel,
            createPluginMenuBar(),
            createLifecycleListener("Plugin Management"),
            WindowPosition.at(300, 200, 650, 400)
        );
        pluginMenuPanel.setWindowTitle("Plugin Management");
        pluginMenuPanel.setAttribute("utility", true);

        // Set control panel as master
        context.registerMasterPanel(controlPanel);

        // Launch and show all panels
        Foundation.launch(context);
        controlPanel.show();
        eventLogPanel.show();
        statsPanel.show();
        // Advanced manager is hidden by default - user can show via control panel

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

        // Section 3: Plugin System
        contentPanel.add(createSectionHeader("3. Plugin System"));
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createPluginSection());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        // Section 4: Registered Panels List
        contentPanel.add(createSectionHeader("4. Panel Registry"));
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

        // Menu bar option checkbox
        JCheckBox menuBarCheckbox = new JCheckBox("Include menu bar with File and View menus");
        menuBarCheckbox.setOpaque(false);
        menuBarCheckbox.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
        section.add(menuBarCheckbox);
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

            boolean includeMenuBar = menuBarCheckbox.isSelected();
            createDynamicPanel(strategyName, position, includeMenuBar);
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
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(350, 120));
        buttonsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JButton showAdvancedManagerButton = new JButton("Show Advanced Frame Manager");
        showAdvancedManagerButton.addActionListener(e -> {
            FrameDescriptor manager = context.getFrameDescriptor("advancedmanager");
            if (manager != null) {
                manager.show();
            }
        });
        buttonsPanel.add(showAdvancedManagerButton);

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
     * Creates the plugin system section.
     */
    private static JPanel createPluginSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><i>Silver tier plugin management:</i></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        section.add(infoLabel);
        section.add(Box.createVerticalStrut(8));

        // Single button to open plugin management panel
        JButton showPluginMenuButton = new JButton("Show Plugin Management");
        showPluginMenuButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        showPluginMenuButton.setMaximumSize(new Dimension(350, 30));
        showPluginMenuButton.addActionListener(e -> showPluginMenu());
        section.add(showPluginMenuButton);

        return section;
    }


    /**
     * Registers all enabled plugins with the action registry.
     */
    private static void registerAllPluginActions() {
        uiPluginManager pluginManager = Foundation.getPluginManager();
        if (pluginManager == null) {
            JOptionPane.showMessageDialog(null,
                "Plugin system not initialized",
                "Plugin System",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        PluginActionRegistry actionRegistry = pluginManager.getPluginActionRegistry();

        List<javax.swing.Action> actions = actionRegistry.registerAllEnabledPlugins();
        logEvent("PLUGIN", "Registered " + actions.size() + " plugin action(s)");

        JOptionPane.showMessageDialog(null,
            "Registered " + actions.size() + " plugin actions",
            "Plugin Actions",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the plugin menu panel (Framework-managed).
     */
    private static void showPluginMenu() {
        // Get the plugin menu panel descriptor
        FrameDescriptor menuPanel = context.getFrameDescriptor("pluginmenu");
        if (menuPanel != null) {
            menuPanel.show();
        } else {
            JOptionPane.showMessageDialog(null,
                "Plugin menu panel not found",
                "Plugin System",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Rescans the plugins directory for new plugins.
     */
    private static void rescanPlugins() {
        uiPluginManager pluginManager = Foundation.getPluginManager();
        if (pluginManager == null || !pluginManager.isInitialized()) {
            JOptionPane.showMessageDialog(null,
                "Plugin system not initialized",
                "Plugin System",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<UnregisteredPlugin> discovered = pluginManager.rescanPlugins();
        logEvent("PLUGIN", "Rescan complete: " + discovered.size() + " new plugin(s) found");

        JOptionPane.showMessageDialog(null,
            "Found " + discovered.size() + " new plugin(s)",
            "Plugin Rescan",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the plugin registration wizard for discovered plugins.
     */
    private static void showPluginRegistrationWizard() {
        uiPluginManager pluginManager = Foundation.getPluginManager();
        if (pluginManager == null || !pluginManager.isInitialized()) {
            JOptionPane.showMessageDialog(null,
                "Plugin system not initialized",
                "Plugin System",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<UnregisteredPlugin> discovered = pluginManager.getDiscoveredPlugins();

        if (discovered.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "No unregistered plugins found.\n\n" +
                "Place plugin directories in:\n" +
                pluginManager.getPluginsDir().getAbsolutePath(),
                "Plugin Registration",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create registration dialog
        JPanel wizardPanel = new JPanel(new BorderLayout(10, 10));
        wizardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JLabel headerLabel = new JLabel(
            "<html><b>Register Discovered Plugins</b><br>" +
            "Select plugins to register and configure their launch settings.</html>");
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wizardPanel.add(headerLabel, BorderLayout.NORTH);

        // Plugin list with checkboxes and launch mode selectors
        JPanel pluginListPanel = new JPanel();
        pluginListPanel.setLayout(new BoxLayout(pluginListPanel, BoxLayout.Y_AXIS));

        List<JCheckBox> checkboxes = new ArrayList<>();
        List<JComboBox<LaunchMode>> launchModeBoxes = new ArrayList<>();

        for (UnregisteredPlugin plugin : discovered) {
            JPanel pluginPanel = new JPanel(new BorderLayout(5, 5));
            pluginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            // Checkbox with plugin info
            JCheckBox checkbox = new JCheckBox(
                String.format("<html><b>%s</b> v%s<br><i>%s</i></html>",
                    plugin.getName(),
                    plugin.getVersion(),
                    plugin.getDescription() != null ? plugin.getDescription() : "No description")
            );
            checkbox.setSelected(true); // Default to selected
            checkboxes.add(checkbox);

            // Launch mode selector
            JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            settingsPanel.add(new JLabel("Launch Mode:"));

            JComboBox<LaunchMode> launchModeBox = new JComboBox<>(new LaunchMode[] {
                LaunchMode.AUTO,
                LaunchMode.ISOLATED_JVM,
                LaunchMode.EXTERNAL_PROCESS,
                LaunchMode.SHARED_LIBS_JVM,
                LaunchMode.SHARED_JVM
            });
            launchModeBox.setSelectedItem(plugin.getSuggestedLaunchMode());
            launchModeBoxes.add(launchModeBox);
            settingsPanel.add(launchModeBox);

            pluginPanel.add(checkbox, BorderLayout.CENTER);
            pluginPanel.add(settingsPanel, BorderLayout.SOUTH);

            pluginListPanel.add(pluginPanel);
            pluginListPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(pluginListPanel);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        wizardPanel.add(scrollPane, BorderLayout.CENTER);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
            null,
            wizardPanel,
            "Plugin Registration Wizard",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            // Register selected plugins
            int registered = 0;
            for (int i = 0; i < discovered.size(); i++) {
                if (checkboxes.get(i).isSelected()) {
                    UnregisteredPlugin plugin = discovered.get(i);
                    LaunchMode selectedMode = (LaunchMode) launchModeBoxes.get(i).getSelectedItem();

                    try {
                        pluginManager.registerPlugin(plugin, selectedMode, true);
                        logEvent("PLUGIN", "Registered: " + plugin.getName() + " (" + selectedMode + ")");
                        registered++;
                    } catch (Exception ex) {
                        logEvent("PLUGIN", "Failed to register: " + plugin.getName() + " - " + ex.getMessage());
                        JOptionPane.showMessageDialog(null,
                            "Failed to register plugin: " + plugin.getName() + "\n" + ex.getMessage(),
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            if (registered > 0) {
                JOptionPane.showMessageDialog(null,
                    "Successfully registered " + registered + " plugin(s)",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
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
     * Creates the menu bar for the plugin menu panel.
     */
    private static JMenuBar createPluginMenuBar() {
        uiPluginManager pluginManager = Foundation.getPluginManager();

        if (pluginManager == null || !pluginManager.isInitialized()) {
            return null;
        }

        PluginActionRegistry actionRegistry = pluginManager.getPluginActionRegistry();

        // Register actions if not already done
        if (actionRegistry.getAllActions().isEmpty()) {
            actionRegistry.registerAllEnabledPlugins();
        }

        JMenu pluginMenu = actionRegistry.createPluginMenu("Plugins");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(pluginMenu);

        return menuBar;
    }

    /**
     * Refreshes the plugin panel's table data.
     */
    private static void refreshPluginPanel() {
        if (pluginManagementPanel != null) {
            pluginManagementPanel.refreshTableData();
        }
    }

    /**
     * Creates a dynamic panel with the specified positioning strategy.
     */
    private static void createDynamicPanel(String strategyName, WindowPosition position, boolean includeMenuBar) {
        int panelNum = panelCounter.getAndIncrement();
        String panelId = "panel" + panelNum;

        // Create panel content
        JPanel content = createDynamicPanelContent(panelNum, strategyName);

        // Create menu bar if requested (must be created before registerUI)
        JMenuBar menuBar = includeMenuBar ? createSampleMenuBar(panelNum) : null;

        // Register panel with menu bar
        FrameDescriptor registration = context.registerUI(
            panelId,
            content,
            menuBar,
            createLifecycleListener("Dynamic Panel #" + panelNum),
            position
        );
        registration.setAttribute("dynamic", true);

        // Show the panel
        registration.show();

        logEvent("CREATED", "Panel #" + panelNum + " with " + strategyName + " positioning");
    }

    /**
     * Creates a sample menu bar for demonstration purposes.
     */
    private static JMenuBar createSampleMenuBar(final int panelNum) {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(e ->
            JOptionPane.showMessageDialog(null,
                "New action for Panel #" + panelNum,
                "File Menu",
                JOptionPane.INFORMATION_MESSAGE));
        fileMenu.add(newItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e ->
            JOptionPane.showMessageDialog(null,
                "Save action for Panel #" + panelNum,
                "File Menu",
                JOptionPane.INFORMATION_MESSAGE));
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Close Panel");
        exitItem.addActionListener(e -> {
            context.closePanel("panel" + panelNum);
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem detachItem = new JMenuItem("Detach/Attach");
        detachItem.addActionListener(e -> {
            context.detachPanel("panel" + panelNum);
        });
        viewMenu.add(detachItem);

        viewMenu.addSeparator();

        JMenuItem aboutItem = new JMenuItem("About Panel");
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(null,
                "Dynamic Panel #" + panelNum + "\n" +
                "Full ID: showcase:panel" + panelNum + "\n\n" +
                "This menu bar is automatically transferred\n" +
                "when detaching/attaching the panel!",
                "About",
                JOptionPane.INFORMATION_MESSAGE));
        viewMenu.add(aboutItem);

        menuBar.add(viewMenu);

        return menuBar;
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

        // Buttons
        JButton detachButton = new JButton("Detach/Attach");
        detachButton.setToolTipText("Toggle between internal frame (desktop) and external frame (standalone window)");
        detachButton.addActionListener(e -> {
            context.detachPanel("panel" + panelNum);
        });

        JButton closeButton = new JButton("Close This Panel");
        closeButton.addActionListener(e -> {
            context.closePanel("panel" + panelNum);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.add(detachButton);
        buttonPanel.add(closeButton);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a lifecycle listener that logs events.
     */
    private static PanelLifecycleListener createLifecycleListener(final String panelName) {
        return new PanelLifecycleListener() {
            @Override
            public void onCreate(IWindow window) {
                logEvent("onCreate", panelName);
            }

            @Override
            public void onShow(IWindow window) {
                logEvent("onShow", panelName);
                // Updates now handled by registry change listener
            }

            @Override
            public void onHide(IWindow window) {
                logEvent("onHide", panelName);
                // Updates now handled by registry change listener
            }

            @Override
            public void onClose(IWindow window) {
                logEvent("onClose", panelName);
                // Updates now handled by registry change listener
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
            int visibleCount = 0;
            int hiddenCount = 0;
            List<FrameDescriptor> allPanels = context.getRegistrations();
            for (FrameDescriptor reg : allPanels ) {
                if (reg != null && reg.isVisible()) {
                    visibleCount++;
                } else {
                    hiddenCount++;
                }
            }

            StringBuilder stats = new StringBuilder("<html><body style='padding: 10px;'>");
            stats.append("<b>Registry Overview:</b><br>");
            stats.append("Namespace: showcase<br>");
            stats.append("Total Panels: ").append(allPanels.size()).append("<br>");
            stats.append("Visible Panels: ").append(visibleCount).append("<br>");
            stats.append("Hidden Panels: ").append(hiddenCount).append("<br><br>");

            stats.append("<br><b>All Panels:</b><br>");
            for (FrameDescriptor reg : allPanels ) {
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
            List<FrameDescriptor> panelList = context.getRegistrations();
            for (FrameDescriptor reg : panelList ) {
                // Skip the control panel - it's permanent and shouldn't be managed
                if ("control".equals(reg.getPanelId())) {
                    continue;
                }
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
        List<FrameDescriptor> panelList = context.getRegistrations();
        for (FrameDescriptor panel : panelList ) {
            panel.show();
        }
        // Updates now handled by registry change listener
    }

    /**
     * Hides all dynamic panels (keeps permanent panels visible).
     * <p>
     * Uses the "dynamic" attribute to identify which panels to hide.
     */
    private static void hideAllDynamicPanels() {
        List<FrameDescriptor> allPanels = context.getRegistrations();
        for (FrameDescriptor reg : allPanels ) {
            if (isPanelDynamic(reg)) {
                reg.hide();
            }
        }
        // Updates now handled by registry change listener
    }

    /**
     * Closes all dynamic panels (keeps permanent panels).
     * <p>
     * Uses the "dynamic" attribute to identify which panels to close.
     */
    private static void closeAllDynamicPanels() {
        List<FrameDescriptor> allPanels = context.getRegistrations();
        for (FrameDescriptor reg : allPanels ) {
            if (isPanelDynamic(reg)) reg.getWindow().close();
            // This does not actual remove from uiContext but close enough for demo app
        }
        // Updates now handled by registry change listener
    }

    /**
     * Check if a panel is dynamic (i.e., not permanent).
     * <p>
     * Uses the panel's attributes map to check for the "dynamic" attribute.
     * This is cleaner than checking panel IDs against a hardcoded list.
     *
     * @param reg the panel registration to check
     * @return true if the panel is dynamic, false if permanent
     */
    private static boolean isPanelDynamic(FrameDescriptor reg) {
        return Boolean.TRUE.equals(reg.getAttribute("dynamic"));
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
