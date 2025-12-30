package org.jwellman.foundation.listener;

import org.jwellman.foundation.interfaces.uiContext;

/**
 * Listener interface for registry-level change events in Foundation framework.
 * <p>
 * Unlike {@link PanelLifecycleListener} which tracks individual panel events,
 * RegistryChangeListener provides a global view of all registry changes including:
 * <ul>
 * <li>Panel registration (new panels added)</li>
 * <li>Panel removal (panels closed)</li>
 * <li>Visibility changes (show/hide)</li>
 * <li>Detach/attach operations</li>
 * </ul>
 * <p>
 * This is particularly useful for:
 * <ul>
 * <li>Updating global UI elements (statistics panels, frame managers, taskbars)</li>
 * <li>Maintaining synchronized views of the registry</li>
 * <li>Implementing application-wide state tracking</li>
 * <li>Decoupling UI updates from business logic</li>
 * </ul>
 * <p>
 * Design Philosophy:
 * <ul>
 * <li><b>Observer Pattern</b> - Clean separation between registry changes and UI responses</li>
 * <li><b>Simple Notification</b> - Single method called for all changes (listeners query registry for details)</li>
 * <li><b>Thread-Safe</b> - Events fired on EDT via SwingUtilities.invokeLater</li>
 * <li><b>Global Scope</b> - Observes all panels across the entire context</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * // Register a listener to update UI when registry changes
 * context.addRegistryChangeListener(() -> {
 *     updateStatisticsPanel();
 *     updateFrameList();
 *     updateTaskbar();
 * });
 *
 * // Now all registry operations automatically trigger updates:
 * context.registerUI("tool", "main", panel);  // → listener called
 * descriptor.show();                           // → listener called
 * descriptor.hide();                           // → listener called
 * context.detachPanel("tool", "main");        // → listener called
 * context.closePanel("tool", "main");         // → listener called
 * </pre>
 * <p>
 * <b>Thread Safety:</b> Listener callbacks are always invoked on the EDT, ensuring
 * safe UI updates without manual SwingUtilities.invokeLater calls.
 *
 * @author Foundation Framework
 * @since Silver Tier
 * @see PanelLifecycleListener
 * @see uiContext#addRegistryChangeListener(RegistryChangeListener)
 * @see uiContext#removeRegistryChangeListener(RegistryChangeListener)
 */
@FunctionalInterface
public interface RegistryChangeListener {

    /**
     * Called whenever the panel registry changes.
     * <p>
     * This single callback handles all types of registry changes:
     * <ul>
     * <li>Panel registered (new panel added)</li>
     * <li>Panel closed (panel removed from registry)</li>
     * <li>Panel visibility changed (show/hide)</li>
     * <li>Panel detached/attached (frame type changed)</li>
     * </ul>
     * <p>
     * Implementations should query the registry to determine current state
     * rather than trying to infer what changed. This keeps the API simple
     * and makes listeners resilient to future framework changes.
     * <p>
     * <b>Thread Safety:</b> This method is always called on the EDT, so UI
     * updates can be performed directly without additional synchronization.
     * <p>
     * <b>Performance:</b> Avoid expensive operations in this callback. If
     * processing takes significant time, consider using a background thread
     * and updating UI via SwingUtilities.invokeLater.
     */
    void onRegistryChanged();

}
