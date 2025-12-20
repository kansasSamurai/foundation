package org.jwellman.foundation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.framework.uUtility;
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
    protected void _showPanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            reg.show();
        }
    }

    /**
     * Hide a panel (make it invisible).
     * Fires the onHide lifecycle event.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    protected void _hidePanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            reg.hide();
        }
    }

    /**
     * Toggle panel visibility.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    protected void _togglePanel(String namespace, String panelId) {
        PanelRegistration reg = getRegistration(namespace, panelId);
        if (reg != null) {
            reg.toggle();
        } else {
            dumpFoundationStructure();
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
     * <p>
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
            closePanel(ctx, reg);
        }
    }

    /**
     * Close a panel and remove it from the registry.
     * <p>
     * Fires the onClose lifecycle event.
     * TODO Eventually (but probably not soon), the PanelRegistration may
     * contain a reference to its parent uiContext in which case only the
     * PanelRegistration parameter will be necessary here.
     * 
     * @param ctx The uiContext containing the PanelRegistration
     * @param reg The PanelRegistration to be removed from the uiContext
     */
    public void closePanel(uiContext ctx, PanelRegistration reg) {
        // Fire lifecycle event
        reg.fireOnClose();

        // Close the window
        IWindow window = reg.getWindow();
        if (window != null) {
            window.close();
        }

        // Remove from context's registry
        ctx.removePanelRegistration(reg.getPanelId());
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

    // Counter for auto-registration of launched panels
    private int autoRegistrationCounter = 0;

    protected void _launch(uiContext ctx) {

        // Close splash screen if it exists (before showing the main application)
        if (ctx == masterContext) {
            closeSplashScreen(ctx);
        }

        super._launch(ctx);

        // Stone only launches the masterContext so Bronze needs to launch others
        if (ctx == masterContext) {
            System.out.println("INFO - Bronze bypass master context");
        } else {

            //
            /* Once we support the init() method being called more than once
             * (like in multi tool desktop(s), calling it here may be redundant
             *  but would not be expected to hurt since it would just replace
             *  itself in the registry.  Making this note for future self.
             */
            registerContext(ctx);

            if (isDesktop()) {
                // I think we should have already created the internal frame -
                // unless I re-discover why we haven't on purpose, we need to.
                this.createInternalFrameForPanel(ctx.getMasterPanel());
                ctx.getMasterPanel().getInternalFrame().show();
            } else {
                // TODO implement window logic
            }

        }
    }

    /**
     * Make a panel visible.<br>
     * Auto-wraps JPanel in XPanel if needed.
     * <p>
     * If the panel is not already registered, it will be auto-registered
     * with namespace "app.main" and auto-generated panel ID.
     * 
     * @param panel The JPanel to launch
     */
    public void showWindow(JPanel panel) {
        XPanel xpanel = (panel instanceof XPanel) ? (XPanel) panel 
                : new XPanel(panel);
        showWindow(xpanel);
    }

    /**
     * Make a panel visible.
     * <p>
     * If the panel is not already registered, it will be auto-registered
     * with namespace "app.main" and auto-generated panel ID.
     * 
     * @param panel The XPanel to launch
     * @throws IllegalStateException if called more than once in window mode
     */
    public void showWindow(XPanel panel) {

        // Find existing registration
        PanelRegistration reg = findRegistrationByPanel(panel);

        // Auto-register if not registered
        if (reg == null) {
            String namespace = "app.main";
            String panelId = "panel" + autoRegistrationCounter++;
            reg = autoRegisterPanel(namespace, panelId, panel);
        }

        // Window mode: enforce single launch
        if (Boolean.FALSE.equals(isDesktop())) {

            // Window mode: Close splash by replacing content pane
//            if (splashWindow != null && splashProvider != null) {
//                // Replace splash content with the actual panel
//                javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        getExternalFrame().setContentPane(panel);
//                        getExternalFrame().pack();
//                        getExternalFrame().setLocationRelativeTo(null);
//
//                        // Fire splash closed event
//                        splashProvider.onSplashClosed();
//                        splashWindow = null;
//                        splashProvider = null;
//                    }
//                });
//            }

        } else {
            // Desktop mode: Close splash internal frame on first launch
//            if (splashWindow != null && splashProvider != null) {
//                final IWindow splashToClose = splashWindow;
//                final uiSplashProvider providerToNotify = splashProvider;
//
//                javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        splashToClose.setVisible(false);
//                        splashToClose.close();
//
//                        // Fire splash closed event
//                        providerToNotify.onSplashClosed();
//                    }
//                });
//
//                splashWindow = null;
//                splashProvider = null;
//            }

            // Make the launched panel's internal frame visible
            // 12/13 temp removal for debugging
//            IWindow window = reg.getWindow();
//            if (window != null) {
//                window.setVisible(true);
//                reg.setVisible(true);
//                reg.fireOnShow();
//            }
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
            registerContext(ctx);
        }

        // Create registration
        String fullId = namespace + ":" + panelId;
        panel.setName(fullId);
        PanelRegistration reg = new PanelRegistration(namespace, panelId, panel, null);
        ctx.registerPanel(panelId, reg);

        // Create frame immediately if in desktop mode and desktop exists
        if (Boolean.TRUE.equals(isDesktop()) && this.getDesktop() != null) {
            createInternalFrameForPanel(reg);
        }

        return reg;
    }

    /**
     * Register the context in the registry.
     * <p>
     * NOTE: Despite being private, this will end up being called during
     * the _init() hierarchy from all tiers.
     * 
     * @param ctx
     */
    private void registerContext(uiContext ctx) {
        uiContext c = contextRegistry.get(ctx.getNamespace());
        if (c == null) {
            contextRegistry.put(ctx.getNamespace(), ctx);
        } else {
            System.out.println("WARN - Attempt to re-register namespace: " + ctx.getNamespace());
        }

        // I may not keep this here but useful for now
        dumpFoundationStructure();
    }

    /**
     * Permits access to the context registry.
     * <p>
     * TODO I believe this is also a temporary fix in Bronze for the context
     * code to work in the registerUI() method.  This needs more research but
     * I am pretty sure that a "tool context" should not need access to the
     * overall "tool context registry".
     * 
     * @return
     */
    public Map<String, uiContext> getContextRegistry() {
        return contextRegistry;
    }

    /**
     * Creates an internal frame for a single panel registration.
     * <p>
     * This is called either during initialization (for panels registered before init)
     * or immediately when a panel is registered after init.
     * NOTE:  The above comment was written early in development,
     * as of DEC 2025, panels can no longer be registered before init
     * because the role of init is to initialize the swing framework
     * before any swing components (including panels) are created.
     * Remove eventually but I want to keep these comments for now.
     *
     * @param reg The panel registration
     */
    protected void createInternalFrameForPanel(PanelRegistration reg) {

        if (reg.getInternalFrame() != null) {
            // Already has an internal frame, skip
            return;
        }

        // Create internal frame
        final XInternalFrame iframe = new XInternalFrame();

        // Set up bidirectional reference
        reg.getPanel().setParent(iframe);
        reg.setInternalFrame(iframe);

        // Use windowTitle if set, otherwise fallback to fullId
        String title = reg.getWindowTitle() != null ? reg.getWindowTitle() : reg.getFullId();
        iframe.setTitle(title);
        System.out.println("created iframe: " + title);

        // Add contents to internal frame
        iframe.add(reg.getPanel());

        // Configure frame properties
        // System panels (namespace "system") are not minimizable
        // Tool panels can be minimized
        boolean isSystemPanel = "system".equals(reg.getNamespace());
        iframe.setIconifiable(!isSystemPanel);
        iframe.setResizable(true);
        iframe.setClosable(false);
        iframe.setMaximizable(true);

        // Apply positioning
        iframe.pack(); // Pack before positioning to get correct size
        reg.getWindowPosition().apply(iframe, this.getDesktop());

        // Frame is created but NOT visible by default
        // Only frames shown via launch() will be made visible
        iframe.setVisible(false);

        // Add to desktop
        // TODO eventually this needs to add via the desktop provider of the current app context
        JDesktopPane d = this.getDesktop();
        System.out.println("JDesktopPane bronze: " + uUtility.objString(d));
        d.add(iframe);

        // Fire onCreate event
        reg.fireOnCreate();
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     * <p>
     * Bronze adds the ability to register one or more application contexts.
     */
    protected uiContext _init(uiContext ctx) {

        // Bronze requires to register the context first
        registerContext(ctx);

        super._init(ctx);

        return ctx;
    }

    protected void showSplashScreen(uiContext ctx) {

        // If splash screen is enabled, show it.
        if (ctx.getSplashProvider() != null) {

            // Show the external frame synchronously (with desktop as content pane)
            this.showExternalFrameSynchronously();

            uiSplashProvider splasher = ctx.getSplashProvider();
            if (this.isDesktop()) {

                // Create splash screen as a PanelRegistration (just like any other panel)
                JPanel splashContent = splasher.createSplashContent();
                XPanel splashPanel = new XPanel(splashContent);
                // Register in the context so we can find it later to close it
                // Use CENTER positioning to center the splash on the desktop
                PanelRegistration splashReg = ctx.registerUI(
                        "splash", 
                        splashPanel,
                        WindowPosition.center()
                );
                splashReg.setWindowTitle("Loading...");

                // Create the internal frame (adds to desktop, applies positioning)
                createInternalFrameForPanel(splashReg);

                // Show it (makes visible and brings to front)
                splashReg.show();

            } else {

                // Window mode: show splash in the external frame
                JPanel splashContent = splasher.createSplashContent();

                // Show the external frame with splash content synchronously
                if (this.getExternalFrame() != null) {
                    this.getExternalFrame().setContentPane(splashContent);
                }

            }
        }

    }

    /**
     * Closes the splash screen if it exists.
     * <p>
     * This method:
     * - Finds the splash screen panel registration (system:splash)
     * - Closes and removes it from the registry
     * - Calls the splash provider's onSplashClosed() callback
     *
     * @param ctx The context to search for the splash screen
     */
    protected void closeSplashScreen(uiContext ctx) {
        if (ctx.getSplashProvider() == null) {
            return; // No splash provider, nothing to close
        }

        // Find the splash panel registration
        PanelRegistration splashReg = ctx.getPanelRegistration("splash");
        if (splashReg != null) {
            // Close the panel (fires onClose event, closes window, removes from registry)
            closePanel(ctx, splashReg);

            // Notify the splash provider
            ctx.getSplashProvider().onSplashClosed();
        }
    }

    @Override
    /**
     * TODO remove this method, its function has been deprecated from the design
     */
    protected void initializeOtherWindows() {
//        if (Boolean.TRUE.equals(this.isDesktop)) {
//            // Iterate through all contexts and their panels
//            for (uiContext ctx : contextRegistry.values()) {
//                for (PanelRegistration reg : ctx.getAllPanelRegistrations().values()) {
//                    createInternalFrameForPanel(reg);
//                }
//            }
//        }
    }

    /**
     * Dumps the Foundation object containment structure to System.out.
     * Shows all registered contexts and their panels in a directory-like format.
     * <p>
     * Useful for debugging to see what contexts and panels are currently registered.
     */
    public void dumpFoundationStructure() {
        System.out.println("=== Foundation Object Structure ===");
        System.out.println("Registered Contexts: " + contextRegistry.size());

        if (contextRegistry.isEmpty()) {
            System.out.println("  (no contexts registered)");
        } else {
            for (Map.Entry<String, uiContext> entry : contextRegistry.entrySet()) {
                String namespace = entry.getKey();
                uiContext ctx = entry.getValue();

                System.out.println("  [Context] " + namespace);

                Map<String, PanelRegistration> panels = ctx.getAllPanelRegistrations();
                if (panels.isEmpty()) {
                    System.out.println("    └─ (no panels registered)");
                } else {
                    int count = 0;
                    int total = panels.size();
                    for (Map.Entry<String, PanelRegistration> panelEntry : panels.entrySet()) {
                        count++;
                        boolean isLast = (count == total);
                        String prefix = isLast ? "    └─" : "    ├─";

                        String panelId = panelEntry.getKey();
                        PanelRegistration reg = panelEntry.getValue();

                        System.out.println(prefix + " [Panel] " + panelId +
                            " (visible: " + reg.isVisible() + ")");
                    }
                }
            }
        }
        System.out.println("===================================");
    }

}
