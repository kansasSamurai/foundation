package org.jwellman.foundation.examples;

import java.awt.Color;

import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;

import org.jwellman.foundation.interfaces.uiDesktopManager;
import org.jwellman.foundation.interfaces.uiDesktopProvider;
import org.jwellman.foundation.provider.DefaultDesktopManager;
import org.jwellman.foundation.provider.DefaultDesktopUI;

/**
 * Custom desktop provider for the Silver Tier Showcase Demo.
 * <p>
 * This provider demonstrates how to integrate {@link DefaultDesktopUI} to provide
 * desktop management features (cascade, tile, minimize all, etc.) via menu bar.
 * <p>
 * This showcases the pattern where:
 * <ul>
 *   <li>{@link DefaultDesktopManager} provides the logic (how to manage windows)</li>
 *   <li>{@link DefaultDesktopUI} provides the UI (menu bar, toolbar)</li>
 *   <li>Desktop provider wires them together</li>
 * </ul>
 *
 * @author Foundation Framework
 */
public class ShowcaseDesktopProvider implements uiDesktopProvider {

    private JDesktopPane desktop;
    private DefaultDesktopManager desktopManager;
    private DefaultDesktopUI desktopUI;

    @Override
    public JDesktopPane createDesktop() {
        desktop = new JDesktopPane();

        // Configure desktop appearance
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.setBackground(new Color(220, 230, 240)); // Light blue-gray

        // Create desktop manager
        desktopManager = new DefaultDesktopManager(desktop);

        // Create desktop UI controls
        desktopUI = new DefaultDesktopUI(desktopManager);

        return desktop;
    }

    @Override
    public JDesktopPane getDesktop() {
        return desktop;
    }

    @Override
    public JMenuBar createMenuBar() {
        // Use DefaultDesktopUI to create the menu bar
        // This demonstrates how applications can use the reference implementation
        return desktopUI.createDesktopMenuBar();
    }

    @Override
    public uiDesktopManager getDesktopManager() {
        return desktopManager;
    }

    @Override
    public void onDesktopInitialized(JDesktopPane desktop) {
        // No special initialization needed for showcase
        System.out.println("Showcase Desktop initialized with DefaultDesktopUI integration");
    }
}
