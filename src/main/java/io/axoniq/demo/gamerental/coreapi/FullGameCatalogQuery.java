package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.queryhandling.annotation.Query;

@Query(namespace = "game-rental", name = "full")
public class FullGameCatalogQuery {

    public FullGameCatalogQuery() {
    }

    @Override
    public String toString() {
        return "FullGameCatalogQuery";
    }
}
