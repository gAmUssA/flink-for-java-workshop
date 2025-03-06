package io.confluent.developer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manager for centralized configuration.
 * Provides a unified interface for accessing configuration properties across different environments
 * and applications.
 */
public class ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private final String environment;
    private final String application;
    private final String configBasePath;
    private final Properties properties;
    
    /**
     * Create a new ConfigurationManager.
     *
     * @param environment The environment (local or cloud)
     * @param application The application name
     */
    public ConfigurationManager(String environment, String application) {
        this(environment, application, null);
    }
    
    /**
     * Create a new ConfigurationManager with a custom configuration base path.
     *
     * @param environment The environment (local or cloud)
     * @param application The application name
     * @param configBasePath The base path for configuration files (null for default)
     */
    public ConfigurationManager(String environment, String application, String configBasePath) {
        this.environment = environment;
        this.application = application;
        this.configBasePath = configBasePath;
        this.properties = loadConfiguration();
        
        LOG.info("Initialized ConfigurationManager for environment: {}, application: {}", environment, application);
    }
    
    /**
     * Load configuration for the environment and application.
     *
     * @return Properties object containing the configuration
     */
    private Properties loadConfiguration() {
        Properties props = new Properties();
        
        // Load environment-specific properties
        Properties envProps = loadEnvironmentProperties();
        if (envProps != null) {
            props.putAll(envProps);
        }
        
        // Load application-specific properties
        Properties appProps = loadApplicationProperties();
        if (appProps != null) {
            props.putAll(appProps);
        }
        
        return props;
    }
    
    /**
     * Load environment-specific properties (kafka, topics, tables).
     *
     * @return Properties object containing environment-specific configuration
     */
    private Properties loadEnvironmentProperties() {
        Properties props = new Properties();
        
        // Load Kafka properties
        Properties kafkaProps = loadPropertiesFile(getEnvironmentConfigPath("kafka.properties"));
        
        // If Kafka properties are empty, try to load from classpath
        if (kafkaProps.isEmpty()) {
            LOG.info("Kafka properties not found in file system, trying classpath: kafka-{}.properties", environment);
            kafkaProps = ConfigUtils.loadPropertiesFromClasspath("kafka-" + environment + ".properties");
            if (!kafkaProps.isEmpty()) {
                LOG.info("Loaded Kafka properties from classpath");
            } else {
                LOG.warn("Kafka properties not found in classpath either");
            }
        }
        
        if (kafkaProps != null) {
            props.putAll(kafkaProps);
        }
        
        // Load topics properties
        Properties topicsProps = loadPropertiesFile(getEnvironmentConfigPath("topics.properties"));
        if (topicsProps != null) {
            props.putAll(topicsProps);
        }
        
        // Load tables properties
        Properties tablesProps = loadPropertiesFile(getEnvironmentConfigPath("tables.properties"));
        if (tablesProps != null) {
            props.putAll(tablesProps);
        }
        
        return props;
    }
    
    /**
     * Load application-specific properties.
     *
     * @return Properties object containing application-specific configuration
     */
    public Properties getApplicationProperties() {
        return loadPropertiesFile(getApplicationConfigPath());
    }
    
    /**
     * Load application-specific properties.
     *
     * @return Properties object containing application-specific configuration
     */
    private Properties loadApplicationProperties() {
        return getApplicationProperties();
    }
    
    /**
     * Load properties from a file.
     *
     * @param filePath The path to the properties file
     * @return Properties object or null if the file doesn't exist
     */
    private Properties loadPropertiesFile(String filePath) {
        if (filePath == null) {
            return new Properties();
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.debug("Properties file not found: {}", filePath);
            return new Properties();
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            LOG.debug("Loaded {} properties from {}", props.size(), filePath);
            
            // Resolve environment variables if in cloud environment
            if ("cloud".equalsIgnoreCase(environment)) {
                props = ConfigUtils.resolveEnvironmentVariables(props);
            }
            
            return props;
        } catch (IOException e) {
            LOG.error("Error loading properties from {}: {}", filePath, e.getMessage(), e);
            return new Properties();
        }
    }
    
    /**
     * Get the path to an environment-specific configuration file.
     *
     * @param fileName The name of the configuration file
     * @return The path to the configuration file
     */
    private String getEnvironmentConfigPath(String fileName) {
        String basePath = configBasePath != null ? configBasePath : ConfigUtils.getConfigBasePath();
        Path path = Paths.get(basePath, environment, fileName);
        return path.toString();
    }
    
    /**
     * Get the path to an application-specific configuration file.
     *
     * @return The path to the application configuration file
     */
    private String getApplicationConfigPath() {
        String basePath = configBasePath != null ? configBasePath : ConfigUtils.getConfigBasePath();
        Path path = Paths.get(basePath, "application", application + ".properties");
        return path.toString();
    }
    
    /**
     * Get a property value.
     *
     * @param key The property key
     * @param defaultValue The default value if the property is not found
     * @return The property value or the default value
     */
    public String getProperty(String key, String defaultValue) {
        return ConfigUtils.getProperty(properties, key, defaultValue);
    }
    
    /**
     * Get a table name.
     *
     * @param key The table key
     * @param defaultName The default table name
     * @return The table name or the default name
     */
    public String getTableName(String key, String defaultName) {
        return ConfigUtils.getTableName(properties, key, defaultName);
    }
    
    /**
     * Get a topic name.
     *
     * @param key The topic key
     * @param defaultName The default topic name
     * @return The topic name or the default name
     */
    public String getTopicName(String key, String defaultName) {
        return ConfigUtils.getTopicName(properties, key, defaultName);
    }
    
    /**
     * Get all properties.
     *
     * @return All properties
     */
    public Properties getProperties() {
        Properties result = new Properties();
        result.putAll(properties);
        return result;
    }
    
    /**
     * Merge custom properties with the loaded properties.
     * Custom properties will override existing properties with the same key.
     *
     * @param customProps The custom properties to merge
     * @return The merged properties
     */
    public Properties mergeProperties(Properties customProps) {
        Properties result = new Properties();
        result.putAll(properties);
        result.putAll(customProps);
        return result;
    }
    
    /**
     * Get the environment.
     *
     * @return The environment
     */
    public String getEnvironment() {
        return environment;
    }
    
    /**
     * Get the application name.
     *
     * @return The application name
     */
    public String getApplication() {
        return application;
    }
}
