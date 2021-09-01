package io.axoniq.demo.gamerental.controller;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.ConstructorProperties;

@Profile("rsocket")
@Controller
public class GameRentalRSocketController {

    private final ReactorCommandGateway commandGateway;
    private final ReactorQueryGateway queryGateway;

    public GameRentalRSocketController(ReactorCommandGateway commandGateway, ReactorQueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    // Request/Response - Registers a command, returning the identifier.
    @MessageMapping("register")
    public Mono<String> register(GameDto gameDto) {
        return commandGateway.send(new RegisterGameCommand(gameDto.getGameIdentifier(),
                                                           gameDto.getTitle(),
                                                           gameDto.getReleaseDate(),
                                                           gameDto.getDescription(),
                                                           gameDto.isSingleplayer(),
                                                           gameDto.isMultiplayer()));
    }

    // Fire-and-Forget - Renting a game, without any response.
    @MessageMapping("rent")
    public Mono<Void> rentGame(RentDto rentDto) {
        return commandGateway.send(new RentGameCommand(rentDto.gameIdentifier, rentDto.renter));
    }

    // Fire-and-Forget - Returning a rented game, without any response.
    @MessageMapping("return")
    public Mono<Void> returnGame(RentDto rentDto) {
        return commandGateway.send(new ReturnGameCommand(rentDto.gameIdentifier, rentDto.renter));
    }

    // Request/Response - Finding a game, based on an identifier.
    @MessageMapping("find")
    public Mono<Game> findGame(String gameIdentifier) {
        return queryGateway.query(new FindGameQuery(gameIdentifier), Game.class);
    }

    // Request/Streaming - Finding the game catalog, keeping the requester up-to-date.
    @MessageMapping("catalog")
    public Flux<String> watchGameCatalog() {
        return queryGateway.subscriptionQueryMany(new FullGameCatalogQuery(), String.class);
    }

    static class RentDto {

        private final String gameIdentifier;
        private final String renter;

        @ConstructorProperties({"gameIdentifier", "renter"})
        public RentDto(String gameIdentifier, String renter) {
            this.gameIdentifier = gameIdentifier;
            this.renter = renter;
        }

        public String getGameIdentifier() {
            return gameIdentifier;
        }

        public String getRenter() {
            return renter;
        }
    }
}
