package org.jwellman.foundation;

import org.jwellman.foundation.framework.uContext;

/**
 * A micro-framework for Swing applications.
 *
 * Usage:
 * (1) init() - initializes the Swing framework
 * (2) useWindow()/useDesktop() - supply your user interface within a JPanel
 *     and instantiate the supporting Swing containers (JFrame/JInternalFrame).
 * (3) showGUI() - make your user interface/JPanel visible;
 *     most applications will have initialized all data models
 *     and this will usually be the last method called in your startup() code.
 *
 * @author Rick Wellman
 */
public class Foundation extends Platinum {

    private Foundation() {} // private constructor to enforce singleton pattern; use get()

    private static Foundation f; // singleton

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     *
     * This no-args version creates a default uContext for simple use cases.
     * The default context uses "foundation.app" as the namespace and window mode (not desktop).
     *
     * For production applications, use init(uContext) with a properly configured context.
     *
     * @return The Foundation singleton instance
     */
    public static Foundation init() {
        // Create a default context for simple use cases
        // IMPORTANT: Foundation ALWAYS requires a valid uContext object
        // Never pass null - if context is null, that's a fundamental framework bug
        uContext defaultContext = uContext.createDefaultContext(DEFAULT_APP_TITLE);
        return Foundation.init(defaultContext);
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     *
     * The main and most important thing this does is initialize the Java Look and Feel;
     * see the _init() method for details on what few other initialization tasks are done.
     *
     * Because this framework is intended to support a desktop/multi-app environment,
     * the Foundation instance is a singleton and this method returns that single instance.
     *
     * IMPORTANT: As of the Bronze tier redesign, calling init() ALWAYS results in a visible
     * window being displayed (unless a window is already visible). This provides immediate
     * visual feedback that the framework has initialized successfully.
     *
     * The displayed window will be:
     * - In window mode: An empty JFrame (ready for content to be added)
     * - In desktop mode: A JFrame containing an empty JDesktopPane (ready for internal frames)
     *
     * @param c The context (MUST NOT be null - use no-args init() for default context)
     * @return The Foundation singleton instance
     * @throws NullPointerException if context is null (indicates framework bug)
     */
    public static Foundation init(uContext c) {
        if (f == null) {
            f = new Foundation();
        }

        // IMPORTANT: context must NEVER be null
        // If null, this is a fundamental framework bug - fail fast with NPE
        // All callers should either use init() no-args (creates default context)
        // or provide a properly configured context
        f._init(c);
        f._initializeAndShowWindow();

        return f;
    }

}
