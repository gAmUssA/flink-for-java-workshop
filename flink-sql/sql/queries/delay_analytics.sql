-- Sample queries for delay analytics

-- View current airline delays
SELECT * FROM AirlineDelays ORDER BY delayPercentage DESC;

-- Find airlines with highest average delay
SELECT * FROM AirlineDelays WHERE totalFlights > 10 ORDER BY averageDelay DESC LIMIT 5;

-- Find airlines with most reliable on-time performance
SELECT 
    airline, 
    averageDelay,
    totalFlights,
    delayPercentage,
    (100 - delayPercentage) AS onTimePercentage
FROM AirlineDelays 
WHERE totalFlights > 10 
ORDER BY delayPercentage ASC 
LIMIT 5;
