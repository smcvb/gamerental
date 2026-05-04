package io.axoniq.demo.gamerental.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@EventSourcedEntity(tagKey = "gameId")
public class Game {

    private final String gameIdentifier;
    private final int stock;
    private final Instant releaseDate;
    private final Set<String> renters;

    @EntityCreator
    public Game(GameRegisteredEvent event) {
        this.gameIdentifier = event.gameIdentifier();
        this.releaseDate = event.releaseDate();
        this.stock = 1;
        this.renters = new HashSet<>();
    }

    @JsonCreator
    private Game(@JsonProperty("gameIdentifier") String gameIdentifier,
                 @JsonProperty("stock") int stock,
                 @JsonProperty("releaseDate") Instant releaseDate,
                 @JsonProperty("renters") Set<String> renters) {
        this.gameIdentifier = gameIdentifier;
        this.stock = stock;
        this.releaseDate = releaseDate;
        this.renters = renters;
    }

    @CommandHandler(commandName = "game-rental.register")
    public static void handle(RegisterGameCommand command, EventAppender appender) {
        appender.append(new GameRegisteredEvent(command.gameIdentifier(),
                                                command.title(),
                                                command.releaseDate(),
                                                command.description(),
                                                command.singleplayer(),
                                                command.multiplayer()));
    }

    @CommandHandler(commandName = "game-rental.rent")
    public void handle(RentGameCommand command, EventAppender appender) {
        if (stock <= 0) {
            throw new RentalCommandException(ExceptionStatusCode.INSUFFICIENT.getDescription(),
                                             null,
                                             ExceptionStatusCode.INSUFFICIENT);
        }
        if (Instant.now().isBefore(releaseDate)) {
            throw new RentalCommandException(ExceptionStatusCode.UNRELEASED.getDescription(),
                                             null,
                                             ExceptionStatusCode.UNRELEASED);
        }
        appender.append(new GameRentedEvent(gameIdentifier, command.renter()));
    }

    @CommandHandler(commandName = "game-rental.return")
    public void handle(ReturnGameCommand command, EventAppender appender) {
        if (!renters.contains(command.returner())) {
            throw new RentalCommandException(ExceptionStatusCode.DIFFERENT_RETURNER.getDescription(),
                                             null,
                                             ExceptionStatusCode.DIFFERENT_RETURNER);
        }
        appender.append(new GameReturnedEvent(gameIdentifier, command.returner()));
    }

    @EventSourcingHandler
    public Game on(GameRentedEvent event) {
        Set<String> newRenters = new HashSet<>(renters);
        newRenters.add(event.renter());
        return new Game(this.gameIdentifier, this.stock - 1, this.releaseDate, newRenters);
    }

    @EventSourcingHandler
    public Game on(GameReturnedEvent event) {
        Set<String> newRenters = new HashSet<>(renters);
        newRenters.remove(event.returner());
        return new Game(this.gameIdentifier, this.stock + 1, this.releaseDate, newRenters);
    }

    @JsonGetter
    public String gameIdentifier() {
        return gameIdentifier;
    }

    @JsonGetter
    public int stock() {
        return stock;
    }

    @JsonGetter
    public Instant releaseDate() {
        return releaseDate;
    }

    @JsonGetter
    public Set<String> renters() {
        return renters;
    }
}
