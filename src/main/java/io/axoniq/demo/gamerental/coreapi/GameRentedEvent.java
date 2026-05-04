package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event(namespace = "game-rental", name = "rented")
public record GameRentedEvent(
        @EventTag(key = "gameId") String gameIdentifier,
        @EventTag(key = "renter") String renter
) {

}
