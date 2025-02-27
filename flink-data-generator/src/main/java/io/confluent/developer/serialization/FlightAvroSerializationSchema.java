package io.confluent.developer.serialization;

import io.confluent.developer.models.flight.Flight;
import io.confluent.developer.utils.ConfigUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Serialization schema for Flight records using Avro and Schema Registry.
 */
public class FlightAvroSerializationSchema implements KafkaRecordSerializationSchema<Flight> {
    private static final Logger LOG = LoggerFactory.getLogger(FlightAvroSerializationSchema.class);
    private static final long serialVersionUID = 1L;
    
    private final String topic;
    private final String schemaRegistryUrl;
    private final Properties properties;
    private transient KafkaAvroSerializer serializer;

    /**
     * Constructor for the FlightAvroSerializationSchema.
     *
     * @param topic The Kafka topic to write to
     * @param schemaRegistryUrl The URL of the Schema Registry
     */
    public FlightAvroSerializationSchema(String topic, String schemaRegistryUrl) {
        this(topic, schemaRegistryUrl, new Properties());
    }
    
    /**
     * Constructor for the FlightAvroSerializationSchema with properties file.
     *
     * @param topic The Kafka topic to write to
     * @param schemaRegistryUrl The URL of the Schema Registry
     * @param propertiesFile The properties file to load
     */
    public FlightAvroSerializationSchema(String topic, String schemaRegistryUrl, String propertiesFile) {
        this(topic, schemaRegistryUrl, ConfigUtils.loadProperties(propertiesFile));
    }
    
    /**
     * Constructor for the FlightAvroSerializationSchema with properties.
     *
     * @param topic The Kafka topic to write to
     * @param schemaRegistryUrl The URL of the Schema Registry
     * @param properties The properties to use
     */
    public FlightAvroSerializationSchema(String topic, String schemaRegistryUrl, Properties properties) {
        this.topic = topic;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.properties = properties;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
            Flight flight, 
            KafkaRecordSerializationSchema.KafkaSinkContext context, 
            Long timestamp) {
        if (flight == null) {
            return null;
        }
        
        if (serializer == null) {
            initializeSerializer();
        }
        
        // Use flight number as the key for partitioning
        byte[] key = flight.getFlightNumber().toString().getBytes();
        byte[] value = serializer.serialize(topic, flight);
        
        return new ProducerRecord<>(topic, null, timestamp != null ? timestamp : System.currentTimeMillis(), key, value);
    }
    
    private void initializeSerializer() {
        try {
            // Create and configure the serializer
            serializer = new KafkaAvroSerializer();
            Map<String, Object> configs = new HashMap<>();
            
            // Basic Schema Registry configuration
            configs.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
            configs.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
            
            // Check if we need to add authentication for Schema Registry
            String environment = ConfigUtils.getProperty(properties, "environment", "local");
            if ("cloud".equalsIgnoreCase(environment)) {
                String basicAuthUserInfo = ConfigUtils.getProperty(properties, "schema.registry.basic.auth.user.info", "");
                String basicAuthSource = ConfigUtils.getProperty(properties, "schema.registry.basic.auth.credentials.source", "USER_INFO");
                
                if (!basicAuthUserInfo.isEmpty()) {
                    configs.put(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, basicAuthSource);
                    configs.put(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, basicAuthUserInfo);
                    LOG.info("Added Schema Registry authentication for cloud environment");
                }
            }
            
            serializer.configure(configs, false);
            
            LOG.info("Initialized Avro serializer with Schema Registry at {}", schemaRegistryUrl);
        } catch (Exception e) {
            LOG.error("Failed to initialize Avro serializer", e);
            throw new RuntimeException("Could not initialize Avro serializer", e);
        }
    }
}
