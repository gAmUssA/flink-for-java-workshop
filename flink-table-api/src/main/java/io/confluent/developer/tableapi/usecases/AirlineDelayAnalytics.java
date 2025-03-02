package io.confluent.developer.tableapi.usecases;

import io.confluent.developer.tableapi.table.FlightTableApiFactory;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.expressions.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.flink.table.api.Expressions.*;

/**
 * Analyzes airline delay performance to identify trends and patterns.
 * Implemented using Flink Table API with Confluent Avro format.
 */
public class AirlineDelayAnalytics {
    private static final Logger LOG = LoggerFactory.getLogger(AirlineDelayAnalytics.class);
    private static final String TABLE_NAME = "flight_source";
    
    private final StreamExecutionEnvironment streamEnv;
    private final StreamTableEnvironment tableEnv;
    private final Properties kafkaProperties;
    private final String topic;
    
    /**
     * Creates a new AirlineDelayAnalytics instance.
     *
     * @param streamEnv Stream execution environment
     * @param tableEnv Table environment
     * @param kafkaProperties Kafka properties
     * @param topic Kafka topic name
     */
    public AirlineDelayAnalytics(
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
     * Processes flight data to analyze airline delay performance.
     *
     * @return Table with airline delay performance information
     */
    public Table processDelayPerformance() {
        LOG.info("Processing flight data for airline delay performance analysis");
        
        // Create flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv, 
                TABLE_NAME, 
                topic, 
                kafkaProperties
        );
        
        // Calculate delay metrics using Table API
        Table delayPerformance = flightTable
            .filter($("actualDeparture").isNotNull())
            .groupBy($("airline"))
            .select(
                $("airline"),
                $("actualDeparture").minus($("scheduledDeparture")).avg().as("avgDelay"),
                $("actualDeparture").minus($("scheduledDeparture")).max().as("maxDelay"),
                $("airline").count().as("flightCount")
            );
        
        // Register result table
        tableEnv.createTemporaryView("airline_delay_performance", delayPerformance);
        
        return delayPerformance;
    }
    
    /**
     * Processes flight data to analyze time-windowed delays.
     *
     * @return Table with time-windowed delay information
     */
    public Table processTimeWindowedDelays() {
        LOG.info("Processing flight data for time-windowed delay analysis");
        
        // Create flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv, 
                TABLE_NAME, 
                topic, 
                kafkaProperties
        );
        
        // Use SQL for time-windowed analysis since it's more straightforward for this use case
        tableEnv.executeSql(
            "CREATE TEMPORARY VIEW hourly_delays AS " +
            "SELECT " +
            "  airline, " +
            "  TUMBLE_START(event_time, INTERVAL '1' HOUR) AS window_start, " +
            "  TUMBLE_END(event_time, INTERVAL '1' HOUR) AS window_end, " +
            "  AVG(actualDeparture - scheduledDeparture) AS avg_delay, " +
            "  COUNT(*) AS flight_count " +
            "FROM " + TABLE_NAME + " " +
            "WHERE actualDeparture IS NOT NULL " +
            "GROUP BY airline, TUMBLE(event_time, INTERVAL '1' HOUR)"
        );
        
        // Return the table
        return tableEnv.from("hourly_delays");
    }
    
    /**
     * Prints the airline delay performance analysis.
     *
     * @param delayPerformance Table with airline delay performance information
     * @return TableResult containing the execution result
     */
    public TableResult printDelayPerformance(Table delayPerformance) {
        LOG.info("Printing airline delay performance analysis");
        
        // Execute the query and print results
        TableResult result = delayPerformance.execute();
        
        // Print the results to the console
        result.print();
        
        return result;
    }
    
    /**
     * Prints the time-windowed delay analysis.
     *
     * @param timeWindowedDelays Table with time-windowed delay information
     * @return TableResult containing the execution result
     */
    public TableResult printTimeWindowedDelays(Table timeWindowedDelays) {
        LOG.info("Printing time-windowed delay analysis");
        
        // Execute the query and print results
        TableResult result = timeWindowedDelays.execute();
        
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
        LOG.info("Starting Airline Delay Performance Analysis standalone execution");
        
        // Create execution environment
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);
        
        // Create and run the delay analytics
        AirlineDelayAnalytics analytics = new AirlineDelayAnalytics(streamEnv, tableEnv, kafkaProperties, topic);
        
        // Process and print delay performance
        Table delayPerformance = analytics.processDelayPerformance();
        analytics.printDelayPerformance(delayPerformance);
        
        // Process and print time-windowed delays
        Table timeWindowedDelays = analytics.processTimeWindowedDelays();
        analytics.printTimeWindowedDelays(timeWindowedDelays);
        
        // Execute the job
        streamEnv.execute("Airline Delay Performance Analysis");
    }
}