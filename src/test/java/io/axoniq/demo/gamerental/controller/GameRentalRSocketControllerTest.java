package io.axoniq.demo.gamerental.controller;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameRentalRSocketControllerTest {

    private static RSocketRequester testRequester;

    @MockBean
    private ReactorCommandGateway commandGateway;
    @MockBean
    private ReactorQueryGateway queryGateway;

    @BeforeAll
    static void beforeAll(@Autowired RSocketRequester.Builder builder,
                          @LocalRSocketServerPort Integer port) {
        testRequester = builder.tcp("localhost", port);
    }

    @Test
    void testRegister() {
        GameDto testDto =
                new GameDto(TITLE, RELEASE_DATE, DESCRIPTION, true, true);
        testDto.setGameIdentifier(GAME_IDENTIFIER);

        RegisterGameCommand expectedCommand = new RegisterGameCommand(
                GAME_IDENTIFIER, testDto.getTitle(), testDto.getReleaseDate(), testDto.getDescription(),
                testDto.isSingleplayer(), testDto.isMultiplayer()
        );
        when(commandGateway.send(expectedCommand)).thenReturn(Mono.just(GAME_IDENTIFIER));

        Mono<String> result = testRequester.route("register")
                                           .data(testDto)
                                           .retrieveMono(String.class);

        StepVerifier.create(result)
                    .expectNext(GAME_IDENTIFIER)
                    .verifyComplete();

        verify(commandGateway).send(expectedCommand);
    }

    @Test
    void testRent() {
        GameRentalRSocketController.RentDto testDto = new GameRentalRSocketController.RentDto(GAME_IDENTIFIER, RENTER);

        Mono<Void> result = testRequester.route("rent")
                                         .data(testDto)
                                         .retrieveMono(Void.class);

        StepVerifier.create(result)
                    .verifyComplete();

        verify(commandGateway).send(new RentGameCommand(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testReturn() {
        GameRentalRSocketController.RentDto testDto = new GameRentalRSocketController.RentDto(GAME_IDENTIFIER, RENTER);

        Mono<Void> result = testRequester.route("return")
                                         .data(testDto)
                                         .retrieveMono(Void.class);

        StepVerifier.create(result)
                    .verifyComplete();

        verify(commandGateway).send(new ReturnGameCommand(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testFind() {
        Game expectedGame = new Game(TITLE, RELEASE_DATE, DESCRIPTION, true, true);
        when(queryGateway.query(new FindGameQuery(GAME_IDENTIFIER), Game.class)).thenReturn(Mono.just(expectedGame));

        Mono<Game> result = testRequester.route("find")
                                         .data(GAME_IDENTIFIER)
                                         .retrieveMono(Game.class);

        StepVerifier.create(result)
                    .expectNext(expectedGame)
                    .verifyComplete();

        verify(queryGateway).query(new FindGameQuery(GAME_IDENTIFIER), Game.class);
    }

    @Test
    void testCatalog() {
        when(queryGateway.subscriptionQueryMany(any(FullGameCatalogQuery.class), eq(String.class)))
                .thenReturn(Flux.just(TITLE, OTHER_TITLE));

        Flux<String> result = testRequester.route("catalog")
                                           .retrieveFlux(String.class);

        StepVerifier.create(result)
                    .expectNext(TITLE)
                    .expectNext(OTHER_TITLE)
                    .verifyComplete();

        verify(queryGateway).subscriptionQueryMany(any(FullGameCatalogQuery.class), eq(String.class));
    }
}