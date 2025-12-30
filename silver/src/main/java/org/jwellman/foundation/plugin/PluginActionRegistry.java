package org.jwellman.foundation.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for plugin-related Swing Actions.
 *
 * <p>The PluginActionRegistry helps applications integrate plugins into their
 * UI by automatically creating Swing Actions for launching plugins. These actions
 * can be used to build plugin menus, toolbars, or buttons.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Auto-creates launch actions from plugin metadata</li>
 *   <li>Loads plugin icons from plugin directories</li>
 *   <li>Builds plugin menus and toolbars</li>
 *   <li>Tracks action-to-plugin mapping</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * PluginActionRegistry actionRegistry = new PluginActionRegistry(pluginManager);
 *
 * // Register actions for all enabled plugins
 * for (PluginRegistration reg : pluginManager.getEnabledPlugins()) {
 *     actionRegistry.registerPluginAction(reg);
 * }
 *
 * // Create a plugins menu
 * JMenu pluginsMenu = actionRegistry.createPluginMenu("Plugins");
 * menuBar.add(pluginsMenu);
 *
 * // Or create a toolbar
 * JToolBar toolbar = actionRegistry.createPluginToolbar();
 * frame.add(toolbar, BorderLayout.NORTH);
 * </pre>
 */
public class PluginActionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PluginActionRegistry.class);

    private final PluginManager pluginManager;
    private final Map<String, Action> actions;

    /**
     * Creates a plugin action registry.
     *
     * @param pluginManager the plugin manager
     */
    public PluginActionRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.actions = new HashMap<>();
    }

    /**
     * Registers a launch action for a plugin.
     *
     * @param registration the plugin registration
     * @return the created action
     */
    public Action registerPluginAction(PluginRegistration registration) {
        String pluginId = registration.getId();

        if (actions.containsKey(pluginId)) {
            logger.warn("Action already registered for plugin: {}", pluginId);
            return actions.get(pluginId);
        }

        // Create action
        Action action = createLaunchAction(registration);
        actions.put(pluginId, action);

        logger.debug("Registered action for plugin: {}", pluginId);
        return action;
    }

    /**
     * Unregisters an action for a plugin.
     *
     * @param pluginId the plugin ID
     * @return the removed action, or null if not found
     */
    public Action unregisterPluginAction(String pluginId) {
        Action removed = actions.remove(pluginId);
        if (removed != null) {
            logger.debug("Unregistered action for plugin: {}", pluginId);
        }
        return removed;
    }

    /**
     * Gets an action for a plugin.
     *
     * @param pluginId the plugin ID
     * @return the action, or null if not registered
     */
    public Action getAction(String pluginId) {
        return actions.get(pluginId);
    }

    /**
     * Gets all registered actions.
     *
     * @return list of all actions
     */
    public List<Action> getAllActions() {
        return new ArrayList<>(actions.values());
    }

    /**
     * Clears all registered actions.
     */
    public void clearActions() {
        actions.clear();
        logger.debug("Cleared all plugin actions");
    }

    /**
     * Registers actions for all enabled plugins.
     *
     * @return list of created actions
     */
    public List<Action> registerAllEnabledPlugins() {
        List<Action> createdActions = new ArrayList<>();

        for (PluginRegistration reg : pluginManager.getEnabledPlugins()) {
            Action action = registerPluginAction(reg);
            createdActions.add(action);
        }

        logger.info("Registered {} plugin action(s)", createdActions.size());
        return createdActions;
    }

    /**
     * Creates a JMenu containing all registered plugin actions.
     *
     * @param menuTitle the menu title
     * @return the created menu
     */
    public JMenu createPluginMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);

        if (actions.isEmpty()) {
            javax.swing.JMenuItem noPlugins = new javax.swing.JMenuItem("(No plugins available)");
            noPlugins.setEnabled(false);
            menu.add(noPlugins);
        } else {
            // Sort actions by plugin name
            List<Action> sortedActions = new ArrayList<>(actions.values());
            sortedActions.sort((a1, a2) -> {
                String name1 = (String) a1.getValue(Action.NAME);
                String name2 = (String) a2.getValue(Action.NAME);
                return name1.compareTo(name2);
            });

            for (Action action : sortedActions) {
                menu.add(action);
            }
        }

        logger.debug("Created plugin menu with {} item(s)", actions.size());
        return menu;
    }

    /**
     * Creates a JToolBar containing all registered plugin actions.
     *
     * @return the created toolbar
     */
    public JToolBar createPluginToolbar() {
        JToolBar toolbar = new JToolBar("Plugins");

        if (actions.isEmpty()) {
            toolbar.add(new javax.swing.JLabel("No plugins"));
        } else {
            // Sort actions by plugin name
            List<Action> sortedActions = new ArrayList<>(actions.values());
            sortedActions.sort((a1, a2) -> {
                String name1 = (String) a1.getValue(Action.NAME);
                String name2 = (String) a2.getValue(Action.NAME);
                return name1.compareTo(name2);
            });

            for (Action action : sortedActions) {
                toolbar.add(action);
            }
        }

        logger.debug("Created plugin toolbar with {} button(s)", actions.size());
        return toolbar;
    }

    /**
     * Creates a launch action for a plugin.
     *
     * @param registration the plugin registration
     * @return the created action
     */
    private Action createLaunchAction(PluginRegistration registration) {
        String pluginId = registration.getId();
        String name = registration.getName();

        // Try to load plugin descriptor for additional metadata
        String description = null;
        ImageIcon icon = null;

        try {
            PluginDescriptor descriptor = PluginDescriptor.load(registration.getPluginDir());
            description = descriptor.getDescription();

            // Try to load icon
            String iconPath = descriptor.getIcon();
            if (iconPath != null && !iconPath.isEmpty()) {
                File iconFile = new File(registration.getPluginDir(), iconPath);
                if (iconFile.exists()) {
                    icon = new ImageIcon(iconFile.getAbsolutePath());
                    logger.debug("Loaded icon for plugin {}: {}", pluginId, iconPath);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load plugin descriptor for {}: {}", pluginId, e.getMessage());
        }

        // Create action
        final String finalDescription = description;
        Action action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchPlugin(pluginId);
            }
        };

        // Set action properties
        action.putValue(Action.SHORT_DESCRIPTION, finalDescription != null ? finalDescription : "Launch " + name);
        action.putValue("pluginId", pluginId); // Store plugin ID for later reference

        return action;
    }

    /**
     * Launches a plugin by ID.
     * Called when a plugin action is triggered.
     *
     * @param pluginId the plugin ID to launch
     */
    private void launchPlugin(String pluginId) {
        logger.info("Launching plugin via action: {}", pluginId);

        try {
            LoadedPlugin plugin = pluginManager.launchPlugin(pluginId);

            // Run plugin main method with empty args
            plugin.run(new String[0]);

            logger.info("Plugin launched successfully: {}", pluginId);

        } catch (Exception e) {
            logger.error("Failed to launch plugin: {}", pluginId, e);

            // Show error dialog
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Failed to launch plugin: " + e.getMessage(),
                    "Plugin Launch Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Gets the plugin manager.
     *
     * @return the plugin manager
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
