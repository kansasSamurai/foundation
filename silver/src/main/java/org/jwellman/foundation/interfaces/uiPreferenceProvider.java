package org.jwellman.foundation.interfaces;

import java.util.Map;

/**
 * Provides access to user preferences from the Foundation configuration file.
 *
 * <p>The preference system supports a hybrid configuration model with global
 * defaults and per-namespace overrides. Different preference lookup methods
 * support different fallback strategies depending on the nature of the preference.</p>
 *
 * <p><strong>Configuration File Structure (config/foundation.json):</strong></p>
 * <pre>
 * {
 *   "version": "1.0",
 *   "global": {
 *     "splash": {
 *       "maximizeOnDismiss": true
 *     }
 *   },
 *   "namespaces": {
 *     "tool.calculator": {
 *       "splash": {
 *         "maximizeOnDismiss": false
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p><strong>Lookup Strategies:</strong></p>
 * <ul>
 *   <li><strong>WithFallback:</strong> Checks namespace → global → default value</li>
 *   <li><strong>Namespace:</strong> Only checks namespace → default value (no global fallback)</li>
 *   <li><strong>Global:</strong> Only checks global → default value (no namespace override)</li>
 * </ul>
 *
 * <p><strong>Path Format:</strong></p>
 * <p>Preferences are accessed using dot-separated paths that map to nested JSON structure:</p>
 * <ul>
 *   <li>{@code "splash.maximizeOnDismiss"} → {@code {"splash": {"maximizeOnDismiss": true}}}</li>
 *   <li>{@code "window.position.x"} → {@code {"window": {"position": {"x": 100}}}}</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Preference with fallback (namespace → global → default)
 * boolean maximize = prefs.getBooleanWithFallback(namespace, "splash.maximizeOnDismiss", true);
 *
 * // Namespace-only preference (no global fallback)
 * int x = prefs.getNamespaceInt(namespace, "window.position.x", 100);
 *
 * // Global-only preference (no namespace override)
 * String theme = prefs.getGlobalString("theme.defaultLAF", "Nimbus");
 * </pre>
 *
 * @since Silver
 */
public interface uiPreferenceProvider {

    // ========== Fallback Methods (namespace → global → default) ==========

    /**
     * Gets a boolean preference with fallback chain: namespace → global → default.
     *
     * @param namespace the namespace to check first
     * @param path the dot-separated preference path (e.g., "splash.maximizeOnDismiss")
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    boolean getBooleanWithFallback(String namespace, String path, boolean defaultValue);

    /**
     * Gets a string preference with fallback chain: namespace → global → default.
     *
     * @param namespace the namespace to check first
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    String getStringWithFallback(String namespace, String path, String defaultValue);

    /**
     * Gets an integer preference with fallback chain: namespace → global → default.
     *
     * @param namespace the namespace to check first
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    int getIntWithFallback(String namespace, String path, int defaultValue);

    /**
     * Gets a double preference with fallback chain: namespace → global → default.
     *
     * @param namespace the namespace to check first
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    double getDoubleWithFallback(String namespace, String path, double defaultValue);

    // ========== Namespace-Only Methods (no global fallback) ==========

    /**
     * Gets a boolean preference from namespace only (no global fallback).
     *
     * @param namespace the namespace to check
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    boolean getNamespaceBoolean(String namespace, String path, boolean defaultValue);

    /**
     * Gets a string preference from namespace only (no global fallback).
     *
     * @param namespace the namespace to check
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    String getNamespaceString(String namespace, String path, String defaultValue);

    /**
     * Gets an integer preference from namespace only (no global fallback).
     *
     * @param namespace the namespace to check
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    int getNamespaceInt(String namespace, String path, int defaultValue);

    /**
     * Gets a double preference from namespace only (no global fallback).
     *
     * @param namespace the namespace to check
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    double getNamespaceDouble(String namespace, String path, double defaultValue);

    // ========== Global-Only Methods (no namespace override) ==========

    /**
     * Gets a boolean preference from global section only (no namespace override).
     *
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    boolean getGlobalBoolean(String path, boolean defaultValue);

    /**
     * Gets a string preference from global section only (no namespace override).
     *
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    String getGlobalString(String path, String defaultValue);

    /**
     * Gets an integer preference from global section only (no namespace override).
     *
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    int getGlobalInt(String path, int defaultValue);

    /**
     * Gets a double preference from global section only (no namespace override).
     *
     * @param path the dot-separated preference path
     * @param defaultValue the default value if preference not found
     * @return the preference value
     */
    double getGlobalDouble(String path, double defaultValue);

    // ========== Raw Access Methods ==========

    /**
     * Gets all preferences for a specific namespace.
     *
     * @param namespace the namespace
     * @return map of preferences for the namespace (may be empty)
     */
    Map<String, Object> getNamespacePreferences(String namespace);

    /**
     * Gets all global preferences.
     *
     * @return map of global preferences
     */
    Map<String, Object> getGlobalPreferences();
}
