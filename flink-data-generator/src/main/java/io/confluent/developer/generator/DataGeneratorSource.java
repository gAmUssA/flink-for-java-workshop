package io.confluent.developer.generator;

import io.confluent.developer.models.flight.Flight;
import io.confluent.developer.models.generator.FlightGenerator;
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

/**
 * A custom Flink source that generates random flight data.
 */
@Deprecated // We'll eventually migrate to the Flink Source API
public class DataGeneratorSource extends RichSourceFunction<Flight> implements CheckpointedFunction {
    private static final Logger LOG = LoggerFactory.getLogger(DataGeneratorSource.class);
    private static final long serialVersionUID = 1L;
    
    private final int recordsPerSecond;
    private volatile boolean running = true;
    private transient FlightGenerator flightGenerator;
    private transient ListState<Long> checkpointedCount;
    private long count = 0L;

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
        this.flightGenerator = new FlightGenerator();
        LOG.info("DataGeneratorSource initialized with rate: {} records/second", recordsPerSecond);
    }

    @Override
    public void run(SourceContext<Flight> ctx) throws Exception {
        final long sleepTimeMillis = 1000 / recordsPerSecond;
        
        while (running) {
            synchronized (ctx.getCheckpointLock()) {
                Flight flight = flightGenerator.generateFlight();
                ctx.collect(flight);
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
