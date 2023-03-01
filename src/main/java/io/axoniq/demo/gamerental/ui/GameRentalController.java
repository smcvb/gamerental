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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Profile("ui")
@RestController
@RequestMapping("/rental")
class GameRentalController {

    private final ReactorCommandGateway commandGateway;
    private final ReactorQueryGateway queryGateway;

    public GameRentalController(ReactorCommandGateway commandGateway, ReactorQueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        commandGateway.registerResultHandlerInterceptor(
                (cmd, result) -> result.onErrorMap(GameRentalController::mapRemoteException)
        );

        this.queryGateway = queryGateway;
        queryGateway.registerResultHandlerInterceptor(
                (query, result) -> result.onErrorMap(GameRentalController::mapRemoteException)
        );
        queryGateway.registerResultHandlerInterceptor((query, result) -> result.timeout(Duration.ofMillis(500)));
    }

    @PostMapping("/register/{identifier}")
    public Mono<String> register(@PathVariable("identifier") String gameIdentifier,
                                 @RequestBody GameDto gameDto) {
        return commandGateway.send(new RegisterGameCommand(gameIdentifier,
                                                           gameDto.getTitle(),
                                                           gameDto.getReleaseDate(),
                                                           gameDto.getDescription(),
                                                           gameDto.isSingleplayer(),
                                                           gameDto.isMultiplayer()));
    }

    @PostMapping("/rent/{identifier}")
    public Mono<Void> rentGame(@PathVariable String identifier,
                               @RequestParam String renter) {
        return commandGateway.send(new RentGameCommand(identifier, renter));
    }

    @PostMapping("/return/{identifier}")
    public Mono<Void> returnGame(@PathVariable String identifier,
                                 @RequestParam String returner) {
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

    private static Throwable mapRemoteException(Throwable exception) {
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
}
