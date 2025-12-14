package org.jwellman.foundation;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jwellman.foundation.framework.LAFDiscovery;
import org.jwellman.foundation.framework.uUtility;
import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.model.PanelRegistration;
import org.jwellman.foundation.provider.DefaultDesktopProvider;
import org.jwellman.foundation.swing.IWindow;
import org.jwellman.foundation.swing.XFrame;
import org.jwellman.foundation.swing.XInternalFrame;
import org.jwellman.foundation.swing.XPanel;

/**
 * The most basic of Swing application requirements.
 * <p>
 * Stone only supports a single application;
 * in either single frame or desktop mode.<br>
 * The use of extra frames is left to the application programmer;<br>
 * However, it is recommended to use Foundation - Bronze (or above)
 * to provide a ready made API.
 *
 * @author rwellman
 *
 */
public class Stone {

	/** The master application context - controls overall lifecycle */
	protected uiContext masterContext;

	/** Indicates desktop mode; null until first useWindow() or useDesktop() call */
	protected Boolean isDesktop;

	/** Guards the init() method */
	protected boolean isInitialized;

	/** The "controlling" JFrame; used in both modes */
	protected XFrame externalFrame;

	/** The "main" internal frame used in desktop mode */
	// Removed because probably not necessary - can now be accessed via masterContext
	// protected XInternalFrame internalFrame;

	/** The JDesktopPane used in desktop mode */
	protected JDesktopPane desktop;

	/** Default application title */
	protected static final String DEFAULT_APP_TITLE = "Your App -- Powered By the Foundation API";

	/**
	 * This is the workhorse of initializing the graphical "environment" in Swing;
	 * note that it mainly consists of initializing the Look and Feel.
	 * <p>
	 * Note also that "initializing the environment" does not consist of
	 * creating ANY actual Swing components so this method does not have to
	 * occur on the EDT -- creating Swing components will come later
	 * as you build your user interface.  i.e. this method does the
	 * bootstrapping for you -- you get to focus on building the UI.
	 *
	 * Finally, notice the enforcement that the initialization occurs only
	 * once -- any subsequent calls will log a warning but not actually
	 * do anything else.
	 *
	 * @param c the micro context
	 */
	protected uiContext _init(uiContext c) {

        if (isInitialized) {
            // TODO eventually in silver we need to be able to init a new context, it just won't be the master context

            System.out.print("WARN - init() has been called more than once...");
            System.out.print("WARN - ... in a single app use case, this usually indicates a misuse of the API");
            System.out.print("WARN - ... which may often result in unexpected/undesired behavior.");
        } else {
            // TODO eventually in silver we need to be able to init a new context, it just won't be the master context

            // Set the master context & mark initialized as true
            masterContext = c;
            isInitialized = true;

            // I haven't settled on where I want this yet but I do want it 
            // as part of the bootstrapping process for debugging purposes.
            // For now, I have created logEnvironment() for this:
            // logEnvironment();

            @SuppressWarnings("unused")
            Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit()
                    .getDesktopProperty("awt.font.desktophints");

            // Apply anti-aliasing for better rendering (particulary fonts)
            // The following may have some subtle system dependent behavior:
            // http://stackoverflow.com/questions/179955/how-do-you-enable-anti-aliasing-in-arbitrary-java-apps
            // Try System.setProperty("awt.useSystemAAFontSettings", "lcd"); 
            // and you should get ClearType
            // as of 7/5/2019, the combination of "on"/"true" yielded 
            // inconsistent results with different look and feels (i.e. Nimbus vs. Napkin... 
            // napkin actually looked better which surprised me)
            // so, now trying off/false:
            boolean aasettings = false;
            if (aasettings) {
                System.setProperty("swing.aatext", "false");
                System.setProperty("awt.useSystemAAFontSettings", "off");
                System.out.println("Anti-alias settings:  off/false");
            }

            // Make sure our window decorations come from the look and feel.
            // JFrame.setDefaultLookAndFeelDecorated(true); // experimentally moved below on 12/7/2025

            // Use LAFDiscovery to select and apply Look and Feel
            // Priority: uContext.lookAndFeel -> config file -> ./lafs/ directory -> system default
            JFrame.setDefaultLookAndFeelDecorated(true); 
            JDialog.setDefaultLookAndFeelDecorated(true);
            boolean lafApplied = LAFDiscovery.selectAndApplyLookAndFeel(masterContext.getLookAndFeel());
            if (!lafApplied) {
                System.err.println("WARNING: Failed to apply any Look and Feel. UI may not render correctly.");
            } else {
                System.out.println("USING LAF: " + UIManager.getLookAndFeel().getName());
            }

            // Save the context
            // IMPORTANT: context should NEVER be null
            // Foundation.init() ensures a valid context is always provided
            // If context is null here, that's a fundamental framework bug - let it NPE
            // remove this eventually... already saved in initStone
            // masterContext = c;

            // Apply context settings
            if (masterContext.getThemeProvider() != null) {
                masterContext.getThemeProvider().doTheme();
            }

            // This may have to move again but it is done here so 
            // that desktop mode code has a desktop provider after init().
            
            // Get or create desktop provider
            // IMPORTANT: context is never null (guaranteed by Foundation.init())
            if (masterContext.getDesktopProvider() == null) {
                // Use default framework provider
                masterContext.setDesktopProvider(new DefaultDesktopProvider());
            }
            setDesktop(masterContext.getDesktopProvider().createDesktop());

        }

        return c;
    } // end method

