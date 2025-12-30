package org.jwellman.foundation;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.interfaces.uiThemeProvider;
import org.jwellman.foundation.listener.PanelLifecycleListener;
import org.jwellman.foundation.listener.RegistryChangeListener;
import org.jwellman.foundation.model.FrameDescriptor;
import org.jwellman.foundation.swing.XPanel;

/**
 * Default implementation of uiContext interface.
 * <p>
 * A context for a Foundation application or tool.
 * <p>
 * As of the Bronze tier redesign, each context represents a namespace (tool/application)
 * and contains its own panel registry. This allows each tool to have multiple panels
 * with tool-specific configuration.
 *
 * @author rwellman
 */
public class uContext implements uiContext {

    /** Reference to the Foundation singleton for application lifecycle operations */
    private final Foundation foundation;

    /** An identifier for this application context (namespace) */
    public String namespace;

    /**
     * Panel registry for this context.
     * <p>
     * Key: panelId (e.g., "main", "settings", "history")
     * Value: FrameDescriptor metadata
     */
    private final Map<String, FrameDescriptor> panelRegistry = new HashMap<>();

    /**
     * Registry change listeners (Silver tier feature).
     * <p>
     * Listeners are notified whenever the registry changes (panel added,
     * removed, visibility changed, detached, etc.)
     */
    private final List<RegistryChangeListener> registryChangeListeners = new ArrayList<>();

    /**
     * The main user interface for this application context.
     * <p>
     * 1) This is the user interface displayed during launch()<br>
     * 2) This user interface, by default, is considered to control the <br>
     *    application's lifecycle (particularly the ending)
     */
    private FrameDescriptor masterPanel;

    /**
     * The look and feel class to use (will be ignored in
     * a "desktop" environment unless it is the desktop provider)
     */
    public String lookAndFeel;

    /**
     * Package-private constructor for creating a context with a namespace.
     * This should only be called by Foundation.createContext() methods.
     *
     * @param foundation The Foundation singleton instance
     * @param namespace The namespace identifier for this context
     */
    uContext(Foundation foundation, String namespace) {
        if (foundation == null) {
            throw new IllegalArgumentException("Foundation reference cannot be null");
        }
        this.foundation = foundation;
        this.namespace = namespace;
    }

    /**
     * Get the Foundation singleton reference.
     * This allows the context to interact with application lifecycle operations.
     *
     * @return The Foundation instance
     */
    private Foundation getFoundation() {
        return foundation;
    }

    /** An indicator that you are using desktop mode; defaults to false. */
    private boolean desktopMode = false;

    @Override
    public boolean isDesktopMode() {
        return desktopMode;
    }

    @Override
    public void setDesktopMode(boolean mode) {
        this.desktopMode = mode;
    }

    /** An object that implements the themeProvider interface */
    private uiThemeProvider themeProvider;

    @Override
    public void setThemeProvider(uiThemeProvider x) { themeProvider = x; }

    @Override
    public uiThemeProvider getThemeProvider() { return themeProvider; }

    /** An object that implements the desktopProvider interface */
    private uiDesktopProvider desktopProvider;

    @Override
	public void setDesktopProvider(uiDesktopProvider x) { desktopProvider = x; }

    @Override
	public uiDesktopProvider getDesktopProvider() { return desktopProvider; }

    /** An object that implements the splashProvider interface */
    private uiSplashProvider splashProvider;

    @Override
	public void setSplashProvider(uiSplashProvider x) { splashProvider = x; }

    @Override
	public uiSplashProvider getSplashProvider() { return splashProvider; }

    /** A title for the desktop frame */
    private String desktopTitle;

    @Override
	public void setDesktopTitle(String strTitle) { this.desktopTitle = strTitle; }

    @Override
	public String getDesktopTitle() { return desktopTitle; }

    /** A dimension object for the window (w/ default value of 900x500, matching 9:5 ratio) */
    private Dimension dimension = new Dimension(900, 500);

    @Override
    public void setDimension(Dimension x) { dimension = x; }

    @Override
    public void setDimension(int w, int h) { dimension = new Dimension(w,h); }

    @Override
    public void setDimension(int base) { this.setDimension(base, 9, 5); }

    @Override
    public void setDimension(int base, int w, int h) { this.setDimension(base * w, base * h); }

