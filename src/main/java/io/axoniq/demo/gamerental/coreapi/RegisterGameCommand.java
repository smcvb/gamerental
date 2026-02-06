package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

import java.time.Instant;

@Command(name = "registerGame", routingKey = "gameIdentifier")
public record RegisterGameCommand(
        @TargetEntityId String gameIdentifier,
        String title,
        Instant releaseDate,
        String description,
        boolean singleplayer,
        boolean multiplayer
) {

}
