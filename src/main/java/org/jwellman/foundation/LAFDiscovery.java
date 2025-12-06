package org.jwellman.foundation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Discovers Look and Feel implementations at runtime.
 *
 * Discovery strategies:
 * 1. Check UIManager for installed LAFs (built-in Java LAFs)
 * 2. Scan ./lafs/ directory for LAF JARs with optional metadata
 * 3. Scan classpath for known LAF classes
 *
 * LAF JAR Metadata (optional):
 * JARs can include META-INF/foundation-laf.properties with:
 *   laf.class=com.example.MyLookAndFeel
 *   laf.name=My Beautiful LAF
 *   laf.description=A beautiful look and feel
 *
 * If no metadata exists, the JAR will be scanned for classes extending LookAndFeel.
 *
 * @author Foundation Framework
 */
public class LAFDiscovery {

    /** Directory to scan for LAF JARs */
    private static final String LAF_DIRECTORY = "./lafs";

    /** Metadata file path within JARs */
    private static final String METADATA_PATH = "META-INF/foundation-laf.properties";

    /** Configuration file path */
    private static final String CONFIG_FILE_PATH = LAF_DIRECTORY + "/foundation.properties";

    /**
     * Represents a discovered Look and Feel.
     */
    public static class LAFInfo {
        private final String className;
        private final String name;
        private final String description;
        private final ClassLoader classLoader;

        public LAFInfo(String className, String name, String description, ClassLoader classLoader) {
            this.className = className;
            this.name = name;
            this.description = description;
            this.classLoader = classLoader;
        }

        public String getClassName() { return className; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public ClassLoader getClassLoader() { return classLoader; }

        @Override
        public String toString() {
            return name + " (" + className + ")";
        }
    }

    /**
     * Discovers all available Look and Feels.
     *
     * @return List of discovered LAFs
     */
    public static List<LAFInfo> discoverLookAndFeels() {
        List<LAFInfo> lafs = new ArrayList<>();

        // Strategy 1: Built-in LAFs
        lafs.addAll(discoverBuiltInLAFs());

        // Strategy 2: LAFs in ./lafs/ directory
        lafs.addAll(discoverDirectoryLAFs());

        // Strategy 3: Known LAFs on classpath
        lafs.addAll(discoverClasspathLAFs());

        return lafs;
    }

    /**
     * Discovers built-in Java Look and Feels.
     */
    private static List<LAFInfo> discoverBuiltInLAFs() {
        List<LAFInfo> lafs = new ArrayList<>();

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            lafs.add(new LAFInfo(
                info.getClassName(),
                info.getName(),
                "Built-in Java Look and Feel",
                ClassLoader.getSystemClassLoader()
            ));
        }

        return lafs;
    }

    /**
     * Discovers LAFs in the designated directory.
     */
    private static List<LAFInfo> discoverDirectoryLAFs() {
        List<LAFInfo> lafs = new ArrayList<>();

        File lafDir = new File(LAF_DIRECTORY);
        if (!lafDir.exists() || !lafDir.isDirectory()) {
            System.out.println("LAF directory not found: " + LAF_DIRECTORY);
            return lafs;
        }

        File[] jarFiles = lafDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No JAR files found in: " + LAF_DIRECTORY);
            return lafs;
        }

        for (File jarFile : jarFiles) {
            System.out.println("Scanning JAR: " + jarFile.getName());
            lafs.addAll(discoverLAFsInJar(jarFile));
        }

