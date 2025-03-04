package com.example.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Factory for creating Kafka producers for Avro data
 */
public class AvroProducerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AvroProducerFactory.class);

    /**
     * Creates a Kafka producer for Avro data
     *
     * @param properties Kafka properties
     * @return A configured Kafka producer for Avro records
     */
    public static <T extends SpecificRecord> KafkaProducer<String, T> createAvroProducer(Properties properties) {
        Properties producerProps = new Properties();
        producerProps.putAll(properties);
        
        // Set required producer configs if not already set
        if (!producerProps.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        
        if (!producerProps.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        }
        
        if (!producerProps.containsKey(ProducerConfig.ACKS_CONFIG)) {
            producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        }
        
        // Ensure schema registry URL is set
        if (!producerProps.containsKey(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG) && 
            producerProps.containsKey("schema.registry.url")) {
            producerProps.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, 
                              producerProps.getProperty("schema.registry.url"));
        }
        
        // Add Schema Registry authentication if in cloud environment
        if ("cloud".equalsIgnoreCase(producerProps.getProperty("environment")) || 
            "true".equalsIgnoreCase(producerProps.getProperty("cloud"))) {
            
            if (producerProps.containsKey("schema.registry.basic.auth.user.info")) {
                producerProps.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
                producerProps.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, 
                                 producerProps.getProperty("schema.registry.basic.auth.user.info"));
            }
        }
        
        return new KafkaProducer<>(producerProps);
    }
    
    /**
     * Sends an Avro record to Kafka
     *
     * @param producer Kafka producer
     * @param topic Topic name
     * @param key Record key
     * @param value Avro record
     * @return Future for the record metadata
     */
    public static <T extends SpecificRecord> Future<RecordMetadata> sendRecord(
            KafkaProducer<String, T> producer,
            String topic, String key, T value) {
        
        ProducerRecord<String, T> record = new ProducerRecord<>(topic, key, value);
        return producer.send(record);
    }
}
