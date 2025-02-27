FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the shadow JARs
COPY flink-streaming/build/libs/*-all.jar /app/flink-streaming.jar
COPY flink-sql/build/libs/*-all.jar /app/flink-sql.jar

# Copy configuration files
COPY cloud.properties* /app/

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Default command to run the streaming application
CMD ["java", "-jar", "/app/flink-streaming.jar"]
