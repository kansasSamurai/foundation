package org.jwellman.foundation.plugin;

import org.jwellman.foundation.interfaces.uiPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main orchestrator for the Foundation plugin system.
 *
 * <p>The PluginManager coordinates all plugin-related operations including:</p>
 * <ul>
 *   <li>Registry management (load/save plugin registrations)</li>
 *   <li>Plugin discovery (scan for new plugins)</li>
 *   <li>Plugin launching (execute plugins in various modes)</li>
 *   <li>Lifecycle tracking (manage loaded plugin instances)</li>
 * </ul>
 *
 * <p><strong>Typical usage flow:</strong></p>
 * <pre>
 * // Initialize plugin system
 * PluginManager manager = new PluginManager(
 *     new File("./config"),
 *     new File("./plugins")
 * );
 * manager.initialize();
 *
 * // Get newly discovered plugins (for user registration)
 * List&lt;UnregisteredPlugin&gt; newPlugins = manager.getDiscoveredPlugins();
 *
 * // Register a plugin
 * manager.registerPlugin(newPlugin, LaunchMode.ISOLATED_JVM, true);
 *
 * // Launch a registered plugin
 * LoadedPlugin plugin = manager.launchPlugin("myapp");
 * plugin.run(new String[] {"--config", "app.properties"});
 *
 * // Shutdown
 * manager.shutdown();
 * </pre>
 */
