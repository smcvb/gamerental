package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.interceptors.ExceptionHandler;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@EventSourcedEntity(tagKey = "gameId")
class Game {

    private String gameIdentifier;
    private int stock;
    private Instant releaseDate;
    private Set<String> renters;

    @EntityCreator
    public Game() {
        // Required by Axon
    }

    @CommandHandler
    public void handle(RegisterGameCommand command,
                       EventAppender appender) {
        appender.append(new GameRegisteredEvent(command.gameIdentifier(),
                                                command.title(),
                                                command.releaseDate(),
                                                command.description(),
                                                command.singleplayer(),
                                                command.multiplayer()));
    }

    @CommandHandler
    public void handle(RentGameCommand command,
                       EventAppender appender) {
        hasInsufficientStock();
        hasNotBeenReleasedYet();
        appender.append(new GameRentedEvent(gameIdentifier, command.renter()));
    }

    private void hasInsufficientStock() {
        if (stock <= 0) {
            throw new RentalCommandException(
                    ExceptionStatusCode.INSUFFICIENT.getDescription(),
                    null,
                    ExceptionStatusCode.INSUFFICIENT
            );
        }
    }

    private void hasNotBeenReleasedYet() {
        if (Instant.now().isBefore(releaseDate)) {
            throw new RentalCommandException(
                    ExceptionStatusCode.UNRELEASED.getDescription(),
                    null,
                    ExceptionStatusCode.UNRELEASED
            );
        }
    }

    @CommandHandler
    public void handle(ReturnGameCommand command,
                       EventAppender appender) {
        notReturnedByOriginalRenter(command.returner());
        appender.append(new GameReturnedEvent(gameIdentifier, command.returner()));
    }

    private void notReturnedByOriginalRenter(String returner) {
        if (!renters.contains(returner)) {
            throw new RentalCommandException(
                    ExceptionStatusCode.DIFFERENT_RETURNER.getDescription(),
                    null,
                    ExceptionStatusCode.DIFFERENT_RETURNER
            );
        }
    }

    @EventSourcingHandler
    public void on(GameRegisteredEvent event) {
        this.gameIdentifier = event.gameIdentifier();
        this.stock = 1;
        this.releaseDate = event.releaseDate();
        this.renters = new HashSet<>();
    }

    @EventSourcingHandler
    public void on(GameRentedEvent event) {
        this.stock--;
        this.renters.add(event.renter());
    }

    @EventSourcingHandler
    public void on(GameReturnedEvent event) {
        this.stock++;
        this.renters.remove(event.returner());
    }

    @ExceptionHandler(resultType = IllegalStateException.class)
    public void handle(IllegalStateException exception) {
        ExceptionStatusCode statusCode;
        if (exception.getMessage().contains("Insufficient")) {
            statusCode = ExceptionStatusCode.INSUFFICIENT;
        } else if (exception.getMessage().contains("not been released")) {
            statusCode = ExceptionStatusCode.UNRELEASED;
        } else if (exception.getMessage().contains("actually rented it")) {
            statusCode = ExceptionStatusCode.DIFFERENT_RETURNER;
        } else {
            statusCode = ExceptionStatusCode.UNKNOWN_EXCEPTION;
        }
        throw new RentalCommandException(exception.getMessage(), exception, statusCode);
    }
}
