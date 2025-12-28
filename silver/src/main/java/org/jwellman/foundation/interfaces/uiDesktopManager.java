package org.jwellman.foundation.interfaces;

import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;

import org.jwellman.foundation.model.FrameDescriptor;

/**
 * Manager interface for desktop-wide operations on JInternalFrames.
 * <p>
 * While {@link javax.swing.DesktopManager} handles low-level operations on individual frames
 * (drag, resize, iconify, etc.), uiDesktopManager provides high-level operations that
 * affect multiple frames collectively (cascade all, tile all, etc.) and integrates with
 * Foundation's FrameDescriptor model.
 * <p>
 * Design Philosophy:
 * <ul>
 * <li>Desktop-wide orchestration (not single-frame events)</li>
 * <li>Foundation-aware (works with FrameDescriptor, not raw JInternalFrames)</li>
 * <li>Provides model for UI implementations (toolbars, menus, keyboard shortcuts)</li>
 * </ul>
 * <p>
 * Typical implementation would be provided by a {@link uiDesktopProvider} and attached
 * to a specific JDesktopPane instance.
 *
 * @author Foundation Framework
 * @see uiDesktopProvider
 * @see javax.swing.DesktopManager
 */
public interface uiDesktopManager {

    // ========================================================================
    // FRAME ARRANGEMENT OPERATIONS
    // ========================================================================

    /**
     * Arranges all visible frames in a diagonal cascade pattern.
     * <p>
     * Frames are positioned with offset increments, creating a staggered
     * diagonal layout from top-left. Minimized frames may be de-iconified
     * based on policy settings.
     */
    void cascadeFrames();

    /**
     * Tiles all visible frames in a grid pattern.
     * <p>
     * Automatically determines optimal grid layout (rows/columns) to
     * distribute frames evenly across the desktop.
     */
    void tileFrames();

    /**
     * Tiles all visible frames in horizontal rows.
     * <p>
     * Each frame spans the full desktop width, divided equally by height.
     */
    void tileFramesHorizontal();

    /**
     * Tiles all visible frames in vertical columns.
     * <p>
     * Each frame spans the full desktop height, divided equally by width.
     */
    void tileFramesVertical();

    // ========================================================================
    // FRAME STATE OPERATIONS (BATCH)
    // ========================================================================

    /**
     * Minimizes (iconifies) all visible frames.
     */
    void minimizeAll();

    /**
     * Restores all frames from minimized or maximized state to normal state.
     */
    void restoreAll();

    /**
     * Maximizes all visible frames.
     * <p>
     * Note: Less common in practice as all frames would overlap.
     */
    void maximizeAll();

    /**
     * Resets all frames to their preferred size (calls pack()).
     * <p>
     * Useful for resetting frames that have been manually resized.
     */
    void resetAll();

    /**
     * Closes all closable frames.
     * <p>
     * Respects each frame's closable property and close policy.
     */
    void closeAll();

    // ========================================================================
    // FRAME STATE OPERATIONS (INDIVIDUAL)
    // ========================================================================

    /**
     * Minimizes (iconifies) the currently selected frame.
     */
    void minimize();

    /**
     * Restores the currently selected frame from minimized or maximized state.
     */
    void restore();

    /**
     * Maximizes the currently selected frame.
     */
    void maximize();

    /**
     * Resets the currently selected frame to its preferred size (calls pack()).
     */
    void reset();

    /**
     * Closes the currently selected frame if closable.
     */
    void close();

    // ========================================================================
    // FRAME NAVIGATION
    // ========================================================================

    /**
     * Selects the next frame in sequence.
     * <p>
     * Cycles through visible, non-minimized frames. Typical keyboard
     * shortcut: Ctrl+Tab or Ctrl+F6.
     */
    void selectNext();

    /**
     * Selects the previous frame in sequence.
     * <p>
     * Cycles backwards through visible, non-minimized frames. Typical
     * keyboard shortcut: Ctrl+Shift+Tab or Ctrl+Shift+F6.
     */
    void selectPrevious();

