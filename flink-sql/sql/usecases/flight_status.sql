-- Flight Status Dashboard
-- This query enriches flight data with airline and airport information

-- First, make sure tables are created
DROP TABLE IF EXISTS FlightStatusDashboard;

CREATE TABLE FlightStatusDashboard (
    flightId STRING,
    airline STRING,
    airlineName STRING,
    flightNumber STRING,
    origin STRING,
    originCity STRING,
    destination STRING,
    destinationCity STRING,
    status STRING,
    departureTime TIMESTAMP(3),
    arrivalTime TIMESTAMP(3),
    delayMinutes INT,
    updateTime TIMESTAMP(3),
    PRIMARY KEY (flightId) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'flight-status-dashboard',
    'properties.bootstrap.servers' = 'kafka:9092',
    'key.format' = 'json',
    'value.format' = 'json'
);

-- Insert enriched flight information into dashboard
INSERT INTO FlightStatusDashboard
SELECT 
    f.flightId,
    f.airline,
    a.airlineName,
    f.flightNumber,
    f.origin,
    o.city AS originCity,
    f.destination,
    d.city AS destinationCity,
    f.status,
    f.departureTime,
    f.arrivalTime,
    f.delayMinutes,
    CURRENT_TIMESTAMP AS updateTime
FROM Flights f
LEFT JOIN Airlines a ON f.airline = a.airlineCode
LEFT JOIN Airports o ON f.origin = o.airportCode
LEFT JOIN Airports d ON f.destination = d.airportCode;
