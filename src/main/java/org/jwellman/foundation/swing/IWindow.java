package org.jwellman.foundation.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

/**
 * An interface that abstracts the similarities between JFrame and JInternalFrame;
 * therefore, all methods are shared between JFrame and JInternalFrame unless otherwise noted.
 *
 * @author rwellman
 */
public interface IWindow {

	public void pack();

    public void setTitle(String title);

    public void setVisible(boolean aflag);

    public void setResizable(boolean resizable);

    public void setMaximizable(boolean maximable);

    public void setContentPane(Container c);

    /**
     * Add a component to this window's content pane.
     *
     * @param comp the component to add
     */
    public void add(Component comp);

    /**
     * Set the size of this window.
     *
     * @param width the width in pixels
     * @param height the height in pixels
     */
    public void setSize(int width, int height);

    /**
     * Set the bounds (position and size) of this window.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width in pixels
     * @param height the height in pixels
     */
    public void setBounds(int x, int y, int width, int height);

    /**
     * Set the location of this window.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setLocation(int x, int y);

    /**
     * Get the size of this window.
     *
     * @return the window size
     */
    public Dimension getSize();

    /**
     * Get the location of this window.
     *
     * @return the window location
     */
    public Point getLocation();

    /**
     * Close this window. For JFrame, this calls dispose().
     * For JInternalFrame, this calls setClosed(true).
     */
    public void close();

    /**
     * This method is required by the Foundation architecture.
     *
     * @return
     */
    public Component getComponent();

}
