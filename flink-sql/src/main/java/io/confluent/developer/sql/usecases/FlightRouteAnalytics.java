package io.confluent.developer.sql.usecases;

import io.confluent.developer.sql.table.FlightTableApiFactory;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.flink.table.api.Expressions.$;

/**
 * Analyzes flight routes to identify popular routes and airline route patterns.
 * Implemented using Flink Table API with Confluent Avro format.
 */
public class FlightRouteAnalytics {
    private static final Logger LOG = LoggerFactory.getLogger(FlightRouteAnalytics.class);
    private static final String TABLE_NAME = "flight_source";
    
    private final StreamExecutionEnvironment streamEnv;
    private final StreamTableEnvironment tableEnv;
    private final Properties kafkaProperties;
    private final String topic;
    
    /**
     * Creates a new FlightRouteAnalytics instance.
     *
     * @param streamEnv Stream execution environment
     * @param tableEnv Table environment
     * @param kafkaProperties Kafka properties
     * @param topic Kafka topic name
     */
    public FlightRouteAnalytics(
            StreamExecutionEnvironment streamEnv,
            StreamTableEnvironment tableEnv,
            Properties kafkaProperties,
            String topic) {
        this.streamEnv = streamEnv;
        this.tableEnv = tableEnv;
        this.kafkaProperties = kafkaProperties;
        this.topic = topic;
    }
    
    /**
     * Processes flight data to analyze route popularity.
     *
     * @return Table with route popularity information
     */
    public Table processRoutePopularity() {
        LOG.info("Processing flight data for route popularity analysis");
        
        // Create flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv, 
                TABLE_NAME, 
                topic, 
                kafkaProperties
        );
        
        // Group flights by route and count using Table API
        Table routePopularity = flightTable
            .groupBy($("origin"), $("destination"))
            .select(
                $("origin"),
                $("destination"),
                $("origin").count().as("count")
            );
        
        // Register result table
        tableEnv.createTemporaryView("route_popularity", routePopularity);
        
        return routePopularity;
    }
    
    /**
     * Processes flight data to analyze airline routes.
     *
     * @return Table with airline route information
     */
    public Table processAirlineRoutes() {
        LOG.info("Processing flight data for airline route analysis");
        
        // Create flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv, 
                TABLE_NAME, 
                topic, 
                kafkaProperties
        );
        
        // Group flights by airline and route using Table API
        Table airlineRoutes = flightTable
            .groupBy($("airline"), $("origin"), $("destination"))
            .select(
                $("airline"),
                $("origin"),
                $("destination"),
                $("airline").count().as("flight_count")
            );
        
        // Register result table
        tableEnv.createTemporaryView("airline_routes", airlineRoutes);
        
        return airlineRoutes;
    }
    
    /**
     * Prints the route popularity analysis.
     *
     * @param routePopularity Table with route popularity information
     * @return TableResult containing the execution result
     */
    public TableResult printRoutePopularity(Table routePopularity) {
        LOG.info("Printing route popularity analysis");
        
        // Execute the query and print results
        TableResult result = routePopularity.execute();
        
        // Print the results to the console
        result.print();
        
        return result;
    }
    
    /**
     * Prints the airline routes analysis.
     *
     * @param airlineRoutes Table with airline route information
     * @return TableResult containing the execution result
     */
    public TableResult printAirlineRoutes(Table airlineRoutes) {
        LOG.info("Printing airline routes analysis");
        
        // Execute the query and print results
        TableResult result = airlineRoutes.execute();
        
        // Print the results to the console
        result.print();
        
        return result;
    }
    
    /**
     * Executes the use case as a standalone application.
     *
     * @param kafkaProperties Kafka connection properties
     * @param topic Topic containing flight data
     * @throws Exception If execution fails
     */
    public static void executeStandalone(Properties kafkaProperties, String topic) throws Exception {
        LOG.info("Starting Flight Route Analytics standalone execution");
        
        // Create execution environment
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);
        
        // Create and run the route analytics
        FlightRouteAnalytics analytics = new FlightRouteAnalytics(streamEnv, tableEnv, kafkaProperties, topic);
        
        // Process and print route popularity
        Table routePopularity = analytics.processRoutePopularity();
        analytics.printRoutePopularity(routePopularity);
        
        // Process and print airline routes
        Table airlineRoutes = analytics.processAirlineRoutes();
        analytics.printAirlineRoutes(airlineRoutes);
        
        // Execute the job
        streamEnv.execute("Flight Route Analytics");
    }
}
