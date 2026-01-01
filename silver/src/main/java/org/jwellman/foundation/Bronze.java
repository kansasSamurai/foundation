package org.jwellman.foundation;

import java.awt.BorderLayout;
import java.awt.IllegalComponentStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.framework.uUtility;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiSplashProvider;
import org.jwellman.foundation.interfaces.uiViewProvider;
import org.jwellman.foundation.model.FrameDescriptor;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;
import org.jwellman.foundation.swing.XPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(Bronze.class);

    /**
     * The context registry.
     * Key: namespace (e.g., "tool.calculator", "tool.editor")
     * Value: uContext containing panel registry for that namespace
     *
     * This creates a drill-down structure:
     * Bronze -> uContext (by namespace) -> FrameDescriptor (by panelId)
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

        FrameDescriptor reg = ctx.getFrameDescriptor(panelId);
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

        return ctx.getAllFrameDescriptors().values().stream()
                .map(FrameDescriptor::getPanel)
                .collect(Collectors.toList());
    }

    /**
     * Get all panel registrations for a namespace.
     *
     * @param namespace The namespace
     * @return List of FrameDescriptors (may be empty, never null)
     */
    public List<FrameDescriptor> getRegistrations(String namespace) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) return new ArrayList<>();

        return new ArrayList<>(ctx.getAllFrameDescriptors().values());
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
     * @return The FrameDescriptor, or null if not found
     */
    public FrameDescriptor getRegistration(String namespace, String panelId) {
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) {
            dumpFoundationStructure(namespace, panelId);
            return null;
        }

        FrameDescriptor reg = ctx.getFrameDescriptor(panelId);
        if (reg == null) {
            dumpFoundationStructure(namespace, panelId);
        }

        return reg;
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
        FrameDescriptor reg = getRegistration(namespace, panelId);
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
        FrameDescriptor reg = getRegistration(namespace, panelId);
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
        FrameDescriptor reg = getRegistration(namespace, panelId);
        if (reg != null) {
            reg.toggle();
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
        FrameDescriptor reg = getRegistration(namespace, panelId);
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

        FrameDescriptor reg = ctx.getFrameDescriptor(panelId);
        if (reg != null) {
            closePanel(ctx, reg);
        }
    }

    /**
     * Close a panel and remove it from the registry.
     * <p>
     * Fires the onClose lifecycle event.
     * TODO Eventually (but probably not soon), the FrameDescriptor may
     * contain a reference to its parent uiContext in which case only the
     * FrameDescriptor parameter will be necessary here.
     *
     * @param ctx The uiContext containing the FrameDescriptor
     * @param reg The FrameDescriptor to be removed from the uiContext
     */
    public void closePanel(uiContext ctx, FrameDescriptor reg) {
        // Fire lifecycle event
        reg.fireOnClose();

        // Close the window
        IWindow window = reg.getWindow();
        if (window != null) {
            window.close();
        }

        // Remove from context's registry
        ctx.removeFrameDescriptor(reg.getPanelId());

        // Fire registry changed event (Silver tier)
        ctx.fireRegistryChanged();
    }

    /**
     * Detach or attach a panel by switching its container type.
     * <p>
     * If the panel is currently in a JInternalFrame (desktop mode), it will be
     * moved to a standalone JFrame (window mode). If it's currently in a JFrame,
     * it will be moved back to a JInternalFrame in the desktop.
     * <p>
     * This enables IDE-like behavior where panels can be "popped out" into
     * separate windows or "docked back" into the main desktop environment.
     * <p>
     * The operation preserves window title, size, position (translated between
     * desktop and screen coordinates), and visibility state.
     *
     * @param namespace The namespace
     * @param panelId The panel ID
     */
    public void detachPanel(String namespace, String panelId) {
        FrameDescriptor reg = getRegistration(namespace, panelId);
        if (reg == null) {
            log.warn("Cannot detach panel - not found: {}:{}", namespace, panelId);
            return;
        }

        // Determine current type and switch
        if (reg.getInternalFrame() != null) {
            // Currently internal frame, switch to external frame
            detachToExternalFrame(reg);
        } else if (reg.getExternalFrame() != null) {
            // Currently external frame, switch to internal frame
            attachToInternalFrame(reg);
        } else {
            log.warn("Panel has no window container: {}:{}", namespace, panelId);
        }

        // Fire registry changed event (Silver tier)
        uiContext ctx = contextRegistry.get(namespace);
        ctx.fireRegistryChanged();
    }

    /**
     * Detach a panel from its internal frame and create an external frame.
     *
     * @param reg The panel registration
     */
    private void detachToExternalFrame(FrameDescriptor reg) {
        XInternalFrame iframe = reg.getInternalFrame();
        JDesktopPane desktop = this.getDesktop();

        // Preserve state
        boolean wasVisible = iframe.isVisible();
        java.awt.Dimension iframeSize = iframe.getSize();
        java.awt.Point iframeLocation = iframe.getLocation();

        // Menu bar is already stored in FrameDescriptor
        // (No need to extract from internal frame since it's in the wrapper panel)

        // Convert desktop coordinates to screen coordinates
        java.awt.Point screenLocation;
        if (desktop != null) {
            try {
                java.awt.Point desktopScreenLocation = desktop.getLocationOnScreen();
                screenLocation = new java.awt.Point(
                    desktopScreenLocation.x + iframeLocation.x,
                    desktopScreenLocation.y + iframeLocation.y
                );
            } catch (IllegalComponentStateException e) {
                // Desktop not showing, use default location
                screenLocation = new java.awt.Point(100, 100);
            }
        } else {
            screenLocation = new java.awt.Point(100, 100);
        }

        // Hide internal frame and remove from desktop
        iframe.setVisible(false);
        if (desktop != null) {
            desktop.remove(iframe);
        }

        // Clear internal frame reference so createFrameForPanel can proceed
        reg.setInternalFrame(null);

        // Reuse existing method to create external frame with proper setup
        createFrameForPanel(reg);

        // Override size and location with preserved values
        XFrame frame = reg.getExternalFrame();
        frame.setSize(iframeSize);
        frame.setLocation(screenLocation);

        // Restore visibility
        if (wasVisible) {
            frame.setVisible(true);
        }

        log.info("Detached panel to external frame: {}", reg.getFullId());
    }

    /**
     * Attach a panel from its external frame back to an internal frame.
     *
     * @param reg The panel registration
     */
    private void attachToInternalFrame(FrameDescriptor reg) {
        JDesktopPane desktop = this.getDesktop();
        if (desktop == null) {
            log.warn("Cannot attach to desktop - no desktop available for: {}", reg.getFullId());
            return;
        }

        XFrame frame = reg.getExternalFrame();

        // Preserve state
        boolean wasVisible = frame.isVisible();
        java.awt.Dimension frameSize = frame.getSize();
        java.awt.Point screenLocation = frame.getLocation();

        // Menu bar is already stored in FrameDescriptor (set during registerUI)
        // It will be automatically applied when createInternalFrameForPanel is called

        // Convert screen coordinates to desktop coordinates
        java.awt.Point desktopLocation;
        try {
            java.awt.Point desktopScreenLocation = desktop.getLocationOnScreen();
            desktopLocation = new java.awt.Point(
                screenLocation.x - desktopScreenLocation.x,
                screenLocation.y - desktopScreenLocation.y
            );
        } catch (IllegalComponentStateException e) {
            // Desktop not showing, use default location
            desktopLocation = new java.awt.Point(10, 10);
        }

        // Hide and dispose external frame
        frame.setVisible(false);
        // frame.getContentPane().removeAll(); // leaving as a reminder that this causes a bug so do not use it for other implementations
        frame.dispose();

        // Clear external frame reference so createInternalFrameForPanel can proceed
        reg.setExternalFrame(null);

        // Reuse existing method to create internal frame with proper setup
        createInternalFrameForPanel(reg);

        // Override size and location with preserved values
        XInternalFrame iframe = reg.getInternalFrame();
        iframe.setSize(frameSize);
        iframe.setLocation(desktopLocation);

        // Restore visibility
        if (wasVisible) {
            iframe.setVisible(true);
        }

        log.info("Attached panel to internal frame: {}", reg.getFullId());
    }

    /**
     * Get all registrations (for internal use).
     *
     * @return List of all FrameDescriptors across all contexts
     */
    protected List<FrameDescriptor> getAllRegistrations() {
        List<FrameDescriptor> allRegs = new ArrayList<>();
        for (uiContext ctx : contextRegistry.values()) {
            allRegs.addAll(ctx.getAllFrameDescriptors().values());
        }
        return allRegs;
    }

    // Counter for auto-registration of launched panels
    private int autoRegistrationCounter = 0;

    protected void _launch(uiContext ctx) {

        // Close splash screen if it exists (before showing the main application)
        // UNLESS it's user-dismissable (view provider + minimum display time = 0)
        if (ctx == masterContext) {
            uiViewProvider viewProvider = ctx.getViewProvider();
            uiSplashProvider splashProvider = ctx.getSplashProvider();

            boolean isUserDismissable = (viewProvider != null && splashProvider != null
                && splashProvider.getMinimumDisplayTime() == 0);

            if (!isUserDismissable) {
                closeSplashScreen(ctx);
            }
            // If user-dismissable, splash remains visible until user clicks dismiss button
        }

        super._launch(ctx);

        // Stone only launches the masterContext so Bronze needs to launch others
        if (ctx == masterContext) {
            log.info("Bronze bypass master context");
        } else {

            //
            /* Once we support the init() method being called more than once
             * (like in multi tool desktop(s), calling it here may be redundant
             *  but would not be expected to hurt since it would just replace
             *  itself in the registry.  Making this note for future self.
             */
            registerContext(ctx);

            if (isDesktop()) {
                // Desktop mode: create internal frame (if not already created) and show it
                this.createInternalFrameForPanel(ctx.getMasterPanel());
                ctx.getMasterPanel().getInternalFrame().show();
            } else {
                // Window mode: create external frame (if not already created) and show it
                this.createFrameForPanel(ctx.getMasterPanel());
                ctx.getMasterPanel().getExternalFrame().setVisible(true);
            }

        }
    }

    /**
     * Creates an external frame (JFrame) for a panel registration in multi-window mode.
     * <p>
     * This mirrors createInternalFrameForPanel() but creates a top-level JFrame instead.
     * The frame is created but NOT made visible - visibility is handled later by
     * existing show mechanisms (reg.show(), launchWindow(), etc.).
     *
     * @param reg The panel registration
     */
    protected void createFrameForPanel(FrameDescriptor reg) {
        if (reg.getExternalFrame() != null) {
            // Already has an external frame, skip
            return;
        }

        // Create external frame
        String title = reg.getWindowTitle() != null ? reg.getWindowTitle() : reg.getFullId();
        final XFrame frame = new XFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app when closing individual windows

        // Set up bidirectional reference
        reg.getPanel().setParent(frame);
        reg.setExternalFrame(frame);

        log.debug("Created external frame: {}", title);

        // Set panel as content
        frame.setContentPane(reg.getPanel());

        // Set menu bar if present
        if (reg.getMenuBar() != null) {
            frame.setJMenuBar(reg.getMenuBar());
        }

        // Apply positioning
        frame.pack(); // Pack before positioning to get correct size
        reg.getWindowPosition().apply(frame, null); // null desktop for window positioning

        // Frame is created but NOT visible by default
        // Only frames shown via launch() or show() will be made visible
        frame.setVisible(false);

        // Fire onCreate event
        reg.fireOnCreate();
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
        FrameDescriptor reg = findRegistrationByPanel(panel);

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
     * @return The FrameDescriptor, or null if not found
     */
    private FrameDescriptor findRegistrationByPanel(XPanel panel) {
        for (uiContext ctx : contextRegistry.values()) {
            for (FrameDescriptor reg : ctx.getAllFrameDescriptors().values()) {
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
     * @return The created FrameDescriptor
     */
    private FrameDescriptor autoRegisterPanel(String namespace, String panelId, XPanel panel) {

        // Get or create context
        uiContext ctx = contextRegistry.get(namespace);
        if (ctx == null) {
            ctx = Foundation.createContext(namespace);
            registerContext(ctx);
        }

        // Create registration
        String fullId = namespace + ":" + panelId;
        panel.setName(fullId);
        FrameDescriptor reg = new FrameDescriptor(namespace, panelId, panel, null);
        ctx.registerPanel(panelId, reg);

        // Create frame immediately based on mode
        if (Boolean.TRUE.equals(isDesktop()) && this.getDesktop() != null) {
            createInternalFrameForPanel(reg);
        } else if (!Boolean.TRUE.equals(isDesktop())) {
            // Window mode: create external frame for multi-window mode
            createFrameForPanel(reg);
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
            log.warn("Attempt to re-register namespace: {}", ctx.getNamespace());
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
    protected void createInternalFrameForPanel(FrameDescriptor reg) {

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
        log.debug("Created internal frame: {}", title);

        // Add contents to internal frame
        XPanel wrapper;
        if (reg.getPanel() instanceof XPanel) {
            // Panel is already XPanel, use it as wrapper
            wrapper = (XPanel) reg.getPanel();
        } else {
            // Panel is not XPanel, create wrapper and log warning
            // Use XPanel as wrapper (has BorderLayout by default)
            log.warn("Panel {} is not an XPanel - creating XPanel wrapper for menu bar", reg.getFullId());
            wrapper = new XPanel(reg.getPanel());
            wrapper.add(reg.getPanel(), BorderLayout.CENTER);
        }

        // For JInternalFrame, menu bar must be manually placed in BorderLayout
        if (reg.getMenuBar() != null) {
            wrapper.add(reg.getMenuBar(), BorderLayout.NORTH);
        }
        iframe.add(wrapper);

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
        log.debug("JDesktopPane bronze: {}", uUtility.objString(d));
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

    /**
     * Shows the splash screen if a splash provider exists.
     * Called from Stone._initializeAndShowWindow() during init() if splash provider exists.
     * <p>
     * This method:
     * - Creates the splash content using the splash provider
     * - In desktop mode: Registers splash as a panel and creates/shows internal frame
     * - In window mode: Sets splash content as the frame's content pane
     * <p>
     * Note: Must be called AFTER showExternalFrameSynchronously() so the external frame exists.
     * In desktop mode, this method DOES show the splash internal frame via splashReg.show().
     */
    @Override
    protected void showSplashScreen(uiContext ctx) {
        if (ctx.getSplashProvider() == null) {
            return; // No splash provider, nothing to prepare
        }

        uiSplashProvider splasher = ctx.getSplashProvider();
        JPanel splashContent = splasher.createSplashContent();

        // Check if we're using card-based view management (Silver tier)
        uiViewProvider viewProvider = ctx.getViewProvider();

        if (viewProvider != null) {
            // Silver tier: Card-based splash - add as "splash" card BEFORE "main" card
            // This must happen before the "main" card is added by showExternalFrameSynchronously()
            // or showFrameWithContent()
            viewProvider.addCard("splash", splashContent);
            viewProvider.showCard("splash");

            log.debug("Splash screen added as 'splash' card");

            // Note: No separate window/frame needed - splash is a card in the CardLayout container

        } else {
            // Legacy behavior (Stone/Bronze without view provider)

            if (this.isDesktop()) {
                // Desktop mode: Create splash as an internal frame

                // Create splash screen as a FrameDescriptor (just like any other panel)
                XPanel splashPanel = new XPanel(splashContent);

                // Register in the context so we can find it later to close it
                // Use CENTER positioning to center the splash on the desktop
                FrameDescriptor splashReg = ctx.registerUI(
                        "splash",
                        splashPanel,
                        WindowPosition.center()
                );
                splashReg.setWindowTitle("Loading...");

                // Create the internal frame (adds to desktop, applies positioning)
                createInternalFrameForPanel(splashReg);
                XInternalFrame iframe = splashReg.getInternalFrame();
                iframe.setIconifiable(false);
                iframe.setResizable(false);
                iframe.setClosable(false);
                iframe.setMaximizable(false);

                // Show it (makes visible and brings to front)
                splashReg.show();

            } else {
                // Window mode: Set splash content as frame's content pane

                // Show frame with splash content (sets content pane, packs, centers, shows)
                showFrameWithContent(splashContent);
            }
        }
    }

    /**
     * Closes the splash screen if it exists.
     * <p>
     * This method:
     * - In Silver tier with view provider: Switches from "splash" card to "main" card
     * - In legacy mode: Finds and closes the splash panel registration
     * - Calls the splash provider's onSplashClosed() callback
     *
     * @param ctx The context to search for the splash screen
     */
    protected void closeSplashScreen(uiContext ctx) {
        if (ctx.getSplashProvider() == null) {
            return; // No splash provider, nothing to close
        }

        // Check if we're using card-based view management (Silver tier)
        uiViewProvider viewProvider = ctx.getViewProvider();

        if (viewProvider != null) {
            // Silver tier: Card-based splash - switch from "splash" to "main"
            if ("splash".equals(viewProvider.getCurrentCard())) {
                viewProvider.showCard("main");
                log.debug("Switched from splash card to main card");
            }

            // No frame/panel to close - splash is just a card

        } else {
            // Legacy behavior (Stone/Bronze without view provider)

            // Find the splash panel registration
            FrameDescriptor splashReg = ctx.getFrameDescriptor("splash");
            if (splashReg != null) {
                // Close the panel (fires onClose event, closes window, removes from registry)
                closePanel(ctx, splashReg);
            }
        }

        // Notify the splash provider that splash is closed
        ctx.getSplashProvider().onSplashClosed();
    }

    /**
     * Utility method to log searched values before dumping the structure to logs.
     * 
     * @param namespace
     * @param panelId
     */
    private void dumpFoundationStructure(String namespace, String panelId) {
        log.warn("=== Foundation Object Search failed ===");
        System.out.println("=== Foundation Object Search failed ===");
        System.out.println(String.format("String namespace: %s, String panelId: %s",
                namespace, panelId));

        dumpFoundationStructure();
    }

    /**
     * Dumps the Foundation object containment structure to System.out.
     * Shows all registered contexts and their panels in a directory-like format.
     * <p>
     * Useful for debugging to see what contexts and panels are currently registered.
     */
    public void dumpFoundationStructure() {
        log.info("=== Foundation Object Structure ===");
        System.out.println("=== Foundation Object Structure ===");
        System.out.println("Registered Contexts: " + contextRegistry.size());

        if (contextRegistry.isEmpty()) {
            System.out.println("  (no contexts registered)");
        } else {
            for (Map.Entry<String, uiContext> entry : contextRegistry.entrySet()) {
                String namespace = entry.getKey();
                uiContext ctx = entry.getValue();

                System.out.println("  [Context] " + namespace);

                Map<String, FrameDescriptor> panels = ctx.getAllFrameDescriptors();
                if (panels.isEmpty()) {
                    System.out.println("    └─ (no panels registered)");
                } else {
                    int count = 0;
                    int total = panels.size();
                    for (Map.Entry<String, FrameDescriptor> panelEntry : panels.entrySet()) {
                        count++;
                        boolean isLast = (count == total);
                        String prefix = isLast ? "    └─" : "    ├─";

                        String panelId = panelEntry.getKey();
                        FrameDescriptor reg = panelEntry.getValue();

                        System.out.println(prefix + " [Panel] " + panelId +
                            " (visible: " + reg.isVisible() + ")");
                    }
                }
            }
        }
        System.out.println("===================================");
    }

}
