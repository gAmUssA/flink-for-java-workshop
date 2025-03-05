package io.confluent.developer.tableapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConfigLoader class.
 */
public class ConfigLoaderTest {

    private Properties testProperties;

    @BeforeEach
    public void setUp() {
        testProperties = new Properties();
        testProperties.setProperty("topic.flights", "flights");
        testProperties.setProperty("topic.airlines", "airlines");
        testProperties.setProperty("topic.airports", "airports");
        
        testProperties.setProperty("table.flights", "Flights");
        testProperties.setProperty("table.airlines", "Airlines");
        testProperties.setProperty("table.airports", "Airports");
        testProperties.setProperty("table.airline-delay-performance", "AirlineDelayPerformance");
    }

    @Test
    public void testGetTopicName() {
        // Test with existing key
        assertEquals("flights", ConfigLoader.getTopicName(testProperties, "flights", "default"));
        assertEquals("airlines", ConfigLoader.getTopicName(testProperties, "airlines", "default"));
        assertEquals("airports", ConfigLoader.getTopicName(testProperties, "airports", "default"));
        
        // Test with non-existing key
        assertEquals("default", ConfigLoader.getTopicName(testProperties, "non-existing", "default"));
    }
    
    @Test
    public void testGetTableName() {
        // Test with existing key
        assertEquals("Flights", ConfigLoader.getTableName(testProperties, "flights", "default"));
        assertEquals("Airlines", ConfigLoader.getTableName(testProperties, "airlines", "default"));
        assertEquals("Airports", ConfigLoader.getTableName(testProperties, "airports", "default"));
        assertEquals("AirlineDelayPerformance", ConfigLoader.getTableName(testProperties, "airline-delay-performance", "default"));
        
        // Test with non-existing key
        assertEquals("default", ConfigLoader.getTableName(testProperties, "non-existing", "default"));
    }
    
    @Test
    public void testMergeProperties() {
        // Create base properties
        Properties base = new Properties();
        base.setProperty("key1", "value1");
        base.setProperty("key2", "value2");
        
        // Create override properties
        Properties override = new Properties();
        override.setProperty("key2", "new-value2");
        override.setProperty("key3", "value3");
        
        // Merge properties
        Properties merged = ConfigLoader.mergeProperties(base, override);
        
        // Verify merged properties
        assertEquals("value1", merged.getProperty("key1"));
        assertEquals("new-value2", merged.getProperty("key2"));
        assertEquals("value3", merged.getProperty("key3"));
    }
}
