package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.axoniq.demo.gamerental.TestUtils.*;
import static org.axonframework.test.matchers.Matchers.matches;

class GameTest {

    private FixtureConfiguration<Game> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Game.class);
    }

    @Test
    void testRegisterGameCommandAppliesGameRegisteredEvent() {
        fixture.givenNoPriorActivity()
               .when(new RegisterGameCommand(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .expectEvents(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true));
    }

    @Test
    void testRentGameCommandAppliesGameRentedEvent() {
        fixture.given(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .expectEvents(new GameRentedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testRentGameCommandThrowsExceptionForInsufficientStock() {
        fixture.given(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                      new GameRentedEvent(GAME_IDENTIFIER, RENTER))
               .when(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .expectException(matches(
                       e -> e instanceof RentalCommandException
                               && ((RentalCommandException) e).getDetails().isPresent()
                               && ((RentalCommandException) e).getDetails().get()
                                                              .equals(ExceptionStatusCode.INSUFFICIENT)
               ));
    }

    @Test
    void testRentGameCommandThrowsExceptionForToEarlyRenting() {
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        fixture.given(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, futureDate, DESCRIPTION, true, true))
               .when(new RentGameCommand(GAME_IDENTIFIER, RENTER))
               .expectException(matches(
                       e -> e instanceof RentalCommandException
                               && ((RentalCommandException) e).getDetails().isPresent()
                               && ((RentalCommandException) e).getDetails().get().equals(ExceptionStatusCode.UNRELEASED)
               ));
    }

    @Test
    void testReturnGameCommandAppliesGameReturnedEvent() {
        fixture.given(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true),
                      new GameRentedEvent(GAME_IDENTIFIER, RENTER))
               .when(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .expectEvents(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));
    }

    @Test
    void testReturnGameCommandThrowsExceptionForReturnerNotMatchingOriginalRenter() {
        fixture.given(new GameRegisteredEvent(GAME_IDENTIFIER, TITLE, RELEASE_DATE, DESCRIPTION, true, true))
               .when(new ReturnGameCommand(GAME_IDENTIFIER, RENTER))
               .expectException(matches(
                       e -> e instanceof RentalCommandException
                               && ((RentalCommandException) e).getDetails().isPresent()
                               && ((RentalCommandException) e).getDetails().get()
                                                              .equals(ExceptionStatusCode.DIFFERENT_RETURNER)
               ));
    }
}
