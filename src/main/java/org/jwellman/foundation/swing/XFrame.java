package org.jwellman.foundation.swing;

import java.awt.Component;

/**
 *
 * @author rwellman
 */
@SuppressWarnings("serial")
public class XFrame extends javax.swing.JFrame implements IWindow {

    public XFrame(String title) {
        super(title);
    }

	@Override
	public void setMaximizable(boolean maximable) {
		// This is a no-op for JFrames since they can always be maximized.		
	}

	@Override
	public Component getComponent() {
		return this;
	}

}
