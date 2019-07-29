package org.jwellman.foundation.interfaces;

import javax.swing.JDesktopPane;

import org.jwellman.foundation.swing.IWindow;

/**
* An interface for providing custom desktop components.
* 
* @author rwellman
*/
public interface uiDesktopProvider {

	public JDesktopPane doCustomDesktop(IWindow w);
	
}
