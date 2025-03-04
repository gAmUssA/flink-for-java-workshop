package com.example.generator;

import io.confluent.developer.models.reference.Airport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class AirportGeneratorTest {
    private final AirportGenerator generator = new AirportGenerator();

    @Test
    void testBasicAirportModelAccess() {
        System.out.println("[DEBUG_LOG] Starting basic Airport model test");

        // Create a simple airport object
        Airport airport = Airport.newBuilder()
            .setAirportCode("TST")
            .setAirportName("Test Airport")
            .setCity("Test City")
            .setCountry("Test Country")
            .setLatitude(0.0)
            .setLongitude(0.0)
            .build();

        System.out.println("[DEBUG_LOG] Created test airport: " + 
            airport.getAirportCode() + " - " + 
            airport.getAirportName() + " - " + 
            airport.getCity());

        // Basic assertions
        assertNotNull(airport, "Airport object should not be null");
        assertEquals("TST", airport.getAirportCode(), "Airport code should match");
        assertEquals("Test Airport", airport.getAirportName(), "Airport name should match");
        assertEquals("Test City", airport.getCity(), "City should match");
        assertEquals("Test Country", airport.getCountry(), "Country should match");
    }

    @Test
    void testGenerateAirportsAvroContainsPredefinedAirports() {
        int expectedCount = 15;
        List<Airport> airports = generator.generateAirportsAvro(expectedCount);

        // First check if we got any airports
        assertNotNull(airports, "Generated airports list should not be null");
        System.out.println("[DEBUG_LOG] Generated airports count: " + airports.size());

        // Check the size
        assertTrue(airports.size() > 0, "Generated airports list should not be empty");
        assertEquals(expectedCount, airports.size(), "Generated airports count should match expected count");

        System.out.println("[DEBUG_LOG] Generated airports:");
        airports.forEach(a -> System.out.println("[DEBUG_LOG] " + a.getAirportCode() + " - " + a.getAirportName() + " - " + a.getCity()));

        // Check JFK separately
        Optional<Airport> jfk = airports.stream()
            .filter(a -> "JFK".equals(a.getAirportCode()))
            .findFirst();

        assertTrue(jfk.isPresent(), "JFK should be present");
        if (jfk.isPresent()) {
            Airport jfkAirport = jfk.get();
            assertEquals("John F. Kennedy International Airport", jfkAirport.getAirportName(), "Incorrect airport name for JFK");
            assertEquals("New York", jfkAirport.getCity(), "Incorrect city for JFK");
        }

        // Check LAX separately
        Optional<Airport> lax = airports.stream()
            .filter(a -> "LAX".equals(a.getAirportCode()))
            .findFirst();

        assertTrue(lax.isPresent(), "LAX should be present");
        if (lax.isPresent()) {
            Airport laxAirport = lax.get();
            assertEquals("Los Angeles International Airport", laxAirport.getAirportName(), "Incorrect airport name for LAX");
            assertEquals("Los Angeles", laxAirport.getCity(), "Incorrect city for LAX");
        }
    }

    @Test
    void testGenerateAirportsAvroRespectsCount() {
        int expectedCount = 15;
        List<Airport> airports = generator.generateAirportsAvro(expectedCount);
        System.out.println("[DEBUG_LOG] Expected count: " + expectedCount + ", Actual count: " + airports.size());
        assertEquals(expectedCount, airports.size(), "Generated airports count doesn't match expected count");
    }

    @Test
    void testGenerateAirportsAvroHasUniqueAirportCodes() {
        List<Airport> airports = generator.generateAirportsAvro(20);
        Set<String> airportCodes = new HashSet<>();
        System.out.println("[DEBUG_LOG] Checking uniqueness for airport codes:");

        for (Airport airport : airports) {
            String airportCode = airport.getAirportCode();
            System.out.println("[DEBUG_LOG] Processing airport code: " + airportCode);

            boolean isUnique = airportCodes.add(airportCode);
            if (!isUnique) {
                System.out.println("[DEBUG_LOG] Duplicate found for code: " + airportCode);
            }
            assertTrue(isUnique, "Airport code " + airportCode + " is not unique");
        }
    }

    @Test
    void testGeneratedAirportsHaveValidData() {
        List<Airport> airports = generator.generateAirportsAvro(15);
        System.out.println("[DEBUG_LOG] Validating airport data format:");

        for (Airport airport : airports) {
            System.out.println("[DEBUG_LOG] Validating airport: " + 
                "Code=" + airport.getAirportCode() + 
                ", Name=" + airport.getAirportName() + 
                ", City=" + airport.getCity() + 
                ", Country=" + airport.getCountry());

            // Check airport code format (3 uppercase letters)
            assertEquals(3, airport.getAirportCode().length(), 
                "Airport code should be exactly 3 characters: " + airport.getAirportCode());
            assertTrue(airport.getAirportCode().matches("[A-Z]{3}"), 
                "Airport code should be 3 uppercase letters: " + airport.getAirportCode());

            // Check that required fields are not empty
            assertNotNull(airport.getAirportName(), "Airport name should not be null");
            assertFalse(airport.getAirportName().isEmpty(), "Airport name should not be empty");

            assertNotNull(airport.getCity(), "City should not be null");
            assertFalse(airport.getCity().isEmpty(), "City should not be empty");

            assertNotNull(airport.getCountry(), "Country should not be null");
            assertFalse(airport.getCountry().isEmpty(), "Country should not be empty");

            // Check latitude and longitude ranges
            assertTrue(airport.getLatitude() >= -90 && airport.getLatitude() <= 90,
                "Latitude should be between -90 and 90: " + airport.getLatitude());
            assertTrue(airport.getLongitude() >= -180 && airport.getLongitude() <= 180,
                "Longitude should be between -180 and 180: " + airport.getLongitude());
        }
    }
}