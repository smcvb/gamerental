package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

@Command(namespace = "game-rental", name = "return")
public record ReturnGameCommand(
        @TargetEntityId String gameIdentifier,
        String returner
) {

}
