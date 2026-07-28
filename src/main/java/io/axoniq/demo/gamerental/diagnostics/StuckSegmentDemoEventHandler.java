package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("diagnostics")
@Component
@Namespace("stuck-segment-demo")
class StuckSegmentDemoEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(StuckSegmentDemoEventHandler.class);

    private final StuckSegmentState state;

    StuckSegmentDemoEventHandler(StuckSegmentState state) {
        this.state = state;
    }

    @EventHandler
    public void on(StuckSegmentDemoEvent event) throws InterruptedException {
        String segmentKey = event.getSegmentKey();
        logger.info("Handling event #{} for segment key [{}] on thread [{}]",
                    event.getSequenceNumber(), segmentKey, Thread.currentThread().getName());

        state.awaitReleaseIfTarget(segmentKey);

        logger.info("Finished handling event #{} for segment key [{}] on thread [{}]",
                    event.getSequenceNumber(), segmentKey, Thread.currentThread().getName());
    }
}
