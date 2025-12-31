package org.jwellman.foundation.provider;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.jwellman.foundation.interfaces.uiDesktopManager;
import org.jwellman.foundation.model.FrameDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of desktop UI controls for Foundation framework.
 * <p>
 * This class provides menu bars, toolbars, and menus that expose desktop management
 * features in a standard desktop application style (similar to IDEs, Eclipse, etc.).
 * <p>
 * <b>IMPORTANT:</b> This is a <b>reference implementation</b> showing one way to build
 * desktop UI controls. It is NOT part of the framework's required API. Applications can:
 * <ul>
 *   <li>Use DefaultDesktopUI as-is</li>
 *   <li>Subclass and customize it</li>
 *   <li>Build completely custom UI calling DefaultDesktopManager methods directly</li>
 *   <li>Ignore it entirely and not provide any desktop UI</li>
 * </ul>
 * <p>
 * <b>Design Philosophy:</b> This class separates "presentation" (what buttons/menus to show)
 * from "logic" (how to manage windows). The {@link DefaultDesktopManager} provides the
 * logic, while this class provides one possible UI presentation.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * DefaultDesktopManager desktopMgr = context.getDesktopManager();
 * DefaultDesktopUI desktopUI = new DefaultDesktopUI(desktopMgr);
 *
 * // Option 1: Full menu bar
 * JMenuBar menuBar = desktopUI.createDesktopMenuBar();
 * frame.setJMenuBar(menuBar);
 *
 * // Option 2: Just add Window menu to existing menu bar
 * JMenu windowMenu = desktopUI.createWindowMenu();
 * existingMenuBar.add(windowMenu);
 *
 * // Option 3: Toolbar
 * JToolBar toolbar = desktopUI.createDesktopToolbar();
 * frame.add(toolbar, BorderLayout.NORTH);
 * </pre>
 *
 * @author Foundation Framework
 * @see DefaultDesktopManager
 * @see uiDesktopManager
 */
public class DefaultDesktopUI {

    private static final Logger log = LoggerFactory.getLogger(DefaultDesktopUI.class);

    /** The desktop manager that performs the actual operations */
    private final uiDesktopManager desktopManager;

    /**
     * Creates a new DefaultDesktopUI.
     *
     * @param desktopManager The desktop manager to use for operations
     * @throws IllegalArgumentException if desktopManager is null
     */
    public DefaultDesktopUI(uiDesktopManager desktopManager) {
        if (desktopManager == null) {
            throw new IllegalArgumentException("Desktop manager cannot be null");
        }
        this.desktopManager = desktopManager;
    }

    // ========================================================================
    // PUBLIC API - MENU BAR AND MENUS
    // ========================================================================

