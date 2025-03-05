package io.confluent.developer.tableapi.usecases;

import io.confluent.developer.tableapi.config.ConfigLoader;
import io.confluent.developer.tableapi.table.FlightTableApiFactory;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.flink.table.api.Expressions.*;

/**
 * Flight Status Dashboard application.
 * This application shows the current status of flights in real-time.
 * Implemented using Flink Table API with Confluent Avro format.
 */
public class FlightStatusDashboard {
    private static final Logger LOG = LoggerFactory.getLogger(FlightStatusDashboard.class);
    
    private final StreamExecutionEnvironment streamEnv;
    private final StreamTableEnvironment tableEnv;
    private final Properties kafkaProperties;
    private final String topic;
    private final String tableName;
    
    /**
     * Constructor for the FlightStatusDashboard.
     *
     * @param streamEnv The Flink streaming environment
     * @param tableEnv The Flink table environment
     * @param kafkaProperties Kafka properties
     * @param topic The Kafka topic to read from
     */
    public FlightStatusDashboard(
            StreamExecutionEnvironment streamEnv,
            StreamTableEnvironment tableEnv,
            Properties kafkaProperties,
            String topic) {
        this.streamEnv = streamEnv;
        this.tableEnv = tableEnv;
        this.kafkaProperties = kafkaProperties;
        this.topic = topic;
        this.tableName = ConfigLoader.getTableName(kafkaProperties, "flights", "Flights");
    }

    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        final ParameterTool params = ParameterTool.fromArgs(args);
        String env = params.get("env", "local");
        
        LOG.info("Starting Flight Status Dashboard with environment: {}", env);
        
        // Load Kafka properties
        Properties properties = ConfigLoader.loadKafkaProperties(env);
        String topic = ConfigLoader.getTopicName(properties, "flights", "flights");
        
        // Set up the streaming execution environment
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);
        
        // Create the dashboard instance
        FlightStatusDashboard dashboard = new FlightStatusDashboard(streamEnv, tableEnv, properties, topic);
        
        // Process the flight status
        dashboard.processFlightStatus();
        
        // Execute the job
        streamEnv.execute("Flight Status Dashboard");
    }
    
    /**
     * Process the flight status data.
     * Creates a table from the Kafka topic and executes a query to count flights by status.
     *
     * @return The result table containing flight status counts
     */
    public Table processFlightStatus() {
        LOG.info("Creating flight table: {}", tableName);
        
        // Create the flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv,
                tableName,
                topic,
                kafkaProperties
        );
        
        // Execute the flight status dashboard query using Table API
        LOG.info("Executing flight status query using Table API");
        
        // Group by status and count flights
        Table resultTable = flightTable
                .filter($("status").isNotNull())
                .select(
                        $("flight_number"),
                        $("origin"),
                        $("destination"),
                        $("status"),
                        $("departure_time"),
                        $("arrival_time")
                );
        
        // Print the results
        LOG.info("Query result schema: {}", resultTable.getResolvedSchema());
        TableResult result = resultTable.execute();
        result.print();
        
        return resultTable;
    }
}