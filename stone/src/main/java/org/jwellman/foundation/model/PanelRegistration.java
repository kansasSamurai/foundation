package org.jwellman.foundation.model;

import org.jwellman.foundation.framework.WindowPosition;
import org.jwellman.foundation.interfaces.PanelLifecycleListener;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;
import org.jwellman.foundation.swing.XPanel;

/**
 * Metadata container for a registered panel in the Foundation framework.
 *
 * This class tracks all information about a panel including:
 * - Namespace and panel ID for identification
 * - The panel itself (wrapped in XPanel)
 * - Window containers (JFrame or JInternalFrame)
 * - Visibility state
 * - Positioning preferences
 * - Lifecycle listeners
 *
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

    /** The wrapped panel */
    private final XPanel panel;

    /** Desktop mode container (null in window mode) */
    private XInternalFrame internalFrame;

    /** Window mode container (null in desktop mode) */
    private XFrame externalFrame;

    /** Current visibility state */
    private boolean visible;

    /** Window positioning strategy */
    private WindowPosition windowPosition;

    /** Lifecycle event listener (optional) */
    private PanelLifecycleListener lifecycleListener;

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
        this.visible = false;

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
    public PanelRegistration(String namespace, String panelId, XPanel panel, PanelLifecycleListener listener) {
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

    // Getters and setters

    public String getNamespace() {
        return namespace;
    }

    public String getPanelId() {
        return panelId;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public WindowPosition getWindowPosition() {
        return windowPosition;
    }

    public void setWindowPosition(WindowPosition windowPosition) {
        this.windowPosition = windowPosition;
    }

    public PanelLifecycleListener getLifecycleListener() {
        return lifecycleListener;
    }

    public void setLifecycleListener(PanelLifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public String toString() {
        return "PanelRegistration{" +
                "namespace='" + namespace + '\'' +
                ", panelId='" + panelId + '\'' +
                ", visible=" + visible +
                ", fullId='" + getFullId() + '\'' +
                '}';
    }

}
