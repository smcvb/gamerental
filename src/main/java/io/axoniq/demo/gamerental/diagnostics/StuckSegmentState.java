package io.axoniq.demo.gamerental.diagnostics;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
class StuckSegmentState {

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicReference<String> targetKey = new AtomicReference<>(StuckSegmentKeys.keys()[0]);
    private final AtomicReference<CountDownLatch> gate = new AtomicReference<>(new CountDownLatch(1));
    private final Map<String, AtomicLong> handledCounts = new ConcurrentHashMap<>();

    boolean isActive() {
        return active.get();
    }

    String targetKey() {
        return targetKey.get();
    }

    void activate(String targetKey) {
        this.targetKey.set(targetKey);
        gate.set(new CountDownLatch(1));
        active.set(true);
    }

    void deactivate() {
        active.set(false);
        gate.get().countDown();
    }

    /**
     * Called by the event handler for every event it processes. Blocks indefinitely, on the calling
     * (segment worker) thread, when this demo is active and the event belongs to the current target segment
     * key. This simulates a hung external call inside an event handler.
     */
    void awaitReleaseIfTarget(String segmentKey) throws InterruptedException {
        handledCounts.computeIfAbsent(segmentKey, key -> new AtomicLong()).incrementAndGet();
        if (active.get() && segmentKey.equals(targetKey.get())) {
            gate.get().await();
        }
    }

    Map<String, Long> handledCountsSnapshot() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        handledCounts.forEach((key, count) -> snapshot.put(key, count.get()));
        return snapshot;
    }
}
