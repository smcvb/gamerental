package io.axoniq.demo.gamerental.query;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
class GameView {

    @Id
    private String gameIdentifier;
    private String title;
    private Instant releaseDate;
    private String description;
    private boolean singleplayer;
    private boolean multiplayer;
    private int stock;

    public GameView(String gameIdentifier,
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
        this.stock = 1;
    }

    public GameView() {
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

    public int getStock() {
        return stock;
    }

    public void incrementStock() {
        this.stock++;
    }

    public void decrementStock() {
        this.stock--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameView gameView = (GameView) o;
        return singleplayer == gameView.singleplayer &&
                multiplayer == gameView.multiplayer &&
                stock == gameView.stock &&
                Objects.equals(gameIdentifier, gameView.gameIdentifier) &&
                Objects.equals(title, gameView.title) &&
                Objects.equals(releaseDate, gameView.releaseDate) &&
                Objects.equals(description, gameView.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier,
                            title,
                            releaseDate,
                            description,
                            singleplayer,
                            multiplayer,
                            stock);
    }

    @Override
    public String toString() {
        return "GameView{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", title='" + title + '\'' +
                ", releaseDate=" + releaseDate +
                ", description='" + description + '\'' +
                ", singleplayer=" + singleplayer +
                ", multiplayer=" + multiplayer +
                ", stock=" + stock +
                '}';
    }
}