package org.jwellman.foundation;

import java.awt.Dimension;

import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.interfaces.uiThemeProvider;

/**
 * A context for a Foundation application.
 * 
 * @author rwellman
 */
public class uContext {

    public static uContext createContext() {
        return new uContext();
    }

    private uContext() {
        // Empty/private constructor to enforce factory pattern
    }

    /** An indicator that you are using desktop mode; defaults to false. */
    private boolean desktopMode = false;

    /** A title for the desktop frame */
    private String desktopTitle;

    /** The look and feel class to use (will be ignored in a "desktop" environment) */
    private String lookAndFeel;

    /** An object that implements the themeProvider interface */
    private uiThemeProvider themeProvider;

    /** An object that implements the desktopProvider interface */
    private uiDesktopProvider desktopProvider;

    /** A dimension object for the window - I removed the default value on 10/17/2022 new Dimension(450,250) */
    private Dimension dimension;

    /** Get the desktop mode. */
    public boolean isDesktopMode() {
        return desktopMode;
    }

    /** Sets the desktop mode; this can only be done once. */
    public void setDesktopMode(boolean mode) {
        this.desktopMode = mode;
	}

    public void setThemeProvider(uiThemeProvider x) {
        themeProvider = x;
    }

    public uiThemeProvider getThemeProvider() {
        return themeProvider;
    }

    public void setDesktopProvider(uiDesktopProvider x) {
        desktopProvider = x;
    }

    public uiDesktopProvider getDesktopProvider() {
        return desktopProvider;
    }

    public void setDesktopTitle(String strTitle) {
        this.desktopTitle = strTitle;
    }

    public String getDesktopTitle() {
        return desktopTitle;
    }

    /**
     * Set the dimension for the main application window.
     * This is a convenience method for a 9wide x 5high window.
     * 
     * @param base
     */
    public void setDimension(int base) {
        this.setDimension(base, 9, 5);
    }

    /**
     * Set the dimension for the main application window.
     * This method is a convenience method for when you want
     * to specify the display ratio and control its overall 
     * size by adjusting the base unit.<br/>
     * i.e. 9w x 5h, base 10 = 90w x 50h
     * 
     * @param base
     * @param w The width (in "base" units)
     * @param h The height (in "base" units)
     */
    public void setDimension(int base, int w, int h) {
        this.setDimension(base * w, base * h);
    }

    /**
     * Set the dimension for the main application window.
     * This method uses pixels for unit size.
     * 
     * @param w The width in pixels
     * @param h The height in pixels
     */
    public void setDimension(int w, int h) {
        dimension = new Dimension(w, h);
    }

    /**
     * Set the dimension for the main application window.
     * All setDimension() methods lead here.
     * 
     * @param dim
     */
    public void setDimension(Dimension dim) {
        dimension = dim;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public String getLookAndFeel() {
        return lookAndFeel;
    }

    /**
     * Sets the Look and Feel for this application.
     * This may be ignored if the application is running in a
     * multi-application environment such as jpad.
     * 
     * @param laf
     */
    public void setLookAndFeel(String laf) {
        this.lookAndFeel = laf;
    }

}
