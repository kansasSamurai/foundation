package org.jwellman.foundation;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiPanelLifecycleListener;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.interfaces.uiThemeProvider;
import org.jwellman.foundation.model.PanelRegistration;
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
     * Value: PanelRegistration metadata
     */
    private final Map<String, PanelRegistration> panelRegistry = new HashMap<>();

    /**
     * The main user interface for this application context.
     * <p>
     * 1) This is the user interface displayed during launch()<br>
     * 2) This user interface, by default, is considered to control the <br>
     *    application's lifecycle (particularly the ending)
     */
    private PanelRegistration masterPanel;

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
    public void registerPanel(String panelId, PanelRegistration registration) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }
        panelRegistry.put(panelId, registration);
    }

    /**
     * 
     */
    @Override
    public PanelRegistration registerMasterPanel(String panelId, JPanel panel) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }
        PanelRegistration reg = registerUI(panelId, panel);
        return this.registerMasterPanel(reg);
    }

    /**
     * 
     * @param reg
     */
    @Override
    public PanelRegistration registerMasterPanel(PanelRegistration reg) {
        masterPanel = reg;
        return reg;
    }

    /**
     * 
     */
    @Override
    public PanelRegistration getMasterPanel() {
        return masterPanel;
    }

    /**
     * Get a panel registration by panelId.
     *
     * @param panelId The panel identifier
     * @return The PanelRegistration, or null if not found
     */
    @Override
    public PanelRegistration getPanelRegistration(String panelId) {
        return panelRegistry.get(panelId);
    }

    /**
     * Get all panel registrations in this context.
     *
     * @return Map of panelId to PanelRegistration
     */
    @Override
    public Map<String, PanelRegistration> getAllPanelRegistrations() {
        return new HashMap<>(panelRegistry);
    }

    /**
     * Remove a panel from this context's registry.
     *
     * @param panelId The panel identifier
     * @return The removed PanelRegistration, or null if not found
     */
    @Override
    public PanelRegistration removePanelRegistration(String panelId) {
        return panelRegistry.remove(panelId);
    }

    /**
     * Check if a panel is registered in this context.
     *
     * @param panelId The panel identifier
     * @return true if registered, false otherwise
     */
    @Override
    public boolean hasPanelRegistration(String panelId) {
        return panelRegistry.containsKey(panelId);
    }

    /**
     * Register a panel with required panel ID.
     *
     * @param panelId Unique ID within this context's namespace (e.g., "main", "settings", "history")
     * @param ui The JPanel to register
     * @return The PanelRegistration for this panel
     */
    public PanelRegistration registerUI(String panelId, JPanel ui) {
        return registerUI(panelId, ui, null, null);
    }

    /**
     * Register a panel with window positioning (no lifecycle listener).
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param position Window positioning strategy
     * @return The PanelRegistration for this panel
     */
    public PanelRegistration registerUI(String panelId, JPanel ui, WindowPosition position) {
        return registerUI(panelId, ui, null, position);
    }

    /**
     * Register a panel with lifecycle listener and window positioning.
     *
     * @param panelId Unique ID within this context's namespace
     * @param ui The JPanel to register
     * @param listener Lifecycle event listener (may be null)
     * @param position Window positioning strategy (may be null, defaults to CASCADE)
     * @return The PanelRegistration for this panel
     */
    public PanelRegistration registerUI(String panelId, JPanel ui, uiPanelLifecycleListener listener, WindowPosition position) {

        // Get or create the uContext for this namespace
        uiContext ctx = this;

        // Check if panel already registered in this context
        if (ctx.hasPanelRegistration(panelId)) {
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
        PanelRegistration reg = new PanelRegistration(namespace, panelId, xpanel, listener);
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
     * Convenience method for getAllPanelRegistrations().values().
     *
     * @return List of PanelRegistrations (may be empty, never null)
     */
    @Override
    public java.util.List<PanelRegistration> getRegistrations() {
        return foundation.getRegistrations(namespace);
    }

}
