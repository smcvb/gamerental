package io.axoniq.demo.gamerental.diagnostics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StuckSegmentDemoEventHandlerTest {

    private StuckSegmentState state;

    private StuckSegmentDemoEventHandler testSubject;

    @BeforeEach
    void setUp() {
        state = mock(StuckSegmentState.class);

        testSubject = new StuckSegmentDemoEventHandler(state);
    }

    @Test
    void delegatesToStateWithTheEventsSegmentKey() throws InterruptedException {
        StuckSegmentDemoEvent event = new StuckSegmentDemoEvent("demo-key-0", 1L, Instant.now());

        testSubject.on(event);

        verify(state).awaitReleaseIfTarget("demo-key-0");
    }
}
