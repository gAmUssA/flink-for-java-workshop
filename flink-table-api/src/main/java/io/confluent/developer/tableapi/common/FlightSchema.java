package io.confluent.developer.tableapi.common;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;

/**
 * Common schema definition for flight data across all use cases.
 * This class ensures consistency in data structure and handling.
 */
public class FlightSchema {

    /**
     * Creates a standardized schema for flight data.
     *
     * @return Schema object with standardized flight data structure
     */
    public static Schema create() {
        return Schema.newBuilder()
                .column("flight_id", DataTypes.STRING())
                .column("flight_number", DataTypes.STRING())
                .column("airline_code", DataTypes.STRING())
                .column("origin", DataTypes.STRING())
                .column("destination", DataTypes.STRING())
                .column("scheduled_departure", DataTypes.TIMESTAMP(3))
                .column("actual_departure", DataTypes.TIMESTAMP(3))
                .column("status", DataTypes.STRING())
                .column("aircraft_type", DataTypes.STRING())
                .columnByMetadata("event_time", DataTypes.TIMESTAMP(3), "timestamp")
                .watermark("event_time", "event_time - INTERVAL '30' SECONDS")
                .build();
    }

    /**
     * Creates a schema with additional processing time for real-time analytics.
     *
     * @return Schema object with processing time column
     */
    public static Schema createWithProcessingTime() {
        return Schema.newBuilder()
                .column("flight_id", DataTypes.STRING())
                .column("flight_number", DataTypes.STRING())
                .column("airline_code", DataTypes.STRING())
                .column("origin", DataTypes.STRING())
                .column("destination", DataTypes.STRING())
                .column("scheduled_departure", DataTypes.TIMESTAMP(3))
                .column("actual_departure", DataTypes.TIMESTAMP(3))
                .column("status", DataTypes.STRING())
                .column("aircraft_type", DataTypes.STRING())
                .columnByExpression("processing_time", "PROCTIME()")
                .columnByMetadata("event_time", DataTypes.TIMESTAMP(3), "timestamp")
                .watermark("event_time", "event_time - INTERVAL '30' SECONDS")
                .build();
    }
}
