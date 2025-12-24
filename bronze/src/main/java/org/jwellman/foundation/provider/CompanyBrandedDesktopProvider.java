package org.jwellman.foundation.provider;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jwellman.foundation.interfaces.uiDesktopProvider;

/**
 * Example custom desktop provider demonstrating application-specific desktop branding.
 * <p>
 * This implementation shows how to create a custom desktop environment with:
 * <ul>
 * <li>Custom gradient background instead of solid color</li>
 * <li>Corporate color scheme (matching CompanyBrandedSplashProvider)</li>
 * <li>Desktop-level menu bar with File, Window, and Help menus</li>
 * <li>Post-initialization hook for logging or setup</li>
 * <li>Professional appearance for enterprise applications</li>
 * </ul>
 * <p>
 * This demonstrates the provider pattern - applications can completely customize
 * the desktop environment without modifying framework code by implementing uiDesktopProvider.
 * <p>
 * Design Rationale:
 * <ul>
 * <li><b>Parameter-free methods</b> - Supports nested desktops (desktop within desktop)</li>
 * <li><b>No framework coupling</b> - Works with any container (JFrame, JInternalFrame)</li>
 * <li><b>Constructor injection</b> - If context is needed, inject via constructor (not shown here)</li>
 * <li><b>Strategy pattern</b> - Runtime selection of desktop creation strategy</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * uContext context = Foundation.createContext("myapp");
 * context.setDesktopMode(true);
 * context.setDesktopProvider(new CompanyBrandedDesktopProvider());
 * Foundation.init(context);
 * </pre>
 *
 * @author Foundation Framework
 * @see org.jwellman.foundation.examples.CustomDesktopProviderDemo
 */
public class CompanyBrandedDesktopProvider implements uiDesktopProvider {

    private JDesktopPane desktop;

    // Corporate color scheme (matching CompanyBrandedSplashProvider)
    private static final Color CORPORATE_GREEN = new Color(34, 139, 34);
    private static final Color CORPORATE_BLUE = new Color(70, 130, 180);
    private static final Color GRADIENT_START = new Color(240, 245, 250);
    private static final Color GRADIENT_END = new Color(220, 230, 240);

