package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.axonframework.test.matchers.Matchers.matches;

class GameTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = AxonTestFixture.with(EventSourcingConfigurer.create()
                                                              .registerEntity(EventSourcedEntityModule.autodetected(
                                                                      String.class,
                                                                      Game.class)));
    }

    @Test
    void testRegisterGameCommandAppliesGameRegisteredEvent() {
        fixture.given()
               .noPriorActivity()
               .when()
               .command(new RegisterGameCommand(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .then()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
    }

    @Test
    void testRentGameCommandAppliesGameRentedEvent() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .events(new GameRentedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testRentGameCommandThrowsExceptionForInsufficientStock() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                       new GameRentedEvent(GAME_IDENTIFIER, RENTER))
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exceptionSatisfies(throwable -> matches(
                                           e -> e instanceof RentalCommandException rce
                                                   && rce.getDetails().isPresent()
                                                   && rce.getDetails().get().equals(ExceptionStatusCode.INSUFFICIENT)
                                   ).matches(throwable)
               );
    }

    @Test
    void testRentGameCommandThrowsExceptionForToEarlyRenting() {
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, futureDate, DESCRIPTION, true, true))
               .when()
               .command(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exceptionSatisfies(throwable -> matches(
                                           e -> e instanceof RentalCommandException rce
                                                   && rce.getDetails().isPresent()
                                                   && rce.getDetails().get().equals(ExceptionStatusCode.UNRELEASED)
                                   ).matches(throwable)
               );
    }

    @Test
    void testReturnGameCommandAppliesGameReturnedEvent() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                       new GameRentedEvent(GAME_IDENTIFIER, RENTER))
               .when()
               .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .events(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testReturnGameCommandThrowsExceptionForReturnerNotMatchingOriginalRenter() {
        fixture.given()
               .events(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when()
               .command(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .then()
               .exceptionSatisfies(throwable -> matches(
                                           e -> e instanceof RentalCommandException rce
                                                   && rce.getDetails().isPresent()
                                                   && rce.getDetails().get()
                                                         .equals(ExceptionStatusCode.DIFFERENT_RETURNER)
                                   ).matches(throwable)
               );
    }

    @AfterEach
    void tearDown() {
        fixture.stop();
    }
}
