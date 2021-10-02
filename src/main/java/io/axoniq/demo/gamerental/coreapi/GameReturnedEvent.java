package io.axoniq.demo.gamerental.coreapi;

import java.util.Objects;

public class GameReturnedEvent {

    private final String gameIdentifier;
    private final String returner;

    public GameReturnedEvent(String gameIdentifier, String returner) {
        this.gameIdentifier = gameIdentifier;
        this.returner = returner;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }

    public String getReturner() {
        return returner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameReturnedEvent that = (GameReturnedEvent) o;
        return Objects.equals(gameIdentifier, that.gameIdentifier) &&
                Objects.equals(returner, that.returner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier, returner);
    }

    @Override
    public String toString() {
        return "GameReturnedEvent{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", returner='" + returner + '\'' +
                '}';
    }
}
