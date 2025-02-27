plugins {
    application
    id("com.github.johnrengelman.shadow")
}

val flinkVersion = "1.20.0"
val confluentVersion = "7.9.0"
val datafakerVersion = "2.1.0"

dependencies {
    // Common modules
    implementation(project(":common:models"))
    implementation(project(":common:utils"))
    
    // Flink Core
    implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    
    // Flink Connectors
    implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
    implementation("org.apache.flink:flink-avro:$flinkVersion")
    implementation("org.apache.flink:flink-connector-base:$flinkVersion")
    
    // Confluent
    implementation("io.confluent:kafka-schema-registry-client:$confluentVersion")
    implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
    
    // Data Faker for generating random data
    implementation("net.datafaker:datafaker:$datafakerVersion")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.confluent.developer.generator.DataGeneratorJob"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("flink-data-generator")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

application {
    mainClass.set("io.confluent.developer.generator.DataGeneratorJob")
}
