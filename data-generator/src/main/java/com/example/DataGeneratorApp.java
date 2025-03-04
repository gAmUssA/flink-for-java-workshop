package com.example;

import com.example.generator.AirlineGenerator;
import com.example.generator.AirportGenerator;
import com.example.kafka.AvroProducerFactory;
import com.example.kafka.KafkaProducerFactory;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import io.confluent.developer.models.reference.Airline;
import io.confluent.developer.models.reference.Airport;
import io.confluent.developer.utils.ConfigUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "reference-data-generator", 
         description = "Generate reference data for airlines and airports",
         mixinStandardHelpOptions = true)
public class DataGeneratorApp implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorApp.class);
    
    @Option(names = {"-e", "--env"}, description = "Environment (local or cloud)", defaultValue = "local")
    private String environment;
    
    @Option(names = {"-p", "--properties"}, description = "Path to properties file")
    private String propertiesPath;
    
    @Option(names = {"--cloud-key"}, description = "Confluent Cloud API Key (overrides properties)")
    private String cloudKey;
    
    @Option(names = {"--cloud-secret"}, description = "Confluent Cloud API Secret (overrides properties)")
    private String cloudSecret;
    
    @Option(names = {"-b", "--bootstrap-servers"}, description = "Kafka bootstrap servers (overrides properties)")
    private String bootstrapServers;

    private Properties properties;
    private String topicAirlines;
    private String topicAirports;
    private int numAirlines;
    private int numAirports;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DataGeneratorApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Load properties
        loadProperties();
        
        // Extract configuration from properties
        extractConfiguration();
        
        LOG.info("Generating {} airlines and {} airports", numAirlines, numAirports);
        
        // Create Kafka topics if they don't exist
        createKafkaTopics();
        
        // Generate and send airline data
        List<Airline> airlines = new AirlineGenerator().generateAirlinesAvro(numAirlines);
        sendAirlineData(airlines);
        
        // Generate and send airport data
        List<Airport> airports = new AirportGenerator().generateAirportsAvro(numAirports);
        sendAirportData(airports);
        
        LOG.info("Reference data generation completed successfully");
        return 0;
    }
    
    private void loadProperties() {
        String propertiesFile;
        
        // Determine which properties file to use
        if (propertiesPath != null) {
            propertiesFile = propertiesPath;
        } else {
            propertiesFile = environment.equalsIgnoreCase("cloud") ? 
                    "cloud.properties" : "local.properties";
        }

        LOG.info("Loading properties from: {}", propertiesFile);
        properties = ConfigUtils.loadProperties(propertiesFile);
        
        // Extract configuration values
        String bootstrapServers = ConfigUtils.getProperty(properties, "bootstrap.servers", "localhost:29092");
        String schemaRegistryUrl = ConfigUtils.getProperty(properties, "schema.registry.url", "http://localhost:8081");
        int airlinesCount = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.airlines.count", "20"));
        int airportsCount = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.airports.count", "50"));
        environment = ConfigUtils.getProperty(properties, "environment", "local");

        LOG.info("Starting Flight Data Generator with configuration:");
        LOG.info("  Environment: {}", environment);
        LOG.info("  Bootstrap Servers: {}", bootstrapServers);
        LOG.info("  Schema Registry URL: {}", schemaRegistryUrl);
        LOG.info("  Airlines Count: {}", airlinesCount);
        LOG.info("  Airports Count: {}", airportsCount);
        
        // If properties couldn't be loaded, create an empty one to avoid NPE
        if (properties == null) {
            properties = new Properties();
            LOG.warn("Could not load properties from {}. Using defaults.", propertiesFile);
        }
        
        // Set environment property
        properties.setProperty("environment", environment);
        
        LOG.info("Loaded properties for environment: {}", environment);
    }
    
    private void extractConfiguration() {
        // Get topic names
        topicAirlines = ConfigUtils.getProperty(properties, "topic.airlines", "airlines");
        topicAirports = ConfigUtils.getProperty(properties, "topic.airports", "airports");
        
        // Get generator configuration
        numAirlines = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.airlines.count", "20"));
        numAirports = Integer.parseInt(ConfigUtils.getProperty(properties, "generator.airports.count", "50"));
    }
    
    /**
     * Sends airline data using Avro serialization
     *
     * @param airlines List of Avro airline objects
     * @throws Exception If an error occurs during sending
     */
    private void sendAirlineData(List<Airline> airlines) throws Exception {
        LOG.info("Sending {} airlines to Kafka topic using Avro: {}", airlines.size(), topicAirlines);
        
        KafkaProducer<String, Airline> producer = 
                AvroProducerFactory.createAvroProducer(properties);
        
        try {
            for (Airline airline : airlines) {
                String key = airline.getAirlineCode();
                AvroProducerFactory.sendRecord(producer, topicAirlines, key, airline);
                LOG.debug("Sent airline: {}", airline);
            }
            LOG.info("All airlines sent successfully");
        } finally {
            producer.close();
        }
    }
    
    /**
     * Sends airport data using Avro serialization
     *
     * @param airports List of Avro airport objects
     * @throws Exception If an error occurs during sending
     */
    private void sendAirportData(List<Airport> airports) throws Exception {
        LOG.info("Sending {} airports to Kafka topic using Avro: {}", airports.size(), topicAirports);
        
        KafkaProducer<String, Airport> producer = 
                AvroProducerFactory.createAvroProducer(properties);
        
        try {
            for (Airport airport : airports) {
                String key = airport.getAirportCode();
                AvroProducerFactory.sendRecord(producer, topicAirports, key, airport);
                LOG.debug("Sent airport: {}", airport);
            }
            LOG.info("All airports sent successfully");
        } finally {
            producer.close();
        }
    }
    
    private void createKafkaTopics() {
        LOG.info("Creating Kafka topics if they don't exist");
        
        // Determine replication factor based on environment
        short replicationFactor = environment.equalsIgnoreCase("cloud") ? (short)3 : (short)1;
        
        // Create topics
        KafkaProducerFactory.createTopicsIfNotExist(
                properties, 
                Arrays.asList(topicAirlines, topicAirports),
                1, 
                replicationFactor
        );
    }
}
