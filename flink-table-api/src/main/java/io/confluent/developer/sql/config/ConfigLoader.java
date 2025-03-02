package io.confluent.developer.sql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading configuration properties.
 */
public class ConfigLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
    
    private static final String LOCAL_PROPERTIES_PATH = "src/main/resources/kafka-local.properties";
    private static final String CLOUD_PROPERTIES_PATH = "src/main/resources/kafka-cloud.properties";
    
    /**
     * Loads Kafka properties based on the specified environment.
     *
     * @param env Environment name ("local" or "cloud")
     * @return Properties object with Kafka configuration
     */
    public static Properties loadKafkaProperties(String env) {
        String propertiesPath = "local".equalsIgnoreCase(env) ? LOCAL_PROPERTIES_PATH : CLOUD_PROPERTIES_PATH;
        
        LOG.info("Loading Kafka properties from: {}", propertiesPath);
        
        Properties properties = new Properties();
        
        try (InputStream input = new FileInputStream(propertiesPath)) {
            properties.load(input);
            LOG.info("Loaded {} properties", properties.size());
            
            // If using cloud environment, resolve environment variables
            if ("cloud".equalsIgnoreCase(env)) {
                resolveEnvironmentVariables(properties);
            }
            
        } catch (IOException e) {
            LOG.error("Failed to load properties from: {}", propertiesPath, e);
            throw new RuntimeException("Failed to load Kafka properties", e);
        }
        
        return properties;
    }
    
    /**
     * Resolves environment variables in property values.
     * Replaces ${ENV_VAR} with the corresponding environment variable value.
     *
     * @param properties Properties to process
     */
    private static void resolveEnvironmentVariables(Properties properties) {
        LOG.info("Resolving environment variables in properties");
        
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            
            if (value != null && value.contains("${")) {
                String resolvedValue = resolveEnvVars(value);
                properties.setProperty(key, resolvedValue);
            }
        }
    }
    
    /**
     * Resolves environment variables in a string.
     * Replaces ${ENV_VAR} with the corresponding environment variable value.
     *
     * @param value String to process
     * @return String with environment variables resolved
     */
    private static String resolveEnvVars(String value) {
        int startIndex = value.indexOf("${");
        
        while (startIndex >= 0) {
            int endIndex = value.indexOf("}", startIndex);
            
            if (endIndex < 0) {
                break;
            }
            
            String envVar = value.substring(startIndex + 2, endIndex);
            String envValue = System.getenv(envVar);
            
            if (envValue == null) {
                LOG.warn("Environment variable not found: {}", envVar);
                envValue = "";
            }
            
            value = value.substring(0, startIndex) + envValue + value.substring(endIndex + 1);
            startIndex = value.indexOf("${");
        }
        
        return value;
    }
    
    /**
     * Merges two Properties objects.
     * Properties from the second object override properties from the first.
     *
     * @param base Base properties
     * @param override Properties to override with
     * @return Merged properties
     */
    public static Properties mergeProperties(Properties base, Properties override) {
        Properties result = new Properties();
        result.putAll(base);
        result.putAll(override);
        return result;
    }
}
