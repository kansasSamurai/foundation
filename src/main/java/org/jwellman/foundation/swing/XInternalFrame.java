package org.jwellman.foundation.swing;

import java.awt.Component;

/**
 *
 * @author rwellman
 */
@SuppressWarnings("serial")
public class XInternalFrame extends javax.swing.JInternalFrame implements IWindow {

    public XInternalFrame() {
        super();
    }

    public XInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
    }

	@Override
	public Component getComponent() {
		return this;
	}

}
