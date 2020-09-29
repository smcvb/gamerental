package io.axoniq.demo.gamerental.coreapi;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class Game {

    private final String title;
    private final String description;
    private final boolean singleplayer;
    private final boolean multiplayer;

    @ConstructorProperties({"title", "description", "singleplayer", "multiplayer"})
    public Game(String title, String description, boolean singleplayer, boolean multiplayer) {
        this.title = title;
        this.description = description;
        this.singleplayer = singleplayer;
        this.multiplayer = multiplayer;
    }

    public String getTitle() {
        return title;
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
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, singleplayer, multiplayer);
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", singleplayer=" + singleplayer +
                ", multiplayer=" + multiplayer +
                '}';
    }
}
