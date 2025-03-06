package io.confluent.developer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for configuration management.
 */
public class ConfigUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);
    
    // Default config path
    private static String CONFIG_BASE_PATH = "config";
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    
    private ConfigUtils() {
        // Utility class, no instantiation
    }
    
    /**
     * Get the base path for configuration files.
     * This method tries to find the config directory in several locations:
     * 1. Current working directory
     * 2. Project root directory (up to 3 levels up)
     * 3. Default to "config" if not found
     *
     * @return The base path for configuration files
     */
    public static String getConfigBasePath() {
        // First, try the current directory
        Path currentDir = Paths.get(CONFIG_BASE_PATH);
        if (Files.exists(currentDir) && Files.isDirectory(currentDir)) {
            LOG.info("Using config from current directory: {}", currentDir.toAbsolutePath());
            return currentDir.toString();
        }
        
        // Try to find config directory in parent directories (up to 3 levels)
        Path workingDir = Paths.get("").toAbsolutePath();
        LOG.info("Current working directory: {}", workingDir);
        
        for (int i = 0; i < 3; i++) {
            Path configPath = workingDir.resolve(CONFIG_BASE_PATH);
            if (Files.exists(configPath) && Files.isDirectory(configPath)) {
                LOG.info("Found config directory at: {}", configPath);
                return configPath.toString();
            }
            
            // Move up one directory
            if (workingDir.getParent() != null) {
                workingDir = workingDir.getParent();
            } else {
                break;
            }
        }
        
        // If we get here, we couldn't find the config directory
        LOG.warn("Config directory not found in filesystem, will try to load from classpath");
        return CONFIG_BASE_PATH;
    }
    
    /**
     * Set the base path for configuration files.
     * This method is primarily for testing purposes.
     *
     * @param configBasePath The base path for configuration files
     */
    public static void setConfigBasePath(String configBasePath) {
        CONFIG_BASE_PATH = configBasePath;
    }
    
    /**
     * Load properties from a file in the classpath.
     *
     * @param resourcePath The path to the properties file in the classpath
     * @return Properties object or empty Properties if the file doesn't exist
     */
    public static Properties loadPropertiesFromClasspath(String resourcePath) {
        Properties props = new Properties();
        
        try (InputStream is = ConfigUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOG.debug("Properties file not found in classpath: {}", resourcePath);
                return props;
            }
            
            props.load(is);
            LOG.debug("Loaded {} properties from classpath: {}", props.size(), resourcePath);
            return props;
        } catch (IOException e) {
            LOG.error("Error loading properties from classpath {}: {}", resourcePath, e.getMessage(), e);
            return props;
        }
    }
    
    /**
     * Load properties from a file in the file system with classpath fallback.
     *
     * @param filePath The path to the properties file in the file system
     * @return Properties object or empty Properties if the file doesn't exist
     */
    public static Properties loadPropertiesFromFileSystem(String filePath) {
        Properties props = new Properties();
        
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.debug("Properties file not found: {}", filePath);
            
            // Try to load from classpath as a fallback
            String resourceName = new File(filePath).getName();
            LOG.debug("Attempting to load {} from classpath", resourceName);
            
            // Try environment-specific path first (e.g., "cloud/cloud.properties")
            String parentDirName = file.getParentFile() != null ? file.getParentFile().getName() : "";
            String envResourcePath = parentDirName + "/" + resourceName;
            
            Properties classpathProps = loadPropertiesFromClasspath(envResourcePath);
            if (!classpathProps.isEmpty()) {
                LOG.info("Loaded {} from classpath at {}", resourceName, envResourcePath);
                return classpathProps;
            }
            
            // Try just the filename
            classpathProps = loadPropertiesFromClasspath(resourceName);
            if (!classpathProps.isEmpty()) {
                LOG.info("Loaded {} from classpath root", resourceName);
                return classpathProps;
            }
            
            LOG.warn("Could not find {} in filesystem or classpath", resourceName);
            return props;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            LOG.debug("Loaded {} properties from {}", props.size(), filePath);
            return props;
        } catch (IOException e) {
            LOG.error("Error loading properties from {}: {}", filePath, e.getMessage(), e);
            return props;
        }
    }
    
    /**
     * Load environment-specific properties.
     *
     * @param environment The environment (local or cloud)
     * @return Properties object containing environment-specific configuration
     */
    public static Properties loadEnvironmentConfig(String environment) {
        Properties props = new Properties();
        
        // First try to load the environment-specific properties file directly
        String envPropsPath = Paths.get(getConfigBasePath(), environment, environment + ".properties").toString();
        Properties envProps = loadPropertiesFromFileSystem(envPropsPath);
        if (!envProps.isEmpty()) {
            LOG.info("Loaded environment properties from {}", envPropsPath);
            props.putAll(envProps);
        }
        
        // Load Kafka properties
        String kafkaPropsPath = Paths.get(getConfigBasePath(), environment, "kafka.properties").toString();
        Properties kafkaProps = loadPropertiesFromFileSystem(kafkaPropsPath);
        props.putAll(kafkaProps);
        
        // Load topics properties
        String topicsPropsPath = Paths.get(getConfigBasePath(), environment, "topics.properties").toString();
        Properties topicsProps = loadPropertiesFromFileSystem(topicsPropsPath);
        props.putAll(topicsProps);
        
        // Load tables properties
        String tablesPropsPath = Paths.get(getConfigBasePath(), environment, "tables.properties").toString();
        Properties tablesProps = loadPropertiesFromFileSystem(tablesPropsPath);
        props.putAll(tablesProps);
        
        // Resolve environment variables if in cloud environment
        if ("cloud".equalsIgnoreCase(environment)) {
            props = resolveEnvironmentVariables(props);
        }
        
        return props;
    }
    
    /**
     * Load application-specific properties.
     *
     * @param application The application name
     * @return Properties object containing application-specific configuration
     */
    public static Properties loadApplicationConfig(String application) {
        String appPropsPath = Paths.get(getConfigBasePath(), "application", application + ".properties").toString();
        return loadPropertiesFromFileSystem(appPropsPath);
    }
    
    /**
     * Load environment and application-specific properties.
     *
     * @param environment The environment (local or cloud)
     * @param application The application name
     * @return Properties object containing environment and application-specific configuration
     */
    public static Properties loadApplicationConfig(String environment, String application) {
        Properties props = new Properties();
        
        // Load environment-specific properties
        Properties envProps = loadEnvironmentConfig(environment);
        props.putAll(envProps);
        
        // Load application-specific properties
        Properties appProps = loadApplicationConfig(application);
        props.putAll(appProps);
        
        return props;
    }
    
    /**
     * Get a property value.
     *
     * @param properties The properties object
     * @param key The property key
     * @param defaultValue The default value if the property is not found
     * @return The property value or the default value
     */
    public static String getProperty(Properties properties, String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get a table name.
     *
     * @param properties The properties object
     * @param key The table key
     * @param defaultName The default table name
     * @return The table name or the default name
     */
    public static String getTableName(Properties properties, String key, String defaultName) {
        String propertyKey = "table." + key;
        return properties.getProperty(propertyKey, defaultName);
    }
    
    /**
     * Get a topic name.
     *
     * @param properties The properties object
     * @param key The topic key
     * @param defaultName The default topic name
     * @return The topic name or the default name
     */
    public static String getTopicName(Properties properties, String key, String defaultName) {
        String propertyKey = "topic." + key;
        return properties.getProperty(propertyKey, defaultName);
    }
    
    /**
     * Resolve environment variables in property values.
     *
     * @param properties The properties object
     * @return Properties object with resolved environment variables
     */
    public static Properties resolveEnvironmentVariables(Properties properties) {
        Properties resolvedProps = new Properties();
        
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            
            // Resolve environment variables in the value
            Matcher matcher = ENV_VAR_PATTERN.matcher(value);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String envVarName = matcher.group(1);
                String envVarValue = System.getenv(envVarName);
                
                if (envVarValue == null) {
                    LOG.warn("Environment variable {} not found, keeping placeholder", envVarName);
                    matcher.appendReplacement(sb, "\\${" + envVarName + "}");
                } else {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(envVarValue));
                }
            }
            
            matcher.appendTail(sb);
            resolvedProps.setProperty(key, sb.toString());
        }
        
        return resolvedProps;
    }
    
    /**
     * Merge two Properties objects.
     *
     * @param base The base properties
     * @param override The properties to override base properties
     * @return The merged properties
     */
    public static Properties mergeProperties(Properties base, Properties override) {
        Properties result = new Properties();
        result.putAll(base);
        result.putAll(override);
        return result;
    }
    
    /**
     * Load properties from a file path.
     *
     * @param propertiesFile The path to the properties file
     * @return Properties object loaded from the file
     */
    public static Properties loadProperties(String propertiesFile) {
        // Track which file was successfully loaded
        String loadedFrom = null;
        
        // First try to load from filesystem
        Properties props = loadPropertiesFromFileSystem(propertiesFile);
        if (!props.isEmpty()) {
            loadedFrom = new File(propertiesFile).getAbsolutePath();
        }
        
        // If no properties were loaded, try to load from classpath
        if (props.isEmpty()) {
            Properties classpathProps = loadPropertiesFromClasspath(propertiesFile);
            if (!classpathProps.isEmpty()) {
                props = classpathProps;
                loadedFrom = "classpath:" + propertiesFile;
            }
        }
        
        // If still no properties, try with config base path
        if (props.isEmpty()) {
            String configPath = Paths.get(CONFIG_BASE_PATH, propertiesFile).toString();
            Properties configProps = loadPropertiesFromFileSystem(configPath);
            if (!configProps.isEmpty()) {
                props = configProps;
                loadedFrom = new File(configPath).getAbsolutePath();
            }
        }
        
        // If still no properties, try templates directory
        if (props.isEmpty()) {
            String templatesPath = Paths.get("templates", propertiesFile).toString();
            Properties templateProps = loadPropertiesFromFileSystem(templatesPath);
            if (!templateProps.isEmpty()) {
                props = templateProps;
                loadedFrom = new File(templatesPath).getAbsolutePath();
            }
        }
        
        if (props.isEmpty()) {
            LOG.warn("No properties loaded from {}", propertiesFile);
        } else {
            LOG.info("Loaded {} properties from file: {}", props.size(), loadedFrom);
        }
        
        return props;
    }
}
