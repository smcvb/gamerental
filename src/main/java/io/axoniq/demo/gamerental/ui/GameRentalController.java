package io.axoniq.demo.gamerental.ui;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryExecutionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/rental")
class GameRentalController {

    private final ReactorCommandGateway commandGateway;
    private final ReactorQueryGateway queryGateway;

    public GameRentalController(ReactorCommandGateway commandGateway, ReactorQueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/register/{identifier}")
    public Mono<String> register(@PathVariable("identifier") String gameIdentifier,
                                 @RequestBody GameDto gameDto) {
        return commandGateway.send(new RegisterGameCommand(gameIdentifier,
                                                           gameDto.title,
                                                           gameDto.releaseDate,
                                                           gameDto.description,
                                                           gameDto.singleplayer,
                                                           gameDto.multiplayer));
    }

    @PostMapping("/register")
    public Mono<String> register(@RequestBody GameDto gameDto) {
        return register(UUID.randomUUID().toString(), gameDto);
    }

    @PostMapping("/rent/{identifier}")
    public Mono<Object> rentGame(@PathVariable("identifier") String identifier,
                                 @RequestParam("renter") String renter) {
        return commandGateway.send(new RentGameCommand(identifier, renter))
                             .onErrorMap(this::mapRemoteException);
    }

    @PostMapping("/return/{identifier}")
    public Mono<Object> returnGame(@PathVariable("identifier") String identifier,
                                   @RequestParam("returner") String returner) {
        return commandGateway.send(new ReturnGameCommand(identifier, returner))
                             .onErrorMap(this::mapRemoteException);
    }

    @GetMapping("/{identifier}")
    public Mono<Game> findGame(@PathVariable("identifier") String identifier) {
        return queryGateway.query(new FindGameQuery(identifier), Game.class)
                           .onErrorMap(this::mapRemoteException);
    }

    @GetMapping("/catalog")
    public Mono<List<String>> findGameCatalog() {
        return queryGateway.query(new FullGameCatalogQuery(), ResponseTypes.multipleInstancesOf(String.class))
                           .onErrorMap(this::mapRemoteException);
    }

    @GetMapping(value = "/catalog/watch", produces = "text/event-stream")
    public Flux<String> watchGameCatalog() {
        return queryGateway.subscriptionQueryMany(new FullGameCatalogQuery(), String.class)
                           .onErrorMap(this::mapRemoteException);
    }

    private Throwable mapRemoteException(Throwable exception) {
        if (exception instanceof CommandExecutionException) {
            Optional<Object> details = ((CommandExecutionException) exception).getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalCommandException(statusCode.getDescription(), null, statusCode);
            }
        } else if ((exception instanceof QueryExecutionException)) {
            Optional<Object> details = ((QueryExecutionException) exception).getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalQueryException(statusCode.getDescription(), null, statusCode);
            }
        }
        return exception;
    }

    private static class GameDto {

        private final String title;
        private final Instant releaseDate;
        private final String description;
        private final boolean singleplayer;
        private final boolean multiplayer;

        @ConstructorProperties({"title", "releaseDate", "description", "singleplayer", "multiplayer"})
        public GameDto(String title,
                       Instant releaseDate,
                       String description,
                       boolean singleplayer,
                       boolean multiplayer) {
            this.title = title;
            this.releaseDate = releaseDate;
            this.description = description;
            this.singleplayer = singleplayer;
            this.multiplayer = multiplayer;
        }
    }
}
