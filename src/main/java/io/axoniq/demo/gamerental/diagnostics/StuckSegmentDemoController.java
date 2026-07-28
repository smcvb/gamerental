package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Toggle for reproducing a "stuck segment" scenario: a Pooled Streaming Event Processor whose worker for
 * one specific segment blocks indefinitely (as if an event handler were waiting on a hung external call),
 * while events keep flowing to the processor's other segments.
 */
@Profile("diagnostics")
@RestController
@RequestMapping("/diagnostics/stuck-segment")
class StuckSegmentDemoController {

    private static final Logger logger = LoggerFactory.getLogger(StuckSegmentDemoController.class);
    private static final long PUBLISH_INTERVAL_MS = 200;

    private final EventGateway eventGateway;
    private final StuckSegmentState state;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "stuck-segment-demo-publisher");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicLong sequenceNumber = new AtomicLong();
    private final AtomicReference<ScheduledFuture<?>> publishTask = new AtomicReference<>();

    StuckSegmentDemoController(EventGateway eventGateway, StuckSegmentState state) {
        this.eventGateway = eventGateway;
        this.state = state;
    }

    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestParam(defaultValue = "0") int targetSegment) {
        if (state.isActive()) {
            stop();
        } else {
            start(targetSegment);
        }
        return status();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "active", state.isActive(),
                "targetKey", state.targetKey(),
                "handledCounts", state.handledCountsSnapshot()
        );
    }

    private void start(int targetSegment) {
        String[] keys = StuckSegmentKeys.keys();
        String targetKey = keys[Math.floorMod(targetSegment, keys.length)];
        state.activate(targetKey);
        logger.info("Stuck-segment demo activated, blocking segment key [{}]", targetKey);
        publishTask.set(scheduler.scheduleAtFixedRate(this::publishRound, 0, PUBLISH_INTERVAL_MS, TimeUnit.MILLISECONDS));
    }

    private void stop() {
        state.deactivate();
        logger.info("Stuck-segment demo deactivated, releasing blocked segment key [{}]", state.targetKey());
        ScheduledFuture<?> task = publishTask.getAndSet(null);
        if (task != null) {
            task.cancel(false);
        }
    }

    private void publishRound() {
        List<Object> events = new ArrayList<>();
        for (String key : StuckSegmentKeys.keys()) {
            events.add(new StuckSegmentDemoEvent(key, sequenceNumber.incrementAndGet(), Instant.now()));
        }
        eventGateway.publish(events)
                    .exceptionally(exception -> {
                        logger.warn("Failed to publish stuck-segment demo events", exception);
                        return null;
                    });
    }
}
