package io.axoniq.demo.gamerental.query;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class GameCatalogProjectorTest {

    private static final int DEFAULT_STOCK = 1;
    private static final GameView QUERY_MODEL = new GameView(
            GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true
    );

    @SpyBean
    private GameViewRepository repository;
    private QueryUpdateEmitter updateEmitter;

    @Autowired
    private TestEntityManager entityManager;

    private GameCatalogProjector testSubject;

    @BeforeEach
    void setUp() {
        updateEmitter = mock(QueryUpdateEmitter.class);

        testSubject = new GameCatalogProjector(repository, updateEmitter);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(repository);
    }

    @Test
    void testGameRegisteredEventStoresGameViewAndEmitsGameTitle() {
        testSubject.on(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
        entityManager.flush();

        Optional<GameView> resultOptional = repository.findById(GAME_IDENTIFIER);
        assertTrue(resultOptional.isPresent());
        GameView resultModel = resultOptional.get();

        assertEquals(GAME_IDENTIFIER, resultModel.getGameIdentifier());
        assertEquals(TITLE, resultModel.getTitle());
        assertEquals(RELEASE_DATE, resultModel.getReleaseDate());
        assertEquals(DESCRIPTION, resultModel.getDescription());
        assertTrue(resultModel.isSingleplayer());
        assertTrue(resultModel.isMultiplayer());
        assertEquals(DEFAULT_STOCK, resultModel.getStock());

        verify(repository).save(QUERY_MODEL);

        ArgumentCaptor<String> updateCaptor = ArgumentCaptor.forClass(String.class);
        verify(updateEmitter).emit(eq(FullGameCatalogQuery.class), any(), updateCaptor.capture());
        assertEquals(TITLE, updateCaptor.getValue());
    }

    @Test
    void testGameRentedEventDecrementsStock() {
        entityManager.persist(new GameView(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));

        testSubject.on(new GameRentedEvent(GAME_IDENTIFIER, RENTER));
        entityManager.flush();

        Optional<GameView> resultOptional = repository.findById(GAME_IDENTIFIER);
        assertTrue(resultOptional.isPresent());
        GameView resultModel = resultOptional.get();

        assertEquals(0, resultModel.getStock());

        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testGameRentedEventThrowsExceptionForNonStoredGame() {
        assertThrows(
                IllegalArgumentException.class, () -> testSubject.on(new GameRentedEvent(GAME_IDENTIFIER, RENTER))
        );

        verify(repository).findById(GAME_IDENTIFIER);
        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testGameReturnedEventIncrementsStock() {
        entityManager.persist(new GameView(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));

        testSubject.on(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));
        entityManager.flush();

        Optional<GameView> resultOptional = repository.findById(GAME_IDENTIFIER);
        assertTrue(resultOptional.isPresent());
        GameView resultModel = resultOptional.get();

        assertEquals(2, resultModel.getStock());

        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testGameReturnedEventThrowsExceptionForNonStoredGame() {
        assertThrows(
                IllegalArgumentException.class, () -> testSubject.on(new GameReturnedEvent(GAME_IDENTIFIER, RENTER))
        );

        verify(repository).findById(GAME_IDENTIFIER);
        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testFindGameQueryReturnsGame() {
        entityManager.persist(new GameView(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
        entityManager.flush();

        Game resultResponse = testSubject.handle(new FindGameQuery(GAME_IDENTIFIER));

        assertEquals(TITLE, resultResponse.getTitle());
        assertEquals(RELEASE_DATE, resultResponse.getReleaseDate());
        assertEquals(DESCRIPTION, resultResponse.getDescription());
        assertTrue(resultResponse.isSingleplayer());
        assertTrue(resultResponse.isMultiplayer());

        verify(repository).findById(GAME_IDENTIFIER);
        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testFindGameQueryThrowsExceptionForNonStoredGame() {
        assertThrows(IllegalArgumentException.class, () -> testSubject.handle(new FindGameQuery(GAME_IDENTIFIER)));

        verify(repository).findById(GAME_IDENTIFIER);
        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testFullGameQueryReturnsAllGames() {
        entityManager.persist(new GameView(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
        entityManager.persist(new GameView(
                OTHER_GAME_IDENTIFIER, OTHER_TITLE, OTHER_RELEASE_DATE, OTHER_DESCRIPTION, true, false
        ));
        entityManager.flush();

        List<String> resultResponse = testSubject.handle(new FullGameCatalogQuery());

        assertTrue(resultResponse.contains(TITLE));
        assertTrue(resultResponse.contains(OTHER_TITLE));

        verify(repository).findAll();
        verifyNoInteractions(updateEmitter);
    }

    @Test
    void testHandleIllegalArgumentExceptionMapsToGameNotFoundRentalQueryException() {
        RentalQueryException result = assertThrows(
                RentalQueryException.class, () -> testSubject.handle(new IllegalArgumentException("could not be found"))
        );

        assertTrue(result.getDetails().isPresent());
        assertEquals(ExceptionStatusCode.GAME_NOT_FOUND, result.getDetails().get());
    }

    @Test
    void testHandleIllegalArgumentExceptionMapsToUnknownExceptionRentalQueryException() {
        RentalQueryException result = assertThrows(
                RentalQueryException.class, () -> testSubject.handle(new IllegalArgumentException("some unknown exp"))
        );

        assertTrue(result.getDetails().isPresent());
        assertEquals(ExceptionStatusCode.UNKNOWN_EXCEPTION, result.getDetails().get());
    }
}