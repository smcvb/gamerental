package io.axoniq.demo.gamerental.controller;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.mockito.Mockito.*;

class GameRentalRestControllerTest {

    private CommandGateway commandGateway;
    private QueryGateway queryGateway;

    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        commandGateway = mock(CommandGateway.class);
        queryGateway = mock(QueryGateway.class);

        testClient = WebTestClient.bindToController(new GameRentalRestController(commandGateway, queryGateway)).build();
    }

    @Test
    void testRegisterGame() {
        GameDto testDto =
                new GameDto(TITLE, RELEASE_DATE, DESCRIPTION, true, true);

        RegisterGameCommand expectedCommand = new RegisterGameCommand(
                GAME_IDENTIFIER, testDto.getTitle(), testDto.getReleaseDate(), testDto.getDescription(),
                testDto.isSingleplayer(), testDto.isMultiplayer()
        );
        when(commandGateway.send(expectedCommand, String.class))
                .thenReturn(CompletableFuture.completedFuture(GAME_IDENTIFIER));

        testClient.post()
                  .uri(uriBuilder -> uriBuilder.path("/rental/register/{identifier}")
                                               .build(GAME_IDENTIFIER))
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(testDto)
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(String.class).isEqualTo(GAME_IDENTIFIER);

        verify(commandGateway).send(expectedCommand, String.class);
    }

    @Test
    void testRentGame() {
        testClient.post()
                  .uri(uriBuilder -> uriBuilder.path("/rental/rent/{identifier}")
                                               .queryParam("renter", RENTER)
                                               .build(GAME_IDENTIFIER))
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody().isEmpty();

        verify(commandGateway).send(new RentGameCommand(GAME_IDENTIFIER, RENTER), Void.class);
    }

    @Test
    void testReturnGame() {
        testClient.post()
                  .uri(uriBuilder -> uriBuilder.path("/rental/return/{identifier}")
                                               .queryParam("returner", RENTER)
                                               .build(GAME_IDENTIFIER))
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody().isEmpty();

        verify(commandGateway).send(new ReturnGameCommand(GAME_IDENTIFIER, RENTER), Void.class);
    }

    @Test
    void testFindGame() {
        Game expectedGame = new Game(TITLE, RELEASE_DATE, DESCRIPTION, true, true);
        when(queryGateway.query(new FindGameQuery(GAME_IDENTIFIER), Game.class))
                .thenReturn(CompletableFuture.completedFuture(expectedGame));

        testClient.get()
                  .uri(uriBuilder -> uriBuilder.path("/rental/{identifier}")
                                               .build(GAME_IDENTIFIER))
                  .exchange()
                  .expectStatus().isOk()
                  .expectHeader().contentType(MediaType.APPLICATION_JSON)
                  .expectBody(Game.class).isEqualTo(expectedGame);

        verify(queryGateway).query(new FindGameQuery(GAME_IDENTIFIER), Game.class);
    }

    @Test
    void testFindGameCatalog() {
        List<String> expectedTitles = new ArrayList<>();
        expectedTitles.add(TITLE);
        expectedTitles.add(OTHER_TITLE);
        when(queryGateway.queryMany(any(FullGameCatalogQuery.class), eq(String.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedTitles));

        testClient.get()
                  .uri(uriBuilder -> uriBuilder.path("/rental/catalog")
                                               .build())
                  .exchange()
                  .expectStatus().isOk()
                  .expectHeader().contentType(MediaType.APPLICATION_JSON)
                  .expectBody(List.class).isEqualTo(expectedTitles);

        verify(queryGateway).queryMany(any(FullGameCatalogQuery.class), eq(String.class));
    }

    @Test
    void testWatchGameCatalog() {
        String expectedMediaType = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8";

        when(queryGateway.subscriptionQuery(any(FullGameCatalogQuery.class), eq(String.class)))
                .thenReturn(Flux.just(TITLE, OTHER_TITLE));

        Flux<String> result = testClient.get()
                                        .uri(uriBuilder -> uriBuilder.path("/rental/catalog/watch")
                                                                     .build())
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectHeader().contentType(expectedMediaType)
                                        .returnResult(String.class).getResponseBody();

        StepVerifier.create(result)
                    .expectNext(TITLE)
                    .expectNext(OTHER_TITLE)
                    .expectComplete()
                    .verify();

        verify(queryGateway).subscriptionQuery(any(FullGameCatalogQuery.class), eq(String.class));
    }
}