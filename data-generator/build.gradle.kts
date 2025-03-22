plugins {
    java
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    // Common utilities
    implementation(project(":common:utils"))
    implementation(project(":common:models"))

    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    // Avro and Schema Registry
    implementation("org.apache.avro:avro:1.12.0")
    implementation("io.confluent:kafka-avro-serializer:7.9.0")
    implementation("io.confluent:kafka-schema-registry-client:7.9.0")

    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    // Data generation
    implementation("net.datafaker:datafaker:2.0.2")

    // CLI parsing
    implementation("info.picocli:picocli:4.7.6")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.12.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(project(":common:models"))
}

application {
    mainClass.set("com.example.DataGeneratorApp")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Ensure models are generated before running tests
    dependsOn(":common:models:generateAvroJava")
}
