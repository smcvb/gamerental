package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.eventsourcing.annotations.EventTag;

import java.time.Instant;

public record GameRegisteredEvent(
        @EventTag(key = "gameId") String gameIdentifier,
        @EventTag(key = "title") String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
