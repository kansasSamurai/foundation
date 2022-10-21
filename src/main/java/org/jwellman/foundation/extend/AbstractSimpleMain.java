package org.jwellman.foundation.extend;

import javax.swing.JPanel;

import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XPanel;

/**
 * A base class providing common variables needed for basic Foundation app.
 * 
 * @author rwellman
 *
 */
abstract public class AbstractSimpleMain {

    /**
     * A reference to the Foundation framework
     */
    protected Foundation framework;

    /**
     * A reference to your JPanel's container - for customization
     */
    protected IWindow window;

    /**
     * Your user interface - JPanel(s) only!
     */
    protected XPanel mainui;

    /**
     * The canonical example of starting up a Foundation application
     * with a single panel/window user interface.
     * <p>
     * You must precede a call to this method with the following
     * initialization somewhere in your code:<code>
     * this.framework = Foundation.init();</code>
     * 
     * @param title
     * @param yourui a JPanel representing your simple user interface
     */
    public void simpleStartup(String title, JPanel yourui) {

        // Step 1 - Initialize Swing
        this.framework = Foundation.init();

        // Step 2 - Create your UIs in JPanel(s)
        mainui = framework.registerUI("viewer", yourui);

        // Step 3 - Use Foundation to create your "window"; give it your UI.
        window = framework.useWindow(mainui);
        // Step 3a (optional) - Customize your window
        window.setTitle(title + Foundation.POWEREDBY); 
        window.setResizable(true);
        window.setMaximizable(true);

        // Step 4 - Create data models, controllers, and other non-UI objects
        // n/a

        // Step 4a (optional)- Associate models with views
        // n/a

        // Step 5 - Display your main UI Frame
        framework.showGUI(window);

        // Step 5a (optional) - Display any secondary user interfaces frames
        // n/a

    }

}
