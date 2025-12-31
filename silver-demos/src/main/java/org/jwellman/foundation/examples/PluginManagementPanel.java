package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.interfaces.uiPluginManager;
import org.jwellman.foundation.plugin.PluginRegistration;
import org.jwellman.foundation.plugin.UnregisteredPlugin;

/**
 * A comprehensive plugin management panel for the Foundation framework.
 * <p>
 * This panel demonstrates best practices for creating reusable UI components:
 * <ul>
 *   <li>Extends JPanel - can be used in any window or desktop mode</li>
 *   <li>Self-contained logic - all UI and behavior encapsulated</li>
 *   <li>Callback support - uses Runnable callbacks for external actions</li>
 *   <li>Clean separation - presentation logic separate from framework logic</li>
 * </ul>
 * <p>
 * Features:
 * <ul>
 *   <li>JTable displaying both registered and discovered plugins</li>
 *   <li>Columns: State, Name, ID, Mode, Enabled, Dir</li>
 *   <li>Action buttons for registration, scanning, and refreshing</li>
 *   <li>Professional table-based UI</li>
 * </ul>
 *
 * @author Foundation Framework
 */
@SuppressWarnings("serial")
public class PluginManagementPanel extends JPanel {

    private final Runnable onRegisterDiscovered;
    private final Runnable onRegisterAllActions;
    private final Runnable onRescan;
    private final Runnable onRefresh;

    private JTable pluginTable;
    private DefaultTableModel tableModel;

    /**
     * Creates a plugin management panel with callback actions.
     *
     * @param onRegisterDiscovered callback when "Register Discovered" is clicked
     * @param onRegisterAllActions callback when "Register All Actions" is clicked
     * @param onRescan callback when "Rescan for Plugins" is clicked
     * @param onRefresh callback when "Refresh Display" is clicked
     */
    public PluginManagementPanel(
            Runnable onRegisterDiscovered,
            Runnable onRegisterAllActions,
            Runnable onRescan,
            Runnable onRefresh) {

        this.onRegisterDiscovered = onRegisterDiscovered;
        this.onRegisterAllActions = onRegisterAllActions;
        this.onRescan = onRescan;
        this.onRefresh = onRefresh;

        initializeUI();
    }

    /**
     * Initializes the panel UI.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 248, 250));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header
        JLabel headerLabel = new JLabel("Plugin Management");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(new Color(70, 130, 180));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Get plugin manager
        uiPluginManager pluginManager = Foundation.getPluginManager();

        if (pluginManager == null || !pluginManager.isInitialized()) {
            JLabel errorLabel = new JLabel(
                "<html><center>Plugin system not initialized</center></html>",
                JLabel.CENTER
            );
            errorLabel.setForeground(Color.RED);
            add(headerLabel, BorderLayout.NORTH);
            add(errorLabel, BorderLayout.CENTER);
            return;
        }

        // Create plugin table
        String[] columnNames = {"State", "Name", "ID", "Mode", "Enabled", "Dir"};
        Object[][] data = buildPluginTableData(pluginManager);

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        pluginTable = new JTable(tableModel);
        pluginTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pluginTable.setRowHeight(22);
        pluginTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));

        // Set column widths
        pluginTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // State
        pluginTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Name
        pluginTable.getColumnModel().getColumn(2).setPreferredWidth(100); // ID
        pluginTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Mode
        pluginTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Enabled
        pluginTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Dir

        JScrollPane tableScrollPane = new JScrollPane(pluginTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Info label above table
        JLabel infoLabel = new JLabel(
            "<html><center><i>Use the 'Plugins' menu above to launch registered plugins</i></center></html>",
            JLabel.CENTER
        );
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setOpaque(false);
        tablePanel.add(infoLabel, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonsPanel = createButtonsPanel();

        // Layout
        add(headerLabel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the buttons panel with action buttons.
     */
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonsPanel.setOpaque(false);

        JButton registerDiscoveredButton = new JButton("Register Discovered");
        registerDiscoveredButton.addActionListener(e -> {
            if (onRegisterDiscovered != null) {
                onRegisterDiscovered.run();
            }
        });

        JButton registerAllActionsButton = new JButton("Register All Actions");
        registerAllActionsButton.addActionListener(e -> {
            if (onRegisterAllActions != null) {
                onRegisterAllActions.run();
            }
        });

        JButton rescanButton = new JButton("Rescan for Plugins");
        rescanButton.addActionListener(e -> {
            if (onRescan != null) {
                onRescan.run();
            }
        });

        JButton refreshButton = new JButton("Refresh Display");
        refreshButton.addActionListener(e -> {
            if (onRefresh != null) {
                onRefresh.run();
            }
        });

        buttonsPanel.add(registerDiscoveredButton);
        buttonsPanel.add(registerAllActionsButton);
        buttonsPanel.add(rescanButton);
        buttonsPanel.add(refreshButton);

        return buttonsPanel;
    }

    /**
     * Builds the data array for the plugin table.
     */
    private Object[][] buildPluginTableData(uiPluginManager pluginManager) {
        List<Object[]> rows = new ArrayList<>();

        // Add registered plugins
        List<PluginRegistration> registered = pluginManager.getAllRegisteredPlugins();
        for (PluginRegistration reg : registered) {
            rows.add(new Object[]{
                "Registered",
                reg.getName(),
                reg.getId(),
                reg.getLaunchMode().toString(),
                reg.isEnabled() ? "Yes" : "No",
                reg.getPluginDir().getName()
            });
        }

        // Add discovered plugins
        List<UnregisteredPlugin> discovered = pluginManager.getDiscoveredPlugins();
        for (UnregisteredPlugin plugin : discovered) {
            rows.add(new Object[]{
                "Discovered",
                plugin.getName(),
                plugin.getId(),
                plugin.getSuggestedLaunchMode().toString(),
                "-",
                plugin.getPluginDir().getName()
            });
        }

        return rows.toArray(new Object[0][]);
    }

    /**
     * Refreshes the table data by reloading from the plugin manager.
     * <p>
     * This can be called externally after plugin registration changes.
     */
    public void refreshTableData() {
        uiPluginManager pluginManager = Foundation.getPluginManager();
        if (pluginManager != null && pluginManager.isInitialized() && tableModel != null) {
            Object[][] newData = buildPluginTableData(pluginManager);

            // Clear existing rows
            tableModel.setRowCount(0);

            // Add new rows
            for (Object[] row : newData) {
                tableModel.addRow(row);
            }
        }
    }
}
