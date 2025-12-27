package org.jwellman.foundation.swing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author rwellman
 *
 */
abstract public class XComponent implements IComponent {

    private static final Logger log = LoggerFactory.getLogger(XComponent.class);

    protected JComponent wrapped;

    protected XComponent(JComponent j) {
        this.wrapped = j;
    }
    
    @Override
    public JComponent get() {
        return wrapped;
    }
    
    @Override
    public XComponent setFont(Font f) {
        wrapped.setFont(f);
        return this;
    }

    @Override
    public XComponent setText(String string) {
        if (wrapped instanceof JTextComponent) {
            final JTextComponent c = (JTextComponent) wrapped;
            c.setText(string);
        } else if (wrapped instanceof AbstractButton) {
            final AbstractButton c = (AbstractButton) wrapped;
            c.setText(string);            
        } else if (wrapped instanceof JLabel) {
            final JLabel c = (JLabel) wrapped;
            c.setText(string);            
        } else {
            log.warn("Wrapped JComponent does not implement setText(String)");
        }
        return this;
    }
    
    @Override
    public XComponent setAction(AbstractAction action) {
        if (wrapped instanceof AbstractButton) {
            final AbstractButton b = (AbstractButton) wrapped;
            b.setAction(action);            
        } else {
            log.warn("Wrapped JComponent does not implement setAction(AbstractAction)");
        }
        return this;
    }

    @Override
    public XComponent setSelected(boolean value) {
        if (wrapped instanceof AbstractButton) {
            final AbstractButton b = (AbstractButton) wrapped;
            b.setSelected(value);            
        } else {
            log.warn("Wrapped JComponent does not implement setSelected(boolean)");
        }
        return this;
    }
    
    @Override
    public XComponent addTo(ButtonGroup bg) {
        if (wrapped instanceof AbstractButton) {
            final AbstractButton b = (AbstractButton) wrapped;
            bg.add(b);            
        } else {
            log.warn("Wrapped JComponent cannot be added to ButtonGroup");
        }
        return this;
    }
    
    @Override
    public XComponent setForeground(Color color) {
        wrapped.setForeground(color);
        return this;
    }
    
    @Override
    public XComponent setBackground(Color color) {
        wrapped.setBackground(color);
        return this;
    }

}
