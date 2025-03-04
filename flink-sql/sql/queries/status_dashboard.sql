-- Sample queries for flight status dashboard

-- View upcoming departures within the next hour
SELECT 
    flightId,
    airlineName,
    flightNumber,
    origin,
    originCity,
    destination,
    destinationCity,
    status,
    departureTime,
    delayMinutes
FROM FlightStatusDashboard
WHERE departureTime BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '1' HOUR
ORDER BY departureTime;

-- View delayed flights
SELECT 
    flightId,
    airlineName,
    flightNumber,
    origin,
    destination,
    status,
    departureTime,
    delayMinutes
FROM FlightStatusDashboard
WHERE delayMinutes > 0
ORDER BY delayMinutes DESC;

-- View flights by status
SELECT 
    status,
    COUNT(*) as flightCount
FROM FlightStatusDashboard
GROUP BY status;
