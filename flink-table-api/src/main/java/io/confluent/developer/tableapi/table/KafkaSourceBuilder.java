package io.confluent.developer.tableapi.table;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Builder for creating Kafka sources with proper configuration.
 */
public class KafkaSourceBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaSourceBuilder.class);

    /**
     * Builds a KafkaSource for consuming Flight records.
     *
     * @param topic Kafka topic to read from
     * @param properties Kafka properties
     * @return Configured KafkaSource
     */
    public static KafkaSource<Row> buildKafkaSource(String topic, Properties properties) {
        LOG.info("Building Kafka source for topic: {}", topic);
        
        // Extract necessary properties
        String bootstrapServers = properties.getProperty("bootstrap.servers");
        String groupId = properties.getProperty("group.id", "flink-sql-processor");
        
        // Build the Kafka source
        KafkaSource<Row> kafkaSource = KafkaSource.<Row>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(AvroTableUtils.createFlightDeserializationSchema(properties))
                .setProperties(properties)
                .build();
        
        LOG.info("Kafka source built successfully for topic: {}", topic);
        return kafkaSource;
    }
}