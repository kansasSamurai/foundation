package org.jwellman.foundation.preferences;

import org.jwellman.foundation.interfaces.uiPreferenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Default implementation of {@link uiPreferenceProvider}.
 *
 * <p>Provides access to preferences loaded from config/foundation.json
 * with support for multiple lookup strategies (fallback, namespace-only, global-only).</p>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe for read operations
 * after initialization. The preference data is immutable once loaded.</p>
 *
 * @since Silver
 */
public class PreferenceManager implements uiPreferenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceManager.class);

    private final PreferenceData data;

    /**
     * Creates a preference manager by loading config/foundation.json.
     *
     * <p>This constructor validates and loads the configuration file.
     * If the file is missing or malformed, the application will be terminated
     * with an error dialog (Silver+ requirement).</p>
     */
    public PreferenceManager() {
        PreferenceLoader loader = new PreferenceLoader();
        this.data = loader.load(); // May terminate app if config invalid
    }

    /**
     * Creates a preference manager with pre-loaded data (for testing).
     *
     * @param data the preference data
     */
    PreferenceManager(PreferenceData data) {
        this.data = data;
    }

    // ========== Fallback Methods (namespace → global → default) ==========

    @Override
    public boolean getBooleanWithFallback(String namespace, String path, boolean defaultValue) {
        Object value = getValueWithFallback(namespace, path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        logger.debug("Boolean preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public String getStringWithFallback(String namespace, String path, String defaultValue) {
        Object value = getValueWithFallback(namespace, path);
        if (value instanceof String) {
            return (String) value;
        }
        logger.debug("String preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public int getIntWithFallback(String namespace, String path, int defaultValue) {
        Object value = getValueWithFallback(namespace, path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        logger.debug("Integer preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public double getDoubleWithFallback(String namespace, String path, double defaultValue) {
        Object value = getValueWithFallback(namespace, path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        logger.debug("Double preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    // ========== Namespace-Only Methods (no global fallback) ==========

    @Override
    public boolean getNamespaceBoolean(String namespace, String path, boolean defaultValue) {
        Object value = getNamespaceValue(namespace, path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        logger.debug("Namespace boolean preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public String getNamespaceString(String namespace, String path, String defaultValue) {
        Object value = getNamespaceValue(namespace, path);
        if (value instanceof String) {
            return (String) value;
        }
        logger.debug("Namespace string preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public int getNamespaceInt(String namespace, String path, int defaultValue) {
        Object value = getNamespaceValue(namespace, path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        logger.debug("Namespace integer preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    @Override
    public double getNamespaceDouble(String namespace, String path, double defaultValue) {
        Object value = getNamespaceValue(namespace, path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        logger.debug("Namespace double preference '{}' not found for namespace '{}', using default: {}",
                path, namespace, defaultValue);
        return defaultValue;
    }

    // ========== Global-Only Methods (no namespace override) ==========

    @Override
    public boolean getGlobalBoolean(String path, boolean defaultValue) {
        Object value = getGlobalValue(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        logger.debug("Global boolean preference '{}' not found, using default: {}", path, defaultValue);
        return defaultValue;
    }

    @Override
    public String getGlobalString(String path, String defaultValue) {
        Object value = getGlobalValue(path);
        if (value instanceof String) {
            return (String) value;
        }
        logger.debug("Global string preference '{}' not found, using default: {}", path, defaultValue);
        return defaultValue;
    }

    @Override
    public int getGlobalInt(String path, int defaultValue) {
        Object value = getGlobalValue(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        logger.debug("Global integer preference '{}' not found, using default: {}", path, defaultValue);
        return defaultValue;
    }

    @Override
    public double getGlobalDouble(String path, double defaultValue) {
        Object value = getGlobalValue(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        logger.debug("Global double preference '{}' not found, using default: {}", path, defaultValue);
        return defaultValue;
    }

    // ========== Raw Access Methods ==========

    @Override
    public Map<String, Object> getNamespacePreferences(String namespace) {
        return Collections.unmodifiableMap(data.getNamespacePreferences(namespace));
    }

    @Override
    public Map<String, Object> getGlobalPreferences() {
        return Collections.unmodifiableMap(data.getGlobal());
    }

    // ========== Internal Lookup Methods ==========

    /**
     * Gets a value with fallback chain: namespace → global → null.
     *
     * @param namespace the namespace to check first
     * @param path the dot-separated path
     * @return the value, or null if not found
     */
    private Object getValueWithFallback(String namespace, String path) {
        // Try namespace first
        Object value = getNamespaceValue(namespace, path);
        if (value != null) {
            return value;
        }

        // Fall back to global
        return getGlobalValue(path);
    }

    /**
     * Gets a value from namespace preferences only.
     *
     * @param namespace the namespace
     * @param path the dot-separated path
     * @return the value, or null if not found
     */
    private Object getNamespaceValue(String namespace, String path) {
        Map<String, Object> namespacePrefs = data.getNamespacePreferences(namespace);
        return navigatePath(namespacePrefs, path);
    }

    /**
     * Gets a value from global preferences only.
     *
     * @param path the dot-separated path
     * @return the value, or null if not found
     */
    private Object getGlobalValue(String path) {
        return navigatePath(data.getGlobal(), path);
    }

    /**
     * Navigates a dot-separated path through nested maps.
     *
     * <p>Example: "splash.maximizeOnDismiss" navigates:</p>
     * <pre>
     * map.get("splash") → Map
     *   → nestedMap.get("maximizeOnDismiss") → value
     * </pre>
     *
     * @param map the root map to navigate
     * @param path the dot-separated path (e.g., "splash.maximizeOnDismiss")
     * @return the value at the path, or null if not found or path invalid
     */
    @SuppressWarnings("unchecked")
    private Object navigatePath(Map<String, Object> map, String path) {
        if (map == null || path == null || path.trim().isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = map;

        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null; // Path invalid - hit non-map before end
            }

            Map<String, Object> currentMap = (Map<String, Object>) current;
            current = currentMap.get(part);

            if (current == null) {
                return null; // Path not found
            }
        }

        return current;
    }
}
