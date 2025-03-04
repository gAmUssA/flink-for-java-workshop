package com.example.generator;

import io.confluent.developer.models.reference.Airport;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AirportGenerator {
    private final Faker faker = new Faker(Locale.US);
    private final Set<String> usedCodes = new HashSet<>(Arrays.asList(
        "JFK", "LAX", "ORD", "LHR", "CDG", "DFW", "ATL", "DXB", "HND", "PEK"
    ));

    /**
     * Generate Avro airport objects
     * 
     * @param count Number of airports to generate
     * @return List of Avro Airport objects
     */
    public List<Airport> generateAirportsAvro(int count) {
        List<Airport> airports = new ArrayList<>();

        // Add some real airports first
        airports.add(Airport.newBuilder()
                .setAirportCode("JFK")
                .setAirportName("John F. Kennedy International Airport")
                .setCity("New York")
                .setCountry("United States")
                .setLatitude(40.6413)
                .setLongitude(-73.7781)
                .build());

        airports.add(Airport.newBuilder()
                .setAirportCode("LAX")
                .setAirportName("Los Angeles International Airport")
                .setCity("Los Angeles")
                .setCountry("United States")
                .setLatitude(33.9416)
                .setLongitude(-118.4085)
                .build());

        airports.add(Airport.newBuilder()
                .setAirportCode("ORD")
                .setAirportName("O'Hare International Airport")
                .setCity("Chicago")
                .setCountry("United States")
                .setLatitude(41.9742)
                .setLongitude(-87.9073)
                .build());

        airports.add(Airport.newBuilder()
                .setAirportCode("LHR")
                .setAirportName("Heathrow Airport")
                .setCity("London")
                .setCountry("United Kingdom")
                .setLatitude(51.4700)
                .setLongitude(-0.4543)
                .build());

        airports.add(Airport.newBuilder()
                .setAirportCode("CDG")
                .setAirportName("Charles de Gaulle Airport")
                .setCity("Paris")
                .setCountry("France")
                .setLatitude(49.0097)
                .setLongitude(2.5479)
                .build());

        // Generate additional random airports if needed
        for (int i = airports.size(); i < count; i++) {
            String airportName = faker.aviation().airport();
            String city = faker.address().city();
            String country = faker.country().name();
            String airportCode;

            // Generate a unique airport code
            do {
                airportCode = generateAirportCode(airportName, city);
            } while (usedCodes.contains(airportCode));

            usedCodes.add(airportCode);

            Airport airport = Airport.newBuilder()
                .setAirportCode(airportCode)
                .setAirportName(airportName)
                .setCity(city)
                .setCountry(country)
                .setLatitude(Double.parseDouble(faker.address().latitude()))
                .setLongitude(Double.parseDouble(faker.address().longitude()))
                .build();

            airports.add(airport);
        }

        return airports;
    }

    private String generateAirportCode(String airportName, String city) {
        // Clean and normalize the input strings
        String cleanCity = city.replaceAll("[^A-Za-z]", "").trim();
        String cleanAirportName = airportName.replaceAll("[^A-Za-z]", "").trim();

        // Strategy 1: First three letters of city
        if (cleanCity.length() >= 3) {
            String code = cleanCity.substring(0, 3).toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        // Strategy 2: First two letters of city + first letter of airport name
        if (cleanCity.length() >= 2 && cleanAirportName.length() >= 1) {
            String code = (cleanCity.substring(0, 2) + cleanAirportName.charAt(0)).toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        // Strategy 3: First letter of city + first two letters of airport name
        if (cleanCity.length() >= 1 && cleanAirportName.length() >= 2) {
            String code = (cleanCity.charAt(0) + cleanAirportName.substring(0, 2)).toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        // Strategy 4: First letter of city with sequential numbers
        if (cleanCity.length() >= 1) {
            char firstLetter = cleanCity.charAt(0);
            for (int i = 0; i <= 99; i++) {
                String code = String.format("%c%02d", firstLetter, i).toUpperCase();
                if (!usedCodes.contains(code)) {
                    return code;
                }
            }
        }

        // Strategy 5: Random generation with retries
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            String code = faker.letterify("???").toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        throw new RuntimeException("Failed to generate unique airport code after " + maxAttempts + " attempts");
    }
}
