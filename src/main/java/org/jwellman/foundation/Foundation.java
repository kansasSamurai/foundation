package org.jwellman.foundation;

/**
 * A micro-framework for Swing applications.
 *
 * Usage:
 * (1) init() - initializes the Swing framework
 * (2) useWindow()/useDesktop() - supply your user interface within a JPanel
 *     and instantiate the supporting Swing containers (JFrame/JInternalFrame).
 * (3) showGUI() - make your user interface/JPanel visible;
 *     most applications will have initialized all data models
 *     and this will usually be the last method called in your startup() code.
 *
 * @author Rick Wellman
 */
public class Foundation extends Platinum {

    public enum APPTYPE {
        DESKTOP, STANDALONE
    }

    protected static APPTYPE apptype;

    private static Foundation f; // singleton

    private Foundation() {} // private constructor to enforce singleton pattern; use get()

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     * 
     * This no-args version is typically only used for demos, SCCEs, or other "simple" use cases.  
     * Finished apps will almost always want to provide a uContext via the init(uContext c) version.
     * 
     * @return
     */
    public static Foundation init() {
        return Foundation.init(APPTYPE.STANDALONE, null);
    }

    /**
     * Initialize the Java Swing graphics environment via the Foundation API.
     * 
     * The main and most important thing this does is initialize the Java Look and Feel;
     * see the _init() method for details on what few other initialization tasks are done. 
     * 
     * Because this framework is intended to support a desktop/multi-app environment,
     * the Foundation instance is a singleton and this method returns that single instance.
     * 
     * @return
     */
    public static Foundation init(APPTYPE apptype, uContext c) {

        if (f == null) {
            f = new Foundation();
        }
        
        f._init(apptype, c);
        
        return f;
    }

}