    @Override
    public Dimension getDimension() { return dimension; }

    @Override
	public String getLookAndFeel() {
		return lookAndFeel;
	}

    @Override
	public void setLookAndFeel(String laf) {
		this.lookAndFeel = laf;
	}

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Register a panel in this context's registry.
     *
     * @param panelId The panel identifier (e.g., "main", "settings")
     * @param registration The panel registration
     * @throws IllegalArgumentException if panelId is already registered
     */
    @Override
    public void registerPanel(String panelId, FrameDescriptor registration) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }

        // Set owning context for registry change events (Silver tier)
        registration.setOwningContext(this);

        panelRegistry.put(panelId, registration);

        // Fire registry changed event (Silver tier)
        fireRegistryChanged();
    }

    /**
     * 
     */
    @Override
    public FrameDescriptor registerMasterPanel(String panelId, JPanel panel) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }
        FrameDescriptor reg = registerUI(panelId, panel);
        return this.registerMasterPanel(reg);
    }

    /**
     * 
     * @param reg
     */
    @Override
    public FrameDescriptor registerMasterPanel(FrameDescriptor reg) {
        masterPanel = reg;
        return reg;
    }

    /**
     * 
     */
    @Override
    public FrameDescriptor getMasterPanel() {
        return masterPanel;
    }

    /**
     * Get a panel registration by panelId.
     *
     * @param panelId The panel identifier
     * @return The FrameDescriptor, or null if not found
     */
    @Override
    public FrameDescriptor getFrameDescriptor(String panelId) {
        return panelRegistry.get(panelId);
    }

    /**
     * Get all panel registrations in this context.
     *
     * @return Map of panelId to FrameDescriptor
     */
    @Override
    public Map<String, FrameDescriptor> getAllFrameDescriptors() {
        return new HashMap<>(panelRegistry);
    }

    /**
     * Remove a panel from this context's registry.
     *
     * @param panelId The panel identifier
     * @return The removed FrameDescriptor, or null if not found
     */
    @Override
    public FrameDescriptor removeFrameDescriptor(String panelId) {
        return panelRegistry.remove(panelId);
    }

    /**
     * Check if a panel is registered in this context.
     *
     * @param panelId The panel identifier
     * @return true if registered, false otherwise
     */
    @Override
    public boolean hasFrameDescriptor(String panelId) {
        return panelRegistry.containsKey(panelId);
    }

    /**
     * Register a panel with required panel ID.
     *
     * @param panelId Unique ID within this context's namespace (e.g., "main", "settings", "history")
     * @param ui The JPanel to register
     * @return The FrameDescriptor for this panel
     */
    public FrameDescriptor registerUI(String panelId, JPanel ui) {
        return registerUI(panelId, ui, null, null);
    }

    /**
     * Register a panel with window positioning (no lifecycle listener).
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param position Window positioning strategy
     * @return The FrameDescriptor for this panel
     */
    public FrameDescriptor registerUI(String panelId, JPanel ui, WindowPosition position) {
        return registerUI(panelId, ui, null, position);
    }

    /**
     * Register a panel with lifecycle listener and window positioning.
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param listener Lifecycle event listener (may be null)
     * @param position Window positioning strategy (may be null, defaults to CASCADE)
     * @return The FrameDescriptor for this panel
     */
    public FrameDescriptor registerUI(String panelId, JPanel ui, PanelLifecycleListener listener, WindowPosition position) {
        return registerUI(panelId, ui, null, listener, position);
    }

    /**
     * Register a panel with menu bar, lifecycle listener, and window positioning.
     * <p>
     * This is the most complete registerUI overload, providing all optional parameters.
     * The menu bar must be provided at registration time and cannot be changed later
     * (FrameDescriptor is mostly immutable).
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param menuBar Optional menu bar for this panel (may be null)
     * @param listener Lifecycle event listener (may be null)
     * @param position Window positioning strategy (may be null, defaults to CASCADE)
     * @return The FrameDescriptor for this panel
     */
    public FrameDescriptor registerUI(String panelId, JPanel ui, javax.swing.JMenuBar menuBar, PanelLifecycleListener listener, WindowPosition position) {

        // Get or create the uContext for this namespace
        uiContext ctx = this;

        // Check if panel already registered in this context
        if (ctx.hasFrameDescriptor(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered: " + namespace + ":" + panelId +
                    ". Each panel must have a unique namespace:panelId combination.");
        }

        // Create wrapped panel
        String fullId = namespace + ":" + panelId;
        // because XPanels inherit from JPanel, only wrap with XPanel if required.
        XPanel xpanel = (ui instanceof XPanel) ? (XPanel)ui : new XPanel(ui);
        xpanel.setName(fullId);

        // Create registration and immediately register in the context
        FrameDescriptor reg = new FrameDescriptor(namespace, panelId, xpanel, menuBar, listener);
        ctx.registerPanel(panelId, reg);

        // Set positioning (or use default CASCADE)
        if (position != null) {
            reg.setWindowPosition(position);
        }

        // Create the appropriate container for this panel based on mode
        // Desktop mode: create internal frame (inside JDesktopPane)
        // Window mode: create external frame (separate JFrame) - enables multi-window mode
        if (Boolean.TRUE.equals(this.isDesktopMode()) ) {
            getFoundation().createInternalFrameForPanel(reg);
        } else {
            // Window mode: create external frame for this panel (multi-window mode)
            getFoundation().createFrameForPanel(reg);
        }

        return reg;
    }

    /**
     * Close a panel and remove it from this context's registry.
     * <p>
     * Fires the onClose lifecycle event.
     *
     * @param panelId The panel identifier
     */
    @Override
    public void closePanel(String panelId) {
        foundation.closePanel(namespace, panelId);
    }

    /**
     * Detach or attach a panel by switching its container type.
     * <p>
     * If the panel is currently in a JInternalFrame (desktop mode), it will be
     * moved to a standalone JFrame (window mode). If it's currently in a JFrame,
     * it will be moved back to a JInternalFrame in the desktop.
     *
     * @param panelId The panel identifier
     */
    @Override
    public void detachPanel(String panelId) {
        foundation.detachPanel(namespace, panelId);
    }

    /**
     * Check if a panel is currently visible.
     *
     * @param panelId The panel identifier
     * @return true if visible, false otherwise (or if panel doesn't exist)
     */
    @Override
    public boolean isPanelVisible(String panelId) {
        return foundation.isPanelVisible(namespace, panelId);
    }

    /**
     * Get all panels in this context as a list.
     * <p>
     * Convenience method for extracting XPanel instances from registrations.
     *
     * @return List of XPanels (may be empty, never null)
     */
    @Override
    public java.util.List<XPanel> getPanels() {
        return foundation.getPanels(namespace);
    }

    /**
     * Get all panel registrations in this context as a list.
     * <p>
     * Convenience method for getAllFrameDescriptors().values().
     *
     * @return List of FrameDescriptors (may be empty, never null)
     */
    @Override
    public java.util.List<FrameDescriptor> getRegistrations() {
        return foundation.getRegistrations(namespace);
    }

    // ========================================================================
    // REGISTRY CHANGE LISTENERS (Silver Tier)
    // ========================================================================

    /**
     * Adds a registry change listener to be notified of all registry changes.
     *
     * @param listener The listener to add (must not be null)
     * @throws IllegalArgumentException if listener is null
     */
    @Override
    public void addRegistryChangeListener(RegistryChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Registry change listener cannot be null");
        }
        registryChangeListeners.add(listener);
    }

    /**
     * Removes a previously registered registry change listener.
     *
     * @param listener The listener to remove
     */
    @Override
    public void removeRegistryChangeListener(RegistryChangeListener listener) {
        registryChangeListeners.remove(listener);
    }

    /**
     * Fires registry changed event to all registered listeners.
     * <p>
     * This is called internally by the framework whenever the registry changes.
     * Listeners are always invoked on the EDT for thread safety.
     */
    @Override
    public void fireRegistryChanged() {
        // Make a copy to avoid ConcurrentModificationException if listeners
        // add/remove other listeners during callback
        List<RegistryChangeListener> listenersCopy = new ArrayList<>(registryChangeListeners);

        // Always fire on EDT for thread safety
        SwingUtilities.invokeLater(() -> {
            for (RegistryChangeListener listener : listenersCopy) {
                try {
                    listener.onRegistryChanged();
                } catch (Exception e) {
                    // Log but don't let listener exceptions break other listeners
                    System.err.println("Error in registry change listener: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

}
