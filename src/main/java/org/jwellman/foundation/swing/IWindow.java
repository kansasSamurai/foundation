package org.jwellman.foundation.swing;

import java.awt.Component;
import java.awt.Container;

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
     * This method is required by the Foundation architecture.
     * 
     * @return
     */
    public Component getComponent();

}
