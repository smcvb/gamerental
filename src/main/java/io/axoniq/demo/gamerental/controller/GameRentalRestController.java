package io.axoniq.demo.gamerental.controller;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
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

@Profile("ui")
@RestController
@RequestMapping("/rental")
class GameRentalRestController {

    private final ReactorCommandGateway commandGateway;
    private final ReactorQueryGateway queryGateway;

    public GameRentalRestController(ReactorCommandGateway commandGateway, ReactorQueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/register/{identifier}")
    public Mono<String> register(@PathVariable String identifier, @RequestBody GameDto gameDto) {
        return commandGateway.send(new RegisterGameCommand(identifier,
                                                           gameDto.getTitle(),
                                                           gameDto.getReleaseDate(),
                                                           gameDto.getDescription(),
                                                           gameDto.isSingleplayer(),
                                                           gameDto.isMultiplayer()));
    }

    @PostMapping("/rent/{identifier}")
    public Mono<Void> rentGame(@PathVariable String identifier, @RequestParam String renter) {
        return commandGateway.send(new RentGameCommand(identifier, renter));
    }

    @PostMapping("/return/{identifier}")
    public Mono<Void> returnGame(@PathVariable String identifier, @RequestParam String returner) {
        return commandGateway.send(new ReturnGameCommand(identifier, returner));
    }

    @GetMapping("/{identifier}")
    public Mono<Game> findGame(@PathVariable String identifier) {
        return queryGateway.query(new FindGameQuery(identifier), Game.class);
    }

    @GetMapping("/catalog")
    public Mono<List<String>> findGameCatalog() {
        return queryGateway.query(new FullGameCatalogQuery(), ResponseTypes.multipleInstancesOf(String.class));
    }

    @GetMapping(value = "/catalog/watch", produces = "text/event-stream")
    public Flux<String> watchGameCatalog() {
        return queryGateway.subscriptionQueryMany(new FullGameCatalogQuery(), String.class);
    }
}
