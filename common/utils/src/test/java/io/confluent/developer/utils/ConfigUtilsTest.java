package io.confluent.developer.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the ConfigUtils class.
 */
class ConfigUtilsTest {

    @Test
    void shouldLoadPropertiesFromClasspath() {
        // When
        Properties properties = ConfigUtils.loadProperties("test.properties");

        // Then
        assertThat(properties).isNotEmpty();
        assertThat(properties.getProperty("bootstrap.servers")).isEqualTo("test-bootstrap-servers");
        assertThat(properties.getProperty("topic.name")).isEqualTo("test-topic");
        assertThat(properties.getProperty("schema.registry.url")).isEqualTo("test-registry-url");
        assertThat(properties.getProperty("security.protocol")).isEqualTo("SASL_SSL");
        assertThat(properties.getProperty("sasl.mechanism")).isEqualTo("PLAIN");
        assertThat(properties.getProperty("environment")).isEqualTo("test");
    }

    @Test
    void shouldLoadStandardPropertiesFromClasspath() {
        // When
        Properties properties = ConfigUtils.loadProperties("standard.properties");

        // Then
        assertThat(properties).isNotEmpty();
        assertThat(properties.getProperty("bootstrap.servers")).isEqualTo("localhost:9092");
        assertThat(properties.getProperty("topic.name")).isEqualTo("test-topic");
        assertThat(properties.getProperty("schema.registry.url")).isEqualTo("http://localhost:8081");
        assertThat(properties.getProperty("security.protocol")).isEqualTo("PLAINTEXT");
        assertThat(properties.getProperty("sasl.mechanism")).isEqualTo("PLAIN");
        assertThat(properties.getProperty("environment")).isEqualTo("local");
    }

    @Test
    void shouldReturnEmptyPropertiesWhenFileNotFound() {
        // When
        Properties properties = ConfigUtils.loadProperties("non-existent.properties");

        // Then
        assertThat(properties).isEmpty();
    }

    @Test
    void shouldLoadPropertiesFromFileSystem(@TempDir Path tempDir) throws IOException {
        // Given
        Path propertiesFile = tempDir.resolve("file-system.properties");
        Files.writeString(propertiesFile, "client.cloud=file-system-cloud\nclient.region=file-system-region");

        // When
        Properties properties = ConfigUtils.loadProperties(propertiesFile.toString());

        // Then
        assertThat(properties).isNotEmpty();
        assertThat(properties.getProperty("client.cloud")).isEqualTo("file-system-cloud");
        assertThat(properties.getProperty("client.region")).isEqualTo("file-system-region");
    }

    @ParameterizedTest
    @CsvSource({
            "key1, default1, value1",
            "key2, default2, default2",
            "key3, , "
    })
    void shouldGetPropertyWithDefault(String key, String defaultValue, String expected) {
        // Given
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");

        // When
        String result = ConfigUtils.getProperty(properties, key, defaultValue);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