    /**
     * Creates a window for the given JPanel based on the current mode (window or desktop).
     * The mode is determined by the uContext provided during init().
     * Convention: If no uContext provided, defaults to window mode (standalone).
     *
     * @param ui The JPanel to display
     * @return IWindow abstraction (JFrame or JInternalFrame depending on mode)
     */
    protected IWindow createWindow(JPanel ui) {
        if (ui == null) {
            throw new RuntimeException("FATAL - JPanel cannot be null");
        }

        // Determine mode if not already set
        // Convention over configuration: default to window mode if not specified
        if (isDesktop == null) {
            isDesktop = masterContext.isDesktopMode();
        }

        if (isDesktop) {
            // Create internal frame for desktop mode
            final XInternalFrame internalFrame = new XInternalFrame("Your UI", true, true, true, true);
            internalFrame.setBounds(10, 10, 225, 125);
            internalFrame.setMaximizable(false);
            internalFrame.setClosable(false);
            internalFrame.add(ui);

            this.initializeOtherWindows();

            return internalFrame;
        } else {
            // Create JFrame for window mode
            if (externalFrame == null) {
                this.setExternalFrame(new XFrame(DEFAULT_APP_TITLE));

                if (masterContext.getDesktopTitle() != null)
                    externalFrame.setTitle(masterContext.getDesktopTitle());

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
            masterContext.setDesktopMode(true);
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

        return internalFrame; // frame;
    } // end method

    /**
     * UPDATE DEC 2025: I hate this name - needs to be showWindow but not conflict with bronze._showWindow()
     * 
     * Convenience method: 
     * Creates a window for the JPanel with the specified title and immediately launches it.
     * This is the one-step approach for simple applications.
     *
     * @param jpanel The JPanel to display
     * @param title The title for the window
     * @return The IWindow that was created and launched
     */
    public IWindow launchWindow(JPanel jpanel, String title) {
        throw new RuntimeException("Deprecated - Refactor any code that uses this.");
//        IWindow window = this.createWindow(jpanel);
//        window.setTitle(title);
//        this.launchWindow(window);
//        return window;
    }

    /**
     * UPDATE DEC 2025: I hate this name - needs to be showWindow but not conflict with bronze._showWindow()
     * 
     * Convenience method: 
     * Creates a window for the JPanel and immediately launches it.
     * Uses a default title: "Your App -- Powered By the Foundation API"
     * This is the one-step approach for simple applications.
     *
     * @param jpanel The JPanel to display
     * @return The IWindow that was created and launched
     */
    public IWindow launchWindow(JPanel jpanel) {
        throw new RuntimeException("Deprecated - Refactor any code that uses this.");
        // return this.launchWindow(jpanel, DEFAULT_APP_TITLE);
    }

    /**
     * UPDATE DEC 2025: I hate this name - needs to be showWindow but not conflict with bronze._showWindow()
     * 
     * Overloaded launchWindow that takes PanelRegistration object.
     * 
     * @param masterPanel
     */
    public IWindow launchWindow(PanelRegistration reg) {
        reg.setVisible(true);

        IWindow w = reg.getWindow();
        this.launchWindow(w);
        return w;
    }

    /**
     * UPDATE DEC 2025: I hate this name - needs to be showWindow but not conflict with bronze._showWindow()
     * 
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
     * UPDATE DEC 2025: I hate this name - needs to be showWindow but not conflict with bronze._showWindow()
     * 
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
                    masterContext.setDesktopMode(true);
                    isDesktop = true;
                }
            }

            // We have not registered a desktop/main so create one
            this.setExternalFrame(new XFrame(DEFAULT_APP_TITLE));

            // TODO The jPAD security manager doesn't like this line
            // but other apps without jpad might... review this design
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        }

        // Start the GUI on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (masterContext.isDesktopMode()) {
                    if (masterContext.getDesktopProvider() == null) {
                        setDesktop( new JDesktopPane()); // a specialized layered pane
                        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE); 
                        // Make dragging a little faster but perhaps uglier.

                        externalFrame.setContentPane(desktop);
                    } else {
                        setDesktop( masterContext.getDesktopProvider().getDesktop());
                    }

                    // Note that this only ADDs the window to the desktop;
                    // it is not pack(ed) nor setVisible()... that occurs later.
//                    for (IWindow w : windows) {
//                        if (w != externalFrame) {
//                            w.pack();
//                            w.setVisible(true);
//                        }
//                    }

                }

                // Display the window.
                // In desktop mode, use explicit sizing (JDesktopPane cannot calculate preferred size)
                // In window mode, pack() calculates size from JPanel content
//                if (masterContext.isDesktopMode()) { // TODO this has already occurred in desktop mode so research and fix code
//                    externalFrame.setSize(masterContext.getDimension()); // [E]
//                } else {
//                    // Window mode: Use explicit dimension if set, otherwise pack()
//                    Dimension dim = masterContext.getDimension();
//                    if (dim != null && !dim.equals(new Dimension(900, 500))) {
//                        // User specified a custom dimension
//                        externalFrame.setSize(dim);
//                    } else {
//                        // Use default behavior: pack() sizes to content
//                        externalFrame.pack(); // [A] Let JPanel determine size
//                    }
//                }
//                externalFrame.setLocationRelativeTo(null); // [C]
//                externalFrame.setVisible(true);

               } // end run()

        } ); // end runnable / invokeLater()

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
                    // 12/13 commenting most of this out for debugging.
                    // (but now realizing maybe this is a "conflict" between
                    // stone and bronze code)
                    // in bronze.createInternalFrameForPanel() we do most of 
                    // this work already except setting visible.  
                    w.setTitle(uUtility.valueOrDefault(w.getTitle(), "iwindow"));
// These were to debug but not necessary because bronze.createInternalFrame... is doing it correctly.
// The missing piece was desktop.add() [but am a little confused because that WAS part of createInternalFrame?]
// It may turn out that you can't add to a desktop until it has been made visible?
//                    w.add(new JLabel("temp"));
//                    w.pack(); // hopefully done in Bronze.createInternalFrameForPanel
//                    w.setLocation(100, 100);
                    // need access to PanelRegistration to get WindowPosition and call apply()
                    w.setVisible(true);
                    System.out.println("JDesktopPane stone: " + uUtility.objString(desktop));
//                    desktop.add(w.getComponent()); // hopefully done in Bronze.createInternalFrameForPanel
                    System.out.println("Make visible: " + w.getTitle());

                    // This follows the oracle tutorial
                    //... set the window size or call pack
                    //... set the window's location
                    //... set visible
                    //... add to desktop
//                    System.out.println("w " + w.toString());
//                    System.out.println("c " + w.getComponent().toString());
//                    desktop.add(w.getComponent());
//                      desktop.setPosition(w.getComponent(), 0);
// This will create an internal frame for debugging purposes
//                    JInternalFrame f = new JInternalFrame();
//                    f.setTitle("temp");
//                    f.add(new JLabel("temp"));
//                    f.pack();
//                    f.setLocation(50, 50);
//                    f.setVisible(true);
//                    desktop.add(f);
                    
                }
            });
        }

    } // end method

    /**
     * A private setter to enforce that external logic should never try to 
     * set the external frame after it has already been created.  
     * 
     * @param xFrame
     */
    private void setExternalFrame(XFrame xFrame) {
        if (this.externalFrame != null) {
            throw new IllegalArgumentException("ExternalFrame has already been initialized.");
        }
        this.externalFrame = xFrame;
    }

    /**
     * I don't think we want this public in the long term - may have to compare
     * Stone vs Bronze.
     * 
     * @return
     */
    public JDesktopPane getDesktop() {
        return desktop;
    }

    protected void setDesktop(JDesktopPane p) {
        if (desktop == null) {
            desktop = p;
            System.out.println("INFO - Setting desktop: " + uUtility.objString(p));
        } else {
            System.out.println("ERROR - Cannot override current desktop");
        }
    }

    protected XFrame getExternalFrame() {
        return externalFrame;
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
     * @param ctx 
     */
    protected void _initializeAndShowWindow(uiContext ctx) {

        // If we already have a visible frame, do nothing
        if (externalFrame != null && externalFrame.isVisible()) {
            return;
        }

        // Determine mode (desktop vs window) from context
        // If mode hasn't been set yet, use the context setting (defaults to window mode)
        // TODO this setting of desktop mode may have to occur before now
        if (isDesktop == null) {
            isDesktop = masterContext.isDesktopMode();
        }

        // Create the external frame if it doesn't exist
        if (externalFrame == null) {
            String title = uUtility.valueOrDefault(masterContext.getDesktopTitle(), "Foundation Application");
            this.setExternalFrame(new XFrame(title));
            externalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Set up desktop mode if needed
        if (isDesktop) {

            uiDesktopProvider provider = masterContext.getDesktopProvider();

            // Create desktop using provider (no parameters - supports nested desktops)
            setDesktop(provider.createDesktop());

            // Framework sets desktop as content pane
            externalFrame.setContentPane(desktop);

            // Add menu bar if provider supplies one
            javax.swing.JMenuBar menuBar = provider.createMenuBar();
            if (menuBar != null) {
                externalFrame.setJMenuBar(menuBar);
            }

            // Store provider reference for post-initialization callback
            final uiDesktopProvider finalProvider = provider;
            final JDesktopPane finalDesktop = desktop;

            // We'll call onDesktopInitialized after the window is shown
            // This will be done in the EDT runnable below
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    finalProvider.onDesktopInitialized(finalDesktop);
                }
            });

            // Initialize other windows (Bronze tier will create internal frames here)
            // Note: These frames are created but NOT visible (will be shown via launch())
            this.initializeOtherWindows();
        } else {
            XPanel master = masterContext.getMasterPanel().getPanel();
            externalFrame.setContentPane(master);
        }

        // Show the window on the EDT
        try {
            final XFrame frameToShow = externalFrame;
            final Dimension size = masterContext.getDimension();
            final boolean isDesktopMode = isDesktop;

            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // Set size
                    if (isDesktopMode) {
                        // Desktop mode: always use explicit sizing
                        frameToShow.setSize(size);
                    } else {
                        // Window mode: pack to fit splash content
                        frameToShow.pack();
                    }

                    frameToShow.setLocationRelativeTo(null); // Center on screen
                    frameToShow.setVisible(true);

                    if (isDesktopMode) {
                        // Desktop mode: always use explicit sizing
                        launchWindow(masterContext.getMasterPanel());
                    }

                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // temporarily disable while debugging demos
        // this.logEnvironment();

    }

    /**
     * A convenience method for code that I want to run at startup for the
     * foreseeable future.  The return value is a bit of a hack to support
     * some demo mode code.
     * 
     * @return
     */
    public int logEnvironment() {

        // Log the directory from which the JVM was launched (working directory) 
        String currentDir = System.getProperty("user.dir");
        System.out.println("----- Current Directory -----");
        System.out.println(currentDir);

        // Log the application classpath 
        System.out.println("----- Application Classpath -----");

        // Works in all Java versions
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        // This is the old implementation - keeping for a while
        //      final ClassLoader cl = ClassLoader.getSystemClassLoader();
        //      final URL[] urls = ((URLClassLoader) cl).getURLs();
        //      for (URL url : urls) {
        //          System.out.println(url.getFile());
        //      }

        // Log the system fonts available for debugging purposes
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final Font[] fonts = ge.getAllFonts();

        new Thread(() -> {
            for (String entry : classpathEntries) {
                System.out.println(entry);
            }
            for (Font font : fonts) {
                System.out.print("FONT: ");
                System.out.print(font.getFontName());
                System.out.print(" : ");
                System.out.println(font.getFamily());
            }
        }).start();

        return classpathEntries.length + fonts.length;
    }

    /**
     * This is basically a noop in Stone since it only supports a single
     * application window.  Other levels will definitely override this.
     * TODO remove this method, its function has been deprecated from the design
     */
    protected void initializeOtherWindows() {}

    /**
     * Launch the application with the given context.
     *
     * This creates and displays the main window based on the context configuration.
     * The context determines window mode vs desktop mode, dimensions, title, etc.
     *
     * For Stone tier: This displays a single JFrame (window or desktop mode).
     *
     * @param ctx The uiContext to launch
     */
    protected void _launch(uiContext ctx) {
        if (!isInitialized) {
            throw new IllegalStateException(
                "Foundation must be initialized (call init()) before calling launch()");
        }

        // For Stone tier: Show the main window
        _initializeAndShowWindow(ctx);
    }

} // end class
