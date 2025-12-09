package org.jwellman.foundation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.PanelLifecycleListener;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.model.PanelRegistration;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XInternalFrame;
import org.jwellman.foundation.swing.XPanel;

/**
 * Bronze tier of Foundation framework.
 *
 * Adds multi-panel registration and window management capabilities:
 * - Hierarchical registry: namespace contexts containing panel registrations
 * - Each namespace (tool) has its own uContext with panel registry
 * - Multiple panels per namespace (tool/application)
 * - Panel visibility management (show/hide)
 * - Window positioning strategies
 * - Panel lifecycle events (onCreate, onShow, onHide, onClose)
 *
 * @author rwellman
 */
public class Bronze extends Stone {

    /**
     * The context registry.
     * Key: namespace (e.g., "tool.calculator", "tool.editor")
     * Value: uContext containing panel registry for that namespace
     *
     * This creates a drill-down structure:
     * Bronze -> uContext (by namespace) -> PanelRegistration (by panelId)
     */
    private final Map<String, uiContext> contextRegistry = new HashMap<>();

    /**
     * Register a panel with required namespace and panel ID.
     *
     * @param namespace Tool/application identifier (e.g., "tool.calculator")
     * @param panelId Unique ID within namespace (e.g., "main", "settings", "history")
     * @param ui The JPanel to register
     * @return The wrapped XPanel
     */
    public XPanel registerUI(String namespace, String panelId, JPanel ui) {
        return registerUI(namespace, panelId, ui, null, null);
    }

    /**
     * Register a panel with lifecycle listener.
     *
     * @param namespace Tool/application identifier
     * @param panelId Unique ID within namespace
     * @param ui The JPanel to register
     * @param listener Lifecycle event listener
     * @return The wrapped XPanel
     */
    public XPanel registerUI(String namespace, String panelId, JPanel ui, PanelLifecycleListener listener) {
        return registerUI(namespace, panelId, ui, listener, null);
    }

    /**
     * Register a panel with window positioning.
     *
     * @param namespace Tool/application identifier
     * @param panelId Unique ID within namespace
     * @param ui The JPanel to register
     * @param position Window positioning strategy
     * @return The wrapped XPanel
     */
    public XPanel registerUI(String namespace, String panelId, JPanel ui, WindowPosition position) {
        return registerUI(namespace, panelId, ui, null, position);
    }

    /**
     * Register a panel with lifecycle listener and window positioning.
     *
     * @param namespace Tool/application identifier
     * @param panelId Unique ID within namespace
     * @param ui The JPanel to register
     * @param listener Lifecycle event listener (may be null)
     * @param position Window positioning strategy (may be null, defaults to CASCADE)
     * @return The wrapped XPanel
     */
    public XPanel registerUI(String namespace, String panelId, JPanel ui,
                             PanelLifecycleListener listener, WindowPosition position) {

        // Get or create the uContext for this namespace
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) {
            ctx = Foundation.createContext(namespace);
            contextRegistry.put(namespace, ctx);
        }

        // Check if panel already registered in this context
        if (ctx.hasPanelRegistration(panelId)) {
            throw new IllegalArgumentException(
                    "Panel already registered: " + namespace + ":" + panelId +
                    ". Each panel must have a unique namespace:panelId combination.");
        }

        // Create wrapped panel
        String fullId = namespace + ":" + panelId;
        XPanel xpanel = new XPanel(ui);
        xpanel.setName(fullId);

        // Create registration
        PanelRegistration reg = new PanelRegistration(namespace, panelId, xpanel, listener);

        // Set positioning (or use default CASCADE)
        if (position != null) {
            reg.setWindowPosition(position);
        }

        // Register in the context's panel registry
        ctx.registerPanel(panelId, reg);

        // If we're in desktop mode and the desktop already exists (meaning init() has been called
        // and window is visible), immediately create the internal frame for this panel
        if (Boolean.TRUE.equals(this.isDesktop) && this.getDesktop() != null) {
            createInternalFrameForPanel(reg);
        }

