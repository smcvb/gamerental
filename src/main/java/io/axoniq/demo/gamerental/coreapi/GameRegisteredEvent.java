package io.axoniq.demo.gamerental.coreapi;

import java.time.Instant;
import java.util.Objects;

public class GameRegisteredEvent {

    private final String gameIdentifier;
    private final String title;
    private final Instant releaseDate;
    private final String description;
    private final boolean singleplayer;
    private final boolean multiplayer;

    public GameRegisteredEvent(String gameIdentifier,
                               String title,
                               Instant releaseDate,
                               String description,
                               boolean singleplayer,
                               boolean multiplayer) {
        this.gameIdentifier = gameIdentifier;
        this.title = title;
        this.releaseDate = releaseDate;
        this.description = description;
        this.singleplayer = singleplayer;
        this.multiplayer = multiplayer;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
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
        GameRegisteredEvent that = (GameRegisteredEvent) o;
        return singleplayer == that.singleplayer &&
                multiplayer == that.multiplayer &&
                Objects.equals(gameIdentifier, that.gameIdentifier) &&
                Objects.equals(title, that.title) &&
                Objects.equals(releaseDate, that.releaseDate) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier, title, releaseDate, description, singleplayer, multiplayer);
    }

    @Override
    public String toString() {
        return "GameRegisteredEvent{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", title='" + title + '\'' +
                ", releaseDate=" + releaseDate +
                ", description='" + description + '\'' +
                ", singleplayer=" + singleplayer +
                ", multiplayer=" + multiplayer +
                '}';
    }
}
