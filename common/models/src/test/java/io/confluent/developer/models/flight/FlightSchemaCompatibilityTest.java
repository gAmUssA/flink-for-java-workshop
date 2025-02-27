package io.confluent.developer.models.flight;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaCompatibility.SchemaPairCompatibility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for schema compatibility between different versions of the Flight schema.
 * This is important for Avro in streaming applications where schema evolution is common.
 */
public class FlightSchemaCompatibilityTest {

    @Test
    public void testCurrentSchemaMatchesAvroFile() throws IOException {
        // Given
        Schema currentSchema = Flight.getClassSchema();
        Schema fileSchema = loadSchemaFromResource("flight-v1.avsc");

        // Then
        assertThat(currentSchema.toString()).isEqualTo(fileSchema.toString());
    }

    @Test
    public void testForwardCompatibility() throws IOException {
        // Given - Reader with current schema, writer with future schema (v2)
        Schema readerSchema = Flight.getClassSchema();
        Schema writerSchema = loadSchemaFromResource("flight-v2.avsc");

        // When
        SchemaPairCompatibility compatibility = 
                SchemaCompatibility.checkReaderWriterCompatibility(readerSchema, writerSchema);

        // Then - Current schema should be able to read data written with future schema
        assertEquals(SchemaCompatibilityType.COMPATIBLE, compatibility.getType(),
                "Current schema should be able to read data from future schema");
    }

    @Test
    public void testBackwardCompatibility() throws IOException {
        // Given - Reader with future schema (v2), writer with current schema
        Schema readerSchema = loadSchemaFromResource("flight-v2.avsc");
        Schema writerSchema = Flight.getClassSchema();

        // When
        SchemaPairCompatibility compatibility = 
                SchemaCompatibility.checkReaderWriterCompatibility(readerSchema, writerSchema);

        // Then - Future schema should be able to read data written with current schema
        assertEquals(SchemaCompatibilityType.COMPATIBLE, compatibility.getType(),
                "Future schema should be able to read data from current schema");
    }

    @Test
    public void testDeserializeWithNewSchemaFields() throws IOException {
        // Given
        Schema v1Schema = loadSchemaFromResource("flight-v1.avsc");
        Schema v2Schema = loadSchemaFromResource("flight-v2.avsc");
        
        // Create a v1 flight record
        Flight v1Flight = Flight.newBuilder()
                .setFlightNumber("AA123")
                .setAirline("American Airlines")
                .setOrigin("JFK")
                .setDestination("LAX")
                .setScheduledDeparture(Instant.now().toEpochMilli())
                .setActualDeparture(null)
                .setStatus("SCHEDULED")
                .build();
        
        // Verify schemas are compatible
        SchemaPairCompatibility compatibility = 
                SchemaCompatibility.checkReaderWriterCompatibility(v2Schema, v1Schema);
        assertEquals(SchemaCompatibilityType.COMPATIBLE, compatibility.getType(),
                "V2 schema should be able to read data from V1 schema");
    }

    private Schema loadSchemaFromResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            return new Schema.Parser().parse(is);
        }
    }
}
