val flinkVersion = rootProject.extra["flinkVersion"] as String
val kafkaVersion = "3.4.0-1.20" // Correct version format as per requirements

dependencies {
    // Flink core dependencies
    implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    
    // Flink Table API dependencies
    implementation("org.apache.flink:flink-table-api-java-bridge:$flinkVersion")
    implementation("org.apache.flink:flink-table-planner-loader:$flinkVersion")
    implementation("org.apache.flink:flink-table-runtime:$flinkVersion")
    
    // Kafka connector
    implementation("org.apache.flink:flink-connector-kafka:$kafkaVersion")
    
    // Avro dependencies
    implementation("org.apache.flink:flink-avro:$flinkVersion")
    implementation("org.apache.flink:flink-avro-confluent-registry:$flinkVersion")
    implementation("org.apache.avro:avro:1.12.0")
    
    // Dependencies for the Schema Registry
    implementation("io.confluent:kafka-schema-registry-client:7.9.2")
    implementation("io.confluent:kafka-avro-serializer:7.9.0")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    
    // Project dependencies
    implementation(project(":common:models"))
    
    // Test dependencies
    testImplementation("org.apache.flink:flink-test-utils:$flinkVersion")
    testImplementation(platform("org.junit:junit-bom:5.12.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0") // Added for aligned JUnit dependencies
    
    // Mockito for testing
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
}
