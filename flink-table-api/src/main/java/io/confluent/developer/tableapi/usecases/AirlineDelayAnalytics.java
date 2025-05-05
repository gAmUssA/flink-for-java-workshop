package io.confluent.developer.tableapi.usecases;

import io.confluent.developer.tableapi.common.FlightSchema;
import io.confluent.developer.tableapi.common.KafkaConfig;
import io.confluent.developer.utils.ConfigurationManager;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.expressions.TimePointUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.flink.table.api.Expressions.*;

/**
 * Airline Delay Analytics - Analyzes airline delay performance.
 * <p>
 * This use case analyzes the delay performance of airlines, including average
 * delay times, percentage of on-time flights, and delay trends over time.
 */
public class AirlineDelayAnalytics {
    private static final Logger LOG = LoggerFactory.getLogger(AirlineDelayAnalytics.class);

    private final StreamExecutionEnvironment streamEnv;
    private final StreamTableEnvironment tableEnv;
    private final ConfigurationManager configManager;

    /**
     * Create a new AirlineDelayAnalytics.
     *
     * @param streamEnv The Flink stream execution environment
     * @param tableEnv  The Flink table environment
     * @param environment The environment (local or cloud)
     */
    public AirlineDelayAnalytics(
            StreamExecutionEnvironment streamEnv,
            StreamTableEnvironment tableEnv,
            String environment) {
        this.streamEnv = streamEnv;
        this.tableEnv = tableEnv;
        this.configManager = new ConfigurationManager(environment, "flink-table-api");
    }

    /**
     * Process airline delay information.
     * <p>
     * This method creates the necessary tables and executes the queries to
     * process airline delay information using the Table API.
     *
     * @return Table containing the airline delay information
     */
    public Table process() {
        LOG.info("Processing airline delay analytics...");

        // Create all necessary tables
        createFlightsAndDelaysTables();

        // Get the airline delay table name
        String airlineDelayTable = configManager.getTableName("airline-delay-performance", "AirlineDelayPerformance");

        // Create a view with on_time_percentage calculation
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW airline_delay_with_percentage AS " +
            "SELECT " +
            "  airline_code, " +
            "  total_flights, " +
            "  departed_flights, " +
            "  cancelled_flights, " +
            "  avg_delay_minutes, " +
            "  CASE " +
            "    WHEN departed_flights = 0 THEN NULL " +
            "    ELSE (on_time_flights * 100.0 / departed_flights) " +
            "  END AS on_time_percentage " +
            "FROM " + airlineDelayTable
        );

        // Query the view with on_time_percentage
        Table result = tableEnv.from("airline_delay_with_percentage");

