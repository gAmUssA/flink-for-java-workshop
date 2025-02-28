package io.confluent.developer.models.generator;

import io.confluent.developer.models.flight.Flight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FlightGeneratorTest {

    @Test
    void shouldGenerateRandomFlight() {
        // Given
        FlightGenerator generator = new FlightGenerator(42L); // fixed seed for reproducibility
        
        // When
        Flight flight = generator.generateFlight();
        
        // Then
        assertThat(flight).isNotNull();
        assertThat(flight.getFlightNumber()).isNotNull().isNotEmpty();
        assertThat(flight.getAirline()).isNotNull().isNotEmpty();
        assertThat(flight.getOrigin()).isNotNull().isNotEmpty();
        assertThat(flight.getDestination()).isNotNull().isNotEmpty();
        assertThat(flight.getStatus()).isNotNull().isNotEmpty();
        assertThat(flight.getScheduledDeparture()).isGreaterThan(0L);
    }

    @Test
    void shouldGenerateUniqueFlights() {
        // Given
        FlightGenerator generator = new FlightGenerator();
        Set<String> flightNumbers = new HashSet<>();
        int flightCount = 100;
        
        // When
        for (int i = 0; i < flightCount; i++) {
            Flight flight = generator.generateFlight();
            flightNumbers.add(flight.getFlightNumber());
        }
        
        // Then - not all flight numbers should be unique due to random generation,
        // but we should have a good amount of diversity
        assertThat(flightNumbers.size()).isGreaterThan((int)(flightCount * 0.8));
    }
    
    @Test
    void originAndDestinationShouldBeDifferent() {
        // Given
        FlightGenerator generator = new FlightGenerator();
        int flightCount = 100;
        
        // When/Then
        for (int i = 0; i < flightCount; i++) {
            Flight flight = generator.generateFlight();
            assertThat(flight.getOrigin()).isNotEqualTo(flight.getDestination());
        }
    }
    
    @Test
    void shouldProvideValidAirportsList() {
        // Given
        FlightGenerator generator = new FlightGenerator();
        
        // When
        var airports = generator.getAirports();
        
        // Then
        assertThat(airports).isNotNull().isNotEmpty();
        assertThat(airports).allMatch(airport -> airport.length() == 3);
    }
    
    @Test
    void shouldProvideValidAirlinesList() {
        // Given
        FlightGenerator generator = new FlightGenerator();
        
        // When
        var airlines = generator.getAirlines();
        
        // Then
        assertThat(airlines).isNotNull().isNotEmpty();
        assertThat(airlines).allMatch(airline -> !airline.isEmpty());
    }
    
    @Test
    void shouldProvideValidStatusesList() {
        // Given
        FlightGenerator generator = new FlightGenerator();
        
        // When
        var statuses = generator.getStatuses();
        
        // Then
        assertThat(statuses).isNotNull().isNotEmpty();
        assertThat(statuses).contains("DELAYED", "CANCELLED", "ON_TIME");
    }
    
    @ParameterizedTest
    @ValueSource(longs = {1L, 42L, 100L, 999L})
    void shouldBeDeterministicWithSeed(long seed) {
        // Given
        FlightGenerator generator1 = new FlightGenerator(seed);
        FlightGenerator generator2 = new FlightGenerator(seed);
        
        // When
        Flight flight1 = generator1.generateFlight();
        Flight flight2 = generator2.generateFlight();
        
        // Then
        assertThat(flight1.getFlightNumber()).isEqualTo(flight2.getFlightNumber());
        assertThat(flight1.getAirline()).isEqualTo(flight2.getAirline());
        assertThat(flight1.getOrigin()).isEqualTo(flight2.getOrigin());
        assertThat(flight1.getDestination()).isEqualTo(flight2.getDestination());
        assertThat(flight1.getStatus()).isEqualTo(flight2.getStatus());
        
        // Due to timing differences, we don't strictly compare timestamps
        // Time-based values may differ slightly due to execution timing
        
        // If actual departure exists in one flight, it should exist in both
        if (flight1.getActualDeparture() != null) {
            assertThat(flight2.getActualDeparture()).isNotNull();
        }
    }
}