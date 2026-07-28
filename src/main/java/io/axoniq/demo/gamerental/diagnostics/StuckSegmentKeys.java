package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.eventhandling.processing.streaming.segmenting.Segment;

/**
 * Brute-forces one {@code segmentKey} per segment of a freshly split, 4-segment
 * {@link org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor}.
 * Uses the real {@link Segment#matches(Object)} logic so the keys are guaranteed to route to distinct
 * segments regardless of Axon's internal hashing/masking details.
 */
final class StuckSegmentKeys {

    static final int SEGMENT_COUNT = 4;
    private static final int MASK = SEGMENT_COUNT - 1;
    private static final String[] KEYS = compute();

    private StuckSegmentKeys() {
    }

    static String[] keys() {
        return KEYS.clone();
    }

    private static String[] compute() {
        Segment[] segments = new Segment[SEGMENT_COUNT];
        for (int id = 0; id < SEGMENT_COUNT; id++) {
            segments[id] = new Segment(id, MASK);
        }

        String[] keys = new String[SEGMENT_COUNT];
        int found = 0;
        for (int n = 0; found < SEGMENT_COUNT; n++) {
            String candidate = "demo-key-" + n;
            for (int id = 0; id < SEGMENT_COUNT; id++) {
                if (keys[id] == null && segments[id].matches(candidate)) {
                    keys[id] = candidate;
                    found++;
                    break;
                }
            }
        }
        return keys;
    }
}