    /**
     * Creates a custom JDesktopPane with gradient background.
     * <p>
     * This is the primary method called by the Foundation framework when
     * initializing desktop mode. This implementation:
     * <ul>
     * <li>Creates a JDesktopPane with custom painting (gradient background)</li>
     * <li>Configures LIVE_DRAG_MODE for smoother window dragging (vs OUTLINE_DRAG_MODE)</li>
     * <li>Returns the configured desktop for framework to use as content pane</li>
     * </ul>
     * <p>
     * Note: No parameters allows this to work with any container type and
     * supports nested desktops. If provider needs context, inject via constructor.
     *
     * @return The configured JDesktopPane with custom gradient background
     */
    @Override
    public JDesktopPane createDesktop() {
        // Create custom desktop pane with gradient background
        desktop = new JDesktopPane() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Paint gradient background from top to bottom
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    0, getHeight(), GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add subtle company branding watermark in corner
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(CORPORATE_BLUE.getRed(), CORPORATE_BLUE.getGreen(),
                                      CORPORATE_BLUE.getBlue(), 30)); // Very transparent
                g2d.setFont(g2d.getFont().deriveFont(48f));
                g2d.drawString("Acme Business Solutions", 20, getHeight() - 30);
            }
        };

        // Use LIVE_DRAG_MODE for smoother dragging (shows full window while dragging)
        // Alternative: OUTLINE_DRAG_MODE (faster but shows only outline)
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);

        return desktop;
    }

    /**
     * Returns the desktop created by createDesktop().
     *
     * @return The configured JDesktopPane
     */
    @Override
    public JDesktopPane getDesktop() {
        return desktop;
    }

    /**
     * Provides a custom menu bar for the desktop environment.
     * <p>
     * This implementation creates a professional desktop-level menu bar with:
     * <ul>
     * <li><b>File menu</b> - Exit action</li>
     * <li><b>Window menu</b> - Cascade, Tile, and Minimize All actions</li>
     * <li><b>Help menu</b> - About action</li>
     * </ul>
     * <p>
     * Note: Menu actions are basic demonstrations. Production applications
     * would implement full functionality (actual cascade/tile logic, etc.).
     *
     * @return A JMenuBar with File, Window, and Help menus
     */
    @Override
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem newMenuItem = new JMenuItem("New...");
        newMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(desktop,
                "New action would open a dialog to create new items.\n" +
                "This is a demo placeholder.",
                "New", JOptionPane.INFORMATION_MESSAGE);
        });
        fileMenu.add(newMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener((ActionEvent e) -> {
            int result = JOptionPane.showConfirmDialog(desktop,
                "Are you sure you want to exit?",
                "Exit Application", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        // Window Menu
        JMenu windowMenu = new JMenu("Window");

        JMenuItem cascadeMenuItem = new JMenuItem("Cascade");
        cascadeMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(desktop,
                "Cascade would arrange windows diagonally.\n" +
                "This is a demo placeholder - full implementation in Silver tier.",
                "Cascade Windows", JOptionPane.INFORMATION_MESSAGE);
        });
        windowMenu.add(cascadeMenuItem);

        JMenuItem tileMenuItem = new JMenuItem("Tile");
        tileMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(desktop,
                "Tile would arrange windows in a grid.\n" +
                "This is a demo placeholder - full implementation in Silver tier.",
                "Tile Windows", JOptionPane.INFORMATION_MESSAGE);
        });
        windowMenu.add(tileMenuItem);

        windowMenu.addSeparator();

        JMenuItem minimizeAllMenuItem = new JMenuItem("Minimize All");
        minimizeAllMenuItem.addActionListener((ActionEvent e) -> {
            // Actually implement minimize all
            try {
                javax.swing.JInternalFrame[] frames = desktop.getAllFrames();
                for (javax.swing.JInternalFrame frame : frames) {
                    frame.setIcon(true);
                }
            } catch (java.beans.PropertyVetoException ex) {
                // Ignore veto
            }
        });
        windowMenu.add(minimizeAllMenuItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(desktop,
                "<html><center>" +
                "<h2>Acme Business Solutions</h2>" +
                "<p>Enterprise Management Suite</p>" +
                "<p>Version 2.5.1 (Build 2025.12)</p>" +
                "<br>" +
                "<p><i>Powered by Foundation Framework</i></p>" +
                "<p>© 2025 Acme Business Solutions</p>" +
                "</center></html>",
                "About", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutMenuItem);

        // Assemble menu bar
        menuBar.add(fileMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Called after the desktop is fully initialized and visible.
     * <p>
     * This implementation logs initialization to demonstrate the hook.
     * Production implementations might:
     * <ul>
     * <li>Add desktop icons or shortcuts</li>
     * <li>Start background services or monitoring</li>
     * <li>Display welcome dialogs or tips</li>
     * <li>Load user preferences for window positions</li>
     * <li>Initialize desktop-level features</li>
     * </ul>
     *
     * @param desktop The initialized desktop pane
     */
    @Override
    public void onDesktopInitialized(JDesktopPane desktop) {
        System.out.println("=== Custom Desktop Provider Initialized ===");
        System.out.println("Desktop environment ready with custom branding");
        System.out.println("Desktop size: " + desktop.getWidth() + "x" + desktop.getHeight());
        System.out.println("Drag mode: LIVE_DRAG_MODE");
        System.out.println("Menu bar: Enabled (File, Window, Help)");
        System.out.println("Background: Custom gradient (GRADIENT_START -> GRADIENT_END)");
        System.out.println("===========================================");
    }

}
