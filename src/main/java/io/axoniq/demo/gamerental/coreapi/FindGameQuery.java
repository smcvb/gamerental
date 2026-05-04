package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.queryhandling.annotation.Query;

@Query(namespace = "game-rental", name = "find")
public record FindGameQuery(
        String gameIdentifier
) {

}
