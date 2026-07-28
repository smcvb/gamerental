package io.axoniq.demo.gamerental.diagnostics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StuckSegmentStateTest {

    private StuckSegmentState testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new StuckSegmentState();
    }

    @Test
    void doesNotBlockAndCountsHandledEventsWhenInactive() throws InterruptedException {
        testSubject.awaitReleaseIfTarget("some-key");
        testSubject.awaitReleaseIfTarget("some-key");

        assertEquals(2L, testSubject.handledCountsSnapshot().get("some-key"));
    }

    @Test
    void doesNotBlockNonTargetKeysWhileActive() throws InterruptedException {
        testSubject.activate("target-key");

        testSubject.awaitReleaseIfTarget("other-key");

        assertEquals(1L, testSubject.handledCountsSnapshot().get("other-key"));
    }

    @Test
    void blocksTheTargetKeyUntilDeactivated() throws InterruptedException {
        String targetKey = "target-key";
        testSubject.activate(targetKey);

        AtomicBoolean released = new AtomicBoolean(false);
        Thread blockedThread = new Thread(() -> {
            try {
                testSubject.awaitReleaseIfTarget(targetKey);
                released.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        blockedThread.start();

        Thread.sleep(200);
        assertFalse(released.get(), "Handler should still be blocked on the target key");

        testSubject.deactivate();
        blockedThread.join(2000);

        assertTrue(released.get(), "Handler should have been released after deactivate()");
    }
}
