package org.jwellman.foundation.plugin;

import java.io.File;
import java.util.Properties;

/**
 * Represents a plugin that has been discovered but not yet registered
 * in the plugin registry.
 *
 * <p>UnregisteredPlugin instances are created during the discovery process
 * when scanning the plugins directory for new applications. They contain
 * the basic metadata needed to present the plugin to the user for registration.</p>
 *
 * <p>After the user approves registration (potentially via a wizard UI),
 * the UnregisteredPlugin is converted to a {@link PluginRegistration} and
 * saved to the registry.</p>
 */
public class UnregisteredPlugin {

    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final File pluginDir;
    private final Properties properties;

    /**
     * Creates an unregistered plugin instance.
     *
     * @param id the plugin ID (typically directory name)
     * @param name the plugin display name
     * @param version the plugin version
     * @param description the plugin description (may be null)
     * @param pluginDir the plugin directory
     * @param properties the discovered properties from plugin.properties or manifest
     */
    public UnregisteredPlugin(String id, String name, String version,
                              String description, File pluginDir, Properties properties) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.pluginDir = pluginDir;
        this.properties = properties;
    }

    /**
     * Gets the plugin ID (unique identifier).
     * Typically derived from the plugin directory name.
     *
     * @return the plugin ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the plugin display name.
     *
     * @return the plugin name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin version.
     *
     * @return the plugin version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the plugin description.
     *
     * @return the plugin description, or null if not available
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the plugin directory containing the JAR and resources.
     *
     * @return the plugin directory
     */
    public File getPluginDir() {
        return pluginDir;
    }

    /**
     * Gets the discovered properties from plugin.properties or JAR manifest.
     *
     * @return the plugin properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets the suggested launch mode from the properties.
     * Returns AUTO if not specified.
     *
     * @return the suggested launch mode
     */
    public LaunchMode getSuggestedLaunchMode() {
        String mode = properties.getProperty("launchMode", "AUTO");
        try {
            return LaunchMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LaunchMode.AUTO;
        }
    }

    @Override
    public String toString() {
        return String.format("UnregisteredPlugin[id=%s, name=%s, version=%s, dir=%s]",
                id, name, version, pluginDir.getAbsolutePath());
    }
}
