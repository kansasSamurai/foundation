package org.jwellman.foundation.framework;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.interfaces.uiThemeProvider;
import org.jwellman.foundation.model.FrameDescriptor;

/**
 * A context for a Foundation application or tool.
 *
 * As of the Bronze tier redesign, each context represents a namespace (tool/application)
 * and contains its own panel registry. This allows each tool to have multiple panels
 * with tool-specific configuration.
 *
 * @author rwellman
 */
public class uContext {

    /** An identifier for this context (namespace) */
    public String namespace;

    /**
     * Panel registry for this context.
     * Key: panelId (e.g., "main", "settings", "history")
     * Value: FrameDescriptor metadata
     */
    private final Map<String, FrameDescriptor> panelRegistry = new HashMap<>();

    /** 
     * The look and feel class to use (will be ignored in 
     * a "desktop" environment unless it is the desktop provider)
     */
    public String lookAndFeel;

    public static uContext createContext(Class<?> clazz) {
        return new uContext(clazz.getName());
    }

    public static uContext createContext(String namespace) {
        return new uContext(namespace);
    }

    private uContext(String namespace) {
        this.namespace = namespace;
    }

    /** An indicator that you are using desktop mode; defaults to false. */
    private Boolean desktopMode = null;
    
    /** An indicator that you are using desktop mode; defaults to false. */
    public boolean isDesktopMode() {
        return desktopMode == null ? false : desktopMode;
    }

    /** Sets the desktop mode; this can only be done once. */
    public void setDesktopMode(boolean mode) {
        if (desktopMode == null) {
            this.desktopMode = mode;
        }
    }

    /** An object that implements the themeProvider interface */
    private uiThemeProvider themeProvider;

    public void setThemeProvider(uiThemeProvider x) { themeProvider = x; }

    public uiThemeProvider getThemeProvider() { return themeProvider; }

    /** An object that implements the desktopProvider interface */
    private uiDesktopProvider desktopProvider;

	public void setDesktopProvider(uiDesktopProvider x) { desktopProvider = x; }

	public uiDesktopProvider getDesktopProvider() { return desktopProvider; }

    /** An object that implements the splashProvider interface */
    private uiSplashProvider splashProvider;

	public void setSplashProvider(uiSplashProvider x) { splashProvider = x; }

	public uiSplashProvider getSplashProvider() { return splashProvider; }

    /** A title for the desktop frame */
    private String desktopTitle;
    
	public void setDesktopTitle(String strTitle) { this.desktopTitle = strTitle; }
	
	public String getDesktopTitle() { return desktopTitle; }

    /** A dimension object for the window (w/ default value of 900x500, matching 9:5 ratio) */
    private Dimension dimension = new Dimension(900, 500);

    public void setDimension(Dimension x) { dimension = x; }

    public void setDimension(int w, int h) { dimension = new Dimension(w,h); }

    public void setDimension(int base) { this.setDimension(base, 9, 5); }
    
    public void setDimension(int base, int w, int h) { this.setDimension(base * w, base * h); }

    public Dimension getDimension() { return dimension; }

	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(String laf) {
		this.lookAndFeel = laf;
	}

    public String getNamespace() {
        return namespace;
    }

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
    public void registerPanel(String panelId, FrameDescriptor registration) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }
        panelRegistry.put(panelId, registration);
    }

    /**
     * Get a panel registration by panelId.
     *
     * @param panelId The panel identifier
     * @return The FrameDescriptor, or null if not found
     */
    public FrameDescriptor getFrameDescriptor(String panelId) {
        return panelRegistry.get(panelId);
    }

    /**
     * Get all panel registrations in this context.
     *
     * @return Map of panelId to FrameDescriptor
     */
    public Map<String, FrameDescriptor> getAllFrameDescriptors() {
        return new HashMap<>(panelRegistry);
    }

    /**
     * Remove a panel from this context's registry.
     *
     * @param panelId The panel identifier
     * @return The removed FrameDescriptor, or null if not found
     */
    public FrameDescriptor removeFrameDescriptor(String panelId) {
        return panelRegistry.remove(panelId);
    }

    /**
     * Check if a panel is registered in this context.
     *
     * @param panelId The panel identifier
     * @return true if registered, false otherwise
     */
    public boolean hasFrameDescriptor(String panelId) {
        return panelRegistry.containsKey(panelId);
    }

    /**
     * Fire registry changed event to notify listeners.
     * <p>
     * Bronze tier stub implementation - does nothing.
     * Registry change listeners are a Silver tier feature.
     * This method exists to satisfy the uiContext interface contract.
     */
    public void fireRegistryChanged() {
        // No-op in Bronze tier
        // Silver tier implements actual registry change listener notification
    }

}
