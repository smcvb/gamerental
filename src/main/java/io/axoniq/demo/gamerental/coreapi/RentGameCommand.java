package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.annotation.TargetEntityId;

public record RentGameCommand(
        @TargetEntityId String gameIdentifier,
        String renter
) {

}
