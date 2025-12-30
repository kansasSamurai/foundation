package org.jwellman.foundation.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher for plugin applications in various execution modes.
 *
 * <p>The launcher supports multiple execution strategies based on {@link LaunchMode}:</p>
 * <ul>
 *   <li><strong>EXTERNAL_PROCESS</strong> - Separate JVM process</li>
 *   <li><strong>ISOLATED_JVM</strong> - Same JVM, isolated classloader</li>
 *   <li><strong>SHARED_LIBS_JVM</strong> - Hybrid classloader with shared libraries</li>
 *   <li><strong>SHARED_JVM</strong> - Same JVM, shared classloader</li>
 *   <li><strong>AUTO</strong> - Automatically determine best mode</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * PluginLauncher launcher = new PluginLauncher();
 * PluginDescriptor descriptor = PluginDescriptor.load(pluginDir);
 *
 * LoadedPlugin plugin = launcher.launch("myapp", descriptor, LaunchMode.ISOLATED_JVM);
 * plugin.run(new String[] {"--config", "app.properties"});
 * </pre>
 */
public class PluginLauncher {

    private static final Logger logger = LoggerFactory.getLogger(PluginLauncher.class);

    // Size threshold for AUTO mode decision (20 MB)
    private static final long LARGE_PLUGIN_THRESHOLD = 20_000_000;

    /**
     * Launches a plugin in the specified launch mode.
     *
     * @param pluginId the plugin ID
     * @param descriptor the plugin descriptor
     * @param launchMode the launch mode to use
     * @return the loaded plugin instance
     * @throws Exception if plugin launch fails
     */
    public LoadedPlugin launch(String pluginId, PluginDescriptor descriptor, LaunchMode launchMode)
            throws Exception {

        // Resolve AUTO mode to concrete mode
        if (launchMode == LaunchMode.AUTO) {
            launchMode = determineBestLaunchMode(descriptor);
            logger.info("AUTO mode resolved to {} for plugin: {}", launchMode, pluginId);
        }

        logger.info("Launching plugin {} in {} mode", pluginId, launchMode);

        switch (launchMode) {
            case EXTERNAL_PROCESS:
                return launchExternalProcess(pluginId, descriptor);

            case ISOLATED_JVM:
                return launchIsolatedJVM(pluginId, descriptor);

            case SHARED_LIBS_JVM:
                return launchSharedLibsJVM(pluginId, descriptor);

            case SHARED_JVM:
                return launchSharedJVM(pluginId, descriptor);

            default:
                throw new IllegalArgumentException("Unsupported launch mode: " + launchMode);
        }
    }

    // ========== Launch Mode Implementations ==========

    /**
     * Launches plugin in a separate JVM process.
     *
     * @param pluginId the plugin ID
     * @param descriptor the plugin descriptor
     * @return loaded plugin with process reference
     * @throws IOException if process creation fails
     */
    private LoadedPlugin launchExternalProcess(String pluginId, PluginDescriptor descriptor)
            throws IOException {

        List<String> command = new ArrayList<>();

        // Java executable
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        command.add(javaBin);

        // JVM arguments
        List<String> jvmArgs = descriptor.getJvmArgs();
        command.addAll(jvmArgs);

        // Classpath
        StringBuilder classpath = new StringBuilder();
        classpath.append(descriptor.getJarFile().getAbsolutePath());

        for (File cpEntry : descriptor.getClasspath()) {
            classpath.append(File.pathSeparator);
            classpath.append(cpEntry.getAbsolutePath());
        }

        command.add("-cp");
        command.add(classpath.toString());

        // Main class
        command.add(descriptor.getMainClass());

        // Application arguments
        List<String> appArgs = descriptor.getAppArgs();
        command.addAll(appArgs);

        logger.debug("Launching external process: {}", command);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(descriptor.getPluginDir());

        // Inherit I/O streams (can be customized later)
        pb.inheritIO();

        Process process = pb.start();
        logger.info("Started external process for plugin: {} (PID: {})", pluginId, getProcessId(process));

        return new LoadedPlugin(pluginId, process, descriptor);
    }

    /**
     * Launches plugin with isolated classloader in same JVM.
     *
     * @param pluginId the plugin ID
     * @param descriptor the plugin descriptor
     * @return loaded plugin with isolated classloader
     * @throws Exception if plugin loading fails
     */
    private LoadedPlugin launchIsolatedJVM(String pluginId, PluginDescriptor descriptor)
            throws Exception {

        // Build classpath URLs
        List<URL> urls = new ArrayList<>();
        urls.add(descriptor.getJarFile().toURI().toURL());

        for (File cpEntry : descriptor.getClasspath()) {
            urls.add(cpEntry.toURI().toURL());
        }

        logger.debug("Creating isolated classloader for {} with {} classpath entries",
                pluginId, urls.size());

        // Create isolated classloader with null parent (bootstrap only)
        // This provides maximum isolation from application classes
        URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                null  // null parent = bootstrap classloader only
        );

        // Load main class
        String mainClassName = descriptor.getMainClass();
        Class<?> mainClass = classLoader.loadClass(mainClassName);

        logger.info("Loaded plugin {} with isolated classloader", pluginId);

