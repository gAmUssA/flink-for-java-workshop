package io.confluent.developer.generator;

import io.confluent.developer.models.flight.Flight;
import io.confluent.developer.serialization.FlightAvroSerializationSchema;
import io.confluent.developer.utils.ConfigUtils;
import io.confluent.developer.utils.ConfigurationManager;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

/**
 * Main Flink job for generating flight data and sending it to Kafka.
 */
public class DataGeneratorJob {
    private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorJob.class);
    private static final String DEFAULT_PROPERTIES_FILE = "local.properties";
    private static final String LOCAL_PROPERTIES_FILE = "local.properties";
    private static final String CLOUD_PROPERTIES_FILE = "cloud.properties";

    public static void main(String[] args) throws Exception {
        // Parse command line parameters
        final ParameterTool params = ParameterTool.fromArgs(args);
        
        // Get the use case and environment
        String environment = params.get("env", "local");

        LOG.info("Starting Flink Flight Generator application:, environment: {}", environment);

        final Properties properties = new ConfigurationManager(environment, "flight-data-generator").getProperties();
        // Extract configuration values
        // Extract configuration values with consistent property names
        String bootstrapServers = ConfigUtils.getProperty(properties, "bootstrap.servers", "localhost:29092");
        String topicName = ConfigUtils.getProperty(properties, "topic.name", "flights");
        String schemaRegistryUrl = ConfigUtils.getProperty(properties, "schema.registry.url", "http://localhost:8081");
        int generatorRate = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.rate", "10"));
        int generatorParallelism = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.parallelism", "1"));
        
        // Extract environment information consistently
        boolean isCloud = "cloud".equalsIgnoreCase(environment) || 
                         "true".equalsIgnoreCase(properties.getProperty("cloud"));
        
        LOG.info("Starting Flight Data Generator with configuration:");
        LOG.info("  Environment: {}", environment);
        LOG.info("  Bootstrap Servers: {}", bootstrapServers);
        LOG.info("  Topic Name: {}", topicName);
        LOG.info("  Schema Registry URL: {}", schemaRegistryUrl);
        LOG.info("  Generator Rate: {} records/second", generatorRate);
        LOG.info("  Generator Parallelism: {}", generatorParallelism);
        
        // Set up the streaming execution environment
        final StreamExecutionEnvironment flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        flinkEnv.enableCheckpointing(60000); // Checkpoint every 60 seconds
        
        // Create the data generator source
        DataGeneratorSource source = new DataGeneratorSource(generatorRate);
        
        // Create a DataStream from the source
        DataStream<Flight> flightStream = flinkEnv
                .addSource(source)
                .name("Flight Data Generator")
                .uid("flight-generator")
                .setParallelism(generatorParallelism);
        
        // Create Kafka sink with Avro serialization
        KafkaSinkBuilder<Flight> sinkBuilder = KafkaSink.<Flight>builder()
                .setBootstrapServers(bootstrapServers)
                .setRecordSerializer(new FlightAvroSerializationSchema(topicName, schemaRegistryUrl, properties));
        
        // Add Confluent Cloud specific configurations if in cloud environment
        if (isCloud) {
            Properties kafkaProps = new Properties();
            
            // Security settings
            kafkaProps.setProperty("security.protocol", ConfigUtils.getProperty(properties, "security.protocol", ""));
            kafkaProps.setProperty("sasl.mechanism", ConfigUtils.getProperty(properties, "sasl.mechanism", ""));
            kafkaProps.setProperty("sasl.jaas.config", ConfigUtils.getProperty(properties, "sasl.jaas.config", ""));
            
            // Performance settings
            kafkaProps.setProperty("client.dns.lookup", ConfigUtils.getProperty(properties, "client.dns.lookup", ""));
            kafkaProps.setProperty("session.timeout.ms", ConfigUtils.getProperty(properties, "session.timeout.ms", ""));
            kafkaProps.setProperty("acks", ConfigUtils.getProperty(properties, "acks", ""));
            
            // Client ID
            kafkaProps.setProperty("client.id", ConfigUtils.getProperty(properties, "client.id", ""));
            
            // Add all properties to the sink builder
            sinkBuilder.setKafkaProducerConfig(kafkaProps);
            
            LOG.info("Configured for Confluent Cloud with security settings");
        }
        sinkBuilder.setKafkaProducerConfig(properties);        
        KafkaSink<Flight> kafkaSink = sinkBuilder.build();
        
        // Add the sink to the stream
        flightStream.sinkTo(kafkaSink)
                .name("Kafka Sink")
                .uid("kafka-sink");
        
        // Execute the Flink job
        flinkEnv.execute("Flight Data Generator");
    }
}
