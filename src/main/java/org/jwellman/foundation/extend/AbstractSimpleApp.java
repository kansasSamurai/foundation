package org.jwellman.foundation.extend;

import javax.swing.JPanel;
import org.jwellman.foundation.Foundation;
import org.jwellman.foundation.interfaces.uiThemeProvider;
import org.jwellman.foundation.swing.IWindow;

/**
 * A base class for bootstrapping simple applications
 * that consist of a single JPanel.
 *
 * @author Rick
 */
abstract public class AbstractSimpleApp implements uiThemeProvider {

    /**
     * A reference to your JPanel's container - for customization
     */
    protected IWindow window;

    /**
     * Your user interface - JPanel(s) only!
     */
    protected JPanel mainui;

    /**
     * Property - the title displayed in the title bar
     * 
     * Note that this method can be overridden.
     * 
     * @return
     */
    protected String getTitle() {
        return "Foundation Simple App";
    }

    @Override
    public void doTheme() {
        // TODO implement theme if necessary
    }

    /**
     * This is the entry point for your custom subclass;
     * usually called from public static main(String[] args):<pre>
     *  public static void main(String... args) {
     *    new YourApp().startup(false, args);
     *  }</pre>
     *
     * @param asMainFrame If true, your panel is displayed in a JFrame. Otherwise, displayed in a JInternalFrame.
     * @param args command line args, not used in this example but shown for illustration purposes.
     */
    protected void startup(boolean asMainFrame, String[] args) {

        // [Note 1]
        // The 'asMainFrame' parameter would be used to specify whether you
        // want this application to be displayed as a "main frame" i.e. as a java JFrame.

        // Prepare - User Interface Context
        // int uiunit = 65;
        // final uContext context = uContext.createContext();
        // context.setTheme(this);
        // context.setDimension(uiunit * 5, uiunit * 9);

        // Step 1 - Initialize Swing; if you want a non-null uContext, an example is in the comments above.
        final Foundation f = Foundation.init(Foundation.APPTYPE.STANDALONE, null);

        // Step 2 - Create your UIs in JPanel(s)
        mainui = f.registerUI("org.jwellman.foundation.examples.AbstractSimpleApp", this.getMainUI());

        // Step 3 - Use Foundation to create your "window"; give it your UI.
        window = asMainFrame ? f.useWindow(mainui) : f.useDesktop(mainui);
        window.setTitle(this.getTitle()); // Step 3a (optional) - Customize your window
        window.setResizable(true); // Step 3a (optional) - Customize your window
        window.setMaximizable(true); // Step 3a (optional) - Customize your window

        // Step 4a - Create data models, controllers, and other non-UI objects
        // n/a
        // Step 4b (optional)- Associate models with views
        // n/a

        // Step 5 - Display your User Interface
        f.showGUI(window);
    }

    /**
     * Your application/subclass is expected to override this method
     * and return a JPanel that contains your application user interface.
     * 
     * @return
     */
    protected abstract JPanel getMainUI();

}
