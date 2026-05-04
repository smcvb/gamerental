package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

import java.time.Instant;

@Event(namespace = "game-rental", name = "registered")
public record GameRegisteredEvent(
        @EventTag(key = "gameId") String gameIdentifier,
        @EventTag(key = "title") String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
