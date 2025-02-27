package io.confluent.developer.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Basic event model for the workshop.
 */
public class Event {
    private String id;
    private String type;
    private Instant timestamp;
    private String payload;

    // Default constructor for serialization frameworks
    public Event() {
    }

    public Event(String id, String type, Instant timestamp, String payload) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(type, event.type) &&
                Objects.equals(timestamp, event.timestamp) &&
                Objects.equals(payload, event.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timestamp, payload);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", payload='" + payload + '\'' +
                '}';
    }
}
