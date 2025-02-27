package io.confluent.developer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class for loading configuration properties.
 */
public class ConfigUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);

    /**
     * Load properties from a file.
     *
     * @param filePath Path to the properties file
     * @return Properties object containing the loaded properties
     */
    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        
        // First try to load from classpath
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (input != null) {
                properties.load(input);
                LOG.info("Loaded properties from classpath: {}", filePath);
                return properties;
            }
        } catch (IOException e) {
            LOG.warn("Failed to load properties from classpath: {}", filePath, e);
        }
        
        // Then try to load from file system
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
                LOG.info("Loaded properties from file system: {}", filePath);
                return properties;
            } catch (IOException e) {
                LOG.error("Failed to load properties from file system: {}", filePath, e);
            }
        } else {
            LOG.warn("Properties file not found: {}", filePath);
        }
        
        return properties;
    }
    
    /**
     * Get a property value with a default fallback.
     *
     * @param properties Properties object
     * @param key Property key
     * @param defaultValue Default value to return if the property is not found
     * @return Property value or default value if not found
     */
    public static String getProperty(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? value : defaultValue;
    }
}
