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
    private Boolean desktopMode = null;
    
    /** An indicator that you are using desktop mode; defaults to false. */
	public boolean isDesktopMode() {
		return desktopMode == null ? false : desktopMode;
	}

	/** Sets the desktop mode; this can only be done once. */
	public void setDesktopMode(boolean mode) {
		if (desktopMode == null) {
			this.desktopMode = mode;			
		}
	}

    /** An object that implements the themeProvider interface */
    private uiThemeProvider themeProvider;

    public void setThemeProvider(uiThemeProvider x) { themeProvider = x; }

    public uiThemeProvider getThemeProvider() { return themeProvider; }

    /** An object that implements the desktopProvider interface */
    private uiDesktopProvider desktopProvider;
    
	public void setDesktopProvider(uiDesktopProvider x) { desktopProvider = x; }
	
	public uiDesktopProvider getDesktopProvider() { return desktopProvider; }

    /** A title for the desktop frame */
    private String desktopTitle;
    
	public void setDesktopTitle(String strTitle) { this.desktopTitle = strTitle; }
	
	public String getDesktopTitle() { return desktopTitle; }

    /** A dimension object for the window (w/ default value) */
    private Dimension dimension = new Dimension(450,250);

    public void setDimension(Dimension x) { dimension = x; }

    public void setDimension(int w, int h) { dimension = new Dimension(w,h); }

    public void setDimension(int base) { this.setDimension(base, 9, 5); }
    
    public void setDimension(int base, int w, int h) { this.setDimension(base * w, base * h); }

    public Dimension getDimension() { return dimension; }

    /** The look and feel class to use (will be ignored in a "desktop" environment) */
    public String lookAndFeel;
    
	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(String laf) {
		this.lookAndFeel = laf;
	}

}
