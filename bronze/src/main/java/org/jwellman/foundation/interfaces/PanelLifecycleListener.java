package org.jwellman.foundation.interfaces;

import org.jwellman.foundation.swing.IWindow;

/**
 * Listener interface for panel lifecycle events in Foundation framework.
 * <p>
 * Panels can register lifecycle listeners to be notified of important
 * events such as creation, showing, hiding, and closing.
 * <p>
 * This is particularly useful for:
 * - Initializing resources when a panel is first shown
 * - Saving state when a panel is hidden
 * - Cleanup when a panel is closed
 * - Lazy loading of expensive components
 * <p>
 * Example usage:
 * <pre>
 * Foundation f = Foundation.init();
 * XPanel panel = f.registerUI("tool.calculator", "main", new CalculatorPanel(),
 *     new PanelLifecycleListener() {
 *         public void onCreate(IWindow window) {
 *             System.out.println("Panel created in window");
 *         }
 *         public void onShow(IWindow window) {
 *             System.out.println("Panel shown");
 *         }
 *         public void onHide(IWindow window) {
 *             System.out.println("Panel hidden");
 *         }
 *         public void onClose(IWindow window) {
 *             System.out.println("Panel closed");
 *         }
 *     });
 * </pre>
 *
 * @author Foundation Framework
 */
public interface PanelLifecycleListener {

    /**
     * Called when the panel's window container (JFrame or JInternalFrame) is created.
     * This occurs during Foundation initialization, before the window is shown.
     *
     * @param window The IWindow container (JFrame or JInternalFrame)
     */
    void onCreate(IWindow window);

    /**
     * Called when the panel is shown (made visible).
     * This may be called multiple times if the panel is hidden and shown again.
     *
     * @param window The IWindow container
     */
    void onShow(IWindow window);

    /**
     * Called when the panel is hidden (made invisible).
     * This may be called multiple times during the panel's lifecycle.
     *
     * @param window The IWindow container
     */
    void onHide(IWindow window);

    /**
     * Called when the panel's window is closed/disposed.
     * This is typically a final event before the panel is removed from the registry.
     *
     * @param window The IWindow container
     */
    void onClose(IWindow window);

}
