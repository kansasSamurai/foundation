package org.jwellman.foundation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
     * Demo/test method.
     *
     * Discovers all available LAFs and prints them to console, then applies one according to priority:
     * 1. First LAF found in ./lafs/ directory
     * 2. System default LAF as fallback
     *
     * Then displays a demo window to showcase the selected LAF.
     */
    public static void main(String[] args) {
        System.out.println("Discovering Look and Feels...\n");

        // Discover all LAFs using all strategies
        List<LAFInfo> lafs = discoverLookAndFeels();

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

        // Try to find LAFs in the ./lafs/ directory
        List<LAFInfo> directoryLAFs = discoverDirectoryLAFs();

        LAFInfo selectedLAF = null;
        String selectionReason = "";

        if (!directoryLAFs.isEmpty()) {
            // Use the first LAF found in the directory
            selectedLAF = directoryLAFs.get(0);
            selectionReason = "Found in " + LAF_DIRECTORY + " directory";
            System.out.println("Using LAF from directory: " + selectedLAF.getName());
        } else {
            // Fallback to system LAF
            selectedLAF = getSystemLAF();
            if (selectedLAF != null) {
                selectionReason = "Using system default (no LAFs found in " + LAF_DIRECTORY + ")";
                System.out.println(selectionReason);
            } else {
                System.err.println("ERROR: Could not determine system LAF!");
                return;
            }
        }

        // Apply the selected LAF
        if (!applyLookAndFeel(selectedLAF)) {
            System.err.println("ERROR: Failed to apply LAF. Exiting.");
            return;
        }

        // Create and display demo window
        final LAFInfo finalLAF = selectedLAF;
        final String finalReason = selectionReason;

        // Use Foundation to create and display the window
        Foundation f = Foundation.init();
        org.jwellman.foundation.swing.IWindow window = f.useWindow(showDemoWindow(finalLAF, finalReason));
        window.setTitle("Foundation LAF Discovery - " + finalLAF.getName());
        window.setResizable(true);
        f.showGUI(window);

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
