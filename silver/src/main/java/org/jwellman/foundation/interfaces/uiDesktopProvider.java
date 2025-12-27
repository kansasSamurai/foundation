package org.jwellman.foundation.interfaces;

import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;

/**
 * Strategy interface for providing custom desktop components and behavior.
 *
 * This interface allows customization of the desktop environment (JDesktopPane)
 * that hosts internal frames in desktop mode. Implementations can provide
 * custom desktop panes with specialized backgrounds, behaviors, or features.
 *
 * The Foundation framework provides a default implementation (DefaultDesktopProvider)
 * that creates a standard JDesktopPane with sensible defaults. Applications can
 * provide their own implementations for custom desktop environments.
 *
 * Design Pattern: Strategy pattern
 * - Allows runtime selection of desktop creation strategy
 * - Promotes interface-based design and flexibility
 * - Enables custom desktop environments without modifying framework code
 * - Parameter-free methods support nested desktops (desktop within desktop)
 *
 * Context via Constructor Injection:
 * If a provider needs context (window reference, configuration, etc.), it should
 * receive it via constructor when instantiated, not via these methods. This keeps
 * the interface clean and supports nested desktops.
 *
 * @author rwellman
 */
public interface uiDesktopProvider {

    /**
     * Creates and configures a JDesktopPane.
     *
     * This is the primary method called by the Foundation framework when
     * initializing desktop mode. Implementations should:
     * - Create a JDesktopPane instance
     * - Configure drag mode, background, and other properties
     * - Return the configured desktop
     *
     * The framework handles setting the desktop as the container's content pane.
     *
     * Note: No parameters allows this to work with any container (JFrame,
     * JInternalFrame) and supports nested desktops. If provider needs context,
     * inject it via constructor.
     *
     * @return The configured JDesktopPane
     */
    JDesktopPane createDesktop();

    /**
     * Return the desktop created by createDesktop().
     * 
     * @return The configured JDesktopPane
     */
    JDesktopPane getDesktop();

    /**
     * Provides an optional menu bar for the desktop.
     *
     * Implementations can return a custom menu bar with desktop-level
     * menus (File, Window, Help, etc.). Return null for no menu bar.
     *
     * Note: No parameters allows flexibility. If provider needs context for
     * menu creation, inject it via constructor.
     *
     * @return A JMenuBar, or null if no menu bar is needed
     */
    JMenuBar createMenuBar();

    /**
     * Called after the desktop is fully initialized and visible.
     *
     * Implementations can use this hook to perform post-initialization
     * tasks such as:
     * - Adding desktop icons or shortcuts
     * - Starting background services
     * - Displaying welcome dialogs
     *
     * @param desktop The initialized desktop pane
     */
    void onDesktopInitialized(JDesktopPane desktop);

}
