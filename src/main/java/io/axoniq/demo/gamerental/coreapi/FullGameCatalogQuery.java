package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.queryhandling.annotation.Query;

@Query(name = "fullGameCatalog")
public record FullGameCatalogQuery() {

}