public class PluginManager implements uiPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final PluginRegistry registry;
    private final PluginDiscoveryService discovery;
    private final PluginLauncher launcher;
    private final Map<String, LoadedPlugin> loadedPlugins;
    private final File configDir;
    private final File pluginsDir;

    private boolean initialized = false;

    /**
     * Creates a plugin manager with the specified configuration and plugins directories.
     *
     * @param configDir the configuration directory (contains registry.json)
     * @param pluginsDir the plugins directory (contains plugin subdirectories)
     */
    public PluginManager(File configDir, File pluginsDir) {
        this.configDir = configDir;
        this.pluginsDir = pluginsDir;
        this.registry = new PluginRegistry(new File(configDir, "registry.json"));
        this.discovery = new PluginDiscoveryService(pluginsDir, registry);
        this.launcher = new PluginLauncher();
        this.loadedPlugins = new HashMap<>();
    }

    // ========== Initialization and Lifecycle ==========

    /**
     * Initializes the plugin system.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Creates configuration and plugins directories if needed</li>
     *   <li>Loads the plugin registry from disk</li>
     *   <li>Scans for newly discovered plugins</li>
     * </ol>
     *
     * @throws IOException if initialization fails
     */
    public void initialize() throws IOException {
        if (initialized) {
            logger.warn("PluginManager already initialized");
            return;
        }

        logger.info("Initializing plugin system...");

        // Ensure directories exist
        ensureDirectoriesExist();

        // Load registry
        logger.info("Loading plugin registry from: {}", registry.getRegistryFile().getAbsolutePath());
        registry.load();
        logger.info("Loaded {} registered plugin(s)", registry.getPluginCount());

        // Discover new plugins
        logger.info("Scanning for new plugins in: {}", pluginsDir.getAbsolutePath());
        List<UnregisteredPlugin> discovered = discovery.scanForNewPlugins();

        if (!discovered.isEmpty()) {
            logger.info("Discovered {} new plugin(s) awaiting registration", discovered.size());
        }

        initialized = true;
        logger.info("Plugin system initialized successfully");
    }

    /**
     * Shuts down the plugin system, closing all loaded plugins.
     *
     * @throws IOException if shutdown fails
     */
    public void shutdown() throws IOException {
        logger.info("Shutting down plugin system...");

        // Close all loaded plugins
        List<String> pluginIds = new ArrayList<>(loadedPlugins.keySet());
        for (String pluginId : pluginIds) {
            try {
                closePlugin(pluginId);
            } catch (Exception e) {
                logger.error("Error closing plugin: {}", pluginId, e);
            }
        }

        // Save registry
        registry.save();

        initialized = false;
        logger.info("Plugin system shutdown complete");
    }

    /**
     * Ensures configuration and plugins directories exist.
     */
    private void ensureDirectoriesExist() {
        if (!configDir.exists()) {
            logger.info("Creating config directory: {}", configDir.getAbsolutePath());
            configDir.mkdirs();
        }

        if (!pluginsDir.exists()) {
            logger.info("Creating plugins directory: {}", pluginsDir.getAbsolutePath());
            pluginsDir.mkdirs();
        }
    }

    // ========== Discovery and Registration ==========

    /**
     * Gets the list of discovered but unregistered plugins.
     *
     * <p>This method rescans the plugins directory for new plugins.</p>
     *
     * @return list of unregistered plugins
     */
    public List<UnregisteredPlugin> getDiscoveredPlugins() {
        return discovery.scanForNewPlugins();
    }

    /**
     * Rescans the plugins directory for new plugins.
     * Alias for {@link #getDiscoveredPlugins()}.
     *
     * @return list of unregistered plugins
     */
    public List<UnregisteredPlugin> rescanPlugins() {
        return discovery.rescan();
    }

    /**
     * Registers a newly discovered plugin in the registry.
     *
     * @param plugin the unregistered plugin to register
     * @param launchMode the launch mode to use for this plugin
     * @param enabled whether the plugin should be enabled
     * @throws IOException if saving registry fails
     */
    public void registerPlugin(UnregisteredPlugin plugin, LaunchMode launchMode, boolean enabled)
            throws IOException {

        logger.info("Registering plugin: {} (mode: {}, enabled: {})",
                plugin.getId(), launchMode, enabled);

        PluginRegistration registration = new PluginRegistration(
                plugin.getId(),
                plugin.getName(),
                plugin.getPluginDir(),
                launchMode,
                enabled
        );

        registry.register(registration);
        registry.save();

        logger.info("Plugin registered successfully: {}", plugin.getId());
    }

    /**
     * Unregisters a plugin from the registry.
     *
     * <p>If the plugin is currently loaded, it will be closed first.</p>
     *
     * @param pluginId the plugin ID to unregister
     * @throws IOException if saving registry fails
     */
    public void unregisterPlugin(String pluginId) throws IOException {
        logger.info("Unregistering plugin: {}", pluginId);

        // Close plugin if loaded
        if (isPluginLoaded(pluginId)) {
            closePlugin(pluginId);
        }

        PluginRegistration removed = registry.unregister(pluginId);
        if (removed != null) {
            registry.save();
            logger.info("Plugin unregistered successfully: {}", pluginId);
        } else {
            logger.warn("Plugin not found in registry: {}", pluginId);
        }
    }

    // ========== Plugin Launching and Management ==========

    /**
     * Launches a registered plugin.
     *
     * <p>The plugin must be registered and enabled. If already loaded,
     * returns the existing LoadedPlugin instance (unless multiple instances allowed).</p>
     *
     * @param pluginId the plugin ID to launch
     * @return the loaded plugin instance
     * @throws Exception if plugin launch fails
     */
    public LoadedPlugin launchPlugin(String pluginId) throws Exception {
        return launchPlugin(pluginId, null);
    }

    /**
     * Launches a registered plugin with custom arguments.
     *
     * @param pluginId the plugin ID to launch
     * @param args the arguments to pass to the plugin (overrides descriptor appArgs)
     * @return the loaded plugin instance
     * @throws Exception if plugin launch fails
     */
    public LoadedPlugin launchPlugin(String pluginId, String[] args) throws Exception {
        logger.info("Launching plugin: {}", pluginId);

        // Get registration
        PluginRegistration registration = registry.getPlugin(pluginId);
        if (registration == null) {
            throw new IllegalArgumentException("Plugin not registered: " + pluginId);
        }

        if (!registration.isEnabled()) {
            throw new IllegalStateException("Plugin is disabled: " + pluginId);
        }

        // Load descriptor
        PluginDescriptor descriptor = PluginDescriptor.load(registration.getPluginDir());

        // Check if already loaded
        if (loadedPlugins.containsKey(pluginId)) {
            if (!descriptor.isAllowMultipleInstances()) {
                logger.warn("Plugin already loaded and multiple instances not allowed: {}", pluginId);
                return loadedPlugins.get(pluginId);
            }
        }

        // Launch plugin
        LoadedPlugin loaded = launcher.launch(pluginId, descriptor, registration.getLaunchMode());

        // Track loaded plugin
        loadedPlugins.put(pluginId, loaded);

        logger.info("Plugin launched successfully: {}", pluginId);

        return loaded;
    }

    /**
     * Closes a loaded plugin, releasing resources.
     *
     * @param pluginId the plugin ID to close
     * @throws IOException if closing fails
     */
    public void closePlugin(String pluginId) throws IOException {
        logger.info("Closing plugin: {}", pluginId);

        LoadedPlugin plugin = loadedPlugins.remove(pluginId);
        if (plugin != null) {
            plugin.close();
            logger.info("Plugin closed successfully: {}", pluginId);
        } else {
            logger.warn("Plugin not loaded: {}", pluginId);
        }
    }

    /**
     * Forcibly closes a loaded plugin.
     *
     * @param pluginId the plugin ID to close
     * @throws IOException if closing fails
     */
    public void forceClosePlugin(String pluginId) throws IOException {
        logger.info("Force closing plugin: {}", pluginId);

        LoadedPlugin plugin = loadedPlugins.remove(pluginId);
        if (plugin != null) {
            plugin.forceClose();
            logger.info("Plugin force closed successfully: {}", pluginId);
        } else {
            logger.warn("Plugin not loaded: {}", pluginId);
        }
    }

    // ========== Query Methods ==========

    /**
     * Checks if a plugin is registered.
     *
     * @param pluginId the plugin ID
     * @return true if plugin is registered
     */
    public boolean isPluginRegistered(String pluginId) {
        return registry.isRegistered(pluginId);
    }

    /**
     * Checks if a plugin is currently loaded.
     *
     * @param pluginId the plugin ID
     * @return true if plugin is loaded
     */
    public boolean isPluginLoaded(String pluginId) {
        return loadedPlugins.containsKey(pluginId);
    }

    /**
     * Gets a loaded plugin instance.
     *
     * @param pluginId the plugin ID
     * @return the loaded plugin, or null if not loaded
     */
    public LoadedPlugin getLoadedPlugin(String pluginId) {
        return loadedPlugins.get(pluginId);
    }

    /**
     * Gets all loaded plugins.
     *
     * @return list of loaded plugins
     */
    public List<LoadedPlugin> getLoadedPlugins() {
        return new ArrayList<>(loadedPlugins.values());
    }

    /**
     * Gets a plugin registration.
     *
     * @param pluginId the plugin ID
     * @return the plugin registration, or null if not found
     */
    public PluginRegistration getPluginRegistration(String pluginId) {
        return registry.getPlugin(pluginId);
    }

    /**
     * Gets all registered plugins.
     *
     * @return list of all plugin registrations
     */
    public List<PluginRegistration> getAllRegisteredPlugins() {
        return registry.getAllPlugins();
    }

    /**
     * Gets all enabled registered plugins.
     *
     * @return list of enabled plugin registrations
     */
    public List<PluginRegistration> getEnabledPlugins() {
        return registry.getEnabledPlugins();
    }

    /**
     * Updates a plugin registration and saves the registry.
     *
     * @param registration the updated registration
     * @throws IOException if saving registry fails
     */
    public void updatePluginRegistration(PluginRegistration registration) throws IOException {
        registry.update(registration);
        registry.save();
    }

    // ========== Statistics ==========

    /**
     * Gets the number of registered plugins.
     *
     * @return the registered plugin count
     */
    public int getRegisteredPluginCount() {
        return registry.getPluginCount();
    }

    /**
     * Gets the number of loaded plugins.
     *
     * @return the loaded plugin count
     */
    public int getLoadedPluginCount() {
        return loadedPlugins.size();
    }

    // ========== Accessors ==========

    /**
     * Gets the plugin registry.
     *
     * @return the plugin registry
     */
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * Gets the plugin discovery service.
     *
     * @return the discovery service
     */
    public PluginDiscoveryService getDiscoveryService() {
        return discovery;
    }

    /**
     * Gets the plugin launcher.
     *
     * @return the plugin launcher
     */
    public PluginLauncher getLauncher() {
        return launcher;
    }

    /**
     * Gets the configuration directory.
     *
     * @return the config directory
     */
    public File getConfigDir() {
        return configDir;
    }

    /**
     * Gets the plugins directory.
     *
     * @return the plugins directory
     */
    public File getPluginsDir() {
        return pluginsDir;
    }

    /**
     * Checks if the plugin manager has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
