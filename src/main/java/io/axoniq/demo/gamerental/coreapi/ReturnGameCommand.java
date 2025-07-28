package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReturnGameCommand(
        @TargetAggregateIdentifier String gameIdentifier,
        String returner
) {

}
