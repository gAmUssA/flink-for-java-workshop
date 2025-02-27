import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "io.confluent.developer"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}

subprojects {
    apply(plugin = "java")
    
    val flinkVersion = "1.18.1"
    val confluentVersion = "7.6.0"
    val junitVersion = "5.10.2"
    val logbackVersion = "1.4.14"
    val slf4jVersion = "2.0.11"
    
    dependencies {
        // Logging
        implementation("org.slf4j:slf4j-api:$slf4jVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        
        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    tasks.test {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
}

// Configuration for the main application modules
configure(subprojects.filter { it.name == "flink-streaming" || it.name == "flink-sql" }) {
    apply(plugin = "application")
    apply(plugin = "com.github.johnrengelman.shadow")
    
    val flinkVersion = "1.18.1"
    val confluentVersion = "7.6.0"
    
    dependencies {
        // Common modules
        implementation(project(":common:models"))
        implementation(project(":common:utils"))
        
        // Flink Core
        implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
        implementation("org.apache.flink:flink-clients:$flinkVersion")
        implementation("org.apache.flink:flink-runtime-web:$flinkVersion")
        
        // Flink Connectors
        implementation("org.apache.flink:flink-connector-kafka:3.0.2-1.18")
        implementation("org.apache.flink:flink-connector-files:$flinkVersion")
        
        // Confluent
        implementation("io.confluent:kafka-schema-registry-client:$confluentVersion")
        implementation("io.confluent:kafka-json-schema-serializer:$confluentVersion")
        implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
    }
}

// Configuration specific to the flink-sql module
project(":flink-sql") {
    dependencies {
        val flinkVersion = "1.18.1"
        
        // Flink Table API & SQL
        implementation("org.apache.flink:flink-table-api-java-bridge:$flinkVersion")
        implementation("org.apache.flink:flink-table-planner-loader:$flinkVersion")
        implementation("org.apache.flink:flink-table-runtime:$flinkVersion")
    }
    
    tasks.jar {
        manifest {
            attributes["Main-Class"] = "io.confluent.developer.sql.FlinkSqlMain"
        }
    }
    
    tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("flink-sql")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
    
    application {
        mainClass.set("io.confluent.developer.sql.FlinkSqlMain")
    }
}

// Configuration specific to the flink-streaming module
project(":flink-streaming") {
    tasks.jar {
        manifest {
            attributes["Main-Class"] = "io.confluent.developer.streaming.FlinkStreamingMain"
        }
    }
    
    tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("flink-streaming")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
    
    application {
        mainClass.set("io.confluent.developer.streaming.FlinkStreamingMain")
    }
}

// Gradle wrapper task
tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.BIN
}
