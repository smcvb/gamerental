package io.axoniq.demo.gamerental.command;

import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RegisterGameCommand;
import io.axoniq.demo.gamerental.coreapi.RentGameCommand;
import io.axoniq.demo.gamerental.coreapi.ReturnGameCommand;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.annotation.InjectEntity;

//@Component
class GameCommandHandler {

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
                       @InjectEntity Game game,
                       EventAppender appender) {
        game.hasInsufficientStock();
        game.hasNotBeenReleasedYet();
        appender.append(new GameRentedEvent(game.gameIdentifier(), command.renter()));
    }

    @CommandHandler
    public void handle(ReturnGameCommand command,
                       @InjectEntity Game game,
                       EventAppender appender) {
        game.notReturnedByOriginalRenter(command.returner());
        appender.append(new GameReturnedEvent(game.gameIdentifier(), command.returner()));
    }
}