        return new LoadedPlugin(pluginId, LaunchMode.ISOLATED_JVM, classLoader, mainClass, descriptor);
    }

    /**
     * Launches plugin with hybrid classloader (shared libraries + isolated app code).
     *
     * <p>NOTE: This is a simplified implementation. A full implementation would
     * require a custom classloader that delegates common libraries to a shared
     * classloader while isolating plugin-specific code.</p>
     *
     * @param pluginId the plugin ID
     * @param descriptor the plugin descriptor
     * @return loaded plugin with hybrid classloader
     * @throws Exception if plugin loading fails
     */
    private LoadedPlugin launchSharedLibsJVM(String pluginId, PluginDescriptor descriptor)
            throws Exception {

        // For now, use isolated JVM approach
        // TODO: Implement SharedLibraryClassLoader with configurable shared packages
        logger.warn("SHARED_LIBS_JVM mode not fully implemented, using ISOLATED_JVM for: {}", pluginId);
        return launchIsolatedJVM(pluginId, descriptor);
    }

    /**
     * Launches plugin in shared classloader (same as application).
     *
     * <p><strong>WARNING:</strong> This mode provides no isolation and can cause
     * class version conflicts. Only use for trusted, compatible plugins.</p>
     *
     * @param pluginId the plugin ID
     * @param descriptor the plugin descriptor
     * @return loaded plugin with shared classloader
     * @throws Exception if plugin loading fails
     */
    private LoadedPlugin launchSharedJVM(String pluginId, PluginDescriptor descriptor)
            throws Exception {

        // Build classpath URLs
        List<URL> urls = new ArrayList<>();
        urls.add(descriptor.getJarFile().toURI().toURL());

        for (File cpEntry : descriptor.getClasspath()) {
            urls.add(cpEntry.toURI().toURL());
        }

        logger.warn("Launching {} in SHARED_JVM mode (no isolation)", pluginId);

        // Use application classloader as parent
        URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                getClass().getClassLoader()
        );

        // Load main class
        String mainClassName = descriptor.getMainClass();
        Class<?> mainClass = classLoader.loadClass(mainClassName);

        logger.info("Loaded plugin {} with shared classloader", pluginId);

        return new LoadedPlugin(pluginId, LaunchMode.SHARED_JVM, classLoader, mainClass, descriptor);
    }

    // ========== AUTO Mode Decision Logic ==========

    /**
     * Determines the best launch mode for a plugin based on characteristics.
     *
     * <p>Decision criteria:</p>
     * <ul>
     *   <li>Large plugins (&gt;20MB) → EXTERNAL_PROCESS</li>
     *   <li>Plugins with native libraries → EXTERNAL_PROCESS</li>
     *   <li>Small-medium plugins → ISOLATED_JVM</li>
     * </ul>
     *
     * @param descriptor the plugin descriptor
     * @return the recommended launch mode
     */
    private LaunchMode determineBestLaunchMode(PluginDescriptor descriptor) {
        // Calculate total size
        long totalSize = calculateTotalSize(descriptor);

        logger.debug("Plugin total size: {} bytes", totalSize);

        // Large plugins should use external process
        if (totalSize > LARGE_PLUGIN_THRESHOLD) {
            logger.debug("Plugin exceeds size threshold, recommending EXTERNAL_PROCESS");
            return LaunchMode.EXTERNAL_PROCESS;
        }

        // Check for native libraries
        if (hasNativeLibraries(descriptor)) {
            logger.debug("Native libraries detected, recommending EXTERNAL_PROCESS");
            return LaunchMode.EXTERNAL_PROCESS;
        }

        // Default to isolated JVM for small-medium plugins
        logger.debug("Recommending ISOLATED_JVM for plugin");
        return LaunchMode.ISOLATED_JVM;
    }

    /**
     * Calculates total size of plugin JAR and dependencies.
     *
     * @param descriptor the plugin descriptor
     * @return total size in bytes
     */
    private long calculateTotalSize(PluginDescriptor descriptor) {
        long totalSize = 0;

        File jarFile = descriptor.getJarFile();
        if (jarFile.exists()) {
            totalSize += jarFile.length();
        }

        for (File cpEntry : descriptor.getClasspath()) {
            if (cpEntry.exists()) {
                totalSize += cpEntry.length();
            }
        }

        return totalSize;
    }

    /**
     * Checks if plugin contains native libraries (.dll, .so, .dylib).
     *
     * @param descriptor the plugin descriptor
     * @return true if native libraries detected
     */
    private boolean hasNativeLibraries(PluginDescriptor descriptor) {
        File pluginDir = descriptor.getPluginDir();

        // Check for common native library extensions
        String[] nativeExtensions = {".dll", ".so", ".dylib", ".jnilib"};

        return hasFilesWithExtensions(pluginDir, nativeExtensions);
    }

    /**
     * Recursively checks directory for files with specified extensions.
     *
     * @param dir the directory to check
     * @param extensions the file extensions to look for
     * @return true if any matching files found
     */
    private boolean hasFilesWithExtensions(File dir, String[] extensions) {
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (hasFilesWithExtensions(file, extensions)) {
                    return true;
                }
            } else {
                String fileName = file.getName().toLowerCase();
                for (String ext : extensions) {
                    if (fileName.endsWith(ext)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Gets the process ID for logging purposes.
     * Returns "unknown" on Java 8 (Process.pid() added in Java 9).
     *
     * @param process the process
     * @return process ID as string
     */
    private String getProcessId(Process process) {
        // Java 8 doesn't have Process.pid()
        // Could use reflection to call it on Java 9+, but for simplicity just return "unknown"
        return "unknown";
    }
}
