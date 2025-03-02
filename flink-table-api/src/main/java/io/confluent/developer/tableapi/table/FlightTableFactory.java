package io.confluent.developer.tableapi.table;

import io.confluent.developer.models.flight.Flight;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Factory for creating Flight tables from Kafka sources.
 */
public class FlightTableFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FlightTableFactory.class);
    
    /**
     * Creates a Flight table from a Kafka source.
     *
     * @param env StreamExecutionEnvironment
     * @param tableEnv StreamTableEnvironment
     * @param tableName Name of the table to create
     * @param topic Kafka topic to read from
     * @param properties Kafka properties
     * @return The created Table
     */
    public static Table createFlightTable(
            StreamExecutionEnvironment env,
            StreamTableEnvironment tableEnv,
            String tableName,
            String topic,
            Properties properties) {
        
        LOG.info("Creating Flight table '{}' from topic '{}'", tableName, topic);
        
        // Create Kafka source
        KafkaSource<Row> kafkaSource = KafkaSourceBuilder.buildKafkaSource(topic, properties);
        
        // Create DataStream from source
        DataStream<Row> flightStream = env.fromSource(
                kafkaSource,
                org.apache.flink.api.common.eventtime.WatermarkStrategy.noWatermarks(),
                "Kafka Source: " + topic,
                AvroTableUtils.getFlightRowTypeInfo()
        );
        
        // Convert DataStream to Table
        Schema schema = Schema.newBuilder()
                .column("flightNumber", "STRING")
                .column("airline", "STRING")
                .column("origin", "STRING")
                .column("destination", "STRING")
                .column("scheduledDeparture", "BIGINT")
                .column("actualDeparture", "BIGINT")
                .column("status", "STRING")
                .column("event_time", "TIMESTAMP(3)")
                .watermark("event_time", "event_time - INTERVAL '5' SECOND")
                .build();
        
        // Create a temporary view
        tableEnv.createTemporaryView(tableName, flightStream, schema);
        
        LOG.info("Flight table '{}' created successfully", tableName);
        
        // Return the table
        return tableEnv.from(tableName);
    }
}