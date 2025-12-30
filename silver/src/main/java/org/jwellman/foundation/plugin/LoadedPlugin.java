package org.jwellman.foundation.plugin;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * Represents a loaded and running plugin instance.
 *
 * <p>LoadedPlugin encapsulates the runtime state of a plugin, including
 * its classloader (for in-JVM modes), process reference (for external process mode),
 * and entry point class.</p>
 *
 * <p>The plugin can be executed via {@link #run(String[])} and cleaned up
 * via {@link #close()}.</p>
 */
public class LoadedPlugin {

    private final String pluginId;
    private final LaunchMode launchMode;
    private final URLClassLoader classLoader;
    private final Process process;
    private final Class<?> entryPointClass;
    private final PluginDescriptor descriptor;

    /**
     * Creates a loaded plugin for in-JVM execution modes.
     *
     * @param pluginId the plugin ID
     * @param launchMode the launch mode used
     * @param classLoader the classloader containing plugin classes
     * @param entryPointClass the main class to execute (may be null)
     * @param descriptor the plugin descriptor
     */
    public LoadedPlugin(String pluginId, LaunchMode launchMode, URLClassLoader classLoader,
                        Class<?> entryPointClass, PluginDescriptor descriptor) {
        this.pluginId = pluginId;
        this.launchMode = launchMode;
        this.classLoader = classLoader;
        this.process = null;
        this.entryPointClass = entryPointClass;
        this.descriptor = descriptor;
    }

    /**
     * Creates a loaded plugin for external process execution.
     *
     * @param pluginId the plugin ID
     * @param process the running process
     * @param descriptor the plugin descriptor
     */
    public LoadedPlugin(String pluginId, Process process, PluginDescriptor descriptor) {
        this.pluginId = pluginId;
        this.launchMode = LaunchMode.EXTERNAL_PROCESS;
        this.classLoader = null;
        this.process = process;
        this.entryPointClass = null;
        this.descriptor = descriptor;
    }

    /**
     * Executes the plugin's main method with the given arguments.
     *
     * @param args the arguments to pass to main()
     * @throws Exception if execution fails
     */
    public void run(String[] args) throws Exception {
        if (launchMode == LaunchMode.EXTERNAL_PROCESS) {
            // Process already started, nothing to do
            // (Process was started by launcher before LoadedPlugin creation)
            return;
        }

        if (entryPointClass == null) {
            throw new IllegalStateException("No entry point class available for plugin: " + pluginId);
        }

        // Invoke main(String[] args) method
        Method mainMethod = entryPointClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    /**
     * Closes and unloads the plugin, releasing resources.
     *
     * <p>For in-JVM modes, closes the classloader to allow garbage collection.
     * For external process mode, destroys the process.</p>
     *
     * @throws IOException if closing the classloader fails
     */
    public void close() throws IOException {
        if (classLoader != null) {
            classLoader.close();
        }

        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    /**
     * Forcibly terminates the plugin.
     *
     * <p>For external process mode, forcibly destroys the process.
     * For in-JVM modes, same as {@link #close()}.</p>
     *
     * @throws IOException if closing the classloader fails
     */
    public void forceClose() throws IOException {
        if (classLoader != null) {
            classLoader.close();
        }

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    // ========== Status Methods ==========

    /**
     * Checks if the plugin is still running.
     *
     * @return true if plugin is running
     */
    public boolean isRunning() {
        if (process != null) {
            return process.isAlive();
        }

        // For in-JVM modes, consider running if classloader not closed
        if (classLoader != null) {
            // URLClassLoader doesn't expose isClosed(), so we assume running
            return true;
        }

        return false;
    }

    /**
     * Gets the process exit code (for external process mode only).
     *
     * @return the exit code, or null if process is still running or not in external process mode
     */
    public Integer getExitCode() {
        if (process != null && !process.isAlive()) {
            return process.exitValue();
        }
        return null;
    }

    // ========== Getters ==========

    public String getPluginId() {
        return pluginId;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public Process getProcess() {
        return process;
    }

    public Class<?> getEntryPointClass() {
        return entryPointClass;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return String.format("LoadedPlugin[id=%s, mode=%s, running=%s]",
                pluginId, launchMode, isRunning());
    }
}
