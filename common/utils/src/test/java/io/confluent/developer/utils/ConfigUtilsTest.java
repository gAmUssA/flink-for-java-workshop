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
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigUtilsTest {

    @TempDir
    Path tempDir;
    
    private Path configDir;
    private Path localDir;
    private Path cloudDir;
    private Path applicationDir;
    private String originalConfigBasePath;
    
    @BeforeEach
    void setUp() throws IOException {
        // Save original config base path
        originalConfigBasePath = ConfigUtils.getConfigBasePath();
        
        // Create a temporary config directory structure for testing
        configDir = tempDir.resolve("config");
        localDir = configDir.resolve("local");
        cloudDir = configDir.resolve("cloud");
        applicationDir = configDir.resolve("application");
        
        Files.createDirectories(localDir);
        Files.createDirectories(cloudDir);
        Files.createDirectories(applicationDir);
        
        // Create test properties files
        createPropertiesFile(localDir.resolve("kafka.properties"), 
                "bootstrap.servers=localhost:29092\n" +
                "schema.registry.url=http://localhost:8081");
        
        createPropertiesFile(localDir.resolve("topics.properties"), 
                "topic.test=test-topic\n" +
                "topic.flights=flights-avro");
        
        createPropertiesFile(localDir.resolve("tables.properties"), 
                "table.test=TestTable\n" +
                "table.flights=Flights");
        
        createPropertiesFile(applicationDir.resolve("test-app.properties"), 
                "app.name=TestApp\n" +
                "app.version=1.0.0");
        
        // Create a test properties file in the classpath
        createPropertiesFile(tempDir.resolve("test.properties"), 
                "test.key=test-value\n" +
                "another.key=another-value");
    }
    
    @AfterEach
    void tearDown() {
        // Restore original config base path
        ConfigUtils.setConfigBasePath(originalConfigBasePath);
    }
    
    private void createPropertiesFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }
    
    @Test
    void testLoadPropertiesFromFileSystem() throws IOException {
        // Create a test properties file
        File testFile = tempDir.resolve("test-fs.properties").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("key1=value1\n");
            writer.write("key2=value2\n");
        }
        
        // Load properties from file system
        Properties loadedProps = ConfigUtils.loadPropertiesFromFileSystem(testFile.getAbsolutePath());
        
        // Verify properties were loaded correctly
        assertEquals("value1", loadedProps.getProperty("key1"));
        assertEquals("value2", loadedProps.getProperty("key2"));
    }
    
    @Test
    void testLoadPropertiesFromClasspath() {
        // This test relies on the test.properties file in src/test/resources
        Properties classpathProps = ConfigUtils.loadPropertiesFromClasspath("test.properties");
        
        // Verify properties were loaded correctly from classpath
        assertEquals("test-value", classpathProps.getProperty("test.key"));
        assertEquals("another-value", classpathProps.getProperty("another.key"));
    }
    
    @Test
    void testGetProperty() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        
        assertEquals("value1", ConfigUtils.getProperty(props, "key1", "default"));
        assertEquals("value2", ConfigUtils.getProperty(props, "key2", "default"));
        assertEquals("default", ConfigUtils.getProperty(props, "non-existent", "default"));
    }
    
    @Test
    void testGetTableName() {
        Properties props = new Properties();
        props.setProperty("table.test", "TestTable");
        props.setProperty("table.flights", "Flights");
        
        assertEquals("TestTable", ConfigUtils.getTableName(props, "test", "DefaultTable"));
        assertEquals("Flights", ConfigUtils.getTableName(props, "flights", "DefaultTable"));
        assertEquals("DefaultTable", ConfigUtils.getTableName(props, "non-existent", "DefaultTable"));
    }
    
    @Test
    void testGetTopicName() {
        Properties props = new Properties();
        props.setProperty("topic.test", "test-topic");
        props.setProperty("topic.flights", "flights-avro");
        
        assertEquals("test-topic", ConfigUtils.getTopicName(props, "test", "default-topic"));
        assertEquals("flights-avro", ConfigUtils.getTopicName(props, "flights", "default-topic"));
        assertEquals("default-topic", ConfigUtils.getTopicName(props, "non-existent", "default-topic"));
    }
    
    @Test
    void testMergeProperties() {
        Properties base = new Properties();
        base.setProperty("key1", "value1");
        base.setProperty("key2", "value2");
        
        Properties override = new Properties();
        override.setProperty("key2", "new-value2");
        override.setProperty("key3", "value3");
        
        Properties merged = ConfigUtils.mergeProperties(base, override);
        
        assertEquals("value1", merged.getProperty("key1"));
        assertEquals("new-value2", merged.getProperty("key2"));
        assertEquals("value3", merged.getProperty("key3"));
    }
    
    @Test
    void testResolveEnvironmentVariables() {
        // Set environment variables for testing
        String bootstrapServers = "kafka.example.com:9092";
        String apiKey = "test-api-key";
        String apiSecret = "test-api-secret";
        
        // Create properties with environment variable placeholders
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "${BOOTSTRAP_SERVERS}");
        props.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='${API_KEY}' password='${API_SECRET}';");
        props.setProperty("unchanged", "value");
        
        // Mock environment variables
        // Note: This is a bit tricky in Java as we can't directly set environment variables
        // In a real test, you might use a library like PowerMock or Mockito to mock System.getenv
        // For this example, we'll just test the functionality without actually setting env vars
        
        // Resolve environment variables
        Properties resolved = ConfigUtils.resolveEnvironmentVariables(props);
        
        // Verify that the placeholders are still there (since we didn't actually set the env vars)
        assertTrue(resolved.getProperty("bootstrap.servers").contains("${BOOTSTRAP_SERVERS}"));
        assertTrue(resolved.getProperty("sasl.jaas.config").contains("${API_KEY}"));
        assertTrue(resolved.getProperty("sasl.jaas.config").contains("${API_SECRET}"));
        assertEquals("value", resolved.getProperty("unchanged"));
    }
    
    @Test
    void testLoadEnvironmentConfig() {
        // Set the CONFIG_BASE_PATH to our temp directory
        ConfigUtils.setConfigBasePath(configDir.toString());
        
        // Load environment config
        Properties props = ConfigUtils.loadEnvironmentConfig("local");
        
        // Verify properties were loaded correctly
        assertEquals("localhost:29092", props.getProperty("bootstrap.servers"));
        assertEquals("http://localhost:8081", props.getProperty("schema.registry.url"));
        assertEquals("test-topic", props.getProperty("topic.test"));
        assertEquals("flights-avro", props.getProperty("topic.flights"));
        assertEquals("TestTable", props.getProperty("table.test"));
        assertEquals("Flights", props.getProperty("table.flights"));
    }
    
    @Test
    void testLoadApplicationConfig() {
        // Set the CONFIG_BASE_PATH to our temp directory
        ConfigUtils.setConfigBasePath(configDir.toString());
        
        // Load application config
        Properties props = ConfigUtils.loadApplicationConfig("test-app");
        
        // Verify properties were loaded correctly
        assertEquals("TestApp", props.getProperty("app.name"));
        assertEquals("1.0.0", props.getProperty("app.version"));
    }
    
    @Test
    void testLoadApplicationConfigWithEnvironment() {
        // Set the CONFIG_BASE_PATH to our temp directory
        ConfigUtils.setConfigBasePath(configDir.toString());
        
        // Load application config with environment
        Properties props = ConfigUtils.loadApplicationConfig("local", "test-app");
        
        // Verify properties were loaded correctly
        assertEquals("localhost:29092", props.getProperty("bootstrap.servers"));
        assertEquals("http://localhost:8081", props.getProperty("schema.registry.url"));
        assertEquals("test-topic", props.getProperty("topic.test"));
        assertEquals("flights-avro", props.getProperty("topic.flights"));
        assertEquals("TestTable", props.getProperty("table.test"));
        assertEquals("Flights", props.getProperty("table.flights"));
        assertEquals("TestApp", props.getProperty("app.name"));
        assertEquals("1.0.0", props.getProperty("app.version"));
    }
}
