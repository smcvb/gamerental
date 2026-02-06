package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

import java.time.Instant;

@Event(name = "gameRegistered")
public record GameRegisteredEvent(
        @EventTag(key = "gameIdentifier") String gameIdentifier,
        String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
