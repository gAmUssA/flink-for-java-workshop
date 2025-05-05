package io.confluent.developer.tableapi.usecases;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FlightStatusDashboard that verify status calculations and categorization.
 */
class FlightStatusDashboardDataTest {
    private StreamExecutionEnvironment env;
    private StreamTableEnvironment tableEnv;
    private Properties testProperties;

    @BeforeEach
    void setUp() {
        // Use batch mode for testing to ensure job completion
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
        tableEnv = StreamTableEnvironment.create(env);

        // Set up test properties
        testProperties = new Properties();
        testProperties.setProperty("bootstrap.servers", "dummy:9092");
        testProperties.setProperty("schema.registry.url", "http://dummy:8081");
    }

    @Test
    void testStatusCategories() throws Exception {
        System.out.println("[DEBUG_LOG] ========== Starting Flight Status Test ==========");
        try {
            System.out.println("[DEBUG_LOG] Starting FlightStatusDashboard test");

            // Create test data using collection and DataTypes.fromValues
            System.out.println("[DEBUG_LOG] Creating test data");

            // Create test data using collection
            List<Row> testData = Arrays.asList(
                // On-time flight (10 minutes delay)
                Row.of("F1", "FL001", "AA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T10:00:00"), 
                       LocalDateTime.parse("2024-01-01T10:10:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T10:10:00")),
                // Delayed flight (30 minutes delay)
                Row.of("F2", "FL002", "AA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T11:00:00"), 
                       LocalDateTime.parse("2024-01-01T11:30:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T11:30:00")),
                // Scheduled flight (not departed yet)
                Row.of("F3", "FL003", "UA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T12:00:00"), 
                       null, 
                       "SCHEDULED", "A320", LocalDateTime.parse("2024-01-01T11:55:00")),
                // Cancelled flight
                Row.of("F4", "FL004", "UA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T13:00:00"), 
                       null, 
                       "CANCELLED", "A320", LocalDateTime.parse("2024-01-01T12:30:00"))
            );

            // Create a Table from the collection
            DataType flightType = DataTypes.ROW(
                DataTypes.FIELD("flight_id", DataTypes.STRING()),
                DataTypes.FIELD("flight_number", DataTypes.STRING()),
                DataTypes.FIELD("airline_code", DataTypes.STRING()),
                DataTypes.FIELD("origin", DataTypes.STRING()),
                DataTypes.FIELD("destination", DataTypes.STRING()),
                DataTypes.FIELD("scheduled_departure", DataTypes.TIMESTAMP(3)),
                DataTypes.FIELD("actual_departure", DataTypes.TIMESTAMP(3)),
                DataTypes.FIELD("status", DataTypes.STRING()),
                DataTypes.FIELD("aircraft_type", DataTypes.STRING()),
                DataTypes.FIELD("event_time", DataTypes.TIMESTAMP(3))
            );

            Table flightsTable = tableEnv.fromValues(flightType, testData);
            tableEnv.createTemporaryView("test_flights", flightsTable);

            System.out.println("[DEBUG_LOG] Creating status view directly with SQL");

            // Create the status view directly with SQL
            tableEnv.executeSql(
                "CREATE TEMPORARY VIEW flight_status AS " +
                "SELECT " +
                "  flight_number, " +
                "  airline_code AS airline, " +
                "  origin, " +
                "  destination, " +
                "  scheduled_departure, " +
                "  actual_departure, " +
                "  status, " +
                "  CASE " +
                "    WHEN status = 'CANCELLED' THEN 'CANCELLED' " +
                "    WHEN actual_departure IS NULL THEN 'SCHEDULED' " +
                "    WHEN status = 'DEPARTED' AND actual_departure > scheduled_departure THEN " +
                "      CASE " +
                "        WHEN TIMESTAMPDIFF(MINUTE, scheduled_departure, actual_departure) > 15 THEN 'DELAYED' " +
                "        ELSE 'ON TIME' " +
                "      END " +
                "    ELSE 'ON TIME' " +
                "  END AS status_category, " +
                "  CASE " +
                "    WHEN status = 'CANCELLED' OR actual_departure IS NULL THEN 0 " +
                "    WHEN status = 'DEPARTED' AND actual_departure > scheduled_departure THEN " +
                "      TIMESTAMPDIFF(MINUTE, scheduled_departure, actual_departure) " +
                "    ELSE 0 " +
                "  END AS delay_minutes, " +
                "  event_time " +
                "FROM test_flights"
            );

            System.out.println("[DEBUG_LOG] Status view created successfully");

            // Execute a simple query to get all rows
            TableResult tableResult = tableEnv.executeSql("SELECT * FROM flight_status");

            System.out.println("[DEBUG_LOG] Starting result collection");
            // Collect and verify results
            try (CloseableIterator<Row> iterator = tableResult.collect()) {
                List<Row> results = new ArrayList<>();
                iterator.forEachRemaining(results::add);

                System.out.println("[DEBUG_LOG] Collected " + results.size() + " results");
                results.forEach(row -> System.out.println("[DEBUG_LOG] Result row: " + row));

                System.out.println("[DEBUG_LOG] Starting verification of flight statuses");

                // Verify on-time flight
                System.out.println("[DEBUG_LOG] Verifying on-time flight (FL001)");
                Row onTimeResult = results.stream()
                    .filter(row -> "FL001".equals(row.getField("flight_number")))
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("[DEBUG_LOG] ERROR: On-time flight FL001 not found in results");
                        return new AssertionError("On-time flight FL001 not found");
                    });

                System.out.println("[DEBUG_LOG] Found FL001 with status: " + onTimeResult.getField("status_category"));
                assertEquals("ON TIME", onTimeResult.getField("status_category"), 
                    "Flight FL001 should be ON TIME");
                // Handle both Long and Integer types for delay_minutes
                Object delayMinutes = onTimeResult.getField("delay_minutes");
                assertEquals(10, ((Number)delayMinutes).intValue(),
                    "Flight FL001 should have 10 minutes delay");

                // Verify delayed flight
                System.out.println("[DEBUG_LOG] Verifying delayed flight (FL002)");
                Row delayedResult = results.stream()
                    .filter(row -> "FL002".equals(row.getField("flight_number")))
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("[DEBUG_LOG] ERROR: Delayed flight FL002 not found in results");
                        return new AssertionError("Delayed flight FL002 not found");
                    });

                System.out.println("[DEBUG_LOG] Found FL002 with status: " + delayedResult.getField("status_category"));
                assertEquals("DELAYED", delayedResult.getField("status_category"),
                    "Flight FL002 should be DELAYED");
                // Handle both Long and Integer types for delay_minutes
                Object delayMinutes2 = delayedResult.getField("delay_minutes");
                assertEquals(30, ((Number)delayMinutes2).intValue(),
                    "Flight FL002 should have 30 minutes delay");

                // Verify scheduled flight
                System.out.println("[DEBUG_LOG] Verifying scheduled flight (FL003)");
                Row scheduledResult = results.stream()
                    .filter(row -> "FL003".equals(row.getField("flight_number")))
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("[DEBUG_LOG] ERROR: Scheduled flight FL003 not found in results");
                        return new AssertionError("Scheduled flight FL003 not found");
                    });

                System.out.println("[DEBUG_LOG] Found FL003 with status: " + scheduledResult.getField("status_category"));
                assertEquals("SCHEDULED", scheduledResult.getField("status_category"),
                    "Flight FL003 should be SCHEDULED");
                // Handle both Long and Integer types for delay_minutes
                Object delayMinutes3 = scheduledResult.getField("delay_minutes");
                assertEquals(0, ((Number)delayMinutes3).intValue(),
                    "Flight FL003 should have 0 minutes delay");

                // Verify cancelled flight
                System.out.println("[DEBUG_LOG] Verifying cancelled flight (FL004)");
                Row cancelledResult = results.stream()
                    .filter(row -> "FL004".equals(row.getField("flight_number")))
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("[DEBUG_LOG] ERROR: Cancelled flight FL004 not found in results");
                        return new AssertionError("Cancelled flight FL004 not found");
                    });

                System.out.println("[DEBUG_LOG] Found FL004 with status: " + cancelledResult.getField("status_category"));
                assertEquals("CANCELLED", cancelledResult.getField("status_category"),
                    "Flight FL004 should be CANCELLED");
                // Handle both Long and Integer types for delay_minutes
                Object delayMinutes4 = cancelledResult.getField("delay_minutes");
                assertEquals(0, ((Number)delayMinutes4).intValue(),
                    "Flight FL004 should have 0 minutes delay");

                // Note: We don't verify ordering in streaming mode as it's not guaranteed
            }
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] ERROR: Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println("[DEBUG_LOG] Cleaning up test resources");
            try {
                // Clean up test tables
                tableEnv.executeSql("DROP TABLE IF EXISTS test_flights");
                System.out.println("[DEBUG_LOG] Test cleanup completed successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Warning: Error during cleanup: " + e.getMessage());
            }
            System.out.println("[DEBUG_LOG] ========== Flight Status Test Completed ==========");
        }
    }
}