        return xpanel;
    }

    /**
     * Get a specific panel by namespace and ID.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     * @return The XPanel, or null if not found
     */
    public XPanel getPanel(String namespace, String panelId) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return null;

        PanelRegistration reg = ctx.getPanelRegistration(panelId);
        return reg != null ? reg.getPanel() : null;
    }

    /**
     * Get all panels for a namespace.
     *
     * @param namespace The namespace
     * @return List of XPanels (may be empty, never null)
     */
    public List<XPanel> getPanels(String namespace) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return new ArrayList<>();

        return ctx.getAllPanelRegistrations().values().stream()
                .map(PanelRegistration::getPanel)
                .collect(Collectors.toList());
    }

    /**
     * Get all panel registrations for a namespace.
     *
     * @param namespace The namespace
     * @return List of PanelRegistrations (may be empty, never null)
     */
    public List<PanelRegistration> getRegistrations(String namespace) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return new ArrayList<>();

        return new ArrayList<>(ctx.getAllPanelRegistrations().values());
    }

    /**
     * Get all registered namespaces.
     *
     * @return List of unique namespaces
     */
    public List<String> getNamespaces() {
        return new ArrayList<>(contextRegistry.keySet());
    }

    /**
     * Get a panel registration by namespace and ID.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     * @return The PanelRegistration, or null if not found
     */
    public PanelRegistration getRegistration(String namespace, String panelId) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return null;

        return ctx.getPanelRegistration(panelId);
    }

    /**
     * Get a uContext by namespace.
     *
     * @param namespace The namespace
     * @return The uContext, or null if not found
     */
    public uiContext getContext(String namespace) {
        return contextRegistry.get(namespace);
    }

    /**
     * Show a panel (make it visible).
     * Fires the onShow lifecycle event.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    public void showPanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            IWindow window = reg.getWindow();
            if (window != null && !reg.isVisible()) {
                window.setVisible(true);
                reg.setVisible(true);
                reg.fireOnShow();
            }
        }
    }

    /**
     * Hide a panel (make it invisible).
     * Fires the onHide lifecycle event.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    public void hidePanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            IWindow window = reg.getWindow();
            if (window != null && reg.isVisible()) {
                window.setVisible(false);
                reg.setVisible(false);
                reg.fireOnHide();
            }
        }
    }

    /**
     * Toggle panel visibility.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    public void togglePanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            if (reg.isVisible()) {
                hidePanel(namespace, panelId);
            } else {
                showPanel(namespace, panelId);
            }
        }
    }

    /**
     * Check if a panel is currently visible.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     * @return true if visible, false otherwise
     */
    public boolean isPanelVisible(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        return reg != null && reg.isVisible();
    }

    /**
     * Close a panel and remove it from the registry.
     * Fires the onClose lifecycle event.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    public void closePanel(String namespace, String panelId) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return;

        PanelRegistration reg = ctx.getPanelRegistration(panelId);
        if (reg != null) {
            // Fire lifecycle event
            reg.fireOnClose();

            // Close the window
            IWindow window = reg.getWindow();
            if (window != null) {
                window.close();
            }

            // Remove from context's registry
            ctx.removePanelRegistration(panelId);
        }
    }

    /**
     * Get all registrations (for internal use).
     *
     * @return List of all PanelRegistrations across all contexts
     */
    protected List<PanelRegistration> getAllRegistrations() {
        List<PanelRegistration> allRegs = new ArrayList<>();
        for (uiContext ctx : contextRegistry.values()) {
            allRegs.addAll(ctx.getAllPanelRegistrations().values());
        }
        return allRegs;
    }

    // Track whether launch() has been called in window mode (single-call enforcement)
    private boolean windowModeLaunched = false;

    // Counter for auto-registration of launched panels
    private int autoRegistrationCounter = 0;

    /**
     * Launch a panel (make it visible).
     * Auto-wraps JPanel in XPanel if needed.
     *
     * @param panel The JPanel to launch
     */
    public void launch(JPanel panel) {
        XPanel xpanel = (panel instanceof XPanel) ? (XPanel) panel : new XPanel(panel);
        launch(xpanel);
    }

    /**
     * Launch a panel (make it visible).
     *
     * If the panel is not already registered, it will be auto-registered
     * with namespace "app.main" and auto-generated panel ID.
     *
     * In window mode, launch() can only be called once.
     * In desktop mode, launch() can be called multiple times.
     *
     * The first call to launch() closes the splash screen.
     *
     * @param panel The XPanel to launch
     * @throws IllegalStateException if called more than once in window mode
     */
    public void launch(XPanel panel) {
        // Find existing registration
        PanelRegistration reg = findRegistrationByPanel(panel);

        // Auto-register if not registered
        if (reg == null) {
            String namespace = "app.main";
            String panelId = "panel" + autoRegistrationCounter++;
            reg = autoRegisterPanel(namespace, panelId, panel);
        }

        // Window mode: enforce single launch
        if (Boolean.FALSE.equals(this.isDesktop)) {
            if (windowModeLaunched) {
                throw new IllegalStateException(
                    "launch() can only be called once in window mode. " +
                    "Already launched: " + reg.getFullId());
            }
            windowModeLaunched = true;

            // Window mode: Close splash by replacing content pane
            if (splashWindow != null && splashProvider != null) {
                // Replace splash content with the actual panel
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getExternalFrame().setContentPane(panel);
                        getExternalFrame().pack();
                        getExternalFrame().setLocationRelativeTo(null);

                        // Fire splash closed event
                        splashProvider.onSplashClosed();
                        splashWindow = null;
                        splashProvider = null;
                    }
                });
            }
        } else {
            // Desktop mode: Close splash internal frame on first launch
            if (splashWindow != null && splashProvider != null) {
                final IWindow splashToClose = splashWindow;
                final uiSplashProvider providerToNotify = splashProvider;

                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        splashToClose.setVisible(false);
                        splashToClose.close();

                        // Fire splash closed event
                        providerToNotify.onSplashClosed();
                    }
                });

                splashWindow = null;
                splashProvider = null;
            }

            // Make the launched panel's internal frame visible
            IWindow window = reg.getWindow();
            if (window != null) {
                window.setVisible(true);
                reg.setVisible(true);
                reg.fireOnShow();
            }
        }
    }

    /**
     * Find a panel registration by XPanel reference.
     *
     * @param panel The XPanel to search for
     * @return The PanelRegistration, or null if not found
     */
    private PanelRegistration findRegistrationByPanel(XPanel panel) {
        for (uiContext ctx : contextRegistry.values()) {
            for (PanelRegistration reg : ctx.getAllPanelRegistrations().values()) {
                if (reg.getPanel() == panel) {
                    return reg;
                }
            }
        }
        return null;
    }

    /**
     * Auto-register a panel that was launched without prior registration.
     *
     * @param namespace The namespace to use
     * @param panelId The panel ID to use
     * @param panel The XPanel to register
     * @return The created PanelRegistration
     */
    private PanelRegistration autoRegisterPanel(String namespace, String panelId, XPanel panel) {
        // Get or create context
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) {
            ctx = Foundation.createContext(namespace);
            contextRegistry.put(namespace, ctx);
        }

        // Create registration
        String fullId = namespace + ":" + panelId;
        panel.setName(fullId);
        PanelRegistration reg = new PanelRegistration(namespace, panelId, panel, null);
        ctx.registerPanel(panelId, reg);

        // Create frame immediately if in desktop mode and desktop exists
        if (Boolean.TRUE.equals(this.isDesktop) && this.getDesktop() != null) {
            createInternalFrameForPanel(reg);
        }

        return reg;
    }

    /**
     * Creates an internal frame for a single panel registration.
     * This is called either during initialization (for panels registered before init)
     * or immediately when a panel is registered after init.
     *
     * @param reg The panel registration
     */
    private void createInternalFrameForPanel(PanelRegistration reg) {
        if (reg.getInternalFrame() != null) {
            // Already has an internal frame, skip
            return;
        }

        // Create internal frame
        final XInternalFrame iframe = new XInternalFrame();
        iframe.setTitle(reg.getFullId()); // Default title
        iframe.add(reg.getPanel());

        // Configure frame properties
        // System panels (namespace "system") are not minimizable
        // Tool panels can be minimized
        boolean isSystemPanel = "system".equals(reg.getNamespace());
        iframe.setIconifiable(!isSystemPanel);
        iframe.setResizable(true);
        iframe.setClosable(false);
        iframe.setMaximizable(true);

        // Set up bidirectional reference
        reg.getPanel().setParent(iframe);
        reg.setInternalFrame(iframe);

        // Apply positioning
        iframe.pack(); // Pack before positioning to get correct size
        reg.getWindowPosition().apply(iframe, this.getDesktop());

        // Add to desktop
        this.getDesktop().add(iframe);

        // Frame is created but NOT visible by default
        // Only frames shown via launch() will be made visible
        iframe.setVisible(false);

        // Fire onCreate event
        reg.fireOnCreate();
    }

    @Override
    protected void initializeOtherWindows() {
        if (Boolean.TRUE.equals(this.isDesktop)) {
            // Iterate through all contexts and their panels
            for (uiContext ctx : contextRegistry.values()) {
                for (PanelRegistration reg : ctx.getAllPanelRegistrations().values()) {
                    createInternalFrameForPanel(reg);
                }
            }
        }
    }

}
