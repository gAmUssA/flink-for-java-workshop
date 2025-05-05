package io.confluent.developer.tableapi;

import io.confluent.developer.utils.ConfigurationManager;
import io.confluent.developer.tableapi.usecases.AirlineDelayAnalytics;
import io.confluent.developer.tableapi.usecases.FlightRouteAnalytics;
import io.confluent.developer.tableapi.usecases.FlightStatusDashboard;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Main class for the Flink Table API application.
 * <p>
 * This application demonstrates various use cases using Flink Table API with Kafka and Avro.
 */
public class FlinkTableApiMain {
    private static final Logger LOG = LoggerFactory.getLogger(FlinkTableApiMain.class);

    public static void main(String[] args) {
        StreamExecutionEnvironment streamEnv = null;
        StreamTableEnvironment tableEnv = null;

        try {
            // Parse command line arguments
            final ParameterTool params = ParameterTool.fromArgs(args);

            // Get the use case and environment
            String useCase = params.get("useCase", "status");
            String environment = params.get("env", "local");

            LOG.info("Starting Flink Table API application with use case: {}, environment: {}", useCase, environment);

            // Set up the streaming execution environment with proper configuration
            Configuration config = new Configuration();
            config.setString("table.exec.state.ttl", "1 h");
            config.setString("table.exec.mini-batch.enabled", "true");
            config.setString("table.exec.mini-batch.allow-latency", "5 s");
            config.setString("pipeline.name", "Flink Table API Workshop");

            streamEnv = StreamExecutionEnvironment.getExecutionEnvironment(config);

            // Configure checkpointing and state backend
            streamEnv.enableCheckpointing(60000); // Enable checkpointing every 60 seconds
            streamEnv.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
            streamEnv.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
            streamEnv.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);

            // Configure restart strategy with fixed delay
            streamEnv.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                    3, // max attempts
                    Time.of(10, TimeUnit.SECONDS) // delay between attempts
                )
            );

            // Set parallelism based on environment
            int parallelism = "local".equals(environment) ? 1 : 4;
            streamEnv.setParallelism(parallelism);

            // Create table environment with proper configuration
            EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .withConfiguration(config)
                .build();

            tableEnv = StreamTableEnvironment.create(streamEnv, settings);

            // Configure table environment with idle state retention of 1 hour
            tableEnv.getConfig().setIdleStateRetention(Duration.ofHours(1));

            LOG.info("Starting Flink Table API application with parallelism: {}", parallelism);

            // Run the selected use case
            switch (useCase.toLowerCase()) {
                case "status":
                    LOG.info("Running Flight Status Dashboard");
                    FlightStatusDashboard statusDashboard = new FlightStatusDashboard(streamEnv, tableEnv, environment);
                    TableResult statusResult = statusDashboard.process().execute();
                    LOG.info("Flight Status Dashboard results:");
                    statusResult.print();
                    break;

                case "routes":
                    LOG.info("Running Flight Route Analytics");
                    FlightRouteAnalytics routeAnalytics = new FlightRouteAnalytics(streamEnv, tableEnv, environment);
                    TableResult routeResult = routeAnalytics.process().execute();
                    LOG.info("Flight Route Analytics results:");
                    routeResult.print();
                    break;

                case "delays":
                    LOG.info("Running Airline Delay Analytics");
                    AirlineDelayAnalytics delayAnalytics = new AirlineDelayAnalytics(streamEnv, tableEnv, environment);
                    TableResult delayResult = delayAnalytics.process().execute();
                    LOG.info("Airline Delay Analytics results:");
                    delayResult.print();
                    break;

                case "all":
                    LOG.info("Running all use cases");
                    try {
                        // Run each analytics separately to avoid schema conflicts
                        LOG.info("Starting Flight Status Dashboard...");
                        FlightStatusDashboard statusDashboard2 = new FlightStatusDashboard(streamEnv, tableEnv, environment);
                        TableResult statusResult2 = statusDashboard2.process().execute();
                        LOG.info("Flight Status Dashboard results:");
                        statusResult2.print();

                        LOG.info("Starting Flight Route Analytics...");
                        FlightRouteAnalytics routeAnalytics2 = new FlightRouteAnalytics(streamEnv, tableEnv, environment);
                        TableResult routeResult2 = routeAnalytics2.process().execute();
                        LOG.info("Flight Route Analytics results:");
                        routeResult2.print();

                        LOG.info("Starting Airline Delay Analytics...");
                        AirlineDelayAnalytics delayAnalytics2 = new AirlineDelayAnalytics(streamEnv, tableEnv, environment);
                        TableResult delayResult2 = delayAnalytics2.process().execute();
                        LOG.info("Airline Delay Analytics results:");
                        delayResult2.print();
                    } catch (Exception e) {
                        LOG.error("Error running analytics", e);
                        throw e;
                    }
                    break;

            default:
                LOG.error("Unknown use case: {}", useCase);
                System.out.println("Usage: FlinkTableApiMain --useCase [status|routes|delays|all] --env [local|cloud]");
                System.exit(1);
        }
        } catch (Exception e) {
            LOG.error("Error executing Flink job", e);
            throw new RuntimeException("Error executing Flink job", e);
        } finally {
            if (streamEnv != null) {
                try {
                    // Clean up resources
                    streamEnv.close();
                } catch (Exception e) {
                    LOG.error("Error cleaning up resources", e);
                }
            }
        }
    }
}
