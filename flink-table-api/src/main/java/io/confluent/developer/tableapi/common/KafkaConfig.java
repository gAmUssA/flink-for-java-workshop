package io.confluent.developer.tableapi.common;

import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Common Kafka configuration for all use cases.
 * Provides centralized configuration and error handling for Kafka connectivity.
 */
public class KafkaConfig {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);
    
    private final Properties kafkaProperties;
    private final String groupId;
    
    /**
     * Creates a new KafkaConfig instance.
     *
     * @param kafkaProperties Kafka connection properties
     * @param groupId Consumer group ID for this application
     */
    public KafkaConfig(Properties kafkaProperties, String groupId) {
        this.kafkaProperties = validateProperties(kafkaProperties);
        this.groupId = groupId;
    }
    
    /**
     * Creates a TableDescriptor for Kafka source with proper error handling.
     *
     * @param topic Kafka topic name
     * @param schema Table schema
     * @return TableDescriptor configured for Kafka
     * @throws IllegalStateException if required properties are missing
     */
    public TableDescriptor createSourceDescriptor(String topic, Schema schema) {
        try {
            String bootstrapServers = getRequiredProperty("bootstrap.servers");
            String schemaRegistryUrl = getRequiredProperty("schema.registry.url");
            
            return TableDescriptor.forConnector("kafka")
                    .schema(schema)
                    .option("topic", topic)
                    .option("properties.bootstrap.servers", bootstrapServers)
                    .option("properties.group.id", groupId)
                    .option("scan.startup.mode", "earliest-offset")
                    .option("format", "avro-confluent")
                    .option("avro-confluent.url", schemaRegistryUrl)
                    .option("properties.auto.offset.reset", "earliest")
                    .build();
        } catch (Exception e) {
            String errorMsg = String.format("Failed to create Kafka source descriptor for topic %s: %s", topic, e.getMessage());
            LOG.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    private Properties validateProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Kafka properties cannot be null");
        }
        
        Properties validatedProps = new Properties();
        validatedProps.putAll(properties);
        
        // Set default values if not provided
        setDefaultIfMissing(validatedProps, "bootstrap.servers", "localhost:9092");
        setDefaultIfMissing(validatedProps, "schema.registry.url", "http://localhost:8081");
        
        return validatedProps;
    }
    
    private void setDefaultIfMissing(Properties props, String key, String defaultValue) {
        if (!props.containsKey(key)) {
            LOG.warn("Property {} not set, using default value: {}", key, defaultValue);
            props.setProperty(key, defaultValue);
        }
    }
    
    private String getRequiredProperty(String key) {
        String value = kafkaProperties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required Kafka property '" + key + "' is missing or empty");
        }
        return value;
    }
}