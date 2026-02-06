package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Profile("command")
@EventSourced
class Game {

    private String gameIdentifier;
    private int stock;
    private Instant releaseDate;
    private Set<String> renters;

    @CommandHandler
    public static void handle(RegisterGameCommand command, EventAppender appender) {
        appender.append(new GameRegisteredEvent(command.gameIdentifier(),
                                                command.title(),
                                                command.releaseDate(),
                                                command.description(),
                                                command.singleplayer(),
                                                command.multiplayer()));
    }

    @CommandHandler
    public void handle(RentGameCommand command, EventAppender appender) {
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
        appender.append(new GameRentedEvent(gameIdentifier, command.renter()));
    }

    @CommandHandler
    public void handle(ReturnGameCommand command, EventAppender appender) {
        if (!renters.contains(command.returner())) {
            throw new IllegalStateException("A game should be returned by someone who has actually rented it");
        }
        appender.append(new GameReturnedEvent(gameIdentifier, command.returner()));
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

    @EntityCreator
    public Game() {
        // Required by Axon
    }
}
