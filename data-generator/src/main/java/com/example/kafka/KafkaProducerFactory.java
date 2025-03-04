package com.example.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Factory for creating Kafka producers
 */
public class KafkaProducerFactory {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerFactory.class);

    /**
     * Creates a Kafka producer for JSON data
     *
     * @param properties Kafka properties
     * @return A configured Kafka producer
     */
    public static KafkaProducer<String, String> createJsonProducer(Properties properties) {
        Properties producerProps = new Properties();
        producerProps.putAll(properties);
        
        // Set required producer configs if not already set
        if (!producerProps.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        
        if (!producerProps.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        
        if (!producerProps.containsKey(ProducerConfig.ACKS_CONFIG)) {
            producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        }
        
        return new KafkaProducer<>(producerProps);
    }
    
    /**
     * Creates Kafka topics if they don't exist
     *
     * @param properties Kafka properties
     * @param topics List of topics to create
     * @param partitions Number of partitions for each topic
     * @param replicationFactor Replication factor for each topic
     */
    public static void createTopicsIfNotExist(Properties properties, List<String> topics, 
                                             int partitions, short replicationFactor) {
        logger.info("Creating Kafka topics if they don't exist: {}", topics);
        
        Properties adminProps = new Properties();
        adminProps.putAll(properties);
        
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            List<NewTopic> newTopics = topics.stream()
                    .map(topic -> new NewTopic(topic, partitions, replicationFactor))
                    .collect(Collectors.toList());
            
            adminClient.createTopics(newTopics);
            logger.info("Topics created or already exist: {}", 
                    topics.stream().collect(Collectors.joining(", ")));
        } catch (Exception e) {
            logger.warn("Error creating topics: {}", e.getMessage());
        }
    }
}
