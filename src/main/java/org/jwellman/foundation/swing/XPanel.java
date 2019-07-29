package org.jwellman.foundation.swing;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * A JPanel wrapper around the user-defined JPanel (the child)
 * that is also aware of its IWindow container (the parent).
 * 
 * 7/26/2019:  I will probably have to research the current use of
 * getChild() and setParent() as I have forgotten my original intent
 * for this wrapper class.  Its design does appear that it will have
 * usefulness/merit, but I simply have forgotten the specific use case
 * that inspired the development of this class.
 * 
 * 7/26/2019:  After review the initializeOtherWindows() method in the
 * Bronze.java class, it appears that this class facilitates having a
 * more concrete containment hierarchy for the purposes of the overall
 * desktop environment -- for instance, to possibly provide a user interface
 * (such as a JTree) that shows the desktop containment hierarchy and
 * further allows the user to take action on desktop apps/frames via
 * popup menus.
 * 
 * @author rwellman
 */
public class XPanel extends javax.swing.JPanel {

    private JPanel child;

    private IWindow parent;

    public XPanel(JPanel p) {
        this.setLayout(new BorderLayout());
        this.child = p;
        this.add(p, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aflag) {
        this.parent.setVisible(aflag);
    }

    public void setParent(IWindow i) {
        this.parent = i;
    }

    public JPanel getChild() {
        return this.child;
    }

}
