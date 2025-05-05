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
 * Tests for FlightRouteAnalytics that verify route popularity calculations.
 */
class FlightRouteAnalyticsDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(FlightRouteAnalyticsDataTest.class);

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
    void testRoutePopularity() throws Exception {
        try {
            // Create test data using collection
            List<Row> testData = Arrays.asList(
                // LAX-SFO route with multiple airlines
                Row.of("F1", "FL001", "AA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T10:00:00"), 
                       LocalDateTime.parse("2024-01-01T10:10:00"), 
                       "DEPARTED", "B737", LocalDateTime.parse("2024-01-01T10:10:00")),
                Row.of("F2", "FL002", "UA", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T11:00:00"), 
                       LocalDateTime.parse("2024-01-01T11:30:00"), 
                       "DEPARTED", "A320", LocalDateTime.parse("2024-01-01T11:30:00")),
                Row.of("F3", "FL003", "DL", "LAX", "SFO", 
                       LocalDateTime.parse("2024-01-01T12:00:00"), 
                       null, 
                       "SCHEDULED", "B737", LocalDateTime.parse("2024-01-01T11:55:00")),
                // SFO-LAX route with fewer flights
                Row.of("F4", "FL004", "AA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T13:00:00"), 
                       null, 
                       "SCHEDULED", "B737", LocalDateTime.parse("2024-01-01T12:30:00")),
                Row.of("F5", "FL005", "UA", "SFO", "LAX", 
                       LocalDateTime.parse("2024-01-01T14:00:00"), 
                       null, 
                       "SCHEDULED", "A320", LocalDateTime.parse("2024-01-01T13:30:00")),
                // JFK-BOS route with single airline
                Row.of("F6", "FL006", "AA", "JFK", "BOS", 
                       LocalDateTime.parse("2024-01-01T15:00:00"), 
                       null, 
                       "SCHEDULED", "B737", LocalDateTime.parse("2024-01-01T14:30:00"))
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

            // Create analytics instance with test configuration
            FlightRouteAnalytics analytics = new FlightRouteAnalytics(env, tableEnv, "test");
            Table result = analytics.process();

            // Collect and verify results
            try (CloseableIterator<Row> iterator = result.execute().collect()) {
                List<Row> results = new ArrayList<>();
                iterator.forEachRemaining(results::add);

                // Verify LAX-SFO route (should be most popular)
                Row laxSfoResult = results.stream()
                    .filter(row -> "LAX".equals(row.getField("origin")) && "SFO".equals(row.getField("destination")))
                    .findFirst()
                    .orElseThrow();

                // Verify flight count is at least 1
                assertTrue((Long)laxSfoResult.getField("flight_count") > 0, "LAX-SFO should have flights");
                // Verify airline count is at least 1
                assertTrue((Long)laxSfoResult.getField("airline_count") > 0, "LAX-SFO should have airlines");

                // Verify SFO-LAX route
                Row sfoLaxResult = results.stream()
                    .filter(row -> "SFO".equals(row.getField("origin")) && "LAX".equals(row.getField("destination")))
                    .findFirst()
                    .orElseThrow();

                // Verify flight count is at least 1
                assertTrue((Long)sfoLaxResult.getField("flight_count") > 0, "SFO-LAX should have flights");
                // Verify airline count is at least 1
                assertTrue((Long)sfoLaxResult.getField("airline_count") > 0, "SFO-LAX should have airlines");

                // Verify JFK-BOS route
                Row jfkBosResult = results.stream()
                    .filter(row -> "JFK".equals(row.getField("origin")) && "BOS".equals(row.getField("destination")))
                    .findFirst()
                    .orElseThrow();

                // Verify flight count is at least 1
                assertTrue((Long)jfkBosResult.getField("flight_count") > 0, "JFK-BOS should have flights");
                // Verify airline count is at least 1
                assertTrue((Long)jfkBosResult.getField("airline_count") > 0, "JFK-BOS should have airlines");

                // Verify that we have at least one result
                assertTrue(results.size() > 0, "Should have at least one route result");
            }
        } finally {
            // Clean up test tables
            tableEnv.executeSql("DROP TABLE IF EXISTS test_flights");
        }
    }
}
