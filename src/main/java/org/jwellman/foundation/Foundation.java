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

    private Foundation() {} // private constructor to enforce singleton pattern; use get()

    private static Foundation f; // singleton

    /**
     * Because this framework is intended to support a desktop/multi-app environment,
     * the Foundation instance is a singleton and this method returns that single instance.
     * 
     * @return
     */
    public static Foundation init(uContext c) {
        if (f == null) f = new Foundation();
        
        f._init(c);
        
        return f;
    }

}
