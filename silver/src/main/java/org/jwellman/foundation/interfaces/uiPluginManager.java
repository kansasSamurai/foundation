package org.jwellman.foundation.interfaces;

import org.jwellman.foundation.plugin.LaunchMode;
import org.jwellman.foundation.plugin.LoadedPlugin;
import org.jwellman.foundation.plugin.PluginRegistration;
import org.jwellman.foundation.plugin.UnregisteredPlugin;

import java.io.IOException;
import java.util.List;

/**
 * Interface for managing plugins in the Foundation framework.
 * Plugins extend the framework's capabilities at runtime.
 *
 * <p>The plugin manager provides high-level operations for:</p>
 * <ul>
 *   <li>Discovering new plugins</li>
 *   <li>Registering and unregistering plugins</li>
 *   <li>Launching and managing plugin instances</li>
 *   <li>Querying plugin state</li>
 * </ul>
 */
public interface uiPluginManager {

    /**
     * Initializes the plugin system.
     *
     * @throws IOException if initialization fails
     */
    void initialize() throws IOException;

    /**
     * Shuts down the plugin system.
     *
     * @throws IOException if shutdown fails
     */
    void shutdown() throws IOException;

    /**
     * Gets discovered but unregistered plugins.
     *
     * @return list of unregistered plugins
     */
    List<UnregisteredPlugin> getDiscoveredPlugins();

    /**
     * Registers a plugin.
     *
     * @param plugin the unregistered plugin
     * @param launchMode the launch mode
     * @param enabled whether the plugin is enabled
     * @throws IOException if registration fails
     */
    void registerPlugin(UnregisteredPlugin plugin, LaunchMode launchMode, boolean enabled)
            throws IOException;

    /**
     * Unregisters a plugin.
     *
     * @param pluginId the plugin ID
     * @throws IOException if unregistration fails
     */
    void unregisterPlugin(String pluginId) throws IOException;

    /**
     * Launches a plugin.
     *
     * @param pluginId the plugin ID
     * @return the loaded plugin instance
     * @throws Exception if launch fails
     */
    LoadedPlugin launchPlugin(String pluginId) throws Exception;

    /**
     * Closes a loaded plugin.
     *
     * @param pluginId the plugin ID
     * @throws IOException if closing fails
     */
    void closePlugin(String pluginId) throws IOException;

    /**
     * Checks if a plugin is registered.
     *
     * @param pluginId the plugin ID
     * @return true if registered
     */
    boolean isPluginRegistered(String pluginId);

    /**
     * Checks if a plugin is loaded.
     *
     * @param pluginId the plugin ID
     * @return true if loaded
     */
    boolean isPluginLoaded(String pluginId);

    /**
     * Gets all registered plugins.
     *
     * @return list of plugin registrations
     */
    List<PluginRegistration> getAllRegisteredPlugins();

    /**
     * Gets all loaded plugins.
     *
     * @return list of loaded plugins
     */
    List<LoadedPlugin> getLoadedPlugins();
}
