package io.axoniq.demo.gamerental.coreapi;

import java.time.Instant;
import java.util.Objects;

public class Game {

    private final String title;
    private final Instant releaseDate;
    private final String description;
    private final boolean singleplayer;
    private final boolean multiplayer;

    public Game(String title, Instant releaseDate, String description, boolean singleplayer, boolean multiplayer) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.description = description;
        this.singleplayer = singleplayer;
        this.multiplayer = multiplayer;
    }

    public String getTitle() {
        return title;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSingleplayer() {
        return singleplayer;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return singleplayer == game.singleplayer &&
                multiplayer == game.multiplayer &&
                Objects.equals(title, game.title) &&
                Objects.equals(releaseDate, game.releaseDate) &&
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, releaseDate, description, singleplayer, multiplayer);
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", releaseDate=" + releaseDate +
                ", description='" + description + '\'' +
                ", singleplayer=" + singleplayer +
                ", multiplayer=" + multiplayer +
                '}';
    }
}
