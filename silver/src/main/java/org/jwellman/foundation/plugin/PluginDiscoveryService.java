package org.jwellman.foundation.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Service for discovering unregistered plugins in the plugins directory.
 *
 * <p>The discovery service scans the configured plugins directory for
 * subdirectories containing plugin applications. Each subdirectory is
 * examined for plugin metadata using multiple discovery strategies:</p>
 *
 * <ol>
 *   <li><strong>Explicit descriptor</strong> - Looks for plugin.properties file</li>
 *   <li><strong>JAR manifest</strong> - Discovers Main-Class from JAR manifest if single JAR present</li>
 *   <li><strong>Convention-based</strong> - Uses directory name and JAR name for defaults</li>
 * </ol>
 *
 * <p>Plugins already registered in the {@link PluginRegistry} are skipped during
 * discovery to avoid duplicate registration prompts.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * PluginDiscoveryService discovery = new PluginDiscoveryService(
 *     new File("./plugins"),
 *     pluginRegistry
 * );
 *
 * List&lt;UnregisteredPlugin&gt; newPlugins = discovery.scanForNewPlugins();
 * for (UnregisteredPlugin plugin : newPlugins) {
 *     // Present to user for registration...
 * }
 * </pre>
 */
public class PluginDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(PluginDiscoveryService.class);

    private final File pluginsDir;
    private final PluginRegistry registry;

    /**
     * Creates a plugin discovery service.
     *
     * @param pluginsDir the plugins directory to scan
     * @param registry the plugin registry (to check for already registered plugins)
     */
    public PluginDiscoveryService(File pluginsDir, PluginRegistry registry) {
        this.pluginsDir = pluginsDir;
        this.registry = registry;
    }

    /**
     * Scans the plugins directory for new, unregistered plugins.
     *
     * @return list of discovered unregistered plugins
     */
    public List<UnregisteredPlugin> scanForNewPlugins() {
        List<UnregisteredPlugin> discovered = new ArrayList<>();

        if (!pluginsDir.exists()) {
            logger.warn("Plugins directory does not exist: {}", pluginsDir.getAbsolutePath());
            return discovered;
        }

        if (!pluginsDir.isDirectory()) {
            logger.warn("Plugins path is not a directory: {}", pluginsDir.getAbsolutePath());
            return discovered;
        }

        File[] pluginDirs = pluginsDir.listFiles(File::isDirectory);
        if (pluginDirs == null || pluginDirs.length == 0) {
            logger.debug("No plugin directories found in: {}", pluginsDir.getAbsolutePath());
            return discovered;
        }

        logger.info("Scanning {} for new plugins...", pluginsDir.getAbsolutePath());

        for (File pluginDir : pluginDirs) {
            String pluginId = pluginDir.getName();

            // Check if already registered
            if (registry.isRegistered(pluginId)) {
                logger.debug("Skipping already registered plugin: {}", pluginId);
                continue;
            }

            // Try to discover plugin
            UnregisteredPlugin plugin = discoverPlugin(pluginDir);
            if (plugin != null) {
                discovered.add(plugin);
                logger.info("Discovered new plugin: {} ({})", plugin.getName(), pluginId);
            } else {
                logger.debug("Could not discover plugin metadata in: {}", pluginDir.getName());
            }
        }

        logger.info("Discovery complete. Found {} new plugin(s)", discovered.size());
        return discovered;
    }

    /**
     * Attempts to discover plugin metadata from a plugin directory.
     *
     * @param pluginDir the plugin directory
     * @return unregistered plugin instance, or null if discovery fails
     */
    private UnregisteredPlugin discoverPlugin(File pluginDir) {
        String pluginId = pluginDir.getName();

        // Strategy 1: Look for plugin.properties descriptor
        File descriptor = new File(pluginDir, "plugin.properties");
        if (descriptor.exists()) {
            logger.debug("Found plugin.properties for: {}", pluginId);
            return loadFromDescriptor(pluginDir, descriptor);
        }

        // Strategy 2: Look for single JAR file
        File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars != null && jars.length == 1) {
            logger.debug("Found single JAR for {}, attempting manifest discovery", pluginId);
            return discoverFromJar(pluginDir, jars[0]);
        }

        // Strategy 3: Multiple JARs found, cannot auto-discover
        if (jars != null && jars.length > 1) {
            logger.warn("Multiple JARs found in {}, plugin.properties required", pluginId);
        }

        return null;
    }

    /**
     * Loads plugin metadata from a plugin.properties descriptor file.
     *
     * @param pluginDir the plugin directory
     * @param descriptor the plugin.properties file
     * @return unregistered plugin instance, or null if loading fails
     */
    private UnregisteredPlugin loadFromDescriptor(File pluginDir, File descriptor) {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(descriptor)) {
                props.load(fis);
            }

            String pluginId = pluginDir.getName();
            String name = props.getProperty("name", pluginId);
            String version = props.getProperty("version", "0.0.0");
            String description = props.getProperty("description");

            return new UnregisteredPlugin(
                pluginId,
                name,
                version,
                description,
                pluginDir,
                props
            );

        } catch (IOException e) {
            logger.error("Failed to load plugin.properties from: {}", descriptor.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Discovers plugin metadata from a JAR file manifest.
     *
     * @param pluginDir the plugin directory
     * @param jarFile the JAR file
     * @return unregistered plugin instance, or null if discovery fails
     */
    private UnregisteredPlugin discoverFromJar(File pluginDir, File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();

            if (manifest == null) {
                logger.warn("No manifest found in JAR: {}", jarFile.getName());
                return null;
            }

            Attributes attrs = manifest.getMainAttributes();
            String mainClass = attrs.getValue("Main-Class");

            if (mainClass == null) {
                logger.warn("No Main-Class found in manifest: {}", jarFile.getName());
                return null;
            }

            // Create minimal properties from JAR manifest
            Properties props = new Properties();
            props.setProperty("jar", jarFile.getName());
            props.setProperty("mainClass", mainClass);

            String pluginId = pluginDir.getName();
            String name = attrs.getValue("Implementation-Title");
            if (name == null) {
                name = jarFile.getName().replace(".jar", "");
            }
            props.setProperty("name", name);

            String version = attrs.getValue("Implementation-Version");
            if (version == null) {
                version = "0.0.0";
            }
            props.setProperty("version", version);

            String vendor = attrs.getValue("Implementation-Vendor");
            if (vendor != null) {
                props.setProperty("vendor", vendor);
            }

            String description = attrs.getValue("Implementation-Description");
            if (description != null) {
                props.setProperty("description", description);
            }

            return new UnregisteredPlugin(
                pluginId,
                name,
                version,
                description,
                pluginDir,
                props
            );

        } catch (IOException e) {
            logger.error("Failed to read JAR manifest from: {}", jarFile.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Rescans the plugins directory for new plugins.
     * Alias for {@link #scanForNewPlugins()}.
     *
     * @return list of discovered unregistered plugins
     */
    public List<UnregisteredPlugin> rescan() {
        return scanForNewPlugins();
    }

    /**
     * Gets the configured plugins directory.
     *
     * @return the plugins directory
     */
    public File getPluginsDir() {
        return pluginsDir;
    }

    /**
     * Gets the plugin registry used for checking registered plugins.
     *
     * @return the plugin registry
     */
    public PluginRegistry getRegistry() {
        return registry;
    }
}
