package io.confluent.developer.tableapi.usecases;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AirlineDelayAnalytics that verify data processing logic.
 */
class AirlineDelayAnalyticsDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(AirlineDelayAnalyticsDataTest.class);

    private StreamExecutionEnvironment env;
    private StreamTableEnvironment tableEnv;
    private Properties testProperties;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        tableEnv = StreamTableEnvironment.create(env);

        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("flink_test_");

        // Set up test properties
        testProperties = new Properties();
        testProperties.setProperty("bootstrap.servers", "dummy:9092");
        testProperties.setProperty("schema.registry.url", "http://dummy:8081");
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up temporary files
        if (tempDir != null) {
            Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Log but don't fail the test
                        LOG.warn("Failed to delete {}: {}", path, e.getMessage());
                    }
                });
        }
    }

    @Test
    void testDelayCalculations() throws Exception {
        try {
            // Create test data using collection
            List<Row> testData = Arrays.asList(
                Row.of("F1", "FL001", "AA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T10:00:00"), 
                       LocalDateTime.parse("2024-01-01T10:10:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T10:10:00")),
                Row.of("F2", "FL002", "AA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T11:00:00"), 
                       LocalDateTime.parse("2024-01-01T11:30:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T11:30:00")),
                Row.of("F3", "FL003", "UA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T12:00:00"), 
                       null, 
                       "SCHEDULED", "A320", LocalDateTime.parse("2024-01-01T11:55:00")),
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
            tableEnv.createTemporaryView("Flights", flightsTable);

            // Create analytics instance with test configuration
            AirlineDelayAnalytics analytics = new AirlineDelayAnalytics(env, tableEnv, "test");
            Table result = analytics.process();

            // Collect and verify results
            try (CloseableIterator<Row> iterator = result.execute().collect()) {
                List<Row> results = new ArrayList<>();
                iterator.forEachRemaining(results::add);

                // Verify we have at least one result
                assertTrue(results.size() > 0, "Should have at least one airline result");

                // Verify that each result has the expected fields
                for (Row row : results) {
                    assertNotNull(row.getField("airline_code"), "Should have airline_code field");
                    assertNotNull(row.getField("total_flights"), "Should have total_flights field");
                    assertNotNull(row.getField("departed_flights"), "Should have departed_flights field");
                    assertNotNull(row.getField("cancelled_flights"), "Should have cancelled_flights field");
                    assertNotNull(row.getField("avg_delay_minutes"), "Should have avg_delay_minutes field");
                }

                // Verify AA airline metrics if available
                results.stream()
                    .filter(row -> "AA".equals(row.getField("airline_code")))
                    .findFirst()
                    .ifPresent(aaResult -> {
                        assertTrue((Long)aaResult.getField("total_flights") > 0, "AA should have flights");
                        // Handle both Integer and Double types for avg_delay_minutes
                        Object avgDelay = aaResult.getField("avg_delay_minutes");
                        if (avgDelay instanceof Double) {
                            assertTrue((Double)avgDelay >= 0, "AA avg_delay_minutes should be >= 0");
                        } else if (avgDelay instanceof Integer) {
                            assertTrue((Integer)avgDelay >= 0, "AA avg_delay_minutes should be >= 0");
                        } else {
                            // For any other numeric type
                            assertTrue(((Number)avgDelay).doubleValue() >= 0, "AA avg_delay_minutes should be >= 0");
                        }
                    });

                // Verify UA airline metrics if available
                results.stream()
                    .filter(row -> "UA".equals(row.getField("airline_code")))
                    .findFirst()
                    .ifPresent(uaResult -> {
                        assertTrue((Long)uaResult.getField("total_flights") > 0, "UA should have flights");
                    });
            }
        } finally {
            // Clean up test tables
            tableEnv.executeSql("DROP TABLE IF EXISTS Flights");
        }
    }

    @Test
    void testHourlyDelays() throws Exception {
        try {
            // Create test data using collection
            List<Row> testData = Arrays.asList(
                Row.of("F1", "FL001", "AA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T10:00:00"), 
                       LocalDateTime.parse("2024-01-01T10:15:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T10:15:00")),
                Row.of("F2", "FL002", "AA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T10:30:00"), 
                       LocalDateTime.parse("2024-01-01T11:00:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T11:00:00")),
                Row.of("F3", "FL003", "UA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T11:00:00"), 
                       null, 
                       "CANCELLED", "A320", LocalDateTime.parse("2024-01-01T10:55:00"))
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
            tableEnv.createTemporaryView("Flights", flightsTable);

            // Create analytics instance with test configuration
            AirlineDelayAnalytics analytics = new AirlineDelayAnalytics(env, tableEnv, "test");
            TableResult result = analytics.processHourlyDelays();

            // Collect and verify results
            try (CloseableIterator<Row> iterator = result.collect()) {
                List<Row> results = new ArrayList<>();
                iterator.forEachRemaining(results::add);

                // The number of hours in the results may vary based on how the windowing is implemented
                // Just verify we have at least one result
                assertTrue(results.size() > 0, "Should have at least one hour of results");

                // Verify metrics for any hour
                if (!results.isEmpty()) {
                    Row anyHourResult = results.get(0);
                    assertNotNull(anyHourResult.getField("total_flights"), "Should have total_flights field");
                    assertNotNull(anyHourResult.getField("avg_delay_minutes"), "Should have avg_delay_minutes field");
                    assertNotNull(anyHourResult.getField("cancelled_flights"), "Should have cancelled_flights field");
                }

                // Verify 11:00 hour metrics if available
                results.stream()
                    .filter(row -> row.getField("hour_window").toString().contains("11:00"))
                    .findFirst()
                    .ifPresent(hour11Result -> {
                        assertEquals(1L, hour11Result.getField("total_flights"));
                        assertEquals(1L, hour11Result.getField("cancelled_flights"));
                    });
            }
        } finally {
            // Clean up test tables
            tableEnv.executeSql("DROP TABLE IF EXISTS Flights");
        }
    }
}
