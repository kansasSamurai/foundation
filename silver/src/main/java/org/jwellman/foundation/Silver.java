package org.jwellman.foundation;

import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiPluginManager;
import org.jwellman.foundation.plugin.PluginManager;
import org.jwellman.foundation.provider.DefaultViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Silver tier of Foundation framework.
 *
 * <p>Adds plugin system capabilities:</p>
 * <ul>
 *   <li>Plugin discovery and registration</li>
 *   <li>Plugin launching in multiple modes (isolated, shared, external process)</li>
 *   <li>Plugin action registry for UI integration</li>
 * </ul>
 *
 * @author Rick
 */
public class Silver extends Bronze {

    private static final Logger log = LoggerFactory.getLogger(Silver.class);

    /** The plugin manager (initialized by initPlugins()) */
    private PluginManager pluginManager;

    /** Flag to track if plugin system has been initialized */
    private boolean pluginSystemInitialized = false;

    protected void _launch(uiContext ctx) {
        super._launch(ctx);
    }

    /**
     * Silver tier initialization - auto-creates view provider if not set.
     *
     * @param c The application context
     * @return The initialized context
     */
    protected uiContext _init(uiContext c) {
        // Auto-create DefaultViewProvider if not explicitly set (Silver tier feature)
        if (c.getViewProvider() == null) {
            c.setViewProvider(new DefaultViewProvider());
            log.debug("Auto-created DefaultViewProvider for Silver tier");
        }

        // Call parent initialization
        return super._init(c);
    }

    /**
     * Initializes the plugin system.
     *
     * <p>This method must be called AFTER {@link #_init(uiContext)} to ensure
     * the Foundation framework is properly initialized before loading plugins.</p>
     *
     * <p>The plugin system uses directories configured in the master uContext:</p>
     * <ul>
     *   <li>{@code pluginsDirectory} - Scanned for plugin subdirectories</li>
     *   <li>{@code pluginConfigDirectory} - Stores registry.json</li>
     * </ul>
     *
     * <p>If {@code autoDiscoverPlugins} is enabled in the context, this method
     * will automatically scan for new plugins.</p>
     *
     * @throws IllegalStateException if init() has not been called first
     * @throws IOException if plugin system initialization fails
     */
    protected void _initPlugins() throws IOException {
        // Verify init() has been called
        if (!Foundation.isInitialized()) {
            throw new IllegalStateException(
                    "Foundation.init() must be called before initPlugins(). " +
                    "The plugin system requires the Swing framework to be initialized first.");
        }

        if (pluginSystemInitialized) {
            log.warn("Plugin system already initialized, ignoring duplicate call");
            return;
        }

        log.info("Initializing plugin system...");

        // Get plugin configuration from master context
        File pluginsDir = masterContext.getPluginsDirectory();
        File configDir = masterContext.getPluginConfigDirectory();
        boolean autoDiscover = masterContext.isAutoDiscoverPlugins();

        log.info("Plugins directory: {}", pluginsDir.getAbsolutePath());
        log.info("Config directory: {}", configDir.getAbsolutePath());
        log.info("Auto-discover: {}", autoDiscover);

        // Create plugin manager (action registry is created internally)
        pluginManager = new PluginManager(configDir, pluginsDir);
        pluginManager.initialize();

        pluginSystemInitialized = true;

        log.info("Plugin system initialized successfully");
        log.info("Registered plugins: {}", pluginManager.getRegisteredPluginCount());
    }

    /**
     * Gets the plugin manager.
     *
     * @return the plugin manager, or null if plugin system not initialized
     */
    protected uiPluginManager _getPluginManager() {
        return pluginManager;
    }

    /**
     * Checks if the plugin system has been initialized.
     *
     * @return true if plugin system initialized, false otherwise
     */
    public boolean isPluginSystemInitialized() {
        return pluginSystemInitialized;
    }

}
