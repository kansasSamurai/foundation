package org.jwellman.foundation;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.sourceforge.napkinlaf.NapkinLookAndFeel;
import net.sourceforge.napkinlaf.NapkinTheme;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;

/**
 * The most basic of Swing initialization requirements.
 * 
 * Stone only supports a single application; 
 * in either single frame or desktop mode.
 *
 * @author rwellman
 * 
 */
public class Stone {

	/** The user's entry point UI in a JPanel */
	// protected JPanel panel;
	
	/** A user interface context object */
	private uContext context;
	
	/** Indicates desktop mode */
	protected boolean isDesktop;
	
	/** Guards the init() method */
	protected boolean isInitialized;
	
	/** Guards the useWindow() and useDesktop() methods so the first call "wins"; subsequent calls log a warning */
	//protected boolean isAppModeChosen;
	
	/** The "controlling" JFrame; used in both modes */
	protected XFrame externalFrame;
	
	/** The "main" internal frame used in desktop mode */
	// protected XInternalFrame internalFrame;
	
	/** The JDesktopPane used in desktop mode */
	private JDesktopPane desktop;




/**
 * This is the workhorse of initializing the graphical "environment" in Swing;
 * note that it mainly consists of initializing the Look and Feel.
 * 
 * Note also that "initializing the environment" does not consist of 
 * creating ANY actual Swing components -- that will come later
 * as you build your user interface.  i.e. this method does the
 * bootstrapping for you -- you get to focus on building the UI.
 * 
 * Finally, note that it is enforced that the initialization occurs only
 * once -- any subsequent calls will log a warning but not actually do anything else. 
 * 
 * @param c the micro context
 */
protected final void _init(uContext c) {

    if (isInitialized) {
    	System.out.print("WARN - init() has been called more than once...");
    	System.out.print("WARN - ... in a single app use case, this usually indicates a misuse of the API");
    	System.out.print("WARN - ... which may often result in unexpected/undesired behavior.");
    } else {
        isInitialized = true;

        // Log the application classpath for debugging purposes
        System.out.println("----- Application Classpath -----");
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        final URL[] urls = ((URLClassLoader)cl).getURLs();
        for (URL url: urls){
            System.out.println(url.getFile());
        }

        // Apply anti-aliasing for better rendering (particulary fonts)
        // The following may have some subtle system dependent behavior:
        // http://stackoverflow.com/questions/179955/how-do-you-enable-anti-aliasing-in-arbitrary-java-apps
        // Try System.setProperty("awt.useSystemAAFontSettings", "lcd"); and you should get ClearType
        // as of 7/5/2019, the combination of "on"/"true" yielded inconsistent results with different
        // look and feels (i.e. Nimbus vs. Napkin... napking actually looked better which surprised me)
        // so, now trying off/false: 
        System.setProperty("awt.useSystemAAFontSettings","off");
        System.setProperty("swing.aatext", "false");
        System.out.println("Anti-alias settings:  off/false");

        // Make sure our window decorations come from the look and feel.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Save the context (or create one by default)
        context = (c != null) ? c : uContext.createContext();

        // Conditionally apply context settings...
        if (context.getThemeProvider() != null) { 
        	context.getThemeProvider().doTheme(); 
        }

        // Prefer Nimbus over default look and feel.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                final String name = info.getName(); System.out.println(name);
                if ("Nimbus".equals(name)) {
                    final int version = 1;
                    switch (version) {
                        case 1:
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        case 2:
                            UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel"); // works but need to upgrade to 1.29 from 1.27
                            break;
                        case 3:
                            String[] themeNames = NapkinTheme.Manager.themeNames();
                            String themeToUse = "blueprint"; // napkin | blueprint
                            NapkinTheme.Manager.setCurrentTheme(themeToUse);
                            LookAndFeel laf = new NapkinLookAndFeel();
                            UIManager.setLookAndFeel(laf);
                            break;
                        case 4:
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                            break;
                    }

                    break;
                }
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        } finally {
            System.out.println(UIManager.getLookAndFeel().getName());
        }

    }
    
	} // end method

	/**
	 * Given an instance of JPanel, return an IWindow object compatible
	 * with a "desktop" user experience (i.e. a JInternalFrame).
	 * 
	 * @param ui
	 * @return
	 */
	public IWindow useDesktop(JPanel ui) {
		if (ui == null) {
			throw new RuntimeException("FATAL - JPanel cannot be null");
		}
		
        //panel = ui; // Store a reference to the JPanel
        // TODO may have to deprecate/remove the "panel" reference
        // because there may eventually be many panels

	    final XInternalFrame internalFrame = new XInternalFrame("Your UI", true, true, true, true);
        // TODO figure out the best way to size the internal frame (probably the context); this is just a stopgap 
        internalFrame.setBounds(10, 10, 225, 125);
        // internalFrame.setVisible(true); // [B]
        internalFrame.add(ui);
        // These are the logical default values; maximizable can be later altered... 
        // TODO technically, closable can be altered too but in a single app context,
        // it should probably be prevented somehow. 
        internalFrame.setMaximizable(false);
        internalFrame.setClosable(false);
        
        // Adding this to a desktop has been moved to showGUI()
        // desktop.add(internalFrame); // this does NOT make the internal frame visible

        this.initializeOtherWindows();

	    return internalFrame; // frame;	
	} // end method

	/**
	 * Given an instance of JPanel, return an IWindow object compatible
	 * with a window-based user experience (i.e. a JFrame).
	 * 
	 * @param ui
	 * @return
	 */
	public IWindow useWindow(JPanel ui) {
		if (ui == null) {
			throw new RuntimeException("FATAL - JPanel cannot be null");
		}
	
	    // Create the JFrame
	    final XFrame ajframe = new XFrame("Your App -- powered by the Foundation API");
	    if (context.isDesktopMode()) {
	    } else {
		    ajframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    }
	    // frame.setSize(450, 250); // [D]
	    ajframe.add(ui);

	    return ajframe;		
	}

	/**
	 * 
	 * @param main
	 */
	public void registerDesktopWindow(IWindow main) {
		if (main instanceof XFrame) {
			externalFrame = (XFrame) main;			
		} else {
			throw new RuntimeException("Invalid window registered as desktop; must be an instance of XFrame");
		}
	}
	
	/**
	 * A convenience method for calling showGUI() when you 
	 * only have one IWindow instance.
	 * 
	 * @param window
	 */
	public void showGUI(IWindow window) {
	
		final List<IWindow> list = new ArrayList<>();
		if (window != externalFrame) {
			list.add(window);			
		}
		
		this.showGUI(list);
	}

	/**
	 * This is where your application is finally "visible" to the user.
	 * 
	 * The logic of this method follows two entry conditions:
	 * 1) Has this been called already? which should only be valid if
	 *    you are purposely running in a desktop mode.
	 *    
	 * 2) Has this **not** been called already? which should only be valid
	 *    if your are either just running a standalone application **or**
	 *    if you are creating a generic desktop to host multiple applications
	 *    (such as jPAD).
	 *    
	 * - If the desktop mode has been chosen, create the external frame (JFrame)
	 * - If the window mode has been chosen, create the external frame (JFrame)
	 * 
	 * 
	 */
	public void showGUI(final List<IWindow> windows) {
	
		if (!isInitialized) {
			throw new RuntimeException("Cannot call showGUI() until either useWindow() or useDesktop() is called.");
		}
	
        // Create the JFrame
		if (externalFrame == null) {
			// We have not registered a desktop/main so create one
	        externalFrame = new XFrame(context.getDesktopTitle());	        
	        externalFrame.setSize(context.getDimension());			
		} else {
			// We have registered a desktop so use it
			
		}
        
    // Start the GUI on the Event Dispatch Thread (EDT)
    javax.swing.SwingUtilities.invokeLater(
        new Runnable() { @Override public void run() {

        	if (context.isDesktopMode()) {
        		if (context.getDesktopProvider() == null) {
        	        desktop = new JDesktopPane(); // a specialized layered pane
        	        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE); // Make dragging a little faster but perhaps uglier.        			
        	        externalFrame.setContentPane(desktop);
        		} else {
        			desktop = context.getDesktopProvider().doCustomDesktop(externalFrame);
        		}

                for (IWindow w : windows) {
                	if (w != externalFrame) {
                	    desktop.add(w.getComponent());
                	}
                }

        	}
        	
	        // TODO The jPAD security manager doesn't like this line
	        // but other apps without jpad might... review this design
	        // externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        	        
            // Display the window.
            // externalFrame.pack(); // [A] [E]
            externalFrame.setLocationRelativeTo(null); // [C]
            externalFrame.setVisible(true);

        } } // end runnable / end run()
    ); // end invokeLater()

	    /* All the other windows have been added to the desktop
	     * but they have not been made visible; make them visible now.
	     * 
	     * For possible performance reasons, open each subwindow in a new EDT
	     */
	    for (final IWindow w : windows) {
	    	
	        // Start the GUI on the Event Dispatch Thread (EDT)
	        javax.swing.SwingUtilities.invokeLater(
	            new Runnable() { @Override public void run() {
	                w.pack();
	                w.setVisible(true);            	
	            }} );
	    }
	
	} // end method

	public JDesktopPane getDesktop() {
	    return desktop;
	}

/* ========== Footnotes =====================================================
[A] The swing documentation says that pack() makes the frame "displayable"
    I originally thought that "displayable" meant "visible" but it doesn't 
    (or at least it doesn't work that way).  
    A:  "displayable" means that the component,
        or its root container, has a native-peer.
[B] We intentionally defer showing the internal frame until the user/designer
    calls showGUI() from his code.
[C] Centers the frame/window on the native desktop.
[D] No need to set the size on the JFrame; showGUI() will size it via
    the pack() method.  For single window applications this is by far the
    norm.  However, I probably need to eventually account for the developer
    who wants to setSize() instead of pack()... this option would best be
    implemented in the context.
[E] TODO In desktop mode, we do not want to pack the external JFrame.

/* ========================================================================== */

    /**
     * This is basically a noop in Stone since it only supports a single
     * application window.  Other levels will definitely override this.
     */
    protected void initializeOtherWindows() {}

} // end class
