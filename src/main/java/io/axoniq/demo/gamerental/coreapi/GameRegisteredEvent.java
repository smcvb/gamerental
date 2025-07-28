package io.axoniq.demo.gamerental.coreapi;

import java.time.Instant;

public record GameRegisteredEvent(
        String gameIdentifier,
        String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
