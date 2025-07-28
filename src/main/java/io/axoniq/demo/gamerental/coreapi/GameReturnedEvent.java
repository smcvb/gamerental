package io.axoniq.demo.gamerental.coreapi;

public record GameReturnedEvent(
        String gameIdentifier,
        String returner
) {

}