        return lafs;
    }

    /**
     * Discovers LAFs within a specific JAR file.
     */
    private static List<LAFInfo> discoverLAFsInJar(File jarFile) {
        List<LAFInfo> lafs = new ArrayList<>();

        try {
            // Create a classloader for this JAR
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl},
                                                             ClassLoader.getSystemClassLoader());

            // First, try to find metadata file
            LAFInfo metadataLAF = discoverFromMetadata(jarFile, classLoader);
            if (metadataLAF != null) {
                lafs.add(metadataLAF);
                System.out.println("  Found LAF via metadata: " + metadataLAF.getName());
            } else {
                // Fallback: Scan for LookAndFeel classes
                lafs.addAll(scanJarForLAFs(jarFile, classLoader));
            }

        } catch (Exception e) {
            System.err.println("Error processing JAR " + jarFile.getName() + ": " + e.getMessage());
        }

        return lafs;
    }

    /**
     * Attempts to discover LAF from metadata file in JAR.
     */
    private static LAFInfo discoverFromMetadata(File jarFile, URLClassLoader classLoader) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry metadataEntry = jar.getJarEntry(METADATA_PATH);
            if (metadataEntry == null) {
                return null; // No metadata file
            }

            Properties props = new Properties();
            try (InputStream is = jar.getInputStream(metadataEntry)) {
                props.load(is);
            }

            String className = props.getProperty("laf.class");
            String name = props.getProperty("laf.name", className);
            String description = props.getProperty("laf.description", "");

            if (className != null && !className.isEmpty()) {
                return new LAFInfo(className, name, description, classLoader);
            }

        } catch (IOException e) {
            System.err.println("Error reading metadata from " + jarFile.getName() + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Scans JAR for classes extending LookAndFeel.
     */
    private static List<LAFInfo> scanJarForLAFs(File jarFile, URLClassLoader classLoader) {
        List<LAFInfo> lafs = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Only check .class files
                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.')
                                          .substring(0, name.length() - 6);

                    // Try to load and check if it extends LookAndFeel
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (LookAndFeel.class.isAssignableFrom(clazz) &&
                            !clazz.isInterface() &&
                            !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {

                            String simpleName = clazz.getSimpleName();
                            System.out.println("  Found LAF class: " + className);
                            lafs.add(new LAFInfo(
                                className,
                                simpleName,
                                "Discovered from " + jarFile.getName(),
                                classLoader
                            ));
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        // Class not loadable, skip it
                    } catch (Exception e) {
                        // Other error, log and continue
                        System.err.println("  Error checking class " + className + ": " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error scanning JAR " + jarFile.getName() + ": " + e.getMessage());
        }

        return lafs;
    }

    /**
     * Discovers known LAFs that are on the classpath.
     * This is a fallback for commonly used LAFs.
     */
    private static List<LAFInfo> discoverClasspathLAFs() {
        List<LAFInfo> lafs = new ArrayList<>();

        // List of well-known LAF class names
        String[][] knownLAFs = {
            {"com.alee.laf.WebLookAndFeel", "WebLAF"},
            {"net.sourceforge.napkinlaf.NapkinLookAndFeel", "NapkinLAF"},
            {"com.nilo.plaf.nimrod.NimRODLookAndFeel", "NimROD"},
            {"com.jtattoo.plaf.acryl.AcrylLookAndFeel", "JTattoo Acryl"},
            {"com.jtattoo.plaf.smart.SmartLookAndFeel", "JTattoo Smart"},
            {"com.bulenkov.darcula.DarculaLaf", "Darcula"},
            {"com.formdev.flatlaf.FlatLightLaf", "FlatLaf Light"},
            {"com.formdev.flatlaf.FlatDarkLaf", "FlatLaf Dark"}
        };

        for (String[] lafInfo : knownLAFs) {
            String className = lafInfo[0];
            String name = lafInfo[1];

            try {
                Class.forName(className);
                // Class exists on classpath
                lafs.add(new LAFInfo(
                    className,
                    name,
                    "Found on classpath",
                    ClassLoader.getSystemClassLoader()
                ));
            } catch (ClassNotFoundException e) {
                // LAF not available, skip
            }
        }

        return lafs;
    }

    /**
     * Selects and applies a Look and Feel using Foundation's priority system.
     *
     * Priority order:
     * 1. If preferredLAFClassName is specified, use that LAF
     * 2. LAF specified in ./lafs/foundation.properties config file
     * 3. First LAF found in ./lafs/ directory
     * 4. Nimbus (built-in default)
     *
     * @param preferredLAFClassName Optional LAF class name (can be null)
     * @return true if a LAF was successfully applied, false otherwise
     */
    public static boolean selectAndApplyLookAndFeel(String preferredLAFClassName) {
        System.out.println("=== Foundation LAF Selection ===");

        // Discover all available LAFs
        List<LAFInfo> lafs = discoverLookAndFeels();
        System.out.println("Discovered " + lafs.size() + " Look and Feels");

        // Generate default config if it doesn't exist
        generateDefaultConfig(lafs);

        LAFInfo selectedLAF = null;
        String selectionReason = "";

        // Priority 1: Use preferred LAF from context if specified
        if (preferredLAFClassName != null && !preferredLAFClassName.trim().isEmpty()) {
            selectedLAF = findLAFByClassName(lafs, preferredLAFClassName);
            if (selectedLAF != null) {
                selectionReason = "Specified in uContext: " + preferredLAFClassName;
                System.out.println("Using LAF from context: " + selectedLAF.getName());
            } else {
                System.err.println("WARNING: Preferred LAF not found: " + preferredLAFClassName);
                System.out.println("Falling back to default selection...");
            }
        }

        // Priority 2: Check config file (if not already selected)
        if (selectedLAF == null) {
            Properties config = loadConfig();
            if (config != null) {
                String configuredClassName = config.getProperty("laf.class");
                if (configuredClassName != null && !configuredClassName.trim().isEmpty()) {
                    selectedLAF = findLAFByClassName(lafs, configuredClassName);
                    if (selectedLAF != null) {
                        selectionReason = "Specified in " + CONFIG_FILE_PATH;
                        System.out.println("Using LAF from config: " + selectedLAF.getName());
                    } else {
                        System.err.println("WARNING: Configured LAF not found: " + configuredClassName);
                        System.out.println("Falling back to default selection...");
                    }
                }
            }
        }

        // Priority 3: First LAF from ./lafs/ directory (if not already selected)
        if (selectedLAF == null) {
            List<LAFInfo> directoryLAFs = discoverDirectoryLAFs();
            if (!directoryLAFs.isEmpty()) {
                selectedLAF = directoryLAFs.get(0);
                selectionReason = "First LAF found in " + LAF_DIRECTORY + " directory";
                System.out.println("Using LAF from directory: " + selectedLAF.getName());
            }
        }

        // Priority 4: Nimbus (built-in default - looks better than Windows Classic)
        if (selectedLAF == null) {
            selectedLAF = findLAFByClassName(lafs, "javax.swing.plaf.nimbus.NimbusLookAndFeel");
            if (selectedLAF != null) {
                selectionReason = "Nimbus (built-in default)";
                System.out.println(selectionReason);
            } else {
                // Fallback to system default if Nimbus not found (shouldn't happen)
                selectedLAF = getSystemLAF();
                if (selectedLAF != null) {
                    selectionReason = "System default (Nimbus not available)";
                    System.out.println(selectionReason);
                } else {
                    System.err.println("ERROR: Could not determine any LAF!");
                    return false;
                }
            }
        }

        // Apply the selected LAF
        boolean success = applyLookAndFeel(selectedLAF);
        if (success) {
            System.out.println("LAF Applied: " + selectedLAF.getName());
            System.out.println("Selection Reason: " + selectionReason);
        }

        return success;
    }

    /**
     * Applies a discovered LAF.
     *
     * @param lafInfo The LAF to apply
     * @return true if successful, false otherwise
     */
    public static boolean applyLookAndFeel(LAFInfo lafInfo) {
        try {
            // Use the LAF's classloader to instantiate it
            Class<?> lafClass = lafInfo.getClassLoader().loadClass(lafInfo.getClassName());
            LookAndFeel laf = (LookAndFeel) lafClass.newInstance();
            UIManager.setLookAndFeel(laf);
            System.out.println("Successfully applied LAF: " + lafInfo.getName());
            return true;
        } catch (Exception e) {
            System.err.println("Error applying LAF " + lafInfo.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to find and apply the system Look and Feel.
     *
     * @return LAFInfo for system LAF, or null if not found
     */
    public static LAFInfo getSystemLAF() {
        try {
            String systemLAFClassName = UIManager.getSystemLookAndFeelClassName();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getClassName().equals(systemLAFClassName)) {
                    return new LAFInfo(
                        info.getClassName(),
                        info.getName() + " (System)",
                        "System default Look and Feel",
                        ClassLoader.getSystemClassLoader()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error determining system LAF: " + e.getMessage());
        }
        return null;
    }

    /**
     * Loads the LAF configuration from the config file.
     *
     * @return Properties object, or null if config file doesn't exist
     */
    private static Properties loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(configFile)) {
            props.load(is);
            System.out.println("Loaded configuration from: " + CONFIG_FILE_PATH);
            return props;
        } catch (IOException e) {
            System.err.println("Error loading config file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Finds a LAF by its class name in the list of discovered LAFs.
     *
     * @param lafs List of discovered LAFs
     * @param className The class name to search for
     * @return The matching LAFInfo, or null if not found
     */
    private static LAFInfo findLAFByClassName(List<LAFInfo> lafs, String className) {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }

        for (LAFInfo laf : lafs) {
            if (laf.getClassName().equals(className.trim())) {
                return laf;
            }
        }
        return null;
    }

    /**
     * Generates a default configuration file with all discovered LAFs listed as examples.
     *
     * @param lafs List of discovered LAFs to include in the config
     */
    private static void generateDefaultConfig(List<LAFInfo> lafs) {
        File lafDir = new File(LAF_DIRECTORY);
        if (!lafDir.exists()) {
            lafDir.mkdirs();
            System.out.println("Created directory: " + LAF_DIRECTORY);
        }

        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            // Don't overwrite existing config
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            writer.println("# Foundation Look and Feel Configuration");
            writer.println("# Specify the LAF class name you want to use");
            writer.println("# Uncomment and set the laf.class property to your desired LAF");
            writer.println();
            writer.println("# Example:");
            writer.println("# laf.class=com.formdev.flatlaf.FlatDarkLaf");
            writer.println();
            writer.println("laf.class=");
            writer.println();
            writer.println("# Available Look and Feels discovered:");

            if (lafs.isEmpty()) {
                writer.println("# (No LAFs discovered)");
            } else {
                for (LAFInfo laf : lafs) {
                    writer.println();
                    writer.println("# " + laf.getName());
                    writer.println("# laf.class=" + laf.getClassName());
                    writer.println("# Description: " + laf.getDescription());
                }
            }

            System.out.println("Generated default config file: " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error generating config file: " + e.getMessage());
        }
    }

    /**
     * Demo/test method.
     *
     * Discovers all available LAFs and prints them to console, then applies one according to priority:
     * 1. LAF specified in ./lafs/foundation.properties config file
     * 2. First LAF found in ./lafs/ directory
     * 3. System default LAF as fallback
     *
     * Then displays a demo window to showcase the selected LAF.
     */
    public static void main(String[] args) {
        System.out.println("Discovering Look and Feels...\n");

        // Discover all LAFs using all strategies
        List<LAFInfo> lafs = discoverLookAndFeels();

        // Generate default config file if it doesn't exist
        generateDefaultConfig(lafs);

        // Print all discovered LAFs to console
        System.out.println("\nDiscovered " + lafs.size() + " Look and Feels:");
        for (int i = 0; i < lafs.size(); i++) {
            LAFInfo laf = lafs.get(i);
            System.out.println((i + 1) + ". " + laf.getName());
            System.out.println("   Class: " + laf.getClassName());
            System.out.println("   Description: " + laf.getDescription());
        }

        // Now select which LAF to apply
        System.out.println("\n--- Selecting LAF to Apply ---");

        LAFInfo selectedLAF = null;
        String selectionReason = "";

        // Priority 1: Check config file
        Properties config = loadConfig();
        if (config != null) {
            String configuredClassName = config.getProperty("laf.class");
            if (configuredClassName != null && !configuredClassName.trim().isEmpty()) {
                selectedLAF = findLAFByClassName(lafs, configuredClassName);
                if (selectedLAF != null) {
                    selectionReason = "Specified in " + CONFIG_FILE_PATH;
                    System.out.println("Using LAF from config: " + selectedLAF.getName());
                } else {
                    System.err.println("WARNING: Configured LAF not found: " + configuredClassName);
                    System.out.println("Falling back to default selection...");
                }
            }
        }

        // Priority 2: First LAF from ./lafs/ directory (if not already selected)
        if (selectedLAF == null) {
            List<LAFInfo> directoryLAFs = discoverDirectoryLAFs();
            if (!directoryLAFs.isEmpty()) {
                selectedLAF = directoryLAFs.get(0);
                selectionReason = "First LAF found in " + LAF_DIRECTORY + " directory";
                System.out.println("Using LAF from directory: " + selectedLAF.getName());
            }
        }

        // Priority 3: System default LAF (if not already selected)
        if (selectedLAF == null) {
            selectedLAF = getSystemLAF();
            if (selectedLAF != null) {
                selectionReason = "System default (no LAFs found in " + LAF_DIRECTORY + ")";
                System.out.println(selectionReason);
            } else {
                System.err.println("ERROR: Could not determine system LAF!");
                return;
            }
        }

        // Use Foundation to create and display the window
        // TODO eventually we want to build discovery into init() but for now we just call init() before applyLookAndFeel()
        Foundation f = Foundation.init();

        // Apply the selected LAF
        if (!applyLookAndFeel(selectedLAF)) {
            System.err.println("ERROR: Failed to apply LAF. Exiting.");
            return;
        } else {
            // Create and display demo window
            final LAFInfo finalLAF = selectedLAF;
            final String finalReason = selectionReason;

            org.jwellman.foundation.swing.IWindow window = f.useWindow(showDemoWindow(finalLAF, finalReason));
            window.setTitle("Foundation LAF Discovery - " + finalLAF.getName());
            window.setResizable(true);
            f.showGUI(window);
        }


    }

    /**
     * Creates and displays a demo window showing the selected LAF.
     *
     * @param lafInfo The LAF that was applied
     * @param reason Why this LAF was selected
     */
    private static JPanel showDemoWindow(LAFInfo lafInfo, String reason) {

        // Create a JPanel with demo content
        javax.swing.JPanel demoPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        demoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        javax.swing.JLabel header = new javax.swing.JLabel("Foundation LAF Discovery Demo");
        header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        header.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        demoPanel.add(header, java.awt.BorderLayout.NORTH);

        // Content panel
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel lafLabel = new javax.swing.JLabel(
            "<html><b>Selected LAF:</b> " + lafInfo.getName() + "</html>"
        );
        lafLabel.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);

        javax.swing.JLabel classLabel = new javax.swing.JLabel(
            "<html><b>Class:</b> " + lafInfo.getClassName() + "</html>"
        );
        classLabel.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);

        javax.swing.JLabel reasonLabel = new javax.swing.JLabel(
            "<html><b>Reason:</b> " + reason + "</html>"
        );
        reasonLabel.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);

        javax.swing.JLabel descLabel = new javax.swing.JLabel(
            "<html><b>Description:</b> " + lafInfo.getDescription() + "</html>"
        );
        descLabel.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);

        contentPanel.add(javax.swing.Box.createVerticalStrut(10));
        contentPanel.add(lafLabel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(5));
        contentPanel.add(classLabel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(5));
        contentPanel.add(reasonLabel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(5));
        contentPanel.add(descLabel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // Sample components to showcase the LAF
        javax.swing.JPanel samplePanel = new javax.swing.JPanel(new java.awt.FlowLayout());
        samplePanel.add(new javax.swing.JButton("Sample Button"));
        samplePanel.add(new javax.swing.JCheckBox("Check Box"));
        samplePanel.add(new javax.swing.JRadioButton("Radio Button"));
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(
            new String[]{"Combo Box", "Item 2", "Item 3"}
        );
        samplePanel.add(combo);

        contentPanel.add(samplePanel);
        demoPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        // Button panel
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout());
        javax.swing.JButton closeButton = new javax.swing.JButton("Close");
        closeButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(closeButton);
        demoPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        return demoPanel;
    }
}
