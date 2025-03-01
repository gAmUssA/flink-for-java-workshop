import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.gradleup.shadow") version "8.3.6" apply false
}

// Define versions at the root level
val flinkVersion = "1.20.1"
val confluentVersion = "7.9.0"

allprojects {
    group = "io.confluent.developer"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
    
    // Share versions with all projects
    extra["flinkVersion"] = flinkVersion
    extra["confluentVersion"] = confluentVersion
}

subprojects {
    apply(plugin = "java")

    val junitVersion = "5.12.0"
    val logbackVersion = "1.5.17"
    val slf4jVersion = "2.0.17"

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
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        // Common modules
        implementation(project(":common:models"))
        implementation(project(":common:utils"))

        // Flink Core
        implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
        implementation("org.apache.flink:flink-clients:$flinkVersion")
        implementation("org.apache.flink:flink-runtime-web:$flinkVersion")

        // Flink Connectors
        implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
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

    tasks.named<ShadowJar>("shadowJar") {
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

    tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set("flink-streaming")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }

    application {
        mainClass.set("io.confluent.developer.streaming.FlinkStreamingMain")
    }
}
