package io.confluent.developer.sql.table;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Factory for creating Flight tables using the Flink Table API with Confluent Avro format.
 * This implementation follows the requirements in the PRD to use the Flink Table API builder pattern
 * with Confluent Schema Registry integration.
 */
public class FlightTableApiFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FlightTableApiFactory.class);
    
    /**
     * Creates a Flight table using the Flink Table API with Confluent Avro format.
     *
     * @param tableEnv StreamTableEnvironment
     * @param tableName Name of the table to create
     * @param topic Kafka topic to read from
     * @param properties Kafka properties
     * @return The created Table
     */
    public static Table createFlightTable(
            StreamTableEnvironment tableEnv,
            String tableName,
            String topic,
            Properties properties) {
        
        LOG.info("Creating Flight table '{}' from topic '{}' using Table API", tableName, topic);
        
        // Extract Schema Registry URL from properties
        String schemaRegistryUrl = properties.getProperty("schema.registry.url");
        String bootstrapServers = properties.getProperty("bootstrap.servers");
        
        // Create Schema for Flight data
        // Note: Using TIMESTAMP instead of TIMESTAMP_WITH_LOCAL_TIME_ZONE for Avro compatibility
        Schema schema = Schema.newBuilder()
                .column("flightNumber", DataTypes.STRING())
                .column("airline", DataTypes.STRING())
                .column("origin", DataTypes.STRING())
                .column("destination", DataTypes.STRING())
                .column("scheduledDeparture", DataTypes.BIGINT())
                .column("actualDeparture", DataTypes.BIGINT().nullable())
                .column("status", DataTypes.STRING())
                .column("event_time", DataTypes.TIMESTAMP(3))
                .watermark("event_time", "event_time - INTERVAL '5' SECOND")
                .build();
        
        // Build table descriptor with Kafka connector and Avro-Confluent format
        TableDescriptor.Builder tableDescriptorBuilder = TableDescriptor.forConnector("kafka")
                .schema(schema)
                .option("topic", topic)
                .option("properties.bootstrap.servers", bootstrapServers)
                .option("properties.group.id", properties.getProperty("group.id", "flink-sql-processor"))
                .option("scan.startup.mode", "earliest-offset")
                .option("format", "avro-confluent")
                .option("avro-confluent.url", schemaRegistryUrl);  // Use correct property name
        
        // Add security settings if in cloud environment
        if (properties.containsKey("security.protocol")) {
            tableDescriptorBuilder = tableDescriptorBuilder
                    .option("properties.security.protocol", properties.getProperty("security.protocol"))
                    .option("properties.sasl.mechanism", properties.getProperty("sasl.mechanism"))
                    .option("properties.sasl.jaas.config", properties.getProperty("sasl.jaas.config"));
            
            // Add Schema Registry authentication if available
            if (properties.containsKey("basic.auth.credentials.source")) {
                tableDescriptorBuilder = tableDescriptorBuilder
                        .option("avro-confluent.basic-auth.credentials-source", 
                                properties.getProperty("basic.auth.credentials.source"))
                        .option("avro-confluent.basic-auth.user-info", 
                                properties.getProperty("basic.auth.user.info"));
            }
        }
        
        // Create the table
        tableEnv.createTemporaryTable(tableName, tableDescriptorBuilder.build());
        
        LOG.info("Flight table '{}' created successfully using Table API", tableName);
        
        // Return the table
        return tableEnv.from(tableName);
    }
}
