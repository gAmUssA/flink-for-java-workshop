package com.example.generator;

import io.confluent.developer.models.reference.Airline;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class AirlineGeneratorTest {
    private final AirlineGenerator generator = new AirlineGenerator();

    @Test
    void testBasicAirlineModelAccess() {
        System.out.println("[DEBUG_LOG] Starting basic Airline model test");

        // Create a simple airline object
        Airline airline = Airline.newBuilder()
            .setAirlineCode("TS")
            .setAirlineName("Test Airline")
            .setCountry("Test Country")
            .build();

        System.out.println("[DEBUG_LOG] Created test airline: " + 
            airline.getAirlineCode() + " - " + 
            airline.getAirlineName() + " - " + 
            airline.getCountry());

        // Basic assertions
        assertNotNull(airline, "Airline object should not be null");
        assertEquals("TS", airline.getAirlineCode(), "Airline code should match");
        assertEquals("Test Airline", airline.getAirlineName(), "Airline name should match");
        assertEquals("Test Country", airline.getCountry(), "Country should match");
    }

    @Test
    void testGenerateAirlinesAvroContainsPredefinedAirlines() {
        int expectedCount = 15;
        List<Airline> airlines = generator.generateAirlinesAvro(expectedCount);

        // First check if we got any airlines
        assertNotNull(airlines, "Generated airlines list should not be null");
        System.out.println("[DEBUG_LOG] Generated airlines count: " + airlines.size());

        // Check the size
        assertTrue(airlines.size() > 0, "Generated airlines list should not be empty");
        assertEquals(expectedCount, airlines.size(), "Generated airlines count should match expected count");

        System.out.println("[DEBUG_LOG] Generated airlines:");
        airlines.forEach(a -> System.out.println("[DEBUG_LOG] " + a.getAirlineCode() + " - " + a.getAirlineName() + " - " + a.getCountry()));

        // Check American Airlines separately
        Optional<Airline> aa = airlines.stream()
            .filter(a -> "AA".equals(a.getAirlineCode()))
            .findFirst();

        assertTrue(aa.isPresent(), "American Airlines (AA) should be present");
        if (aa.isPresent()) {
            Airline americanAirlines = aa.get();
            assertEquals("American Airlines", americanAirlines.getAirlineName(), "Incorrect airline name for AA");
            assertEquals("United States", americanAirlines.getCountry(), "Incorrect country for AA");
        }

        // Check Lufthansa separately
        Optional<Airline> lh = airlines.stream()
            .filter(a -> "LH".equals(a.getAirlineCode()))
            .findFirst();

        assertTrue(lh.isPresent(), "Lufthansa (LH) should be present");
        if (lh.isPresent()) {
            Airline lufthansa = lh.get();
            assertEquals("Lufthansa", lufthansa.getAirlineName(), "Incorrect airline name for LH");
            assertEquals("Germany", lufthansa.getCountry(), "Incorrect country for LH");
        }
    }

    @Test
    void testGenerateAirlinesAvroRespectsCount() {
        int expectedCount = 15;
        List<Airline> airlines = generator.generateAirlinesAvro(expectedCount);
        System.out.println("[DEBUG_LOG] Expected count: " + expectedCount + ", Actual count: " + airlines.size());
        assertEquals(expectedCount, airlines.size(), "Generated airlines count doesn't match expected count");
    }

    @Test
    void testGenerateAirlinesAvroHasUniqueAirlineCodes() {
        List<Airline> airlines = generator.generateAirlinesAvro(20);
        Set<String> airlineCodes = new HashSet<>();
        System.out.println("[DEBUG_LOG] Checking uniqueness for airline codes:");

        for (Airline airline : airlines) {
            String airlineCode = airline.getAirlineCode();
            System.out.println("[DEBUG_LOG] Processing airline code: " + airlineCode);

            boolean isUnique = airlineCodes.add(airlineCode);
            if (!isUnique) {
                System.out.println("[DEBUG_LOG] Duplicate found for code: " + airlineCode);
            }
            assertTrue(isUnique, "Airline code " + airlineCode + " is not unique");
        }
    }

    @Test
    void testGeneratedAirlinesHaveValidData() {
        List<Airline> airlines = generator.generateAirlinesAvro(15);
        System.out.println("[DEBUG_LOG] Validating airline data format:");

        for (Airline airline : airlines) {
            System.out.println("[DEBUG_LOG] Validating airline: " + 
                "Code=" + airline.getAirlineCode() + 
                ", Name=" + airline.getAirlineName() + 
                ", Country=" + airline.getCountry());

            // Check airline code format (2 uppercase letters)
            assertEquals(2, airline.getAirlineCode().length(), 
                "Airline code should be exactly 2 characters: " + airline.getAirlineCode());
            assertTrue(airline.getAirlineCode().matches("[A-Z]{2}"), 
                "Airline code should be 2 uppercase letters: " + airline.getAirlineCode());

            // Check that name and country are not empty
            assertNotNull(airline.getAirlineName(), "Airline name should not be null");
            assertFalse(airline.getAirlineName().isEmpty(), "Airline name should not be empty");

            assertNotNull(airline.getCountry(), "Country should not be null");
            assertFalse(airline.getCountry().isEmpty(), "Country should not be empty");
        }
    }
}