        LOG.info("Executing airline delay query with Table API");
        return result;
    }

    /**
     * Process hourly delay analytics.
     * <p>
     * This method queries the hourly delays view to analyze delay trends over time using Table API.
     * If the hourly delays table doesn't exist, it will create it first.
     *
     * @return TableResult containing the hourly delay analytics
     */
    public TableResult processHourlyDelays() {
        LOG.info("Processing hourly delay analytics...");

        String hourlyDelaysTable = configManager.getTableName("hourly-delays", "HourlyDelays");

        // Check if the hourly delays table exists, if not create it
        try {
            tableEnv.from(hourlyDelaysTable);
        } catch (Exception e) {
            LOG.info("Hourly delays table not found, creating it first...");
            createFlightsAndDelaysTables();
        }

        // Query the hourly delays view using Table API
        Table result = tableEnv.from(hourlyDelaysTable)
                .select(
                        $("hour_window"),
                        $("total_flights"),
                        $("avg_delay_minutes"),
                        $("cancelled_flights"),
                        $("cancelled_flights").cast(DataTypes.DOUBLE())
                                .dividedBy($("total_flights"))
                                .times(lit(100)).as("cancellation_rate")
                );

        LOG.info("Executing hourly delays query with Table API");
        return result.execute();
    }

    /**
     * Creates the flights and delays tables needed for analytics.
     * This is a helper method used by both process() and processHourlyDelays().
     */
    private void createFlightsAndDelaysTables() {
        // Get configuration values
        Properties kafkaProperties = configManager.getProperties();
        String flightsTable = configManager.getTableName("flights", "Flights");
        String flightsTopic = configManager.getTopicName("flights", "flights-avro");
        String airlineDelayTable = configManager.getTableName("airline-delay-performance", "AirlineDelayPerformance");
        String hourlyDelaysTable = configManager.getTableName("hourly-delays", "HourlyDelays");

        LOG.info("Initializing airline delay analytics with tables: flights={}, delays={}, hourly={}",
                flightsTable, airlineDelayTable, hourlyDelaysTable);

        // Special handling for test environment
        if ("test".equals(configManager.getEnvironment())) {
            // For testing, we'll skip creating the Flights table and assume it's already created by the test
            LOG.info("Test environment detected, skipping Kafka table creation");

            // Verify the Flights table exists
            try {
                tableEnv.from(flightsTable);
                LOG.info("Found existing Flights table in test environment");
            } catch (Exception e) {
                LOG.error("Flights table not found in test environment. Make sure it's created by the test.", e);
                throw new RuntimeException("Flights table not found in test environment", e);
            }
        } else {
            // For normal operation, create the flights table using Kafka config
            Schema flightsSchema = FlightSchema.create();
            KafkaConfig kafkaConfig = new KafkaConfig(kafkaProperties, "airline-delay-analytics");
            TableDescriptor flightsDescriptor = kafkaConfig.createSourceDescriptor(flightsTopic, flightsSchema);

            LOG.info("Creating flights table with Table API");
            tableEnv.createTemporaryTable(flightsTable, flightsDescriptor);
        }

        // Get the flights table
        Table flights = tableEnv.from(flightsTable);

        // Create a temporary view for calculating on-time flights
        tableEnv.createTemporaryView("flights_temp", flights);

        // Use SQL to create a view with on-time flights
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW flights_with_delay AS " +
            "SELECT *, " +
            "  CASE " +
            "    WHEN status = 'CANCELLED' OR actual_departure IS NULL THEN 0 " +
            "    ELSE " +
            "      CAST(UNIX_TIMESTAMP(CAST(actual_departure AS STRING)) - " +
            "           UNIX_TIMESTAMP(CAST(COALESCE(scheduled_departure, CURRENT_TIMESTAMP) AS STRING)) " +
            "           AS INT) / 60 " +
            "  END AS delay_minutes, " +
            "  CASE " +
            "    WHEN status = 'CANCELLED' OR actual_departure IS NULL THEN 0 " +
            "    WHEN " +
            "      CAST(UNIX_TIMESTAMP(CAST(actual_departure AS STRING)) - " +
            "           UNIX_TIMESTAMP(CAST(COALESCE(scheduled_departure, CURRENT_TIMESTAMP) AS STRING)) " +
            "           AS INT) / 60 <= 15 THEN 1 " +
            "    ELSE 0 " +
            "  END AS is_on_time " +
            "FROM flights_temp"
        );

        // Create the airline delay performance view using SQL
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW " + airlineDelayTable + " AS " +
            "SELECT " +
            "  airline_code, " +
            "  COUNT(*) AS total_flights, " +
            "  COUNT(actual_departure) AS departed_flights, " +
            "  SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_flights, " +
            "  SUM(delay_minutes) AS total_delay_minutes, " +
            "  AVG(delay_minutes) AS avg_delay_minutes, " +
            "  SUM(is_on_time) AS on_time_flights " +
            "FROM flights_with_delay " +
            "GROUP BY airline_code"
        );

        LOG.info("Creating airline delay performance view with SQL");

        // Create a SQL view with hour extraction
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW flights_with_hour AS " +
            "SELECT *, " +
            "  DATE_FORMAT(scheduled_departure, 'yyyy-MM-dd HH:00:00') AS hour_window " +
            "FROM flights_with_delay"
        );

        // Create the hourly delays view using Table API with the SQL-created view
        Table flightsWithHour = tableEnv.from("flights_with_hour");

        // Create the hourly delays view using Table API
        Table hourlyDelays = flightsWithHour
                .groupBy($("hour_window"))
                .select(
                        $("hour_window"),
                        lit(1).count().as("total_flights"),
                        $("delay_minutes").avg().as("avg_delay_minutes"),
                        $("status").isEqual(lit("CANCELLED")).count().as("cancelled_flights")
                );

        LOG.info("Creating hourly delays view with Table API");
        tableEnv.createTemporaryView(hourlyDelaysTable, hourlyDelays);
    }
}
