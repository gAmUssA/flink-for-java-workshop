package io.confluent.developer.generator;

import io.confluent.developer.models.flight.Flight;
import net.datafaker.Faker;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A custom Flink source that generates random flight data.
 */
public class DataGeneratorSource extends RichSourceFunction<Flight> implements CheckpointedFunction {
    private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorSource.class);
    private static final long serialVersionUID = 1L;
    
    private final int recordsPerSecond;
    private volatile boolean running = true;
    private transient Faker faker;
    private transient Random random;
    private transient ListState<Long> checkpointedCount;
    private long count = 0L;
    
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
     * Constructor for the DataGeneratorSource.
     *
     * @param recordsPerSecond The number of records to generate per second
     */
    public DataGeneratorSource(int recordsPerSecond) {
        this.recordsPerSecond = recordsPerSecond;
    }

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        this.faker = new Faker();
        this.random = new Random();
        LOG.info("DataGeneratorSource initialized with rate: {} records/second", recordsPerSecond);
    }

    @Override
    public void run(SourceContext<Flight> ctx) throws Exception {
        final long sleepTimeMillis = 1000 / recordsPerSecond;
        
        while (running) {
            synchronized (ctx.getCheckpointLock()) {
                ctx.collect(generateFlight());
                count++;
                
                if (count % recordsPerSecond == 0) {
                    LOG.info("Generated {} flights", count);
                }
            }
            
            // Sleep to control the generation rate
            if (sleepTimeMillis > 0) {
                Thread.sleep(sleepTimeMillis);
            }
        }
    }

    /**
     * Generates a random flight record.
     *
     * @return A Flight object with random data
     */
    private Flight generateFlight() {
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
        
        return builder.build();
    }

    @Override
    public void cancel() {
        running = false;
        LOG.info("DataGeneratorSource cancelled after generating {} flights", count);
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        checkpointedCount.clear();
        checkpointedCount.add(count);
        LOG.debug("Checkpoint created at count: {}", count);
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<Long> descriptor = new ListStateDescriptor<>(
                "flight-generator-state",
                TypeInformation.of(new TypeHint<Long>() {})
        );
        
        checkpointedCount = context.getOperatorStateStore().getListState(descriptor);
        
        if (context.isRestored()) {
            for (Long countValue : checkpointedCount.get()) {
                count = countValue;
            }
            LOG.info("Restored state with count: {}", count);
        }
    }
}