    /**
     * Creates a complete menu bar with Window menu.
     * <p>
     * This provides a standard menu bar that applications can use as-is or customize.
     * Currently includes only the Window menu, but subclasses could add File, Edit, etc.
     *
     * @return A JMenuBar with desktop management menus
     */
    public JMenuBar createDesktopMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createWindowMenu());
        return menuBar;
    }

    /**
     * Creates a Window menu with desktop management actions.
     * <p>
     * The menu includes:
     * <ul>
     *   <li>Cascade Windows</li>
     *   <li>Tile Windows (Grid)</li>
     *   <li>Tile Horizontally</li>
     *   <li>Tile Vertically</li>
     *   <li>---</li>
     *   <li>Minimize All</li>
     *   <li>Restore All</li>
     *   <li>Maximize All</li>
     *   <li>Close All</li>
     *   <li>---</li>
     *   <li>Next Window</li>
     *   <li>Previous Window</li>
     *   <li>---</li>
     *   <li>[List of open windows]</li>
     * </ul>
     *
     * @return A JMenu for window management
     */
    public JMenu createWindowMenu() {
        JMenu menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);

        // Arrangement actions
        menu.add(createMenuItem("Cascade", KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            e -> desktopManager.cascadeFrames()));

        menu.add(createMenuItem("Tile (Grid)", KeyEvent.VK_T,
            KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            e -> desktopManager.tileFrames()));

        menu.add(createMenuItem("Tile Horizontally", KeyEvent.VK_H,
            KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            e -> desktopManager.tileFramesHorizontal()));

        menu.add(createMenuItem("Tile Vertically", KeyEvent.VK_V,
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            e -> desktopManager.tileFramesVertical()));

        menu.addSeparator();

        // State actions
        menu.add(createMenuItem("Minimize All", KeyEvent.VK_M, null,
            e -> desktopManager.minimizeAll()));

        menu.add(createMenuItem("Restore All", KeyEvent.VK_R, null,
            e -> desktopManager.restoreAll()));

        menu.add(createMenuItem("Maximize All", KeyEvent.VK_X, null,
            e -> desktopManager.maximizeAll()));

        menu.add(createMenuItem("Close All", KeyEvent.VK_L, null,
            e -> desktopManager.closeAll()));

        menu.addSeparator();

        // Navigation actions
        menu.add(createMenuItem("Next Window", KeyEvent.VK_N,
            KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
            e -> desktopManager.selectNext()));

        menu.add(createMenuItem("Previous Window", KeyEvent.VK_P,
            KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_DOWN_MASK),
            e -> desktopManager.selectPrevious()));

        menu.addSeparator();

        // Dynamic window list (populated via desktop manager)
        desktopManager.populateWindowMenu(menu);

        return menu;
    }

    // ========================================================================
    // PUBLIC API - TOOLBAR
    // ========================================================================

    /**
     * Creates a toolbar with desktop management buttons.
     * <p>
     * The toolbar includes buttons for common operations:
     * <ul>
     *   <li>Cascade</li>
     *   <li>Tile (Grid)</li>
     *   <li>Tile Horizontally</li>
     *   <li>Tile Vertically</li>
     *   <li>---</li>
     *   <li>Minimize All</li>
     *   <li>Restore All</li>
     * </ul>
     *
     * @return A JToolBar with desktop management buttons
     */
    public JToolBar createDesktopToolbar() {
        JToolBar toolbar = new JToolBar("Desktop Management");
        toolbar.setFloatable(true);

        // Arrangement buttons
        toolbar.add(createToolbarButton("Cascade", "Cascade all windows diagonally",
            e -> desktopManager.cascadeFrames()));

        toolbar.add(createToolbarButton("Tile Grid", "Tile all windows in a grid",
            e -> desktopManager.tileFrames()));

        toolbar.add(createToolbarButton("Tile →", "Tile windows horizontally (side-by-side)",
            e -> desktopManager.tileFramesHorizontal()));

        toolbar.add(createToolbarButton("Tile ↓", "Tile windows vertically (stacked)",
            e -> desktopManager.tileFramesVertical()));

        toolbar.addSeparator();

        // State buttons
        toolbar.add(createToolbarButton("Minimize All", "Minimize all windows",
            e -> desktopManager.minimizeAll()));

        toolbar.add(createToolbarButton("Restore All", "Restore all minimized windows",
            e -> desktopManager.restoreAll()));

        return toolbar;
    }

    // ========================================================================
    // PROTECTED HELPER METHODS (For Subclassing)
    // ========================================================================

    /**
     * Creates a menu item with action.
     * <p>
     * Subclasses can override this to customize menu item appearance or behavior.
     *
     * @param text The menu item text
     * @param mnemonic The mnemonic key code (or 0 for none)
     * @param accelerator The keyboard accelerator (or null for none)
     * @param action The action to perform
     * @return A configured JMenuItem
     */
    protected JMenuItem createMenuItem(String text, int mnemonic, KeyStroke accelerator, Action action) {
        JMenuItem item = new JMenuItem(new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    action.actionPerformed(e);
                } catch (Exception ex) {
                    log.error("Error executing menu action: {}", text, ex);
                }
            }
        });

        if (mnemonic != 0) {
            item.setMnemonic(mnemonic);
        }

        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }

        return item;
    }

    /**
     * Creates a toolbar button with action.
     * <p>
     * Subclasses can override this to customize button appearance (icons, etc.).
     *
     * @param text The button text
     * @param tooltip The tooltip text
     * @param action The action to perform
     * @return A configured JButton
     */
    protected JButton createToolbarButton(String text, String tooltip, Action action) {
        JButton button = new JButton(new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    action.actionPerformed(e);
                } catch (Exception ex) {
                    log.error("Error executing toolbar action: {}", text, ex);
                }
            }
        });

        button.setToolTipText(tooltip);
        button.setFocusable(false); // Standard toolbar button behavior

        return button;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Gets the desktop manager this UI is controlling.
     *
     * @return The desktop manager
     */
    public uiDesktopManager getDesktopManager() {
        return desktopManager;
    }

    /**
     * Refreshes any dynamic UI components (like window lists).
     * <p>
     * This can be called when the window list changes to update menus.
     * Currently this is a no-op since Zeus WindowManager handles updates automatically,
     * but subclasses might need custom refresh logic.
     */
    public void refresh() {
        // Zeus WindowManager automatically updates the window menu via listeners
        // If we had custom window list UI, we'd refresh it here
        log.trace("DefaultDesktopUI refresh called");
    }
}
