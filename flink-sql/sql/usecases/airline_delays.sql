-- Airline Delay Analytics
-- This query analyzes flight delays by airline and outputs statistics

-- First, make sure tables are created
DROP TABLE IF EXISTS AirlineDelays;

CREATE TABLE AirlineDelays (
    airline STRING,
    averageDelay DOUBLE,
    totalFlights BIGINT,
    delayedFlights BIGINT,
    delayPercentage DOUBLE,
    PRIMARY KEY (airline) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'airline-delays',
    'properties.bootstrap.servers' = 'kafka:9092',
    'key.format' = 'json',
    'value.format' = 'json'
);

-- Insert analytics results into sink table
INSERT INTO AirlineDelays
SELECT 
    f.airline,
    AVG(f.delayMinutes) AS averageDelay,
    COUNT(*) AS totalFlights,
    COUNT(CASE WHEN f.delayMinutes > 0 THEN 1 END) AS delayedFlights,
    CAST(COUNT(CASE WHEN f.delayMinutes > 0 THEN 1 END) AS DOUBLE) / COUNT(*) * 100 AS delayPercentage
FROM Flights f
GROUP BY f.airline;
