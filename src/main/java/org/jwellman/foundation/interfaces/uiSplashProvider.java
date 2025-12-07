package org.jwellman.foundation.interfaces;

import javax.swing.JPanel;

/**
 * Strategy interface for providing custom splash screen content and behavior.
 *
 * This interface allows customization of the splash screen shown during
 * Foundation framework initialization. The splash screen provides visual
 * feedback to users while the Look and Feel is applied and the application
 * initializes.
 *
 * Unlike traditional Java splash screens (SplashScreen API), Foundation uses
 * actual Swing components (JFrame/JInternalFrame) for the splash screen because:
 * 1. Modern hardware initializes Swing fast enough that native splash is unnecessary
 * 2. Swing-based splash screens support progress bars and dynamic updates easily
 * 3. Consistent with Foundation's Swing-first design philosophy
 *
 * The Foundation framework provides a default implementation (DefaultSplashProvider)
 * that creates a modern-looking splash screen with progress bar and status messages.
 * Applications can provide custom implementations for branded splash screens.
 *
 * Design Pattern: Strategy pattern
 * - Allows runtime selection of splash screen appearance
 * - Framework wraps splash content in appropriate container (JFrame or JInternalFrame)
 * - Provider creates content, framework manages window lifecycle
 *
 * Context via Constructor Injection:
 * If a provider needs context (application name, version, etc.), it should
 * receive it via constructor when instantiated, not via these methods.
 *
 * @author rwellman
 */
public interface uiSplashProvider {

    /**
     * Creates the splash screen content panel.
     *
     * The framework calls this method during initialization and wraps the
     * returned panel in:
     * - Window mode: JFrame
     * - Desktop mode: JInternalFrame (on the desktop)
     *
     * The panel should include:
     * - Application branding (logo, name, version)
     * - Progress bar (updateable via updateProgress)
     * - Status message area
     *
     * Note: No parameters allows flexibility and supports nested desktops.
     * If provider needs context, inject it via constructor.
     *
     * @return The splash screen content panel
     */
    JPanel createSplashContent();

    /**
     * Update the splash screen progress.
     * <p>
     * Called by the framework (or application code) during initialization
     * to provide visual feedback on loading progress.
     *
     * @param percent Progress percentage (0-100)
     * @param message Status message to display (e.g., "Loading Look and Feel...")
     */
    void updateProgress(int percent, String message);

    /**
     * Called when the splash screen is closed.
     *
     * Implementations can use this hook to perform cleanup or
     * log initialization completion.
     */
    void onSplashClosed();

}
