/**
 * Demo applications showcasing Foundation Bronze tier capabilities.
 * <p>
 * These demos are separated from the framework JAR to avoid bundling a specific
 * logging implementation. They use SLF4J Simple for visible console output.
 * <p>
 * <b>Core Demos:</b>
 * <ul>
 * <li>{@link org.jwellman.foundation.examples.SimpleWindowDemo} - Minimal window mode example</li>
 * <li>{@link org.jwellman.foundation.examples.SimpleDesktopDemo} - Minimal desktop mode example</li>
 * <li>{@link org.jwellman.foundation.examples.MultiPanelDesktopDemo} - Multi-tool desktop with lifecycle events</li>
 * <li>{@link org.jwellman.foundation.examples.BronzeTierShowcaseDemo} - Comprehensive interactive showcase</li>
 * </ul>
 * <p>
 * <b>Provider Demos:</b>
 * <ul>
 * <li>{@link org.jwellman.foundation.examples.CustomDesktopProviderDemo} - Custom desktop provider</li>
 * <li>{@link org.jwellman.foundation.examples.CustomSplashProviderDemo} - Custom splash provider</li>
 * <li>{@link org.jwellman.foundation.examples.SplashScreenDemo} - Window mode with splash</li>
 * <li>{@link org.jwellman.foundation.examples.SplashScreenDesktopDemo} - Desktop mode with splash</li>
 * </ul>
 * <p>
 * <b>Utility Demos:</b>
 * <ul>
 * <li>{@link org.jwellman.foundation.examples.LookAndFeelDemo} - LAF testing utility</li>
 * </ul>
 * <p>
 * Run demos via Maven:
 * <pre>
 * mvn exec:java -pl bronze-demos
 * mvn exec:java -pl bronze-demos -Dexec.mainClass="org.jwellman.foundation.examples.SimpleWindowDemo"
 * </pre>
 *
 * @author Foundation Framework
 * @see org.jwellman.foundation.Bronze
 */
package org.jwellman.foundation.examples;
