package org.jwellman.foundation;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;

import net.sourceforge.napkinlaf.NapkinLookAndFeel;
import net.sourceforge.napkinlaf.NapkinTheme;

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

	/** Indicates desktop mode; null until first useWindow() or useDesktop() call */
	protected Boolean isDesktop;

	/** Guards the init() method */
	protected boolean isInitialized;

	/** The "controlling" JFrame; used in both modes */
	protected XFrame externalFrame;

	/** The "main" internal frame used in desktop mode */
	// protected XInternalFrame internalFrame;

	/** The JDesktopPane used in desktop mode */
	private JDesktopPane desktop;

	// Look and Feel (LAF) identifiers - DEPRECATED
	// These constants are deprecated in favor of using LAF class names directly.
	// Instead of: context.setLookAndFeel(LAF_NIMBUS)
	// Use: context.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
	// or configure via ./lafs/foundation.properties
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_NIMBUS = 1;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_WEB = 2;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_NAPKIN = 3;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_SYSTEM = 4;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_NIMROD = 5;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_JTATTOO = 6;
	/**
	 * @deprecated Use LAFDiscovery and set LAF via class name in uContext.setLookAndFeel()
	 */
	@Deprecated
	public static final int LAF_DARCULA = 7;



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
//            final ClassLoader cl = ClassLoader.getSystemClassLoader();
//            final URL[] urls = ((URLClassLoader) cl).getURLs();
//            for (URL url : urls) {
//                System.out.println(url.getFile());
//            }
            // Works in all Java versions
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(File.pathSeparator);
            for (String entry : classpathEntries) {
                System.out.println(entry);
            }

            // Log the system fonts available for debugging purposes
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final Font[] fonts = ge.getAllFonts();
            for (Font font : fonts) {
                System.out.print("FONT: " + font.getFontName() + " : ");
                System.out.println(font.getFamily());
            }

            @SuppressWarnings("unused")
            Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit()
                    .getDesktopProperty("awt.font.desktophints");

            // Apply anti-aliasing for better rendering (particulary fonts)
            // The following may have some subtle system dependent behavior:
            // http://stackoverflow.com/questions/179955/how-do-you-enable-anti-aliasing-in-arbitrary-java-apps
            // Try System.setProperty("awt.useSystemAAFontSettings", "lcd"); and you should
            // get ClearType
            // as of 7/5/2019, the combination of "on"/"true" yielded inconsistent results
            // with different
            // look and feels (i.e. Nimbus vs. Napkin... napking actually looked better
            // which surprised me)
            // so, now trying off/false:
            boolean aasettings = false;
            if (aasettings) {
                System.setProperty("awt.useSystemAAFontSettings", "off");
                System.setProperty("swing.aatext", "false");
                System.out.println("Anti-alias settings:  off/false");
            }

            // Make sure our window decorations come from the look and feel.
            JFrame.setDefaultLookAndFeelDecorated(true);

            // Save the context (or create one by default)
            context = (c != null) ? c : uContext.createContext();

            // Conditionally apply context settings...
            if (context.getThemeProvider() != null) {
                context.getThemeProvider().doTheme();
            }

            // Use LAFDiscovery to select and apply Look and Feel
            // Priority: uContext.lookAndFeel -> config file -> ./lafs/ directory -> system default
            boolean lafApplied = LAFDiscovery.selectAndApplyLookAndFeel(context.getLookAndFeel());

            if (!lafApplied) {
                System.err.println("WARNING: Failed to apply any Look and Feel. UI may not render correctly.");
            } else {
                System.out.println("USING LAF: " + UIManager.getLookAndFeel().getName());
            }

    }

    } // end method
    
    /**
     * Creates a window for the given JPanel based on the current mode (window or desktop).
     * The mode is determined by the uContext provided during init().
     * Convention: If no uContext provided, defaults to window mode (standalone).
     *
     * @param ui The JPanel to display
     * @return IWindow abstraction (JFrame or JInternalFrame depending on mode)
     */
    public IWindow createWindow(JPanel ui) {
        if (ui == null) {
            throw new RuntimeException("FATAL - JPanel cannot be null");
        }

        // Determine mode if not already set
        // Convention over configuration: default to window mode if not specified
        if (isDesktop == null) {
            isDesktop = context.isDesktopMode();
        }

        if (isDesktop) {
            // Create internal frame for desktop mode
            final XInternalFrame internalFrame = new XInternalFrame("Your UI", true, true, true, true);
            internalFrame.setBounds(10, 10, 225, 125);
            internalFrame.add(ui);
            internalFrame.setMaximizable(false);
            internalFrame.setClosable(false);

            this.initializeOtherWindows();

            return internalFrame;
        } else {
            // Create JFrame for window mode
            if (externalFrame == null) {
                externalFrame = new XFrame("Your App -- powered by the Foundation API");

                if (context.getDesktopTitle() != null)
                    externalFrame.setTitle(context.getDesktopTitle());

                externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }

            externalFrame.add(ui);
            return externalFrame;
        }
    }

    /**
     * Given an instance of JPanel, return an IWindow object compatible with a
     * "desktop" user experience (i.e. a JInternalFrame).
     *
     * @deprecated Use createWindow(JPanel) instead. Mode is now determined by uContext.
     * @param ui
     * @return
     */
    @Deprecated
    public IWindow useDesktop(JPanel ui) {
        if (ui == null) {
            throw new RuntimeException("FATAL - JPanel cannot be null");
        }

        // Set desktop mode on first call
        if (isDesktop == null) {
            isDesktop = true;
            context.setDesktopMode(true);
        } else if (!isDesktop) {
            System.err.println("WARN - useDesktop() called after useWindow() was already called.");
            System.err.println("WARN - The first call to useWindow() or useDesktop() determines the mode.");
            System.err.println("WARN - Ignoring this call; framework is already in window mode.");
            throw new RuntimeException("Cannot mix useDesktop() and useWindow() modes");
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

        // this.initializeOtherWindows(); // removed 12/6/2025, since this overall method is deprecated, it shouldn't hurt

        return internalFrame; // frame;
    } // end method

    /**
     * Given an instance of JPanel, return an IWindow object compatible with a
     * window-based user experience (i.e. a JFrame).
     *
     * @deprecated Use createWindow(JPanel) instead. Mode is now determined by uContext.
     * @param ui
     * @return
     */
    @Deprecated
    public IWindow useWindow(JPanel ui) {
        if (ui == null) {
            throw new RuntimeException("FATAL - JPanel cannot be null");
        }

        // Set window mode on first call
        if (isDesktop == null) {
            isDesktop = false;
            context.setDesktopMode(false);
        } else if (isDesktop) {
            System.err.println("WARN - useWindow() called after useDesktop() was already called.");
            System.err.println("WARN - The first call to useWindow() or useDesktop() determines the mode.");
            System.err.println("WARN - Ignoring this call; framework is already in desktop mode.");
            throw new RuntimeException("Cannot mix useWindow() and useDesktop() modes");
        }

        // Create the JFrame if not already created
        if (externalFrame == null) {
            externalFrame = new XFrame("Your App -- powered by the Foundation API");

            // ... possibly update the frame title
            if (context.getDesktopTitle() != null)
                externalFrame.setTitle(context.getDesktopTitle());

            // Set default close operation for window mode
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Add the UI to the frame
        externalFrame.add(ui);

        return externalFrame;
    }

    /**
     * Tells Foundation that the given IWindow is the "main" desktop window. i.e.
     * the main window is whatever you consider the "controlling" JFrame. p.s.
     * Further, the main window is the one that closes/exits the application when
     * the window's "close" button is clicked. [1]
     * 
     * TODO There needs to be a mechanism and well defined rules for what happens
     * when this is called AFTER a previous desktop window has already been defined.
     * i.e. the first desktop window "wins".
     * 
     * [1] Yes, there may be a very few applications that do not use this paradigm,
     * but I think you get the idea now what the "main" window is.
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
     * Convenience method: Creates a window for the JPanel with the specified title and immediately launches it.
     * This is the one-step approach for simple applications.
     *
     * @param jpanel The JPanel to display
     * @param title The title for the window
     * @return The IWindow that was created and launched
     */
    public IWindow launchWindow(JPanel jpanel, String title) {
        IWindow window = this.createWindow(jpanel);
        window.setTitle(title);
        this.launchWindow(window);
        return window;
    }

    /**
     * Convenience method: Creates a window for the JPanel and immediately launches it.
     * Uses a default title: "Your App -- Powered By the Foundation API"
     * This is the one-step approach for simple applications.
     *
     * @param jpanel The JPanel to display
     * @return The IWindow that was created and launched
     */
    public IWindow launchWindow(JPanel jpanel) {
        return this.launchWindow(jpanel, "Your App -- Powered By the Foundation API");
    }

    /**
     * Launches the given window, making it visible to the user.
     * This is the second step of the two-step approach (createWindow + launchWindow).
     *
     * @param window The IWindow to launch
     */
    public void launchWindow(IWindow window) {
        final List<IWindow> list = new ArrayList<>();
        if (window != externalFrame) {
            list.add(window);
        }
        this.launchWindow(list);
    }

    /**
     * Launches multiple windows, making them visible to the user.
     * Primarily used in desktop mode to launch multiple internal frames.
     *
     * @param windows List of windows to launch
     */
    public void launchWindow(final List<IWindow> windows) {

        if (!isInitialized) {
            throw new RuntimeException("Cannot call launchWindow() until Foundation.init() is called.");
        }

        // Create the JFrame
        if (externalFrame == null) {

            // This is a bit of a hack for now (12/1/2020)...
            // If the externalFrame has not been explicitly registered then try to decode if
            // desktop mode should be used.
            if (windows.size() == 1) {
                if (windows.get(0) instanceof XInternalFrame) {
                    context.setDesktopMode(true);
                    isDesktop = true;
                }
            }

            // We have not registered a desktop/main so create one
            externalFrame = new XFrame("Your App -- Powered By the Foundation API");

            // TODO The jPAD security manager doesn't like this line
            // but other apps without jpad might... review this design
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        } else {
            // We have registered a desktop so use it

        }

        // Start the GUI on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (context.isDesktopMode()) {
                    if (context.getDesktopProvider() == null) {
                        desktop = new JDesktopPane(); // a specialized layered pane
                        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE); // Make dragging a little faster but
                                                                             // perhaps uglier.
                        externalFrame.setContentPane(desktop);
                    } else {
                        desktop = context.getDesktopProvider().doCustomDesktop(externalFrame);
                    }

                    // Note that this only ADDs the window to the desktop;
                    // it is not pack(ed) nor setVisible()... that occurs later.
                    for (IWindow w : windows) {
                        if (w != externalFrame) {
                            desktop.add(w.getComponent());
                            w.pack();
                        }
                    }

                }

                        // Display the window.
                        // In desktop mode, use explicit sizing (JDesktopPane cannot calculate preferred size)
                        // In window mode, pack() calculates size from JPanel content
                        if (context.isDesktopMode()) {
                            externalFrame.setSize(context.getDimension()); // [E]
                        } else {
                            // Window mode: Use explicit dimension if set, otherwise pack()
                            Dimension dim = context.getDimension();
                            if (dim != null && !dim.equals(new Dimension(900, 500))) {
                                // User specified a custom dimension
                                externalFrame.setSize(dim);
                            } else {
                                // Use default behavior: pack() sizes to content
                                externalFrame.pack(); // [A] Let JPanel determine size
                            }
                        }
                        externalFrame.setLocationRelativeTo(null); // [C]
                        externalFrame.setVisible(true);

                    }
                } // end runnable / end run()
        ); // end invokeLater()

        /*
         * All the other windows have been added to the desktop but they have not been
         * made visible; make them visible now.
         *
         * For possible performance reasons, open each subwindow in a new EDT
         */
        for (final IWindow w : windows) {

            // Start the GUI on the Event Dispatch Thread (EDT)
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    w.pack();
                    w.setVisible(true);
                }
            });
        }

    } // end method

    /**
     * A temporary shim to use with SPAR tool while I'm considering fairly major
     * overhaul in application object design and startup.
     *
     * @deprecated Use launchWindow(JPanel) instead
     * @param jpanel
     */
    @Deprecated
    public void showGUI(JPanel jpanel) {
        this.showGUI(this.useWindow(jpanel));
    }

    /**
     * A convenience method for calling showGUI() when you only have one IWindow
     * instance.
     *
     * @deprecated Use launchWindow(IWindow) instead
     * @param window
     */
    @Deprecated
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
     * The logic of this method follows two entry conditions: 1) Has this been
     * called already? which should only be valid if you are purposely running in a
     * desktop mode.
     *
     * 2) Has this **not** been called already? which should only be valid if your
     * are either just running a standalone application **or** if you are creating a
     * generic desktop to host multiple applications (such as jPAD).
     *
     * - If the desktop mode has been chosen, create the external frame (JFrame) -
     * If the window mode has been chosen, create the external frame (JFrame)
     *
     * @deprecated Use launchWindow(List<IWindow>) instead
     */
    @Deprecated
    public void showGUI(final List<IWindow> windows) {

        if (!isInitialized) {
            throw new RuntimeException("Cannot call showGUI() until either useWindow() or useDesktop() is called.");
        }

        // Create the JFrame
        if (externalFrame == null) {

            // This is a bit of a hack for now (12/1/2020)...
            // If the externalFrame has not been explicitly registered then try to decode if
            // desktop mode should be used.
            if (windows.size() == 1) {
                if (windows.get(0) instanceof XInternalFrame) {
                    context.setDesktopMode(true);
                }
            }

            // We have not registered a desktop/main so create one
            externalFrame = new XFrame("Your App -- Powered By the Foundation API");

            // TODO The jPAD security manager doesn't like this line
            // but other apps without jpad might... review this design
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        } else {
            // We have registered a desktop so use it

        }

        // Start the GUI on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (context.isDesktopMode()) {
                    if (context.getDesktopProvider() == null) {
                        desktop = new JDesktopPane(); // a specialized layered pane
                        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE); // Make dragging a little faster but
                                                                             // perhaps uglier.
                        externalFrame.setContentPane(desktop);
                    } else {
                        desktop = context.getDesktopProvider().doCustomDesktop(externalFrame);
                    }

                    // Note that this only ADDs the window to the desktop;
                    // it is not pack(ed) nor setVisible()... that occurs later.
                    for (IWindow w : windows) {
                        if (w != externalFrame) {
                            desktop.add(w.getComponent());
                            w.pack();
                        }
                    }

                }

                        // Display the window.
                        // In desktop mode, use explicit sizing (JDesktopPane cannot calculate preferred size)
                        // In window mode, pack() calculates size from JPanel content
                        if (context.isDesktopMode()) {
                            externalFrame.setSize(context.getDimension()); // [E]
                        } else {
                            // Window mode: Use explicit dimension if set, otherwise pack()
                            Dimension dim = context.getDimension();
                            if (dim != null && !dim.equals(new Dimension(900, 500))) {
                                // User specified a custom dimension
                                externalFrame.setSize(dim);
                            } else {
                                // Use default behavior: pack() sizes to content
                                externalFrame.pack(); // [A] Let JPanel determine size
                            }
                        }
                        externalFrame.setLocationRelativeTo(null); // [C]
                        externalFrame.setVisible(true);

                    }
                } // end runnable / end run()
        ); // end invokeLater()

        /*
         * All the other windows have been added to the desktop but they have not been
         * made visible; make them visible now.
         *
         * For possible performance reasons, open each subwindow in a new EDT
         */
        for (final IWindow w : windows) {

            // Start the GUI on the Event Dispatch Thread (EDT)
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    w.pack();
                    w.setVisible(true);
                }
            });
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
    [E] In desktop mode, we do not pack the external JFrame because JDesktopPane
        cannot calculate its preferred size from internal frames (they are positioned
        absolutely, not laid out). Always use explicit sizing for desktop mode.
        See: docs/architecture/jdesktoppane-sizing-behavior.md

    /* ========================================================================== */

    /**
     * Initialize and show the main window.
     *
     * This method is called automatically by Foundation.init() to ensure that
     * a visible window is always displayed when the framework initializes.
     *
     * If a window is already visible, this method does nothing (supports the
     * multi-tool desktop scenario where Foundation.init() might be called
     * multiple times as different tools are loaded).
     */
    protected void _initializeAndShowWindow() {
        // If we already have a visible frame, do nothing
        if (externalFrame != null && externalFrame.isVisible()) {
            return;
        }

        // Determine mode (desktop vs window) from context
        // If mode hasn't been set yet, use the context setting (defaults to window mode)
        if (isDesktop == null) {
            isDesktop = context.isDesktopMode();
        }

        // Create the external frame if it doesn't exist
        if (externalFrame == null) {
            String title = context.getDesktopTitle();
            if (title == null) {
                title = "Foundation Application";
            }
            externalFrame = new XFrame(title);
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Set up desktop mode if needed
        if (isDesktop) {
            if (desktop == null) {
                if (context.getDesktopProvider() == null) {
                    desktop = new JDesktopPane();
                    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
                    externalFrame.setContentPane(desktop);
                } else {
                    desktop = context.getDesktopProvider().doCustomDesktop(externalFrame);
                }
            }

            // Initialize other windows (Bronze tier will create internal frames here)
            this.initializeOtherWindows();
        }

        // Show the window on the EDT
        final XFrame frameToShow = externalFrame;
        final Dimension size = context.getDimension();
        final boolean isDesktopMode = isDesktop;

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Set size
                if (isDesktopMode) {
                    // Desktop mode: always use explicit sizing
                    frameToShow.setSize(size);
                } else {
                    // Window mode: use explicit size if non-default, otherwise pack() will be called later
                    if (size != null && !size.equals(new Dimension(900, 500))) {
                        frameToShow.setSize(size);
                    }
                    // Note: If window mode has no content yet, the frame will be very small
                    // Applications should add content after init() and call pack() if needed
                }

                frameToShow.setLocationRelativeTo(null); // Center on screen
                frameToShow.setVisible(true);
            }
        });
    }

    /**
     * This is basically a noop in Stone since it only supports a single
     * application window.  Other levels will definitely override this.
     */
    protected void initializeOtherWindows() {}

} // end class
