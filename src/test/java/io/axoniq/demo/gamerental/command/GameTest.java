package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.ApplicationConfig;
import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.axoniq.demo.gamerental.TestUtils.*;

class GameTest {

    private AxonTestFixture axonFixture;

    @BeforeEach
    void setUp() {
        axonFixture = AxonTestFixture.with(ApplicationConfig.axonConfigurer(GameConfiguration.gameModule()));
    }

    @Test
    void testRegisterGameCommandAppliesGameRegisteredEvent() {
        axonFixture.given()
                   .noPriorActivity()
                   .when()
                   .command(new RegisterGameCommand(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
                   .then()
                   .events(new GameRegisteredEvent(GAME_IDENTIFIER,
                                                   TITLE,
                                                   RELEASE_DATE,
                                                   DESCRIPTION,
                                                   true,
                                                   true));
    }

    @Test
    void testRentGameCommandAppliesGameRentedEvent() {
        axonFixture.given()
                   .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
                   .when()
                   .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
                   .then()
                   .events(new GameRentedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testRentGameCommandThrowsExceptionForInsufficientStock() {
        axonFixture.given()
                   .events(
                           new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                           new GameRentedEvent(GAME_IDENTIFIER, RENTER)
                   )
                   .when()
                   .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
                   .then()
                   .exception(RentalCommandException.class, ExceptionStatusCode.INSUFFICIENT.getDescription());
    }

    @Test
    void testRentGameCommandThrowsExceptionForToEarlyRenting() {
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        axonFixture.given()
                   .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, futureDate, DESCRIPTION, true, true))
                   .when()
                   .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
                   .then()
                   .exception(RentalCommandException.class, ExceptionStatusCode.UNRELEASED.getDescription());
    }

    @Test
    void testReturnGameCommandAppliesGameReturnedEvent() {
        axonFixture.given()
                   .events(
                           new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                           new GameRentedEvent(GAME_IDENTIFIER, RENTER)
                   )
                   .when()
                   .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
                   .then()
                   .events(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testReturnGameCommandThrowsExceptionForReturnerNotMatchingOriginalRenter() {
        axonFixture.given()
                   .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
                   .when()
                   .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
                   .then()
                   .exception(RentalCommandException.class, ExceptionStatusCode.DIFFERENT_RETURNER.getDescription());
    }
}
