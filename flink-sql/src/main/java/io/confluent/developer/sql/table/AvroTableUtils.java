package io.confluent.developer.sql.table;

import io.confluent.developer.models.flight.Flight;
import org.apache.avro.specific.SpecificData;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * Utilities for creating Avro-based table sources and sinks.
 */
public class AvroTableUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AvroTableUtils.class);
    
    private static final TypeInformation<?>[] FIELD_TYPES = new TypeInformation<?>[] {
        Types.STRING,    // flightNumber
        Types.STRING,    // airline
        Types.STRING,    // origin
        Types.STRING,    // destination
        Types.LONG,      // scheduledDeparture
        Types.LONG,      // actualDeparture
        Types.STRING,    // status
        Types.SQL_TIMESTAMP // event_time
    };
    
    private static final String[] FIELD_NAMES = new String[] {
        "flightNumber",
        "airline",
        "origin",
        "destination",
        "scheduledDeparture",
        "actualDeparture",
        "status",
        "event_time"
    };
    
    /**
     * Creates a deserialization schema for Flight Avro records.
     *
     * @param properties Kafka properties
     * @return DeserializationSchema for Flight records
     */
    public static KafkaRecordDeserializationSchema<Row> createFlightDeserializationSchema(Properties properties) {
        LOG.info("Creating Flight deserialization schema");
        
        // Create Avro deserialization schema for Flight records
        DeserializationSchema<Flight> avroSchema = AvroDeserializationSchema.forSpecific(Flight.class);
        
        // Wrap with custom schema that converts Flight to Row
        return new FlightToRowDeserializationSchema(avroSchema);
    }
    
    /**
     * Custom deserialization schema that converts Flight Avro records to Flink Rows.
     */
    private static class FlightToRowDeserializationSchema implements KafkaRecordDeserializationSchema<Row> {
        private final DeserializationSchema<Flight> avroSchema;
        
        public FlightToRowDeserializationSchema(DeserializationSchema<Flight> avroSchema) {
            this.avroSchema = avroSchema;
        }
        
        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<Row> out) throws IOException {
            try {
                // Deserialize the Avro record
                Flight flight = avroSchema.deserialize(record.value());
                
                if (flight != null) {
                    // Convert to Row
                    Row row = new Row(RowKind.INSERT, 8);
                    row.setField(0, flight.getFlightNumber().toString());
                    row.setField(1, flight.getAirline().toString());
                    row.setField(2, flight.getOrigin().toString());
                    row.setField(3, flight.getDestination().toString());
                    row.setField(4, flight.getScheduledDeparture());
                    
                    // Handle null actualDeparture
                    Long actualDeparture = flight.getActualDeparture();
                    row.setField(5, actualDeparture != null ? actualDeparture : null);
                    
                    row.setField(6, flight.getStatus().toString());
                    
                    // Add event_time field
                    row.setField(7, new Timestamp(System.currentTimeMillis()));
                    
                    out.collect(row);
                    LOG.debug("Deserialized flight: {}", flight);
                }
            } catch (Exception e) {
                LOG.error("Error deserializing flight record", e);
                // Don't rethrow to avoid failing the job
            }
        }
        
        @Override
        public TypeInformation<Row> getProducedType() {
            return new RowTypeInfo(FIELD_TYPES, FIELD_NAMES);
        }
    }
    
    /**
     * Gets the row type information for Flight data.
     *
     * @return RowTypeInfo for Flight data
     */
    public static RowTypeInfo getFlightRowTypeInfo() {
        return new RowTypeInfo(FIELD_TYPES, FIELD_NAMES);
    }
    
    /**
     * Creates the field names for the Flight table.
     *
     * @return Array of field names
     */
    public static String[] getFlightFieldNames() {
        return FIELD_NAMES;
    }
    
    /**
     * Creates the field types for the Flight table.
     *
     * @return Array of field types as strings
     */
    public static String[] getFlightFieldTypes() {
        return new String[] {
            "STRING",
            "STRING",
            "STRING",
            "STRING",
            "BIGINT",
            "BIGINT",
            "STRING",
            "TIMESTAMP(3)"
        };
    }
}
