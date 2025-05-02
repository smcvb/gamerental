package io.axoniq.demo.gamerental.query;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.queryhandling.annotation.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile("query")
@Component
//@ProcessingGroup("game-catalog")
class GameCatalogProjector {

    private final GameViewRepository repository;
    private final QueryUpdateEmitter updateEmitter;

    public GameCatalogProjector(GameViewRepository repository, QueryUpdateEmitter updateEmitter) {
        this.repository = repository;
        this.updateEmitter = updateEmitter;
    }

    @EventHandler
    public void on(GameRegisteredEvent event) {
        String title = event.title();

        repository.save(new GameView(event.gameIdentifier(),
                                     title,
                                     event.releaseDate(),
                                     event.description(),
                                     event.singleplayer(),
                                     event.multiplayer()));

        updateEmitter.emit(FullGameCatalogQuery.class, query -> true, title);
    }

    @EventHandler
    public void on(GameRentedEvent event) {
        Optional<GameView> result = repository.findById(event.gameIdentifier());
        if (result.isPresent()) {
            result.get().decrementStock();
        } else {
            throw new IllegalArgumentException("Game with id [" + event.gameIdentifier() + "] could not be found.");
        }
    }

    @EventHandler
    public void on(GameReturnedEvent event) {
        Optional<GameView> result = repository.findById(event.gameIdentifier());
        if (result.isPresent()) {
            result.get().incrementStock();
        } else {
            throw new IllegalArgumentException("Game with id [" + event.gameIdentifier() + "] could not be found.");
        }
    }

    @QueryHandler
    public Game handle(FindGameQuery query) {
        String gameIdentifier = query.getGameIdentifier();
        return repository.findById(gameIdentifier)
                         .map(gameView -> new Game(
                                 gameView.getTitle(),
                                 gameView.getReleaseDate(),
                                 gameView.getDescription(),
                                 gameView.isSingleplayer(),
                                 gameView.isMultiplayer()
                         ))
                         .orElseThrow(() -> new IllegalArgumentException(
                                 "Game with id [" + gameIdentifier + "] could not be found."
                         ));
    }

    @QueryHandler
    public List<String> handle(FullGameCatalogQuery query) {
        return repository.findAll().stream()
                         .map(GameView::getTitle)
                         .collect(Collectors.toList());
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception) {
        ExceptionStatusCode statusCode;
        if (exception.getMessage().contains("could not be found")) {
            statusCode = ExceptionStatusCode.GAME_NOT_FOUND;
        } else {
            statusCode = ExceptionStatusCode.UNKNOWN_EXCEPTION;
        }
        throw new RentalQueryException(exception.getMessage(), exception, statusCode);
    }
}
