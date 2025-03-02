package io.confluent.developer.sql.usecases;

import io.confluent.developer.sql.config.ConfigLoader;
import io.confluent.developer.sql.table.FlightTableApiFactory;
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
    private static final String TABLE_NAME = "flight_source";
    private static final String TOPIC_NAME = "flights";

    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        final ParameterTool params = ParameterTool.fromArgs(args);
        String env = params.get("env", "local");
        
        LOG.info("Starting Flight Status Dashboard with environment: {}", env);
        
        // Load Kafka properties
        Properties properties = ConfigLoader.loadKafkaProperties(env);
        
        // Set up the streaming execution environment
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);
        
        // Create the flight table using the Table API
        Table flightTable = FlightTableApiFactory.createFlightTable(
                tableEnv,
                TABLE_NAME,
                TOPIC_NAME,
                properties
        );
        
        // Execute the flight status dashboard query using Table API
        LOG.info("Executing flight status query using Table API");
        
        // Group by status and count flights
        Table resultTable = flightTable
                .groupBy($("status"))
                .select(
                        $("status"),
                        $("status").count().as("flight_count")
                );
        
        // Print the results
        LOG.info("Query result schema: {}", resultTable.getResolvedSchema());
        resultTable.execute().print();
        
        // Execute the job
        streamEnv.execute("Flight Status Dashboard");
    }
}
