package org.jwellman.foundation.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Handles loading and validation of the Foundation configuration file.
 *
 * <p><strong>Silver+ Requirement:</strong> The configuration file
 * {@code config/foundation.json} MUST exist and be well-formed for
 * Silver tier and above applications. If the file is missing or malformed,
 * this class displays an error dialog and terminates the application.</p>
 *
 * <p><strong>Required Structure:</strong></p>
 * <pre>
 * {
 *   "version": "1.0",
 *   "global": { }
 * }
 * </pre>
 *
 * <p>The {@code global} section is required but may be empty.</p>
 *
 * @since Silver
 */
class PreferenceLoader {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceLoader.class);

    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "foundation.json";
    private static final String REQUIRED_VERSION = "1.0";

    private final ObjectMapper objectMapper;

    /**
     * Creates a preference loader with default ObjectMapper.
     */
    public PreferenceLoader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Loads preferences from config/foundation.json.
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>config/ directory must exist</li>
     *   <li>config/foundation.json file must exist</li>
     *   <li>JSON must be well-formed (parseable)</li>
     *   <li>"version" field must be present and "1.0"</li>
     *   <li>"global" section must be present (may be empty)</li>
     * </ul>
     *
     * <p>If any validation fails, displays error dialog and terminates application.</p>
     *
     * @return the loaded preference data
     */
    public PreferenceData load() {
        File configDir = new File(CONFIG_DIR);
        File configFile = new File(configDir, CONFIG_FILE);

        // Validate directory exists
        if (!configDir.exists()) {
            showErrorAndTerminate(
                    "Configuration Directory Missing",
                    "The required 'config' directory does not exist.\n\n" +
                            "Silver tier applications require a configuration directory.\n" +
                            "Please create the 'config' directory in the application root."
            );
        }

        if (!configDir.isDirectory()) {
            showErrorAndTerminate(
                    "Configuration Path Invalid",
                    "The path 'config' exists but is not a directory.\n\n" +
                            "Silver tier applications require a configuration directory.\n" +
                            "Please remove the 'config' file and create a directory instead."
            );
        }

        // Validate file exists
        if (!configFile.exists()) {
            showErrorAndTerminate(
                    "Configuration File Missing",
                    "The required configuration file 'config/foundation.json' does not exist.\n\n" +
                            "Silver tier applications require a valid configuration file.\n\n" +
                            "Minimum valid file:\n" +
                            "{\n" +
                            "  \"version\": \"1.0\",\n" +
                            "  \"global\": {}\n" +
                            "}"
            );
        }

        // Parse JSON
        PreferenceData data;
        try {
            data = objectMapper.readValue(configFile, PreferenceData.class);
        } catch (IOException e) {
            logger.error("Failed to parse configuration file: {}", configFile.getAbsolutePath(), e);
            showErrorAndTerminate(
                    "Configuration File Malformed",
                    "The configuration file 'config/foundation.json' is not valid JSON.\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Please fix the JSON syntax errors and restart the application."
            );
            return null; // Never reached
        }

        // Validate version
        if (data.getVersion() == null || data.getVersion().trim().isEmpty()) {
            showErrorAndTerminate(
                    "Configuration File Invalid",
                    "The configuration file is missing the required 'version' field.\n\n" +
                            "Please add: \"version\": \"1.0\""
            );
        }

        if (!REQUIRED_VERSION.equals(data.getVersion())) {
            showErrorAndTerminate(
                    "Configuration Version Mismatch",
                    "The configuration file version '" + data.getVersion() + "' is not supported.\n\n" +
                            "This version of Foundation requires version: " + REQUIRED_VERSION + "\n\n" +
                            "Please update your configuration file or upgrade Foundation."
            );
        }

        // Validate global section exists
        if (data.getGlobal() == null) {
            showErrorAndTerminate(
                    "Configuration File Invalid",
                    "The configuration file is missing the required 'global' section.\n\n" +
                            "Please add: \"global\": {}"
            );
        }

        logger.info("Successfully loaded configuration from: {}", configFile.getAbsolutePath());
        logger.debug("Global preferences: {}", data.getGlobal().keySet());
        logger.debug("Namespace count: {}", data.getNamespaces().size());

        return data;
    }

    /**
     * Displays an error dialog and terminates the application.
     *
     * @param title the dialog title
     * @param message the error message
     */
    private void showErrorAndTerminate(String title, String message) {
        logger.error("Configuration error - terminating application: {}", title);
        logger.error(message);

        JOptionPane.showMessageDialog(
                null,
                message,
                "Foundation Configuration Error: " + title,
                JOptionPane.ERROR_MESSAGE
        );

        System.exit(1);
    }
}
