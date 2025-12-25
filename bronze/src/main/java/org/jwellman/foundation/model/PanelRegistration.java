package org.jwellman.foundation.model;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.uiPanelLifecycleListener;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;
import org.jwellman.foundation.swing.XPanel;

/**
 * Metadata container for a registered panel in the Foundation framework.
 * <p>
 * This class tracks all information about a panel including:<br>
 * - Namespace and panel ID for identification<br>
 * - The panel itself (wrapped in XPanel)<br>
 * - Window containers (JFrame or JInternalFrame)<br>
 * - Visibility state<br>
 * - Positioning preferences<br>
 * - Lifecycle listeners<br>
 * <p>
 * PanelRegistration supports the multi-window management capabilities
 * of the Bronze tier and above.
 *
 * @author Foundation Framework
 */
public class PanelRegistration {

    /** Tool/application identifier (e.g., "tool.calculator") */
    private final String namespace;

    /** Unique panel ID within namespace (e.g., "main", "settings", "history") */
    private final String panelId;

    /** The title to display in the window decoration */
    private String windowTitle;

    /** The wrapped panel */
    private final XPanel panel;

    /** Desktop mode container (null in window mode) */
    private XInternalFrame internalFrame;

    /** Window mode container (null in desktop mode) */
    private XFrame externalFrame;

    /** Tracks whether show() has been called and completed at least once */
    private boolean firstShowCompleted;

    /** Window positioning strategy */
    private WindowPosition windowPosition;

    /** Lifecycle event listener (optional) */
    private uiPanelLifecycleListener lifecycleListener;

    /**
     * Creates a new PanelRegistration.
     *
     * @param namespace Tool/application identifier
     * @param panelId Unique ID within namespace
     * @param panel The wrapped panel
     */
    public PanelRegistration(String namespace, String panelId, XPanel panel) {
        if (namespace == null || namespace.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be null or empty");
        }
        if (panelId == null || panelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Panel ID cannot be null or empty");
        }
        if (panel == null) {
            throw new IllegalArgumentException("Panel cannot be null");
        }

        this.namespace = namespace;
        this.panelId = panelId;
        this.panel = panel;

        // Default positioning
        this.windowPosition = WindowPosition.cascade();
    }

    /**
     * Creates a new PanelRegistration with a lifecycle listener.
     *
     * @param namespace Tool/application identifier
     * @param panelId Unique ID within namespace
     * @param panel The wrapped panel
     * @param listener Lifecycle event listener
     */
    public PanelRegistration(String namespace, String panelId, XPanel panel, uiPanelLifecycleListener listener) {
        this(namespace, panelId, panel);
        this.lifecycleListener = listener;
    }

    /**
     * Returns the composite key "namespace:panelId".
     *
     * @return The full identifier
     */
    public String getFullId() {
        return namespace + ":" + panelId;
    }

    /**
     * Gets the IWindow container for this panel.
     * Returns the internal frame in desktop mode, external frame in window mode.
     *
     * @return The window container, or null if not yet created
     */
    public IWindow getWindow() {
        if (internalFrame != null) {
            return internalFrame;
        }
        return externalFrame;
    }

    /**
     * Fire the onCreate lifecycle event.
     */
    public void fireOnCreate() {
        if (lifecycleListener != null) {
            IWindow window = getWindow();
            if (window != null) {
                lifecycleListener.onCreate(window);
            }
        }
    }

    /**
     * Fire the onShow lifecycle event.
     */
    public void fireOnShow() {
        if (lifecycleListener != null) {
            IWindow window = getWindow();
            if (window != null) {
                lifecycleListener.onShow(window);
            }
        }
    }

    /**
     * Fire the onHide lifecycle event.
     */
    public void fireOnHide() {
        if (lifecycleListener != null) {
            IWindow window = getWindow();
            if (window != null) {
                lifecycleListener.onHide(window);
            }
        }
    }

    /**
     * Fire the onClose lifecycle event.
     */
    public void fireOnClose() {
        if (lifecycleListener != null) {
            IWindow window = getWindow();
            if (window != null) {
                lifecycleListener.onClose(window);
            }
        }
    }

    /**
     * Show this panel's window.
     * <p>
     * On first show, applies window positioning and adds the internal frame to the desktop.
     * Fires the onShow lifecycle event.
     */
    public void show() {
        IWindow window = getWindow();
        if (window == null) {
            return; // No window to show
        }

        // Skip if already visible
        if (isVisible()) {
            return;
        }

        // First show: apply positioning and add to desktop
        if (!firstShowCompleted && internalFrame != null) {
            javax.swing.JDesktopPane desktop = internalFrame.getDesktopPane();

            if (desktop != null) {
                // Apply window positioning
                if (windowPosition != null) {
                    // This should already be applied during Bronze:createInternalFrameForPanel()
                    // windowPosition.apply(internalFrame, desktop);
                }

                // Add to desktop if not already added
                if (internalFrame.getParent() == null) {
                    // This should already be applied during Bronze:createInternalFrameForPanel()
                    // desktop.add(internalFrame);
                }
            }

            firstShowCompleted = true;
        }

        // Make visible
        window.setVisible(true);
        fireOnShow();
    }

    /**
     * Hide this panel's window.
     * <p>
     * Fires the onHide lifecycle event.
     */
    public void hide() {
        IWindow window = getWindow();
        if (window == null) {
            return; // No window to hide
        }

        // Skip if already hidden
        if (!isVisible()) {
            return;
        }

        // Make invisible
        window.setVisible(false);
        fireOnHide();
    }

    /**
     * Toggle visibility of this panel's window.
     * <p>
     * Calls show() if currently hidden, hide() if currently visible.
     */
    public void toggle() {
        if (isVisible()) {
            hide();
        } else {
            show();
        }
    }

    // Getters and setters

    public String getNamespace() {
        return namespace;
    }

    public String getPanelId() {
        return panelId;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public XPanel getPanel() {
        return panel;
    }

    public XInternalFrame getInternalFrame() {
        return internalFrame;
    }

    public void setInternalFrame(XInternalFrame internalFrame) {
        this.internalFrame = internalFrame;
    }

    public XFrame getExternalFrame() {
        return externalFrame;
    }

    public void setExternalFrame(XFrame externalFrame) {
        this.externalFrame = externalFrame;
    }

    /**
     * Check if this panel's window is currently visible.
     * <p>
     * This delegates to the actual window's visibility state to ensure
     * synchronization between PanelRegistration and the window.
     *
     * @return true if the window exists and is visible, false otherwise
     */
    public boolean isVisible() {
        IWindow window = getWindow();
        return window != null && window.isVisible();
    }

    /**
     * Set the visibility of this panel's window.
     * <p>
     * This is a convenience wrapper that delegates to show() or hide()
     * to ensure proper lifecycle event handling.
     *
     * @param visible true to show the panel, false to hide it
     */
    public void setVisible(boolean visible) {
        if (visible) {
            show();
        } else {
            hide();
        }
    }

    public WindowPosition getWindowPosition() {
        return windowPosition;
    }

    public void setWindowPosition(WindowPosition windowPosition) {
        this.windowPosition = windowPosition;
    }

    public uiPanelLifecycleListener getLifecycleListener() {
        return lifecycleListener;
    }

    public void setLifecycleListener(uiPanelLifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public String toString() {
        return "PanelRegistration{" +
                "namespace='" + namespace + '\'' +
                ", panelId='" + panelId + '\'' +
                ", visible=" + isVisible() +
                ", fullId='" + getFullId() + '\'' +
                '}';
    }

}
