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
 *     Nearly identical to SimpleWindowDemo - only useDesktop() vs useWindow() differs.
 *     Demonstrates deployment-agnostic design principle.</li>
 *
 * <li><b>MultiPanelDesktopDemo</b> - Shows multiple panels in a desktop environment.
 *     Simulates a multi-tool desktop where each tool is a separate JPanel.
 *     This demonstrates Foundation's vision: tools built once, deployed anywhere.</li>
 *
 * <li><b>LookAndFeelDemo</b> - Tests Look and Feel support.
 *     Validates that all LAF dependencies are configured correctly.
 *     Documents the future architecture for dynamic LAF discovery.</li>
 * </ul>
 *
 * <h2>Key Concepts Demonstrated</h2>
 *
 * <ul>
 * <li><b>Extend JPanel, Not JFrame</b> - Applications extend JPanel; Foundation handles JFrame boilerplate</li>
 * <li><b>Deployment-Agnostic Applications</b> - Same JPanel class works in window or desktop mode</li>
 * <li><b>IWindow Abstraction</b> - Applications never directly reference JFrame/JInternalFrame</li>
 * <li><b>Foundation Lifecycle</b> - init() → useWindow()/useDesktop() → showGUI()</li>
 * <li><b>Minimal Boilerplate</b> - Clean main() methods with clear, simple Foundation API</li>
 * <li><b>Multi-Panel Support</b> - Multiple tools in a single desktop environment</li>
 * </ul>
 *
 * @since 1.0.1
 */
package org.jwellman.foundation.examples;
