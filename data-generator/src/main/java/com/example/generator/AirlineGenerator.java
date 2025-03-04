package com.example.generator;

import io.confluent.developer.models.reference.Airline;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AirlineGenerator {
    private final Faker faker = new Faker(Locale.US);
    private final Set<String> usedCodes = new HashSet<>(Arrays.asList(
        "AA", "DL", "UA", "LH", "BA", "AF", "KL", "EK", "QR", "SQ"
    ));

    /**
     * Generate Avro airline objects
     * 
     * @param count Number of airlines to generate
     * @return List of Avro Airline objects
     */
    public List<Airline> generateAirlinesAvro(int count) {
        List<Airline> airlines = new ArrayList<>();

        // Add some real airlines first
        airlines.add(Airline.newBuilder()
                .setAirlineCode("AA")
                .setAirlineName("American Airlines")
                .setCountry("United States")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("DL")
                .setAirlineName("Delta Air Lines")
                .setCountry("United States")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("UA")
                .setAirlineName("United Airlines")
                .setCountry("United States")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("LH")
                .setAirlineName("Lufthansa")
                .setCountry("Germany")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("BA")
                .setAirlineName("British Airways")
                .setCountry("United Kingdom")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("AF")
                .setAirlineName("Air France")
                .setCountry("France")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("KL")
                .setAirlineName("KLM")
                .setCountry("Netherlands")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("EK")
                .setAirlineName("Emirates")
                .setCountry("United Arab Emirates")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("QR")
                .setAirlineName("Qatar Airways")
                .setCountry("Qatar")
                .build());

        airlines.add(Airline.newBuilder()
                .setAirlineCode("SQ")
                .setAirlineName("Singapore Airlines")
                .setCountry("Singapore")
                .build());

        // Generate additional random airlines if needed
        for (int i = airlines.size(); i < count; i++) {
            String airlineName = faker.aviation().airline();
            String airlineCode;

            // Generate a unique airline code
            do {
                airlineCode = generateAirlineCode(airlineName);
            } while (usedCodes.contains(airlineCode));

            usedCodes.add(airlineCode);

            Airline airline = Airline.newBuilder()
                .setAirlineCode(airlineCode)
                .setAirlineName(airlineName)
                .setCountry(faker.country().name())
                .build();

            airlines.add(airline);
        }

        return airlines;
    }

    private String generateAirlineCode(String airlineName) {
        // Clean and normalize the airline name
        String cleanName = airlineName.replaceAll("[^A-Za-z]", "").trim();

        if (cleanName.length() >= 2) {
            // Strategy 1: First two letters
            String code = cleanName.substring(0, 2).toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }

            // Strategy 2: First and last letter
            if (cleanName.length() > 2) {
                code = (cleanName.charAt(0) + "" + cleanName.charAt(cleanName.length() - 1)).toUpperCase();
                if (!usedCodes.contains(code)) {
                    return code;
                }
            }

            // Strategy 3: First letter with sequential number
            char firstLetter = cleanName.charAt(0);
            for (char secondChar = '0'; secondChar <= '9'; secondChar++) {
                code = (firstLetter + "" + secondChar).toUpperCase();
                if (!usedCodes.contains(code)) {
                    return code;
                }
            }
        }

        // Strategy 4: Random generation with retries
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            String code = faker.letterify("??").toUpperCase();
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        throw new RuntimeException("Failed to generate unique airline code after " + maxAttempts + " attempts");
    }
}
