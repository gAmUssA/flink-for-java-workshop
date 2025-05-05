package io.confluent.developer.tableapi.config;

import io.confluent.developer.utils.ConfigurationManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the configuration management functionality using ConfigurationManager.
 */
public class ConfigurationManagerTest {

    private ConfigurationManager configManager;

    @BeforeEach
    public void setUp() {
        configManager = new ConfigurationManager("local", "flink-table-api");
    }

    @Test
    public void testGetTopicName() {
        // Test with existing key
        assertEquals("flights", configManager.getTopicName("flights", "default"));
        assertEquals("airlines", configManager.getTopicName("airlines", "default"));
        assertEquals("airports", configManager.getTopicName("airports", "default"));

        // Test with non-existing key
        assertEquals("default", configManager.getTopicName("non-existing", "default"));
    }

    @Test
    public void testGetTableName() {
        // Test with existing key
        assertEquals("Flights", configManager.getTableName("flights", "default"));
        assertEquals("Airlines", configManager.getTableName("airlines", "default"));
        assertEquals("Airports", configManager.getTableName("airports", "default"));
        assertEquals("AirlineDelayPerformance", configManager.getTableName("airline-delay-performance", "default"));

        // Test with non-existing key
        assertEquals("default", configManager.getTableName("non-existing", "default"));
    }

    @Test
    public void testMergeProperties() {
        // Get base properties
        Properties baseProps = configManager.getProperties();
        String originalBootstrapServers = baseProps.getProperty("bootstrap.servers");

        // Create override properties
        Properties override = new Properties();
        override.setProperty("bootstrap.servers", "new-server:9092");
        override.setProperty("new.property", "new-value");

        // Merge properties
        Properties merged = configManager.mergeProperties(override);

        // Verify merged properties
        assertNotEquals(originalBootstrapServers, merged.getProperty("bootstrap.servers"));
        assertEquals("new-server:9092", merged.getProperty("bootstrap.servers"));
        assertEquals("new-value", merged.getProperty("new.property"));
    }
}
