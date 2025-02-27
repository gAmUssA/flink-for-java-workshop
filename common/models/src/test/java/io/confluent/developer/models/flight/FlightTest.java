package io.confluent.developer.models.flight;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the Avro-generated Flight class.
 */
public class FlightTest {

    @Test
    public void testFlightCreationAndGetters() {
        // Given
        String flightNumber = "AA123";
        String airline = "American Airlines";
        String origin = "JFK";
        String destination = "LAX";
        long scheduledDeparture = Instant.now().toEpochMilli();
        Long actualDeparture = scheduledDeparture + 1800000; // 30 minutes later
        String status = "ON_TIME";

        // When
        Flight flight = Flight.newBuilder()
                .setFlightNumber(flightNumber)
                .setAirline(airline)
                .setOrigin(origin)
                .setDestination(destination)
                .setScheduledDeparture(scheduledDeparture)
                .setActualDeparture(actualDeparture)
                .setStatus(status)
                .build();

        // Then
        assertEquals(flightNumber, flight.getFlightNumber());
        assertEquals(airline, flight.getAirline());
        assertEquals(origin, flight.getOrigin());
        assertEquals(destination, flight.getDestination());
        assertEquals(scheduledDeparture, flight.getScheduledDeparture());
        assertEquals(actualDeparture, flight.getActualDeparture());
        assertEquals(status, flight.getStatus());
    }

    @Test
    public void testNullableActualDeparture() {
        // Given
        Flight flight = Flight.newBuilder()
                .setFlightNumber("AA123")
                .setAirline("American Airlines")
                .setOrigin("JFK")
                .setDestination("LAX")
                .setScheduledDeparture(Instant.now().toEpochMilli())
                .setStatus("SCHEDULED")
                .build();

        // Then
        assertNull(flight.getActualDeparture());
    }

    @Test
    public void testEquals() {
        // Given
        long now = Instant.now().toEpochMilli();
        Flight flight1 = createSampleFlight("AA123", "JFK", "LAX", now, now + 1800000, "ON_TIME");
        Flight flight2 = createSampleFlight("AA123", "JFK", "LAX", now, now + 1800000, "ON_TIME");
        Flight flight3 = createSampleFlight("AA456", "JFK", "LAX", now, now + 1800000, "ON_TIME");

        // Then
        assertThat(flight1).isEqualTo(flight2);
        assertThat(flight1).isNotEqualTo(flight3);
    }

    @Test
    public void testHashCode() {
        // Given
        long now = Instant.now().toEpochMilli();
        Flight flight1 = createSampleFlight("AA123", "JFK", "LAX", now, now + 1800000, "ON_TIME");
        Flight flight2 = createSampleFlight("AA123", "JFK", "LAX", now, now + 1800000, "ON_TIME");

        // Then
        assertThat(flight1.hashCode()).isEqualTo(flight2.hashCode());
    }

    @Test
    public void testToString() {
        // Given
        Flight flight = createSampleFlight("AA123", "JFK", "LAX", 1614556800000L, 1614558600000L, "ON_TIME");

        // Then
        String toString = flight.toString();
        assertThat(toString).contains("\"flightNumber\": \"AA123\"");
        assertThat(toString).contains("\"airline\": \"American Airlines\"");
        assertThat(toString).contains("\"origin\": \"JFK\"");
        assertThat(toString).contains("\"destination\": \"LAX\"");
        assertThat(toString).contains("\"scheduledDeparture\": 1614556800000");
        assertThat(toString).contains("\"actualDeparture\": 1614558600000");
        assertThat(toString).contains("\"status\": \"ON_TIME\"");
    }

    @Test
    public void testSerializationDeserialization() throws IOException {
        // Given
        Flight originalFlight = createSampleFlight("AA123", "JFK", "LAX", 1614556800000L, 1614558600000L, "ON_TIME");

        // When
        byte[] serialized = serializeToJson(originalFlight);
        Flight deserializedFlight = deserializeFromJson(serialized);

        // Then
        assertThat(deserializedFlight).isEqualTo(originalFlight);
    }

    @Test
    public void testBinarySerializationDeserialization() throws IOException {
        // Given
        Flight originalFlight = createSampleFlight("AA123", "JFK", "LAX", 1614556800000L, 1614558600000L, "ON_TIME");

        // When
        byte[] serialized = serializeToBinary(originalFlight);
        Flight deserializedFlight = deserializeFromBinary(serialized);

        // Then
        assertThat(deserializedFlight).isEqualTo(originalFlight);
    }

    @Test
    public void testBuiltInEncoderDecoder() throws IOException {
        // Given
        Flight originalFlight = createSampleFlight("AA123", "JFK", "LAX", 1614556800000L, 1614558600000L, "ON_TIME");

        // When - Using the built-in encoder/decoder from the Avro-generated class
        ByteBuffer byteBuffer = Flight.getEncoder().encode(originalFlight);
        byte[] serialized = new byte[byteBuffer.remaining()];
        byteBuffer.get(serialized);
        Flight deserializedFlight = Flight.getDecoder().decode(serialized);

        // Then
        assertThat(deserializedFlight).isEqualTo(originalFlight);
    }

    @ParameterizedTest
    @MethodSource("provideFlightStatusScenarios")
    public void testFlightWithDifferentStatuses(String status, Long actualDeparture) {
        // Given
        long scheduledDeparture = Instant.now().toEpochMilli();

        // When
        Flight flight = Flight.newBuilder()
                .setFlightNumber("AA123")
                .setAirline("American Airlines")
                .setOrigin("JFK")
                .setDestination("LAX")
                .setScheduledDeparture(scheduledDeparture)
                .setActualDeparture(actualDeparture)
                .setStatus(status)
                .build();

        // Then
        assertEquals(status, flight.getStatus());
        assertEquals(actualDeparture, flight.getActualDeparture());
    }

    private static Stream<Arguments> provideFlightStatusScenarios() {
        long now = Instant.now().toEpochMilli();
        return Stream.of(
                Arguments.of("SCHEDULED", null),
                Arguments.of("BOARDING", null),
                Arguments.of("DEPARTED", now),
                Arguments.of("DELAYED", now + 3600000), // 1 hour delay
                Arguments.of("CANCELLED", null)
        );
    }

    private Flight createSampleFlight(String flightNumber, String origin, String destination, 
                                     long scheduledDeparture, Long actualDeparture, String status) {
        return Flight.newBuilder()
                .setFlightNumber(flightNumber)
                .setAirline("American Airlines")
                .setOrigin(origin)
                .setDestination(destination)
                .setScheduledDeparture(scheduledDeparture)
                .setActualDeparture(actualDeparture)
                .setStatus(status)
                .build();
    }

    private byte[] serializeToJson(Flight flight) throws IOException {
        DatumWriter<Flight> writer = new SpecificDatumWriter<>(Flight.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(Flight.getClassSchema(), out);
        writer.write(flight, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    private Flight deserializeFromJson(byte[] data) throws IOException {
        DatumReader<Flight> reader = new SpecificDatumReader<>(Flight.class);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(
                Flight.getClassSchema(), 
                new String(data, StandardCharsets.UTF_8)
        );
        return reader.read(null, decoder);
    }
    
    private byte[] serializeToBinary(Flight flight) throws IOException {
        DatumWriter<Flight> writer = new SpecificDatumWriter<>(Flight.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(flight, encoder);
        encoder.flush();
        return out.toByteArray();
    }
    
    private Flight deserializeFromBinary(byte[] data) throws IOException {
        DatumReader<Flight> reader = new SpecificDatumReader<>(Flight.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }
}
