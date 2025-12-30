package org.jwellman.foundation.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the plugin registry, providing persistence and query operations
 * for registered plugins.
 *
 * <p>The registry is persisted to a JSON file (typically registry.json) and
 * contains all registered plugins with their metadata and user preferences.</p>
 *
 * <p><strong>Registry JSON structure:</strong></p>
 * <pre>
 * {
 *   "version": "1.0",
 *   "plugins": [
 *     {
 *       "id": "app1",
 *       "name": "My Application",
 *       "pluginDir": "plugins/app1",
 *       "launchMode": "ISOLATED_JVM",
 *       "enabled": true,
 *       "registered": "2025-01-15T10:30:00",
 *       "userPreferences": {
 *         "autoStart": false,
 *         "position": { "x": 100, "y": 100 }
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
public class PluginRegistry {

    private static final String REGISTRY_VERSION = "1.0";

    private final File registryFile;
    private final ObjectMapper objectMapper;
    private final Map<String, PluginRegistration> plugins;

    /**
     * Creates a plugin registry with the specified registry file.
     *
     * @param registryFile the JSON file to persist registry data
     */
    public PluginRegistry(File registryFile) {
        this.registryFile = registryFile;
        this.objectMapper = createObjectMapper();
        this.plugins = new HashMap<>();
    }

    /**
     * Creates and configures the Jackson ObjectMapper with custom serializers.
     *
     * @return configured ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register custom serializers for File and LocalDateTime
        SimpleModule module = new SimpleModule();
        module.addSerializer(File.class, new FileSerializer());
        module.addDeserializer(File.class, new FileDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

        mapper.registerModule(module);

        // Pretty print for human-readable JSON
        mapper.writerWithDefaultPrettyPrinter();

        return mapper;
    }

    // ========== Persistence Methods ==========

    /**
     * Loads the registry from the JSON file.
     * If the file does not exist, initializes an empty registry.
     *
     * @throws IOException if reading the file fails
     */
    public void load() throws IOException {
        if (!registryFile.exists()) {
            // Initialize empty registry
            plugins.clear();
            return;
        }

        RegistryData data = objectMapper.readValue(registryFile, RegistryData.class);

        plugins.clear();
        for (PluginRegistration registration : data.plugins) {
            plugins.put(registration.getId(), registration);
        }
    }

    /**
     * Saves the registry to the JSON file.
     *
     * @throws IOException if writing the file fails
     */
    public void save() throws IOException {
        // Ensure parent directory exists
        File parentDir = registryFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        RegistryData data = new RegistryData(REGISTRY_VERSION, new ArrayList<>(plugins.values()));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(registryFile, data);
    }

    // ========== Registration Methods ==========

    /**
     * Registers a plugin in the registry.
     *
     * @param registration the plugin registration
     * @throws IllegalArgumentException if plugin ID already registered
     */
    public void register(PluginRegistration registration) {
        if (plugins.containsKey(registration.getId())) {
            throw new IllegalArgumentException("Plugin already registered: " + registration.getId());
        }
        plugins.put(registration.getId(), registration);
    }

    /**
     * Unregisters a plugin from the registry.
     *
     * @param pluginId the plugin ID to unregister
     * @return the removed registration, or null if not found
     */
    public PluginRegistration unregister(String pluginId) {
        return plugins.remove(pluginId);
    }

    /**
     * Updates an existing plugin registration.
     *
     * @param registration the updated plugin registration
     * @throws IllegalArgumentException if plugin not found in registry
     */
    public void update(PluginRegistration registration) {
        if (!plugins.containsKey(registration.getId())) {
            throw new IllegalArgumentException("Plugin not registered: " + registration.getId());
        }
        plugins.put(registration.getId(), registration);
    }

    // ========== Query Methods ==========

    /**
     * Checks if a plugin is registered.
     *
     * @param pluginId the plugin ID
     * @return true if plugin is registered
     */
    public boolean isRegistered(String pluginId) {
        return plugins.containsKey(pluginId);
    }

    /**
     * Gets a plugin registration by ID.
     *
     * @param pluginId the plugin ID
     * @return the plugin registration, or null if not found
     */
    public PluginRegistration getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

    /**
     * Gets all registered plugins.
     *
     * @return list of all plugin registrations
     */
    public List<PluginRegistration> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }

    /**
     * Gets all enabled plugins.
     *
     * @return list of enabled plugin registrations
     */
    public List<PluginRegistration> getEnabledPlugins() {
        List<PluginRegistration> enabled = new ArrayList<>();
        for (PluginRegistration reg : plugins.values()) {
            if (reg.isEnabled()) {
                enabled.add(reg);
            }
        }
        return enabled;
    }

    /**
     * Gets the number of registered plugins.
     *
     * @return the plugin count
     */
    public int getPluginCount() {
        return plugins.size();
    }

    /**
     * Gets the registry file path.
     *
     * @return the registry file
     */
    public File getRegistryFile() {
        return registryFile;
    }

    // ========== JSON Data Structure ==========

    /**
     * Registry data structure for JSON serialization.
     */
    private static class RegistryData {
        @JsonProperty("version")
        public final String version;

        @JsonProperty("plugins")
        public final List<PluginRegistration> plugins;

        @JsonCreator
        public RegistryData(@JsonProperty("version") String version,
                            @JsonProperty("plugins") List<PluginRegistration> plugins) {
            this.version = version;
            this.plugins = plugins != null ? plugins : Collections.emptyList();
        }
    }

    // ========== Custom Jackson Serializers ==========

    /**
     * Serializes File to path string.
     */
    private static class FileSerializer extends JsonSerializer<File> {
        @Override
        public void serialize(File file, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(file.getPath());
        }
    }

    /**
     * Deserializes path string to File.
     */
    private static class FileDeserializer extends JsonDeserializer<File> {
        @Override
        public File deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            return new File(parser.getValueAsString());
        }
    }

    /**
     * Serializes LocalDateTime to ISO-8601 string.
     */
    private static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void serialize(LocalDateTime dateTime, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(dateTime.format(FORMATTER));
        }
    }

    /**
     * Deserializes ISO-8601 string to LocalDateTime.
     */
    private static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            return LocalDateTime.parse(parser.getValueAsString(), FORMATTER);
        }
    }
}
