package org.jwellman.foundation.plugin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a registered plugin in the plugin registry.
 *
 * <p>PluginRegistration instances are persisted to the registry.json file
 * and track both plugin metadata and user preferences/settings.</p>
 *
 * <p>This class is serializable to JSON for persistence in the registry file.</p>
 */
public class PluginRegistration {

    private String id;
    private String name;
    private File pluginDir;
    private LaunchMode launchMode;
    private boolean enabled;
    private LocalDateTime registered;
    private Map<String, Object> userPreferences;

    /**
     * Creates a new plugin registration.
     *
     * @param id the unique plugin ID
     * @param name the plugin display name
     * @param pluginDir the plugin directory
     * @param launchMode the launch mode for this plugin
     * @param enabled whether the plugin is enabled
     */
    public PluginRegistration(String id, String name, File pluginDir,
                              LaunchMode launchMode, boolean enabled) {
        this.id = id;
        this.name = name;
        this.pluginDir = pluginDir;
        this.launchMode = launchMode;
        this.enabled = enabled;
        this.registered = LocalDateTime.now();
        this.userPreferences = new HashMap<>();
    }

    /**
     * Default constructor for deserialization.
     */
    public PluginRegistration() {
        this.userPreferences = new HashMap<>();
    }

    // ========== Getters and Setters ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getPluginDir() {
        return pluginDir;
    }

    public void setPluginDir(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(LaunchMode launchMode) {
        this.launchMode = launchMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getRegistered() {
        return registered;
    }

    public void setRegistered(LocalDateTime registered) {
        this.registered = registered;
    }

    public Map<String, Object> getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(Map<String, Object> userPreferences) {
        this.userPreferences = userPreferences;
    }

    // ========== Convenience Methods ==========

    /**
     * Sets a user preference value.
     *
     * @param key the preference key
     * @param value the preference value
     */
    public void setPreference(String key, Object value) {
        this.userPreferences.put(key, value);
    }

    /**
     * Gets a user preference value.
     *
     * @param key the preference key
     * @return the preference value, or null if not found
     */
    public Object getPreference(String key) {
        return this.userPreferences.get(key);
    }

    /**
     * Gets a user preference value with a default.
     *
     * @param key the preference key
     * @param defaultValue the default value if preference not found
     * @return the preference value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getPreference(String key, T defaultValue) {
        Object value = this.userPreferences.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    /**
     * Checks if a user preference exists.
     *
     * @param key the preference key
     * @return true if preference exists
     */
    public boolean hasPreference(String key) {
        return this.userPreferences.containsKey(key);
    }

    /**
     * Removes a user preference.
     *
     * @param key the preference key
     */
    public void removePreference(String key) {
        this.userPreferences.remove(key);
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginRegistration that = (PluginRegistration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("PluginRegistration[id=%s, name=%s, enabled=%s, mode=%s]",
                id, name, enabled, launchMode);
    }
}
