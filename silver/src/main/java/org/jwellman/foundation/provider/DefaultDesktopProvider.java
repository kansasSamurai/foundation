package org.jwellman.foundation.provider;

import java.awt.Color;
import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;

import org.jwellman.foundation.interfaces.uiDesktopProvider;

/**
 * Default implementation of uiDesktopProvider.
 *
 * This is the framework-provided default desktop provider that creates
 * a standard JDesktopPane with sensible defaults. It is used automatically
 * when no custom desktop provider is specified in uContext.
 *
 * Default Configuration:
 * - Drag mode: OUTLINE_DRAG_MODE (faster dragging, less resource-intensive)
 * - Background: Light gray (standard desktop look)
 * - No menu bar (applications can add their own)
 * - No post-initialization actions
 *
 * Custom Desktop Providers:
 * Applications can create their own implementations of uiDesktopProvider
 * to provide custom desktop environments with:
 * - Custom backgrounds (gradients, images, patterns)
 * - Desktop-level menu bars
 * - Desktop icons or shortcuts
 * - Custom drag modes or behaviors
 * - Background services or monitoring
 *
 * Context via Constructor:
 * If a provider needs context (window reference, configuration), it should
 * receive it via constructor. The interface methods have no parameters to
 * support nested desktops and maintain clean separation of concerns.
 *
 * Example Custom Provider with Context:
 * <pre>
 * public class MyDesktopProvider implements uiDesktopProvider {
 *     private final IWindow containerWindow;
 *
 *     public MyDesktopProvider(IWindow window) {
 *         this.containerWindow = window;
 *     }
 *
 *     public JDesktopPane createDesktop() {
 *         JDesktopPane desktop = new JDesktopPane();
 *         desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
 *         desktop.setBackground(new Color(40, 40, 60)); // Dark blue
 *         return desktop;
 *     }
 *
 *     public JMenuBar createMenuBar() {
 *         JMenuBar menuBar = new JMenuBar();
 *         // Add File, Window, Help menus...
 *         return menuBar;
 *     }
 *
 *     public void onDesktopInitialized(JDesktopPane desktop) {
 *         // Can use containerWindow here if needed
 *         System.out.println("Desktop initialized in: " + containerWindow);
 *     }
 * }
 *
 * // Use in context:
 * uContext context = uContext.createContext("myapp");
 * context.setDesktopProvider(new MyDesktopProvider(myWindow));
 * </pre>
 *
 * @author Foundation Framework
 */
public class DefaultDesktopProvider implements uiDesktopProvider {

    private JDesktopPane desktop;

    /**
     * Creates a standard JDesktopPane with default configuration.
     *
     * Configuration:
     * - Drag mode: OUTLINE_DRAG_MODE
     * - Background: Light gray
     *
     * The framework handles setting this as the container's content pane.
     *
     * @return The configured JDesktopPane
     */
    @Override
    public JDesktopPane createDesktop() {
        desktop = new JDesktopPane();

        // Use OUTLINE_DRAG_MODE for better performance
        // This shows an outline while dragging instead of the full window
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        // Set a standard desktop background color
        desktop.setBackground(Color.LIGHT_GRAY);

        return desktop;
    }

    /**
     * Returns null (no menu bar by default).
     *
     * Applications can provide custom desktop providers that add menu bars,
     * or they can add menu bars directly to their frames.
     *
     * @return null (no menu bar)
     */
    @Override
    public JMenuBar createMenuBar() {
        // No menu bar in the default implementation
        // Applications can add their own menu bars if needed
        return null;
    }

    /**
     * No-op implementation (no post-initialization actions by default).
     *
     * Custom desktop providers can override this to perform actions
     * after the desktop is fully initialized and visible.
     *
     * @param desktop The initialized desktop pane
     */
    @Override
    public void onDesktopInitialized(JDesktopPane desktop) {
        // No post-initialization actions in the default implementation
        // Custom providers can override this to add desktop icons,
        // start background services, display welcome dialogs, etc.
    }

    /**
     * 
     */
    @Override
    public JDesktopPane getDesktop() {
        return desktop;
    }

}
