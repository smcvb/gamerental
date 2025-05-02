package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotations.EventTag;

public record GameRentedEvent(
        @EventTag(key = "gameId") String gameIdentifier,
        String renter
) {

}
