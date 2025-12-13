package org.jwellman.foundation;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jwellman.foundation.interfaces.uiContext;

/**
 * A micro-framework for Swing applications.
 *
 * Foundation is the public API that exposes all tier functionality via static methods.
 * It inherits from all tiers (Stone -> Bronze -> Silver -> Gold -> Platinum -> Foundation).
 *
 * Foundation has three primary responsibilities:
 * 1) Discover Look and Feel and initialize the Swing UIManager
 * 2) Manage application context objects (uiContext implementations)
 *    - One context must always be the "master" (controls app lifecycle/shutdown)
 * 3) Create all IWindow objects (JFrame vs JInternalFrame based on master context state)
 *
 * Usage:
 * (1) createContext() - create a uiContext for your application
 * (2) init() - initializes Look and Feel, returns the master uiContext
 * (3) launch() - launches the application with the given uiContext
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
     * This is called by createContext() methods to allow context creation before init().
     */
    private static void ensureInstance() {
        if (instance == null) {
            instance = new Foundation();
        }
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     *
     * This no-args version creates a default uiContext for simple use cases.
     * The default context uses "foundation.app" as the namespace and window mode (not desktop).
     *
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
     *
     * The main and most important thing this does is initialize the Java Look and Feel;
     * see the _init() method for details on what few other initialization tasks are done.
     *
     * The provided context becomes the "master" context - it controls overall application
     * lifecycle including shutdown behavior.
     *
     * @param c The context (MUST NOT be null - use no-args init() for default context)
     * @return The master uiContext (same instance that was passed in)
     * @throws NullPointerException if context is null (indicates framework bug)
     */
    public static uiContext init(uiContext c) {
        // Ensure singleton exists
        ensureInstance();

        // Delegate to Stone tier implementation
        return instance.initStone(c);
    }

    /**
     * Launch the application with the given context.
     *
     * This creates and displays the main window based on the context configuration.
     * The context determines window mode vs desktop mode, dimensions, title, etc.
     *
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

        // Delegate to Stone tier implementation
        instance._launch(context);
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

    /**
     * This is a temporary workaround to get code working in Bronze.
     * <p>
     * Use of this method indicates an area in the code that needs to be
     * altered/improved in order to NOT need direct access to the
     * Foundation singleton.
     * 
     * @return
     */
    public static Foundation get() {
        return instance;
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

        // Step 1 - Initialize Foundation
        uiContext app = Foundation.init();

        // Step 2 - Create your application (which is a JPanel)
        try {
            Object o = c.newInstance();
            if (o instanceof JPanel) {
                app.registerMasterPanel("master", (JPanel)o);
            } else {
                JPanel x = new JPanel(new BorderLayout());
                x.add((JComponent)o, BorderLayout.CENTER);
                app.registerMasterPanel("master", x);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // Step 3 - Display the UI - this occurs properly on the EDT
        Foundation.launch(app);

    }

}
