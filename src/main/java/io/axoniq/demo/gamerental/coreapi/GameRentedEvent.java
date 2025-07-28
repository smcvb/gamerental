package io.axoniq.demo.gamerental.coreapi;

public record GameRentedEvent(
        String gameIdentifier,
        String renter
) {

}
