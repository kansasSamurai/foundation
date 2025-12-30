package org.jwellman.foundation;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jwellman.foundation.interfaces.uiContext;

/**
 * A micro-framework for Swing applications.
 * <p>
 * Foundation is the public API that exposes all tier functionality via static methods.<br>
 * It inherits from all tiers (Stone -> Bronze -> Silver -> Gold -> Platinum -> Foundation).
 * <p>
 * Foundation has three primary responsibilities:<br>
 * 1) Discover Look and Feel and initialize the Swing UIManager<br>
 * 2) Manage application context objects (uiContext implementations)<br>
 *    - One context must always be the "master" (controls app lifecycle/shutdown)<br>
 * 3) Create all IWindow objects (JFrame vs JInternalFrame based on master context state)<br>
 * <p>
 * Usage:<br>
 * (1) createContext() - create a uiContext for your application<br>
 * (2) init() - initializes Look and Feel, returns the master uiContext<br>
 * (3) launch() - launches the application with the given uiContext<br>
 *
 * @author Rick Wellman
 */
public class Foundation extends Platinum {

    /** private constructor to enforce singleton pattern */
    private Foundation() {}

    /** The singleton instance - never exposed outside this class */
    private static Foundation instance;

    /**
     * Create a context for a Foundation application using a class name as namespace.
     *
     * @param clazz The class whose fully qualified name will be used as the namespace
     * @return A new uiContext instance
     */
    public static uiContext createContext(Class<?> clazz) {
        ensureInstance();
        return new uContext(instance, clazz.getName());
    }

    /**
     * Create a context for a Foundation application using a string as namespace.
     *
     * @param namespace The namespace identifier for this context
     * @return A new uiContext instance
     */
    public static uiContext createContext(String namespace) {
        ensureInstance();
        return new uContext(instance, namespace);
    }

    /**
     * Ensure the singleton instance exists.
     * <p>
     * This is called by createContext() methods to allow context creation before init().
     */
    private static void ensureInstance() {
        if (instance == null) {
            instance = new Foundation();
        }
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     * <p>
     * This no-args version creates a default uiContext for simple use cases.
     * The default context uses "foundation.app" as the namespace and window mode (not desktop).
     * <p>
     * For production applications, use init(uiContext) with a properly configured context.
     *
     * @return The master uiContext that was initialized
     */
    public static uiContext init() {
        // Create a default context for simple use cases
        // IMPORTANT: Foundation ALWAYS requires a valid uiContext object
        // Never pass null - if context is null, that's a fundamental framework bug
        uiContext defaultContext = Foundation.createContext("foundation.app");
        return Foundation.init(defaultContext);
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     * <p>
     * The main and most important thing this does is initialize the Java Look and Feel;
     * see the _init() method for details on what few other initialization tasks are done.
     * <p>
     * The provided context becomes the "master" context - it controls overall application
     * lifecycle including shutdown behavior.
     *
     * @param c The context (MUST NOT be null - use no-args init() for default context)
     * @return The same instance that was passed in
     * @throws NullPointerException if context is null (indicates framework bug)
     */
    public static uiContext init(uiContext c) {

        // IMPORTANT: context must NEVER be null
        if (c == null) {
            throw new NullPointerException("Context cannot be null");
        }

        // Ensure singleton exists
        ensureInstance();

        // Delegate to tier implementation
        return instance._init(c);
    }

    /**
     * Launch the application with the given context.
     * <p>
     * This creates and displays the main window based on the context configuration.
     * The context determines window mode vs desktop mode, dimensions, title, etc.
     * <p>
     * For Stone tier: This displays a single JFrame (window or desktop mode).
     * For Bronze+ tiers: This can manage multiple panels/windows.
     *
     * @param context The uiContext to launch (typically the master context from init())
     */
    public static void launch(uiContext context) {
        if (instance == null) {
            throw new IllegalStateException(
                "Foundation must be initialized (call init()) before calling launch()");
        }

        // Delegate to tier implementation
        instance._launch(context);
    }

    /**
     * Checks if Foundation has been initialized.
     *
     * @return true if init() has been called, false otherwise
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Get the master application context.
     *
     * @return The master uiContext, or null if not initialized
     */
    public static uiContext getMasterContext() {
        return instance.masterContext;
    }

    public static void showPanel(String namespace, String panelId) {
        instance._showPanel(namespace, panelId);
    }

    public static void hidePanel(String namespace, String panelId) {
        instance._hidePanel(namespace, panelId);
    }

    public static void togglePanel(String namespace, String panelId) {
        instance._togglePanel(namespace, panelId);
    }

    // ========================================================================
    // PLUGIN SYSTEM (Silver Tier)
    // ========================================================================

    /**
     * Initializes the plugin system (Silver tier feature).
     * <p>
     * This method must be called AFTER {@link #init(uiContext)} to ensure
     * the Foundation framework is properly initialized before loading plugins.
     * <p>
     * The plugin system uses directories configured in the master uContext:
     * <ul>
     *   <li>{@code pluginsDirectory} - Scanned for plugin subdirectories</li>
     *   <li>{@code pluginConfigDirectory} - Stores registry.json</li>
     * </ul>
     *
     * @throws IllegalStateException if init() has not been called first
     * @throws java.io.IOException if plugin system initialization fails
     * @since Silver Tier
     */
    public static void initPlugins() throws java.io.IOException {
        if (instance == null) {
            throw new IllegalStateException(
                "Foundation must be initialized (call init()) before calling initPlugins()");
        }

        instance._initPlugins();
    }

    /**
     * Gets the plugin manager (Silver tier feature).
     *
     * @return the plugin manager, or null if plugin system not initialized
     * @since Silver Tier
     */
    public static org.jwellman.foundation.plugin.PluginManager getPluginManager() {
        if (instance == null) {
            return null;
        }
        return instance.getPluginManager();
    }

    /**
     * Gets the plugin action registry (Silver tier feature).
     *
     * @return the plugin action registry, or null if plugin system not initialized
     * @since Silver Tier
     */
    public static org.jwellman.foundation.plugin.PluginActionRegistry getPluginActionRegistry() {
        if (instance == null) {
            return null;
        }
        return instance.getPluginActionRegistry();
    }

    /**
     * Demonstrate a user interface in a JFrame.
     * <p>
     * This is the simplest of a Foundation startup sequence and, as its name states,
     * is intended for demonstration/SCCE applications only.
     * <p>
     * This satisfies 99% of demonstration mode requirements - probably the only
     * exceptions are those that are custom frame implementations and/or want
     * to modify typical application startup/shutdown.
     * 
     * @param c The JComponent to be displayed as part of the demo.
     */
    public static void demo(Class<? extends JComponent> c) {

        // Step 1 - Create an application context
        uiContext app = Foundation.createContext(c.getClass());

        // Step 2 - Initialize Foundation
        Foundation.init(app);

        // Step 3 - Create your application (which is a JPanel)
        try {
            Object o = c.newInstance();
            if (o instanceof JPanel) {
                app.registerMasterPanel("master", (JPanel)o);
            } else {
                // Probably rarely used but if JComponent is not something inherited from JPanel...
                JPanel x = new JPanel(new BorderLayout());
                x.add((JComponent)o, BorderLayout.CENTER);
                app.registerMasterPanel("master", x);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // Step 4 - Display the UI - this occurs properly on the EDT
        Foundation.launch(app);

    }

}
