package org.jwellman.foundation;

import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiPluginManager;
import org.jwellman.foundation.interfaces.uiViewProvider;
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

    /**
     * Silver tier initialization.
     * <p>
     * Calls parent initialization (Bronze -> Stone) then performs Silver-specific setup.
     * This maintains the critical polymorphic initialization chain.
     *
     * @param c The application context
     * @return The initialized context
     */
    @Override
    protected uiContext _init(uiContext c) {
        // CRITICAL: Call parent initialization first (Bronze -> Stone)
        super._init(c);

        // Silver-specific initialization goes here (if any beyond the hook)

        return c;
    }

    /**
     * Hook called after LAF initialization but before window display.
     * <p>
     * Silver tier uses this to create the view provider at the correct time:
     * - After LAF is initialized (so Swing components can be created safely)
     * - Before windows/splash are displayed (so view provider is available)
     */
    @Override
    protected void afterLookAndFeelInitialization() {
        // Call parent hook first (Bronze -> Stone)
        super.afterLookAndFeelInitialization();

        // Auto-create DefaultViewProvider if not explicitly set (Silver tier feature)
        if (masterContext.getViewProvider() == null) {
            masterContext.setViewProvider(new DefaultViewProvider());
            log.debug("Auto-created DefaultViewProvider for Silver tier");
        }
    }

    /**
     * Hook called during launch before the main window is displayed.
     * <p>
     * Silver tier uses this to register a card listener that attaches the menu bar
     * when the "main" card is shown. This ensures the menu bar appears synchronized
     * with the main application content, not during splash screen display.
     */
    @Override
    protected void prepareLaunch() {
        // Call parent hook first (Bronze -> Stone)
        super.prepareLaunch();

        // Register listener to attach menu bar when "main" card is shown
        // This ensures menu bar only appears when main app is visible (not during splash)
        uiViewProvider viewProvider = masterContext.getViewProvider();
        if (viewProvider != null && isDesktop()) {
            viewProvider.addCardListener("main", new Runnable() {
                @Override
                public void run() {
                    attachMenuBarToExternalFrame();
                }
            });
            log.debug("Registered menu bar attachment listener for 'main' card");
        }
    }

    @Override
    protected void _launch(uiContext ctx) {
        super._launch(ctx);
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
