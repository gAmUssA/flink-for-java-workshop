import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.gradleup.shadow")
}

val flinkVersion = rootProject.extra["flinkVersion"] as String
val confluentVersion = rootProject.extra["confluentVersion"] as String
val datafakerVersion = "2.4.3"

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
    implementation("org.apache.flink:flink-core:$flinkVersion")
    
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

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("flink-data-generator")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

application {
    mainClass.set("io.confluent.developer.generator.DataGeneratorJob")
}
