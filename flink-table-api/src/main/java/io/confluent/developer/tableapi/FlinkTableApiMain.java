package io.confluent.developer.tableapi;

import io.confluent.developer.tableapi.config.ConfigLoader;
import io.confluent.developer.tableapi.usecases.AirlineDelayAnalytics;
import io.confluent.developer.tableapi.usecases.FlightRouteAnalytics;
import io.confluent.developer.tableapi.usecases.FlightStatusDashboard;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Main entry point for the Flink Table API module.
 * Demonstrates various Table API use cases with flight data.
 */
public class FlinkTableApiMain {
    private static final Logger LOG = LoggerFactory.getLogger(FlinkTableApiMain.class);
    private static final String DEFAULT_TOPIC = "flights-avro";
    private static final String DEFAULT_ENV = "local";

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Flink Table API application");

        // Parse command-line arguments
        String useCase = (args.length > 0) ? args[0] : "all";
        String env = (args.length > 1) ? args[1] : DEFAULT_ENV;
        String topic = (args.length > 2) ? args[2] : DEFAULT_TOPIC;

        LOG.info("Use case: {}, Environment: {}, Topic: {}", useCase, env, topic);

        // Execute the selected use case
        switch (useCase.toLowerCase()) {
            case "status":
                LOG.info("Executing Flight Status Dashboard use case");
                String[] dashboardArgs = {"--env", env};
                FlightStatusDashboard.main(dashboardArgs);
                break;
                
            case "routes":
                LOG.info("Executing Flight Route Analytics use case");
                runRouteAnalytics(env, topic);
                break;
                
            case "delays":
                LOG.info("Executing Airline Delay Performance Analysis use case");
                runDelayAnalytics(env, topic);
                break;
                
            case "all":
                LOG.info("Executing all use cases");
                runAllUseCases(env, topic);
                break;
                
            default:
                LOG.error("Unknown use case: {}", useCase);
                printUsage();
                System.exit(1);
        }
    }
    
    /**
     * Runs all three use cases in sequence.
     *
     * @param env Environment (local or cloud)
     * @param topic Input topic
     * @throws Exception If execution fails
     */
    private static void runAllUseCases(String env, String topic) throws Exception {
        LOG.info("Running all use cases with Table API implementation");
        
        // Load Kafka properties based on environment
        Properties kafkaProperties = ConfigLoader.loadKafkaProperties(env);
        
        // Set up the streaming execution environment
        final StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);
        
        // Run the status dashboard
        LOG.info("Running Flight Status Dashboard");
        String[] dashboardArgs = {"--env", env};
        FlightStatusDashboard.main(dashboardArgs);
        
        // Run the route analytics
        LOG.info("Running Flight Route Analytics");
        FlightRouteAnalytics.executeStandalone(kafkaProperties, topic);
        
        // Run the delay analytics
        LOG.info("Running Airline Delay Performance Analysis");
        AirlineDelayAnalytics.executeStandalone(kafkaProperties, topic);
    }
    
    /**
     * Runs the Flight Route Analytics use case.
     *
     * @param env Environment (local or cloud)
     * @param topic Input topic
     * @throws Exception If execution fails
     */
    private static void runRouteAnalytics(String env, String topic) throws Exception {
        // Load Kafka properties based on environment
        Properties kafkaProperties = ConfigLoader.loadKafkaProperties(env);
        
        // Execute the standalone route analytics
        FlightRouteAnalytics.executeStandalone(kafkaProperties, topic);
    }
    
    /**
     * Runs the Airline Delay Performance Analysis use case.
     *
     * @param env Environment (local or cloud)
     * @param topic Input topic
     * @throws Exception If execution fails
     */
    private static void runDelayAnalytics(String env, String topic) throws Exception {
        // Load Kafka properties based on environment
        Properties kafkaProperties = ConfigLoader.loadKafkaProperties(env);
        
        // Execute the standalone delay analytics
        AirlineDelayAnalytics.executeStandalone(kafkaProperties, topic);
    }
    
    /**
     * Prints usage information.
     */
    private static void printUsage() {
        LOG.info("Usage: FlinkTableApiMain <use-case> [environment] [topic]");
        LOG.info("  use-case: status, routes, delays, or all");
        LOG.info("  environment: local or cloud (default: local)");
        LOG.info("  topic: Kafka topic name (default: flights)");
    }
}