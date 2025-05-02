package io.axoniq.demo.gamerental.controller;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.messaging.unitofwork.NoProcessingContext;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Profile("ui")
@RestController
@RequestMapping("/rental")
class GameRentalRestController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public GameRentalRestController(CommandGateway commandGateway,
                                    QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/register/{identifier}")
    public CompletableFuture<String> register(@PathVariable("identifier") String gameIdentifier,
                                              @RequestBody GameDto gameDto) {
        return commandGateway.send(
                new RegisterGameCommand(gameIdentifier,
                                        gameDto.getTitle(),
                                        gameDto.getReleaseDate(),
                                        gameDto.getDescription(),
                                        gameDto.isSingleplayer(),
                                        gameDto.isMultiplayer()),
                NoProcessingContext.INSTANCE,
                String.class
        );
    }

    @PostMapping("/rent/{identifier}")
    public CompletableFuture<Void> rentGame(@PathVariable String identifier,
                                            @RequestParam String renter) {
        return commandGateway.send(new RentGameCommand(identifier, renter),
                                   NoProcessingContext.INSTANCE,
                                   Void.class);
    }

    @PostMapping("/return/{identifier}")
    public CompletableFuture<Void> returnGame(@PathVariable String identifier,
                                              @RequestParam String returner) {
        return commandGateway.send(new ReturnGameCommand(identifier, returner),
                                   NoProcessingContext.INSTANCE,
                                   Void.class);
    }

    @GetMapping("/{identifier}")
    public CompletableFuture<Game> findGame(@PathVariable String identifier) {
        return queryGateway.query(new FindGameQuery(identifier), Game.class);
    }

    @GetMapping("/catalog")
    public CompletableFuture<List<String>> findGameCatalog() {
        return queryGateway.query(new FullGameCatalogQuery(), ResponseTypes.multipleInstancesOf(String.class));
    }

    @GetMapping(value = "/catalog/watch", produces = "text/event-stream")
    public Flux<String> watchGameCatalog() {
        return queryGateway.subscriptionQuery(new FullGameCatalogQuery(), String.class, String.class)
                           .updates();
    }
}
