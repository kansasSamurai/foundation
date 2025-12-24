/**
 * Example applications demonstrating Foundation framework capabilities.
 *
 * <h2>Running the Examples</h2>
 *
 * <p>Each example can be run via Maven exec plugin:</p>
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SimpleDesktopDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.MultiPanelDesktopDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SplashScreenDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.SplashScreenDesktopDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.CustomSplashProviderDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.CustomDesktopProviderDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.BronzeTierShowcaseDemo"
 * mvn compile exec:java -Dexec.mainClass="org.jwellman.foundation.examples.LookAndFeelDemo"
 * </pre>
 *
 * <p>Or run from compiled JAR:</p>
 * <pre>
 * mvn clean package
 * java -cp target/foundation-1.0.1-SNAPSHOT.jar org.jwellman.foundation.examples.SimpleWindowDemo
 * </pre>
 *
 * <h2>Example Descriptions</h2>
 *
 * <ul>
 * <li><b>SimpleWindowDemo</b> - Minimal example showing window mode (standalone JFrame).
 *     Demonstrates extending JPanel (not JFrame) and minimal boilerplate.
 *     Shows basic Foundation lifecycle and single-panel deployment.</li>
 *
 * <li><b>SimpleDesktopDemo</b> - Minimal example showing desktop mode (JInternalFrame).
 *     Nearly identical to SimpleWindowDemo - demonstrates deployment-agnostic design.
 *     Same application code works in both window and desktop modes.</li>
 *
 * <li><b>MultiPanelDesktopDemo</b> - Shows Bronze tier multi-panel registry in action.
 *     Demonstrates namespace:panelId registration, panel lifecycle events, and window positioning.
 *     Simulates a multi-tool desktop environment with dynamic panel visibility control.</li>
 *
 * <li><b>SplashScreenDemo</b> - Window mode with splash screen during initialization.
 *     Shows DefaultSplashProvider usage with progress updates during app startup.
 *     Demonstrates clean transition from splash to main application UI.</li>
 *
 * <li><b>SplashScreenDesktopDemo</b> - Desktop mode with splash screen during initialization.
 *     Same as SplashScreenDemo but in desktop mode with internal frame splash.
 *     Shows deployment-agnostic splash screen support.</li>
 *
 * <li><b>CustomSplashProviderDemo</b> - Custom splash screen with application-specific branding.
 *     Demonstrates implementing uiSplashProvider with company logo, custom colors, and professional design.
 *     Shows how easy it is to swap providers (one line of code) without modifying framework.</li>
 *
 * <li><b>CustomDesktopProviderDemo</b> - Custom desktop environment with application-specific branding.
 *     Demonstrates implementing uiDesktopProvider with gradient background, company watermark, and desktop menu bar.
 *     Shows provider pattern for desktop customization without framework modification.</li>
 *
 * <li><b>BronzeTierShowcaseDemo</b> - Comprehensive interactive demo combining three key Bronze tier features:
 *     (1) Window Positioning with CASCADE, CENTER, and EXPLICIT strategies;
 *     (2) Panel Lifecycle events (onCreate, onShow, onHide, onClose) with real-time event log;
 *     (3) Dynamic Panel Management with runtime creation/removal and registry queries.
 *     Features interactive control panel, visual event logging, and registry statistics.</li>
 *
 * <li><b>LookAndFeelDemo</b> - Tests Look and Feel support with splash screen.
 *     Validates that all LAF dependencies are configured correctly.
 *     Uses LAFDiscovery system for runtime LAF selection and validation.</li>
 * </ul>
 *
 * <h2>Missing Demos (Future Work)</h2>
 *
 * <p>The following demonstration scenarios are not yet implemented:</p>
 * <ul>
 * <li><b>Theme Provider Demo</b> - Show uiThemeProvider implementation for custom
 *     application themes and color schemes beyond Look and Feel. (May be better suited
 *     for Silver tier when theme support is expanded.)</li>
 * </ul>
 *
 * <h2>Key Concepts Demonstrated</h2>
 *
 * <ul>
 * <li><b>Extend JPanel, Not JFrame</b> - Applications extend JPanel; Foundation handles JFrame boilerplate</li>
 * <li><b>Deployment-Agnostic Applications</b> - Same JPanel class works in window or desktop mode</li>
 * <li><b>IWindow Abstraction</b> - Applications never directly reference JFrame/JInternalFrame</li>
 * <li><b>Foundation Lifecycle</b> - init(context) → register panels → launch(context)</li>
 * <li><b>Splash Screen Support</b> - Optional splash during initialization via uiSplashProvider</li>
 * <li><b>Custom Providers</b> - Easy customization via provider pattern (splash, desktop, theme)</li>
 * <li><b>Minimal Boilerplate</b> - Clean main() methods with clear, simple Foundation API</li>
 * <li><b>Multi-Panel Registry</b> - Bronze tier namespace:panelId registry with lifecycle events</li>
 * <li><b>Window Positioning</b> - Declarative window positioning strategies (CASCADE, CENTER, EXPLICIT)</li>
 * <li><b>Panel Lifecycle Events</b> - onCreate, onShow, onHide, onClose hooks for resource management</li>
 * <li><b>Dynamic Panel Management</b> - Runtime panel creation, removal, visibility control, and registry queries</li>
 * <li><b>Interactive Framework Exploration</b> - Comprehensive showcase demo for hands-on learning</li>
 * </ul>
 *
 * @since 1.0.1
 */
package org.jwellman.foundation.examples;
