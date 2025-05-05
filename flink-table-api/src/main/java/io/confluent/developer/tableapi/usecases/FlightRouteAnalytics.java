package io.confluent.developer.tableapi.usecases;

import io.confluent.developer.tableapi.common.FlightSchema;
import io.confluent.developer.tableapi.common.KafkaConfig;
import io.confluent.developer.utils.ConfigurationManager;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.flink.table.api.Expressions.*;

/**
 * Flight Route Analytics - Analyzes the popularity of flight routes.
 * <p>
 * This use case identifies the most popular flight routes based on the number
 * of flights between origin and destination airports.
 */
public class FlightRouteAnalytics {
    private static final Logger LOG = LoggerFactory.getLogger(FlightRouteAnalytics.class);

    private final StreamExecutionEnvironment streamEnv;
    private final StreamTableEnvironment tableEnv;
    private final ConfigurationManager configManager;

    /**
     * Create a new FlightRouteAnalytics.
     *
     * @param streamEnv The Flink stream execution environment
     * @param tableEnv  The Flink table environment
     * @param environment The environment (local or cloud)
     */
    public FlightRouteAnalytics(
            StreamExecutionEnvironment streamEnv,
            StreamTableEnvironment tableEnv,
            String environment) {
        this.streamEnv = streamEnv;
        this.tableEnv = tableEnv;
        this.configManager = new ConfigurationManager(environment, "flink-table-api");
    }

    /**
     * Process flight route information.
     * <p>
     * This method creates the necessary tables and executes the queries to
     * process flight route information using the Table API.
     *
     * @return Table containing the flight route information
     */
    public Table process() {
        LOG.info("Processing flight route analytics...");

        // Get configuration values
        Properties kafkaProperties = configManager.getProperties();
        String flightsTable = configManager.getTableName("flights", "Flights");
        String flightsTopic = configManager.getTopicName("flights", "flights-avro");
        String routePopularityTable = configManager.getTableName("route-popularity", "RoutePopularity");

        LOG.info("Initializing flight route analytics with tables: flights={}, routes={}",
                flightsTable, routePopularityTable);

        // Special handling for test environment
        if ("test".equals(configManager.getEnvironment())) {
            // For testing, we'll skip creating the Flights table and assume it's already created by the test
            LOG.info("Test environment detected, skipping Kafka table creation");

            // Verify the Flights table exists
            try {
                tableEnv.from(flightsTable);
                LOG.info("Found existing Flights table in test environment");
            } catch (Exception e) {
                // In test environment, check if test_flights exists instead
                try {
                    tableEnv.from("test_flights");
                    LOG.info("Found test_flights table in test environment, using it instead of {}", flightsTable);
                    flightsTable = "test_flights";
                } catch (Exception e2) {
                    LOG.error("Neither {} nor test_flights table found in test environment. Make sure one is created by the test.", flightsTable, e);
                    throw new RuntimeException("No flights table found in test environment", e);
                }
            }
        } else {
            // Create the flights table using common schema and Kafka config
            Schema flightsSchema = FlightSchema.create();
            KafkaConfig kafkaConfig = new KafkaConfig(kafkaProperties, "flight-route-analytics");
            TableDescriptor flightsDescriptor = kafkaConfig.createSourceDescriptor(flightsTopic, flightsSchema);

            LOG.info("Creating flights table with Table API");
            tableEnv.createTemporaryTable(flightsTable, flightsDescriptor);
        }

        // Get the flights table
        Table flights = tableEnv.from(flightsTable);

        // Create a temporary view with distinct airlines per route
        tableEnv.createTemporaryView("flights_temp", flights);

        // Use SQL to calculate distinct airline count to avoid nested aggregates
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW route_airlines AS " +
            "SELECT origin, destination, COUNT(DISTINCT airline_code) AS airline_count " +
            "FROM " + flightsTable + " " +
            "GROUP BY origin, destination"
        );

        // Create the route popularity view using Table API
        Table flightCounts = flights
                .groupBy($("origin"), $("destination"))
                .select(
                        $("origin"),
                        $("destination"),
                        $("flight_id").count().as("flight_count")
                );

        // Create a view for the flight counts
        tableEnv.createTemporaryView("flight_counts", flightCounts);

        // Use SQL to join the flight counts with the airline counts
        Table routePopularity = tableEnv.sqlQuery(
            "SELECT " +
            "  fc.origin, " +
            "  fc.destination, " +
            "  fc.flight_count, " +
            "  ra.airline_count " +
            "FROM flight_counts fc " +
            "JOIN route_airlines ra " +
            "  ON fc.origin = ra.origin AND fc.destination = ra.destination"
        );

        LOG.info("Creating route popularity view with Table API");
        tableEnv.createTemporaryView(routePopularityTable, routePopularity);

        // Query the route popularity view using Table API
        Table result = tableEnv.from(routePopularityTable)
                .select(
                        $("origin"),
                        $("destination"),
                        $("flight_count"),
                        $("airline_count")
                )
                .orderBy($("flight_count").desc())
                .limit(10);

        LOG.info("Executing flight route query with Table API");
        return result;
    }
}
