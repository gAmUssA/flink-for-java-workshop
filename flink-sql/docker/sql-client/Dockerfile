FROM flink:1.20.0-java17

# Install necessary utilities
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    wget \
    jq \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Add Flink connector dependencies
RUN mkdir -p /opt/flink/lib

# Add necessary connectors for SQL Client
# Kafka connector
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.4.0-1.20/flink-sql-connector-kafka-3.4.0-1.20.jar
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/kafka/kafka-clients/3.8.0/kafka-clients-3.8.0.jar

# Avro and Schema Registry support
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-avro-confluent-registry/1.20.0/flink-avro-confluent-registry-1.20.0.jar
RUN wget -P /opt/flink/lib https://repo1.maven.org/maven2/org/apache/avro/avro/1.11.1/avro-1.11.1.jar
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-avro/1.20.0/flink-avro-1.20.0.jar
RUN wget -P /opt/flink/lib https://packages.confluent.io/maven/io/confluent/kafka-avro-serializer/7.9.0/kafka-avro-serializer-7.9.0.jar
RUN wget -P /opt/flink/lib https://packages.confluent.io/maven/io/confluent/kafka-schema-registry-client/7.9.0/kafka-schema-registry-client-7.9.0.jar
RUN wget -P /opt/flink/lib https://packages.confluent.io/maven/io/confluent/common-utils/7.9.0/common-utils-7.9.0.jar

# Add dependencies for JSON serialization
RUN wget -P /opt/flink/lib https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.13.4/jackson-databind-2.13.4.jar
RUN wget -P /opt/flink/lib https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.13.4/jackson-core-2.13.4.jar
RUN wget -P /opt/flink/lib https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.13.4/jackson-annotations-2.13.4.jar


# JSON format
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/1.20.0/flink-json-1.20.0.jar

# Misc
RUN wget -P /opt/flink/lib https://repo1.maven.org/maven2/com/google/guava/guava/30.1-jre/guava-30.1-jre.jar


RUN chown -R flink:flink /opt/flink/lib

# Create directories for SQL files
RUN mkdir -p /opt/sql/usecases /opt/sql/queries

# Copy SQL client start script
COPY sql-client.sh /opt/
RUN chmod +x /opt/sql-client.sh

WORKDIR /opt

#ENTRYPOINT ["/opt/sql-client.sh"]
