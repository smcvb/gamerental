package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotations.EventTag;

public record GameReturnedEvent(
        @EventTag(key = "gameId") String gameIdentifier,
        String returner
) {

}
