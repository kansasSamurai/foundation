package org.jwellman.foundation.interfaces;

import java.awt.Dimension;
import java.util.Map;

import javax.swing.JPanel;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.model.PanelRegistration;

/**
 * Strategy interface for a Foundation application context.
 * <p>
 * This interface defines the contract for context objects that configure
 * and manage Foundation applications. Each context represents a namespace
 * (tool/application) and contains configuration for Look and Feel, desktop mode,
 * providers, and panel registry.
 * <p>
 * Design Pattern: Strategy pattern<br>
 * - Allows different context implementations with varying behavior<br>
 * - Framework works with the interface, not concrete implementations<br>
 * - Enables testing with mock contexts<br>
 *
 * @author rwellman
 */
public interface uiContext {

    /**
     * Get the namespace identifier for this context.
     *
     * @return The namespace string
     */
    String getNamespace();

    /**
     * Set the namespace identifier for this context.
     *
     * @param namespace The namespace string
     */
    void setNamespace(String namespace);

    /**
     * Check if desktop mode is enabled.
     *
     * @return true if desktop mode, false for window mode
     */
    boolean isDesktopMode();

    /**
     * Set the desktop mode.
     * This can typically only be set once.
     *
     * @param mode true for desktop mode, false for window mode
     */
    void setDesktopMode(boolean mode);

    /**
     * Get the theme provider.
     *
     * @return The theme provider, or null if not set
     */
    uiThemeProvider getThemeProvider();

    /**
     * Set the theme provider.
     *
     * @param themeProvider The theme provider implementation
     */
    void setThemeProvider(uiThemeProvider themeProvider);

    /**
     * Get the desktop provider.
     *
     * @return The desktop provider, or null if not set
     */
    uiDesktopProvider getDesktopProvider();

    /**
     * Set the desktop provider.
     *
     * @param desktopProvider The desktop provider implementation
     */
    void setDesktopProvider(uiDesktopProvider desktopProvider);

    /**
     * Get the splash provider.
     *
     * @return The splash provider, or null if not set
     */
    uiSplashProvider getSplashProvider();

    /**
     * Set the splash provider.
     *
     * @param splashProvider The splash provider implementation
     */
    void setSplashProvider(uiSplashProvider splashProvider);

    /**
     * Get the desktop title.
     *
     * @return The desktop title string, or null if not set
     */
    String getDesktopTitle();

    /**
     * Set the desktop title.
     *
     * @param desktopTitle The title for the desktop frame
     */
    void setDesktopTitle(String desktopTitle);

    /**
     * Get the window dimension.
     *
     * @return The dimension object
     */
    Dimension getDimension();

    /**
     * Set the window dimension.
     *
     * @param dimension The dimension object
     */
    void setDimension(Dimension dimension);

    /**
     * Set the window dimension.
     *
     * @param width The width in pixels
     * @param height The height in pixels
     */
    void setDimension(int width, int height);

    /**
     * Set the window dimension using a base multiplier.
     * Uses default 9:5 aspect ratio.
     *
     * @param base The base multiplier (e.g., 100 → 900x500)
     */
    void setDimension(int base);

    /**
     * Set the window dimension using a base multiplier and custom ratio.
     *
     * @param base The base multiplier
     * @param widthRatio The width ratio component
     * @param heightRatio The height ratio component
     */
    void setDimension(int base, int widthRatio, int heightRatio);

    /**
     * Get the Look and Feel class name.
     *
     * @return The LAF class name, or null if not set
     */
    String getLookAndFeel();

    /**
     * Set the Look and Feel class name.
     *
     * @param lookAndFeel The LAF class name
     */
    void setLookAndFeel(String lookAndFeel);

    /**
     * Register a panel in this application context's registry.
     *
     * @param panelId The panel identifier (e.g., "main", "settings")
     * @param registration The panel registration
     * @throws IllegalArgumentException if panelId is already registered
     */
    void registerPanel(String panelId, PanelRegistration registration);

    /**
     * Register the "master" panel of an application context.
     *
     * @param panelId
     * @param panel
     */
    PanelRegistration registerMasterPanel(String string, JPanel panel);

    /**
     * Register the "master" panel of an application context.
     *
     * @param reg, the PanelRegistration designated as the master.
     */
    PanelRegistration registerMasterPanel(PanelRegistration reg);

    /**
     * Retrieve the "master" panel of an application context.
     *
     * @return The master PanelRegistration
     */
    PanelRegistration getMasterPanel();

    /**
     * Get a panel registration by panelId.
     *
     * @param panelId The panel identifier
     * @return The PanelRegistration, or null if not found
     */
    PanelRegistration getPanelRegistration(String panelId);

    /**
     * Get all panel registrations in this context.
     *
     * @return Map of panelId to PanelRegistration
     */
    Map<String, PanelRegistration> getAllPanelRegistrations();

    /**
     * Remove a panel from this context's registry.
     *
     * @param panelId The panel identifier
     * @return The removed PanelRegistration, or null if not found
     */
    PanelRegistration removePanelRegistration(String panelId);

    /**
     * Check if a panel is registered in this context.
     *
     * @param panelId The panel identifier
     * @return true if registered, false otherwise
     */
    boolean hasPanelRegistration(String panelId);

    /**
     * Register a panel with required panel ID.
     *
     * @param panelId Unique ID within this context's namespace (e.g., "main", "settings", "history")
     * @param ui The JPanel to register
     * @return The PanelRegistration for this panel
     */
    PanelRegistration registerUI(String panelId, JPanel ui);

    /**
     * Register a panel with window positioning.
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param position Window positioning strategy
     * @return The PanelRegistration for this panel
     */
    PanelRegistration registerUI(String panelId, JPanel ui, WindowPosition position);

    /**
     * Register a panel with lifecycle listener and window positioning.
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param listener Lifecycle event listener (may be null)
     * @param position Window positioning strategy (may be null, defaults to CASCADE)
     * @return The PanelRegistration for this panel
     */
    PanelRegistration registerUI(String panelId, JPanel ui, uiPanelLifecycleListener listener, WindowPosition position);

    /**
     * Close a panel and remove it from this context's registry.
     * <p>
     * Fires the onClose lifecycle event.
     *
     * @param panelId The panel identifier
     */
    void closePanel(String panelId);

    /**
     * Check if a panel is currently visible.
     *
     * @param panelId The panel identifier
     * @return true if visible, false otherwise (or if panel doesn't exist)
     */
    boolean isPanelVisible(String panelId);

    /**
     * Get all panels in this context as a list.
     * <p>
     * Convenience method for extracting XPanel instances from registrations.
     *
     * @return List of XPanels (may be empty, never null)
     */
    java.util.List<org.jwellman.foundation.swing.XPanel> getPanels();

    /**
     * Get all panel registrations in this context as a list.
     * <p>
     * Convenience method for getAllPanelRegistrations().values().
     *
     * @return List of PanelRegistrations (may be empty, never null)
     */
    java.util.List<PanelRegistration> getRegistrations();

}
