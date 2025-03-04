-- Flight Route Analytics
-- This query analyzes flight routes and calculates statistics

-- First, make sure tables are created
DROP TABLE IF EXISTS RouteAnalytics;

CREATE TABLE RouteAnalytics (
    origin STRING,
    destination STRING,
    totalFlights BIGINT,
    averageDelay DOUBLE,
    PRIMARY KEY (origin, destination) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'route-analytics',
    'properties.bootstrap.servers' = 'kafka:9092',
    'key.format' = 'json',
    'value.format' = 'json'
);

-- Insert route analytics results into sink table
INSERT INTO RouteAnalytics
SELECT 
    f.origin,
    f.destination,
    COUNT(*) AS totalFlights,
    AVG(f.delayMinutes) AS averageDelay
FROM Flights f
GROUP BY f.origin, f.destination;