    /**
     * Selects a specific frame by its panel ID.
     *
     * @param namespace The namespace identifier
     * @param panelId The panel identifier within the namespace
     */
    void selectFrame(String namespace, String panelId);

    /**
     * Brings a frame to the front of its layer.
     *
     * @param namespace The namespace identifier
     * @param panelId The panel identifier within the namespace
     */
    void bringToFront(String namespace, String panelId);

    /**
     * Sends a frame to the back of its layer.
     *
     * @param namespace The namespace identifier
     * @param panelId The panel identifier within the namespace
     */
    void sendToBack(String namespace, String panelId);

    // ========================================================================
    // QUERY METHODS (Desktop State)
    // ========================================================================

    /**
     * Gets all frame descriptors currently in the desktop.
     * <p>
     * This includes hidden frames (HIDE_ON_CLOSE) but not closed frames
     * (DISPOSE_ON_CLOSE).
     *
     * @return List of all FrameDescriptors (may be empty, never null)
     */
    List<FrameDescriptor> getAllFrames();

    /**
     * Gets all visible frame descriptors.
     *
     * @return List of visible FrameDescriptors (may be empty, never null)
     */
    List<FrameDescriptor> getVisibleFrames();

    /**
     * Gets all minimized (iconified) frame descriptors.
     *
     * @return List of minimized FrameDescriptors (may be empty, never null)
     */
    List<FrameDescriptor> getMinimizedFrames();

    /**
     * Gets the currently selected/active frame descriptor.
     *
     * @return The active FrameDescriptor, or null if none selected
     */
    FrameDescriptor getActiveFrame();

    /**
     * Gets the total count of all frames in the desktop.
     * <p>
     * Includes hidden frames. Useful for detecting "ghost" frames.
     *
     * @return Frame count
     */
    int getFrameCount();

    /**
     * Gets the count of visible frames only.
     *
     * @return Visible frame count
     */
    int getVisibleFrameCount();

    // ========================================================================
    // DESKTOP-LEVEL UI COMPONENTS
    // ========================================================================

    /**
     * Populates a menu with items representing all open frames.
     * <p>
     * Typically creates radio button menu items for each frame, allowing
     * users to select frames from a menu. Updates automatically as frames
     * are added/removed.
     * <p>
     * Implementation note: Menu is typically passed during manager construction
     * or initialization, then maintained automatically via listeners.
     *
     * @param windowMenu The menu to populate
     */
    void populateWindowMenu(JMenu windowMenu);

    // ========================================================================
    // POLICY CONFIGURATION
    // ========================================================================

    /**
     * Sets the de-iconify policy for cascade operations.
     * <p>
     * If true, minimized frames are de-iconified during cascade. If false,
     * they remain minimized and are skipped.
     *
     * @param deiconify true to force de-iconify during cascade, false to skip minimized frames
     */
    void setDeiconifyPolicy(boolean deiconify);

    /**
     * Gets the de-iconify policy.
     *
     * @return true if minimized frames are de-iconified during cascade
     */
    boolean getDeiconifyPolicy();

    /**
     * Sets the close policy.
     * <p>
     * If true, frames are force-closed (setClosed(true)). If false, frames
     * perform their default close action (may prompt user, etc.).
     *
     * @param forceClose true to force close, false to use default close action
     */
    void setClosePolicy(boolean forceClose);

    /**
     * Gets the close policy.
     *
     * @return true if frames are force-closed
     */
    boolean getClosePolicy();

    /**
     * Sets the auto-position policy for new frames.
     * <p>
     * If true, newly added frames are automatically positioned using
     * cascade pattern. If false, frames keep their original position.
     *
     * @param autoPosition true to auto-position new frames
     */
    void setAutoPositionPolicy(boolean autoPosition);

    /**
     * Gets the auto-position policy.
     *
     * @return true if new frames are auto-positioned
     */
    boolean getAutoPositionPolicy();

    // ========================================================================
    // INFRASTRUCTURE
    // ========================================================================

    /**
     * Gets the JDesktopPane this manager is attached to.
     *
     * @return The desktop pane
     */
    JDesktopPane getDesktop();

}
