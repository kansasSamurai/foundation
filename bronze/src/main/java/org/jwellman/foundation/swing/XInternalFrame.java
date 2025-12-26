package org.jwellman.foundation.swing;

import java.awt.Component;
import java.beans.PropertyVetoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rwellman
 */
@SuppressWarnings("serial")
public class XInternalFrame extends javax.swing.JInternalFrame implements IWindow {

    private static final Logger log = LoggerFactory.getLogger(XInternalFrame.class);

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
			log.warn("Close operation was vetoed: {}", e.getMessage());
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}

}
