package org.jwellman.foundation.plugin;

/**
 * Defines the execution strategy for plugin applications.
 * Each mode represents different trade-offs between isolation,
 * memory usage, and integration complexity.
 */
public enum LaunchMode {

    /**
     * Launch plugin in a separate JVM process.
     * - Maximum isolation (separate memory space, full GC isolation)
     * - Highest memory overhead (full JVM per plugin)
     * - Best for heavy applications (>20MB)
     * - Best for plugins with native libraries
     * - Requires inter-process communication
     */
    EXTERNAL_PROCESS,

    /**
     * Load plugin in same JVM with isolated classloader.
     * - Good isolation (prevents class conflicts)
     * - Moderate memory overhead (duplicated classes across plugins)
     * - Best for small-medium applications (<20MB)
     * - Direct Java method invocation possible
     * - Classloader can be garbage collected when plugin closes
     */
    ISOLATED_JVM,

    /**
     * Load plugin with hybrid classloader strategy.
     * - Shared classloader for common libraries (Guava, Commons, etc.)
     * - Isolated classloader for plugin-specific code
     * - Balanced memory usage (shared deps, isolated app code)
     * - Best for medium applications with common dependencies
     * - Reduces memory duplication for frequently-used libraries
     */
    SHARED_LIBS_JVM,

    /**
     * Load plugin in shared classloader (same as desktop application).
     * - No isolation (shares all classes with desktop)
     * - Minimal memory overhead
     * - Risk of class version conflicts
     * - Only for trusted, lightweight, compatible plugins
     * - NOT RECOMMENDED for 3rd party plugins
     */
    SHARED_JVM,

    /**
     * Automatically determine best launch mode based on plugin characteristics.
     * - Analyzes plugin size, dependencies, native libraries
     * - Selects appropriate mode at launch time
     * - Default recommendation for most plugins
     */
    AUTO
}
