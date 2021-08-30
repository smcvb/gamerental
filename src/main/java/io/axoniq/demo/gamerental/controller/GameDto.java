package io.axoniq.demo.gamerental.ui;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Objects;

class GameDto {

    private final String title;
    private final Instant releaseDate;
    private final String description;
    private final boolean singleplayer;
    private final boolean multiplayer;

    @ConstructorProperties({"title", "releaseDate", "description", "singleplayer", "multiplayer"})
    public GameDto(String title,
                   Instant releaseDate,
                   String description,
                   boolean singleplayer,
                   boolean multiplayer) {
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
        GameDto gameDto = (GameDto) o;
        return singleplayer == gameDto.singleplayer && multiplayer == gameDto.multiplayer && Objects.equals(title,
                                                                                                            gameDto.title)
                && Objects.equals(releaseDate, gameDto.releaseDate) && Objects.equals(description,
                                                                                      gameDto.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, releaseDate, description, singleplayer, multiplayer);
    }

    @Override
    public String toString() {
        return "GameDto{" +
                "title='" + title + '\'' +
                ", releaseDate=" + releaseDate +
                ", description='" + description + '\'' +
                ", singleplayer=" + singleplayer +
                ", multiplayer=" + multiplayer +
                '}';
    }
}
