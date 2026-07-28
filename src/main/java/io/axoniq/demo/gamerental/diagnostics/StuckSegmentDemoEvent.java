package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.eventhandling.annotation.Event;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Objects;

@Event
public class StuckSegmentDemoEvent {

    private final String segmentKey;
    private final long sequenceNumber;
    private final Instant publishedAt;

    @ConstructorProperties({"segmentKey", "sequenceNumber", "publishedAt"})
    public StuckSegmentDemoEvent(String segmentKey, long sequenceNumber, Instant publishedAt) {
        this.segmentKey = segmentKey;
        this.sequenceNumber = sequenceNumber;
        this.publishedAt = publishedAt;
    }

    public String getSegmentKey() {
        return segmentKey;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StuckSegmentDemoEvent that = (StuckSegmentDemoEvent) o;
        return sequenceNumber == that.sequenceNumber &&
                Objects.equals(segmentKey, that.segmentKey) &&
                Objects.equals(publishedAt, that.publishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentKey, sequenceNumber, publishedAt);
    }

    @Override
    public String toString() {
        return "StuckSegmentDemoEvent{" +
                "segmentKey='" + segmentKey + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
