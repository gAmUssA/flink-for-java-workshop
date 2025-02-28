package io.confluent.developer.models.generator;

import io.confluent.developer.models.flight.Flight;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generator for creating random flight data.
 * This class encapsulates the logic for generating random flight information.
 */
public class FlightGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(FlightGenerator.class);
    
    private final Random random;
    private final Faker faker;
    
    // Lists of airports and airlines for random selection
    private static final List<String> AIRPORTS = Arrays.asList(
            "ATL", "LAX", "ORD", "DFW", "DEN", "JFK", "SFO", "SEA", "LAS", "MCO",
            "EWR", "CLT", "PHX", "IAH", "MIA", "BOS", "MSP", "DTW", "FLL", "PHL"
    );
    
    private static final List<String> AIRLINES = Arrays.asList(
            "Delta", "American", "United", "Southwest", "JetBlue",
            "Alaska", "Spirit", "Frontier", "Hawaiian", "Allegiant"
    );
    
    private static final List<String> STATUSES = Arrays.asList(
            "ON_TIME", "DELAYED", "CANCELLED", "BOARDING", "IN_AIR", "LANDED", "DIVERTED"
    );

    /**
     * Constructor with default seed.
     */
    public FlightGenerator() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructor with specific seed for reproducible results.
     * 
     * @param seed the random seed to use
     */
    public FlightGenerator(long seed) {
        this.random = new Random(seed);
        this.faker = new Faker(random);
        LOG.info("FlightGenerator initialized with seed: {}", seed);
    }

    /**
     * Generates a random flight record.
     *
     * @return A Flight object with random data
     */
    public Flight generateFlight() {
        // Generate a random flight number
        String flightNumber = faker.aviation().flight();
        
        // Select random origin and destination airports
        String origin = AIRPORTS.get(random.nextInt(AIRPORTS.size()));
        String destination;
        do {
            destination = AIRPORTS.get(random.nextInt(AIRPORTS.size()));
        } while (destination.equals(origin)); // Ensure origin and destination are different
        
        // Select a random airline
        String airline = AIRLINES.get(random.nextInt(AIRLINES.size()));
        
        // Generate departure times
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime scheduledDeparture = now.plusHours(random.nextInt(48)); // Random time in next 48 hours
        
        long scheduledDepartureMillis = scheduledDeparture.toInstant().toEpochMilli();
        Long actualDepartureMillis = null;
        
        // Determine if the flight has departed
        boolean hasDeparted = random.nextDouble() < 0.7; // 70% chance the flight has departed
        
        if (hasDeparted) {
            // For departed flights, actual departure could be on time, early, or delayed
            int minutesOffset = random.nextInt(60) - 15; // -15 to +45 minutes
            actualDepartureMillis = scheduledDepartureMillis + TimeUnit.MINUTES.toMillis(minutesOffset);
        }
        
        // Determine flight status
        String status;
        if (!hasDeparted) {
            // Flight hasn't departed yet
            status = random.nextDouble() < 0.9 ? "SCHEDULED" : "CANCELLED";
        } else {
            // Flight has departed
            status = STATUSES.get(random.nextInt(STATUSES.size()));
        }
        
        // Create and return the Flight object
        Flight.Builder builder = Flight.newBuilder()
                .setFlightNumber(flightNumber)
                .setAirline(airline)
                .setOrigin(origin)
                .setDestination(destination)
                .setScheduledDeparture(scheduledDepartureMillis)
                .setStatus(status);
                
        if (actualDepartureMillis != null) {
            builder.setActualDeparture(actualDepartureMillis);
        }
        
        Flight flight = builder.build();
        LOG.debug("Generated flight: {}", flight);
        return flight;
    }
    
    /**
     * Gets a list of random airports used by this generator.
     *
     * @return List of airport codes
     */
    public List<String> getAirports() {
        return AIRPORTS;
    }
    
    /**
     * Gets a list of airlines used by this generator.
     *
     * @return List of airline names
     */
    public List<String> getAirlines() {
        return AIRLINES;
    }
    
    /**
     * Gets a list of possible flight statuses.
     *
     * @return List of possible flight statuses
     */
    public List<String> getStatuses() {
        return STATUSES;
    }
}