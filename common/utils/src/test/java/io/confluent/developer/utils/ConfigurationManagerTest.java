package io.confluent.developer.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationManagerTest {

    private static final String TEST_CONFIG_DIR = "src/test/resources/config";
    private ConfigurationManager localConfigManager;
    private ConfigurationManager cloudConfigManager;
    private String originalConfigBasePath;

    @BeforeEach
    void setUp() {
        // Save original config base path
        originalConfigBasePath = ConfigUtils.getConfigBasePath();
        
        // Set the config base path to our test resources
        ConfigUtils.setConfigBasePath(TEST_CONFIG_DIR);
        
        // Create configuration managers for local and cloud environments
        localConfigManager = new ConfigurationManager("local", "flink-table-api");
        cloudConfigManager = new ConfigurationManager("cloud", "flink-table-api");
    }
    
    @AfterEach
    void tearDown() {
        // Restore original config base path
        ConfigUtils.setConfigBasePath(originalConfigBasePath);
    }

    @Test
    void testGetProperties() {
        // Test local configuration
        Properties localProps = localConfigManager.getProperties();
        assertNotNull(localProps, "Local properties should not be null");
        assertEquals("localhost:9092", localProps.getProperty("bootstrap.servers"), 
                "Local bootstrap.servers should match");
        assertEquals("http://localhost:8081", localProps.getProperty("schema.registry.url"), 
                "Local schema.registry.url should match");
        assertEquals("PLAINTEXT", localProps.getProperty("security.protocol"), 
                "Local security.protocol should match");

        // Test cloud configuration (with environment variables)
        Properties cloudProps = cloudConfigManager.getProperties();
        assertNotNull(cloudProps, "Cloud properties should not be null");
        // Note: Environment variables might not be resolved in tests, so we just check if the property exists
        assertNotNull(cloudProps.getProperty("bootstrap.servers"), 
                "Cloud bootstrap.servers should exist");
        assertNotNull(cloudProps.getProperty("schema.registry.url"), 
                "Cloud schema.registry.url should exist");
        assertEquals("SASL_SSL", cloudProps.getProperty("security.protocol"), 
                "Cloud security.protocol should match");
    }

    @Test
    void testGetTableName() {
        // Test local table names
        assertEquals("Flights", localConfigManager.getTableName("flights", "DefaultFlights"), 
                "Should get correct table name from properties");
        assertEquals("AirlineDelayPerformance", localConfigManager.getTableName("airline-delay-performance", "DefaultDelay"), 
                "Should get correct table name from properties");
        assertEquals("DefaultTable", localConfigManager.getTableName("non-existent", "DefaultTable"), 
                "Should get default table name when not in properties");

        // Test cloud table names (should be the same as local in our test setup)
        assertEquals("Flights", cloudConfigManager.getTableName("flights", "DefaultFlights"), 
                "Should get correct cloud table name from properties");
        assertEquals("DefaultTable", cloudConfigManager.getTableName("non-existent", "DefaultTable"), 
                "Should get default cloud table name when not in properties");
    }

    @Test
    void testGetTopicName() {
        // Test local topic names
        assertEquals("flights-avro", localConfigManager.getTopicName("flights", "default-flights"), 
                "Should get correct topic name from properties");
        assertEquals("airlines", localConfigManager.getTopicName("airlines", "default-airlines"), 
                "Should get correct topic name from properties");
        assertEquals("default-topic", localConfigManager.getTopicName("non-existent", "default-topic"), 
                "Should get default topic name when not in properties");

        // Test cloud topic names (should be the same as local in our test setup)
        assertEquals("flights-avro", cloudConfigManager.getTopicName("flights", "default-flights"), 
                "Should get correct cloud topic name from properties");
        assertEquals("default-topic", cloudConfigManager.getTopicName("non-existent", "default-topic"), 
                "Should get default cloud topic name when not in properties");
    }

    @Test
    void testGetApplicationProperties() {
        // Test application properties
        Properties appProps = localConfigManager.getApplicationProperties();
        assertNotNull(appProps, "Application properties should not be null");
        assertEquals("Flink Table API", appProps.getProperty("app.name"), 
                "Application name should match");
        assertEquals("1.0.0", appProps.getProperty("app.version"), 
                "Application version should match");
        assertEquals("2", appProps.getProperty("app.parallelism"), 
                "Application parallelism should match");
    }

    @Test
    void testMergeProperties() {
        // Create a ConfigurationManager with a custom application name
        ConfigurationManager customConfigManager = new ConfigurationManager("local", "flink-table-api");
        
        // Create custom properties to merge
        Properties customProps = new Properties();
        customProps.setProperty("custom.property", "custom-value");
        customProps.setProperty("bootstrap.servers", "custom-server:9092");
        
        // Merge properties
        Properties mergedProps = customConfigManager.mergeProperties(customProps);
        
        // Verify merged properties
        assertEquals("custom-value", mergedProps.getProperty("custom.property"), 
                "Custom property should be included");
        assertEquals("custom-server:9092", mergedProps.getProperty("bootstrap.servers"), 
                "Custom property should override existing property");
        assertEquals("http://localhost:8081", mergedProps.getProperty("schema.registry.url"), 
                "Existing property should be preserved if not overridden");
    }

    @Test
    void testConfigManagerWithNonExistentApplication(@TempDir Path tempDir) throws IOException {
        // Create a temporary config directory structure
        Path localDir = tempDir.resolve("local");
        Files.createDirectories(localDir);
        
        // Create a simple kafka.properties file
        Path kafkaProps = localDir.resolve("kafka.properties");
        try (FileWriter writer = new FileWriter(kafkaProps.toFile())) {
            writer.write("bootstrap.servers=temp-server:9092\n");
            writer.write("schema.registry.url=http://temp-server:8081\n");
        }
        
        // Create a ConfigurationManager with a non-existent application name
        ConfigurationManager tempConfigManager = new ConfigurationManager("local", "non-existent-app", tempDir.toString());
        
        // Verify that it still loads the kafka properties
        Properties props = tempConfigManager.getProperties();
        assertNotNull(props, "Properties should not be null even with non-existent application");
        assertEquals("temp-server:9092", props.getProperty("bootstrap.servers"), 
                "Should load kafka properties correctly");
        
        // Verify that application properties are empty but don't cause errors
        Properties appProps = tempConfigManager.getApplicationProperties();
        assertTrue(appProps.isEmpty(), "Application properties should be empty for non-existent app");
    }
}
