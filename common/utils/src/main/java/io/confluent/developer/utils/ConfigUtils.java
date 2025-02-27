package io.confluent.developer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading configuration properties.
 */
public class ConfigUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);

    /**
     * Loads properties from a file.
     *
     * @param filePath Path to the properties file
     * @return Properties object with loaded properties
     */
    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOG.warn("Properties file not found: {}", filePath);
            return properties;
        }
        
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            LOG.info("Loaded {} properties from {}", properties.size(), filePath);
        } catch (IOException e) {
            LOG.error("Error loading properties from {}: {}", filePath, e.getMessage(), e);
        }
        
        return properties;
    }
    
    /**
     * Gets a property value, with a default if not found.
     *
     * @param properties Properties object
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default
     */
    public static String getProperty(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            LOG.debug("Property '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        }
        return value;
    }
}
