package org.jwellman.foundation;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JDesktopPane;
import org.jwellman.foundation.swing.IWindow;

/**
 * Defines window positioning strategies for panels in Foundation framework.
 *
 * This class provides both predefined positioning strategies (CASCADE, TILE, CENTER)
 * and support for explicit positioning via coordinates.
 *
 * Positioning applies differently based on mode:
 * - Window mode (JFrame): CENTER is default, EXPLICIT sets frame position on screen
 * - Desktop mode (JInternalFrame): CASCADE is default, TILE arranges in grid, EXPLICIT sets position within desktop
 *
 * @author Foundation Framework
 */
public class WindowPosition {

    /**
     * Positioning strategy enumeration.
     */
    public enum Strategy {
        /**
         * Cascade windows diagonally (each window offset from previous).
         * Default for desktop mode. Not applicable in window mode.
         */
        CASCADE,

        /**
         * Tile windows in a grid pattern to fill available space.
         * Applicable only in desktop mode.
         */
        TILE,

        /**
         * Center the window.
         * In window mode: center JFrame on screen
         * In desktop mode: center JInternalFrame on desktop
         */
        CENTER,

        /**
         * Use explicit coordinates provided via setPosition().
         * In window mode: position JFrame on screen
         * In desktop mode: position JInternalFrame within desktop
         */
        EXPLICIT,

        /**
         * No automatic positioning - use component defaults.
         */
        NONE
    }

    private Strategy strategy;
    private Point position;      // For EXPLICIT positioning
    private Dimension size;      // Optional explicit size

    // Cascade state tracking (shared across all CASCADE positions)
    private static int cascadeOffset = 10;
    private static final int CASCADE_INCREMENT = 25;

    /**
     * Creates a WindowPosition with the specified strategy.
     *
     * @param strategy The positioning strategy
     */
    public WindowPosition(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Creates a WindowPosition with explicit coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public WindowPosition(int x, int y) {
        this.strategy = Strategy.EXPLICIT;
        this.position = new Point(x, y);
    }

    /**
     * Creates a WindowPosition with explicit coordinates and size.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Window width
     * @param height Window height
     */
    public WindowPosition(int x, int y, int width, int height) {
        this.strategy = Strategy.EXPLICIT;
        this.position = new Point(x, y);
        this.size = new Dimension(width, height);
    }

    /**
     * Factory method: Create CASCADE positioning.
     */
    public static WindowPosition cascade() {
        return new WindowPosition(Strategy.CASCADE);
    }

    /**
     * Factory method: Create TILE positioning.
     */
    public static WindowPosition tile() {
        return new WindowPosition(Strategy.TILE);
    }

    /**
     * Factory method: Create CENTER positioning.
     */
    public static WindowPosition center() {
        return new WindowPosition(Strategy.CENTER);
    }

    /**
     * Factory method: Create EXPLICIT positioning.
     */
    public static WindowPosition at(int x, int y) {
        return new WindowPosition(x, y);
    }

    /**
     * Factory method: Create EXPLICIT positioning with size.
     */
    public static WindowPosition at(int x, int y, int width, int height) {
        return new WindowPosition(x, y, width, height);
    }

    /**
     * Apply this positioning to a window.
     *
     * @param window The window to position
     * @param desktop The desktop pane (null if window mode)
     */
    public void apply(IWindow window, JDesktopPane desktop) {
        if (window == null) return;

        switch (strategy) {
            case CASCADE:
                if (desktop != null) {
                    applyCascade(window, desktop);
                }
                break;

            case TILE:
                if (desktop != null) {
                    // TILE requires knowledge of all windows, so it's handled externally
                    // This method just marks the intent; actual tiling happens in Bronze
                    // For now, fall back to cascade
                    applyCascade(window, desktop);
                }
                break;

            case CENTER:
                applyCenter(window, desktop);
                break;

            case EXPLICIT:
                applyExplicit(window);
                break;

            case NONE:
            default:
                // No positioning
                break;
        }
    }

    private void applyCascade(IWindow window, JDesktopPane desktop) {
        int x = cascadeOffset;
        int y = cascadeOffset;
        cascadeOffset += CASCADE_INCREMENT;

        // Reset cascade if we've gone too far
        if (cascadeOffset > 200) {
            cascadeOffset = 10;
        }

        window.setLocation(x, y);
    }

    private void applyCenter(IWindow window, JDesktopPane desktop) {
        if (desktop != null) {
            // Center within desktop
            Dimension desktopSize = desktop.getSize();
            Dimension windowSize = window.getSize();
            int x = (desktopSize.width - windowSize.width) / 2;
            int y = (desktopSize.height - windowSize.height) / 2;
            window.setLocation(Math.max(0, x), Math.max(0, y));
        } else {
            // Window mode - setLocationRelativeTo(null) centers on screen
            // This is handled elsewhere, so we don't need to do anything here
        }
    }

    private void applyExplicit(IWindow window) {
        if (position != null) {
            window.setLocation(position.x, position.y);
        }
        if (size != null) {
            window.setSize(size.width, size.height);
        }
    }

    /**
     * Reset the cascade offset counter. Useful when starting a new layout.
     */
    public static void resetCascade() {
        cascadeOffset = 0;
    }

    // Getters and setters

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
        this.strategy = Strategy.EXPLICIT;
    }

    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
        this.strategy = Strategy.EXPLICIT;
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public void setSize(int width, int height) {
        this.size = new Dimension(width, height);
    }

}
