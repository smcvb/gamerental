package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event(name = "gameRented")
public record GameRentedEvent(
        @EventTag(key = "gameIdentifier") String gameIdentifier,
        String renter
) {

}
