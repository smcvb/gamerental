package io.axoniq.demo.gamerental.ui;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.mockito.Mockito.*;

class GameRentalControllerTest {

    private ReactorCommandGateway commandGateway;
    private ReactorQueryGateway queryGateway;

    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        commandGateway = mock(ReactorCommandGateway.class);
        queryGateway = mock(ReactorQueryGateway.class);

        testClient = WebTestClient.bindToController(new GameRentalController(commandGateway, queryGateway)).build();
    }

    @Test
    void testRegisterGame() {
        GameDto testDto =
                new GameDto(TITLE, RELEASE_DATE, DESCRIPTION, true, true);

        RegisterGameCommand expectedCommand = new RegisterGameCommand(
                GAME_IDENTIFIER, testDto.getTitle(), testDto.getReleaseDate(), testDto.getDescription(),
                testDto.isSingleplayer(), testDto.isMultiplayer()
        );
        when(commandGateway.send(expectedCommand)).thenReturn(Mono.just(GAME_IDENTIFIER));

        testClient.post()
                  .uri(uriBuilder -> uriBuilder.path("/rental/register/{identifier}")
                                               .build(GAME_IDENTIFIER))
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(testDto)
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(String.class).isEqualTo(GAME_IDENTIFIER);

        verify(commandGateway).send(expectedCommand);
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

        verify(commandGateway).send(new RentGameCommand(GAME_IDENTIFIER, RENTER));
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

        verify(commandGateway).send(new ReturnGameCommand(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testFindGame() {
        Game expectedGame = new Game(TITLE, RELEASE_DATE, DESCRIPTION, true, true);
        when(queryGateway.query(new FindGameQuery(GAME_IDENTIFIER), Game.class)).thenReturn(Mono.just(expectedGame));

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
        //noinspection unchecked
        when(queryGateway.query(any(FullGameCatalogQuery.class), any(MultipleInstancesResponseType.class)))
                .thenReturn(Mono.just(expectedTitles));

        testClient.get()
                  .uri(uriBuilder -> uriBuilder.path("/rental/catalog")
                                               .build())
                  .exchange()
                  .expectStatus().isOk()
                  .expectHeader().contentType(MediaType.APPLICATION_JSON)
                  .expectBody(List.class).isEqualTo(expectedTitles);

        //noinspection unchecked
        verify(queryGateway).query(any(FullGameCatalogQuery.class), any(MultipleInstancesResponseType.class));
    }

    @Test
    void testWatchGameCatalog() {
        String expectedMediaType = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8";

        when(queryGateway.subscriptionQueryMany(any(FullGameCatalogQuery.class), eq(String.class)))
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

        verify(queryGateway).subscriptionQueryMany(any(FullGameCatalogQuery.class), eq(String.class));
    }
}