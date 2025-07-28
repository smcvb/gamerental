package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;

public record RegisterGameCommand(
        @TargetAggregateIdentifier String gameIdentifier,
        String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
