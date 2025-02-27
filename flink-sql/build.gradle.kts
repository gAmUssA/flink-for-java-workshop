val flinkVersion = rootProject.extra["flinkVersion"] as String

dependencies {
    // Add SQL-specific dependencies here
    testImplementation("org.apache.flink:flink-test-utils:$flinkVersion")
}
