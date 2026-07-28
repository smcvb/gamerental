package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.core.interception.annotation.ExceptionHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Profile("command")
@EventSourced(tagKey = "Game", idType = String.class)
class Game {

    private String gameIdentifier;
    private int stock;
    private Instant releaseDate;
    private Set<String> renters;

    @CommandHandler
    public static void handle(RegisterGameCommand command, EventAppender eventAppender) {
        eventAppender.append(new GameRegisteredEvent(command.getGameIdentifier(),
                                                     command.getTitle(),
                                                     command.getReleaseDate(),
                                                     command.getDescription(),
                                                     command.isSingleplayer(),
                                                     command.isMultiplayer()));
    }

    @CommandHandler
    public void handle(RentGameCommand command, EventAppender eventAppender) {
        if (stock <= 0) {
            throw new IllegalStateException(
                    "Insufficient items in stock for game with identifier [" + gameIdentifier + "]"
            );
        }
        if (Instant.now().isBefore(releaseDate)) {
            throw new IllegalStateException(
                    "Game with identifier [" + gameIdentifier + "] cannot be rented out as it has not been released yet"
            );
        }
        eventAppender.append(new GameRentedEvent(gameIdentifier, command.getRenter()));
    }

    @CommandHandler
    public void handle(ReturnGameCommand command, EventAppender eventAppender) {
        if (!renters.contains(command.getReturner())) {
            throw new IllegalStateException("A game should be returned by someone who has actually rented it");
        }
        eventAppender.append(new GameReturnedEvent(gameIdentifier, command.getReturner()));
    }

    @EventSourcingHandler
    public void on(GameRegisteredEvent event) {
        this.gameIdentifier = event.getGameIdentifier();
        this.stock = 1;
        this.releaseDate = event.getReleaseDate();
        this.renters = new HashSet<>();
    }

    @EventSourcingHandler
    public void on(GameRentedEvent event) {
        this.stock--;
        this.renters.add(event.getRenter());
    }

    @EventSourcingHandler
    public void on(GameReturnedEvent event) {
        this.stock++;
        this.renters.remove(event.getReturner());
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

    @EntityCreator
    public Game() {
        // Required by Axon
    }
}
