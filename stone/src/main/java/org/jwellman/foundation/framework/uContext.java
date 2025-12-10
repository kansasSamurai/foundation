package org.jwellman.foundation.framework;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.interfaces.uiThemeProvider;
import org.jwellman.foundation.model.PanelRegistration;
import org.jwellman.foundation.swing.XPanel;

/**
 * Default implementation of uiContext interface.
 *
 * A context for a Foundation application or tool.
 *
 * As of the Bronze tier redesign, each context represents a namespace (tool/application)
 * and contains its own panel registry. This allows each tool to have multiple panels
 * with tool-specific configuration.
 *
 * @author rwellman
 */
public class uContext implements uiContext {

    /** An identifier for this context (namespace) */
    public String namespace;

    /**
     * Panel registry for this context.
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
     * Public constructor for creating a context with a namespace.
     *
     * @param namespace The namespace identifier for this context
     */
    public uContext(String namespace) {
        this.namespace = namespace;
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
    public void registerMasterPanel(String panelId, JPanel panel) {
        if (panelRegistry.containsKey(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered in context '" + namespace + "': " + panelId);
        }
        PanelRegistration reg = new PanelRegistration(namespace, panelId, new XPanel(panel), null);
        masterPanel = reg;
        panelRegistry.put(panelId, reg);
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

}
