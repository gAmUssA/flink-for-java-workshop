-- Create Flight table to read from Kafka topic with Avro format
CREATE TABLE Flights (
    flightId STRING,
    origin STRING, 
    destination STRING,
    departureTime TIMESTAMP(3),
    arrivalTime TIMESTAMP(3),
    airline STRING,
    flightNumber STRING,
    aircraft STRING,
    status STRING,
    delayMinutes INT
    --PRIMARY KEY (flightId) NOT ENFORCED
) WITH (
    'connector' = 'kafka',
    'topic' = 'flights-avro',
    'properties.bootstrap.servers' = 'kafka:9092',
    'properties.group.id' = 'flink-sql-client',
    'scan.startup.mode' = 'earliest-offset',
    'format' = 'avro-confluent',
    'avro-confluent.schema-registry.url' = 'http://schema-registry:8081'
);

-- Create Airlines reference table
CREATE TABLE Airlines (
    airlineCode STRING,
    airlineName STRING,
    country STRING,
    PRIMARY KEY (airlineCode) NOT ENFORCED
) WITH (
    'connector' = 'kafka',
    'topic' = 'airlines',
    'properties.bootstrap.servers' = 'kafka:9092',
    'scan.startup.mode' = 'earliest-offset',
    'format' = 'json'
);

-- Create Airports reference table
CREATE TABLE Airports (
    airportCode STRING,
    airportName STRING,
    city STRING,
    country STRING,
    latitude DOUBLE,
    longitude DOUBLE,
    PRIMARY KEY (airportCode) NOT ENFORCED
) WITH (
    'connector' = 'kafka',
    'topic' = 'airports',
    'properties.bootstrap.servers' = 'kafka:9092',
    'scan.startup.mode' = 'earliest-offset',
    'format' = 'json'
);

-- Create Sink table for airline delays analytics
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

-- Create Sink table for route analytics
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

-- Create Sink table for flight status dashboard
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
