package io.axoniq.demo.gamerental.query;

import io.axoniq.demo.gamerental.coreapi.FindGameQuery;
import io.axoniq.demo.gamerental.coreapi.FullGameCatalogQuery;
import io.axoniq.demo.gamerental.coreapi.Game;
import io.axoniq.demo.gamerental.coreapi.GameRegisteredEvent;
import io.axoniq.demo.gamerental.coreapi.GameRentedEvent;
import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.QueryUpdateEmitter;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile("query")
@Component
class GameCatalogProjector {

    private final GameViewRepository repository;

    public GameCatalogProjector(GameViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(GameRegisteredEvent event, QueryUpdateEmitter updateEmitter) {
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
        String gameIdentifier = query.gameIdentifier();
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
}
