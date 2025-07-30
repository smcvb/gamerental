package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.annotation.TargetEntityId;

public record ReturnGameCommand(
        @TargetEntityId String gameIdentifier,
        String returner
) {

}
