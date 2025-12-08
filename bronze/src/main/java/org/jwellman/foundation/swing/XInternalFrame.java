package org.jwellman.foundation.swing;

import java.awt.Component;
import java.beans.PropertyVetoException;

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
	public void close() {
		try {
			setClosed(true);
		} catch (PropertyVetoException e) {
			// If close is vetoed, log but don't throw - maintains IWindow contract
			System.err.println("WARN - Close operation was vetoed: " + e.getMessage());
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}

}
