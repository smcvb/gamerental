package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record RentGameCommand(
        @TargetAggregateIdentifier String gameIdentifier,
        String renter
) {

}
