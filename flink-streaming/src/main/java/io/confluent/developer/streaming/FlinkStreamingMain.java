package io.confluent.developer.streaming;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Flink Streaming module.
 */
public class FlinkStreamingMain {
    private static final Logger LOG = LoggerFactory.getLogger(FlinkStreamingMain.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Flink Streaming application");

        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Add your Flink streaming job here
        
        LOG.info("Executing Flink streaming job");
        env.execute("Flink Streaming Job");
    }
}
