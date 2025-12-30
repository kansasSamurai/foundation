package org.jwellman.foundation.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

/**
 * Descriptor for a plugin, loaded from plugin.properties file
 * or auto-generated from JAR manifest discovery.
 *
 * <p>A plugin descriptor contains all metadata needed to load and launch
 * a plugin application, including JAR location, main class, dependencies,
 * launch configuration, and UI integration metadata.</p>
 *
 * <p><strong>Example plugin.properties:</strong></p>
 * <pre>
 * # Plugin metadata
 * name=My Application
 * version=1.0.0
 * vendor=Third Party Inc.
 * description=A sample application
 *
 * # Launch configuration
 * jar=app.jar
 * mainClass=com.vendor.app.Main
 * launchMode=ISOLATED_JVM
 *
 * # Classpath (relative to plugin directory)
 * classpath=lib/dependency1.jar,lib/dependency2.jar
 *
 * # JVM arguments (for external process mode)
 * jvmArgs=-Xmx512m,-Dapp.mode=production
 *
 * # Application arguments
 * appArgs=--config,config.xml
 *
 * # UI Integration
 * icon=icon.png
 * windowTitle=My Application
 *
 * # Feature flags
 * allowMultipleInstances=false
 * </pre>
 */
public class PluginDescriptor {

    private final Properties properties;
    private final File pluginDir;

    /**
     * Creates a plugin descriptor from properties and plugin directory.
     *
     * @param properties the plugin configuration properties
     * @param pluginDir the plugin directory containing JAR and resources
     */
    public PluginDescriptor(Properties properties, File pluginDir) {
        this.properties = properties;
        this.pluginDir = pluginDir;
    }

    /**
     * Loads a plugin descriptor from the plugin directory.
     * Attempts to load from plugin.properties file, or creates
     * a minimal descriptor from JAR manifest discovery if not found.
     *
     * @param pluginDir the plugin directory
     * @return the loaded plugin descriptor
     * @throws IOException if descriptor cannot be loaded or created
     */
    public static PluginDescriptor load(File pluginDir) throws IOException {
        File descriptorFile = new File(pluginDir, "plugin.properties");
        Properties props = new Properties();

        if (descriptorFile.exists()) {
            // Load from explicit descriptor file
            try (FileInputStream fis = new FileInputStream(descriptorFile)) {
                props.load(fis);
            }
        } else {
            // Create minimal descriptor from discovery
            props = createDefaultDescriptor(pluginDir);
        }

        return new PluginDescriptor(props, pluginDir);
    }

    /**
     * Creates a default descriptor by discovering plugin information
     * from JAR manifest.
     *
     * @param pluginDir the plugin directory
     * @return default properties discovered from JAR
     * @throws IOException if no suitable JAR found or cannot read manifest
     */
    private static Properties createDefaultDescriptor(File pluginDir) throws IOException {
        // Look for single JAR file in directory
        File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jars == null || jars.length == 0) {
            throw new IOException("No JAR file found in plugin directory: " + pluginDir);
        }

        if (jars.length > 1) {
            throw new IOException("Multiple JAR files found, plugin.properties required to specify which to use");
        }

        File jarFile = jars[0];
        Properties props = new Properties();

        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();

            if (manifest != null) {
                Attributes attrs = manifest.getMainAttributes();
                String mainClass = attrs.getValue("Main-Class");

                if (mainClass == null) {
                    throw new IOException("No Main-Class found in JAR manifest: " + jarFile);
                }

                props.setProperty("jar", jarFile.getName());
                props.setProperty("mainClass", mainClass);

                // Optional manifest attributes
                String implTitle = attrs.getValue("Implementation-Title");
                if (implTitle != null) {
                    props.setProperty("name", implTitle);
                } else {
                    props.setProperty("name", jarFile.getName().replace(".jar", ""));
                }

                String implVersion = attrs.getValue("Implementation-Version");
                if (implVersion != null) {
                    props.setProperty("version", implVersion);
                }

                String implVendor = attrs.getValue("Implementation-Vendor");
                if (implVendor != null) {
                    props.setProperty("vendor", implVendor);
                }
            } else {
                throw new IOException("No manifest found in JAR: " + jarFile);
            }
        }

        return props;
    }

    // ========== Metadata Getters ==========

    public String getName() {
        return properties.getProperty("name", "Unknown Plugin");
    }

    public String getVersion() {
        return properties.getProperty("version", "0.0.0");
    }

    public String getVendor() {
        return properties.getProperty("vendor", "Unknown");
    }

    public String getDescription() {
        return properties.getProperty("description", "");
    }

    // ========== Launch Configuration Getters ==========

    public File getJarFile() {
        String jar = properties.getProperty("jar");
        if (jar == null) {
            throw new IllegalStateException("No jar property defined in plugin descriptor");
        }
        return new File(pluginDir, jar);
    }

    public String getMainClass() {
        String mainClass = properties.getProperty("mainClass");
        if (mainClass == null) {
            throw new IllegalStateException("No mainClass property defined in plugin descriptor");
        }
        return mainClass;
    }

    public LaunchMode getLaunchMode() {
        String mode = properties.getProperty("launchMode", "AUTO");
        try {
            return LaunchMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LaunchMode.AUTO;
        }
    }

    /**
     * Returns the classpath entries as a list of Files.
     * Classpath is defined as comma-separated relative paths in plugin.properties.
     *
     * @return list of classpath files (relative to plugin directory)
     */
    public List<File> getClasspath() {
        String classpath = properties.getProperty("classpath", "");
        if (classpath.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<File> classpathFiles = new ArrayList<>();
        String[] entries = classpath.split(",");

        for (String entry : entries) {
            String trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                classpathFiles.add(new File(pluginDir, trimmed));
            }
        }

        return classpathFiles;
    }

    /**
     * Returns JVM arguments for external process launch mode.
     * JVM args are defined as comma-separated values in plugin.properties.
     *
     * @return list of JVM arguments (e.g., "-Xmx512m", "-Dapp.mode=production")
     */
    public List<String> getJvmArgs() {
        String jvmArgs = properties.getProperty("jvmArgs", "");
        if (jvmArgs.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(jvmArgs.split(","));
    }

    /**
     * Returns application arguments to pass to main() method.
     * App args are defined as comma-separated values in plugin.properties.
     *
     * @return list of application arguments
     */
    public List<String> getAppArgs() {
        String appArgs = properties.getProperty("appArgs", "");
        if (appArgs.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(appArgs.split(","));
    }

    // ========== UI Integration Getters ==========

    public String getIcon() {
        return properties.getProperty("icon");
    }

    public String getWindowTitle() {
        return properties.getProperty("windowTitle", getName());
    }

    // ========== Feature Flags ==========

    public boolean isAllowMultipleInstances() {
        return Boolean.parseBoolean(properties.getProperty("allowMultipleInstances", "false"));
    }

    // ========== General Accessors ==========

    public File getPluginDir() {
        return pluginDir;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets a custom property value from the descriptor.
     * Allows plugins to define additional metadata beyond the standard properties.
     *
     * @param key the property key
     * @param defaultValue the default value if property not found
     * @return the property value or default
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String toString() {
        return String.format("PluginDescriptor[name=%s, version=%s, jar=%s, mainClass=%s]",
                getName(), getVersion(), getJarFile().getName(), getMainClass());
    }
}
