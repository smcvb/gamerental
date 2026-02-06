package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

@Command(name = "returnGame", routingKey = "gameIdentifier")
public record ReturnGameCommand(
        @TargetEntityId String gameIdentifier,
        String returner
) {

}
