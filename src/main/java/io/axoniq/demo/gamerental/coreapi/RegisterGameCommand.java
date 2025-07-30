package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.annotation.TargetEntityId;

import java.time.Instant;

public record RegisterGameCommand(
        @TargetEntityId String gameIdentifier,
        String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
