package org.jwellman.foundation.preferences;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data structure for Foundation preferences JSON file.
 *
 * <p>Represents the structure of config/foundation.json:</p>
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
 * @since Silver
 */
class PreferenceData {

    @JsonProperty("version")
    private final String version;

    @JsonProperty("global")
    private final Map<String, Object> global;

    @JsonProperty("namespaces")
    private final Map<String, Map<String, Object>> namespaces;

    /**
     * Creates preference data from JSON deserialization.
     *
     * @param version the configuration version
     * @param global global preferences (required)
     * @param namespaces per-namespace preference overrides (optional)
     */
    @JsonCreator
    public PreferenceData(
            @JsonProperty("version") String version,
            @JsonProperty("global") Map<String, Object> global,
            @JsonProperty("namespaces") Map<String, Map<String, Object>> namespaces) {
        this.version = version;
        this.global = global != null ? global : new HashMap<>();
        this.namespaces = namespaces != null ? namespaces : new HashMap<>();
    }

    /**
     * Gets the configuration version.
     *
     * @return the version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the global preferences map.
     *
     * @return the global preferences (never null)
     */
    public Map<String, Object> getGlobal() {
        return global;
    }

    /**
     * Gets all namespace preference overrides.
     *
     * @return map of namespace to preferences (never null)
     */
    public Map<String, Map<String, Object>> getNamespaces() {
        return namespaces;
    }

    /**
     * Gets preferences for a specific namespace.
     *
     * @param namespace the namespace
     * @return the namespace preferences, or empty map if not found
     */
    public Map<String, Object> getNamespacePreferences(String namespace) {
        Map<String, Object> prefs = namespaces.get(namespace);
        return prefs != null ? prefs : Collections.emptyMap();
    }
}
