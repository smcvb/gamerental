package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.eventhandling.processing.streaming.segmenting.Segment;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StuckSegmentKeysTest {

    @Test
    void producesOneDistinctKeyPerSegment() {
        String[] keys = StuckSegmentKeys.keys();

        assertEquals(StuckSegmentKeys.SEGMENT_COUNT, keys.length);
        assertEquals(StuckSegmentKeys.SEGMENT_COUNT, Set.of(keys).size());

        int mask = StuckSegmentKeys.SEGMENT_COUNT - 1;
        for (int segmentId = 0; segmentId < keys.length; segmentId++) {
            Segment segment = new Segment(segmentId, mask);
            assertTrue(segment.matches(keys[segmentId]),
                       "Key [" + keys[segmentId] + "] should match segment " + segmentId);

            for (int otherId = 0; otherId < keys.length; otherId++) {
                if (otherId == segmentId) {
                    continue;
                }
                Segment other = new Segment(otherId, mask);
                assertFalse(other.matches(keys[segmentId]),
                            "Key [" + keys[segmentId] + "] should not also match segment " + otherId);
            }
        }
    }

    @Test
    void isStableAcrossCalls() {
        assertArrayEquals(StuckSegmentKeys.keys(), StuckSegmentKeys.keys());
    }
}
