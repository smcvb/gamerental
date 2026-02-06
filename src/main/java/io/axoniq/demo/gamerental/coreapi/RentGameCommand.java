package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

@Command(name = "rentGame", routingKey = "gameIdentifier")
public record RentGameCommand(
        @TargetEntityId String gameIdentifier,
        String renter
) {

}
