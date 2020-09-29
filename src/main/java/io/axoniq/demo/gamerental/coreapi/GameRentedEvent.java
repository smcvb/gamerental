package io.axoniq.demo.gamerental.coreapi;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class GameRentedEvent {

    private final String gameIdentifier;
    private final String renter;

    @ConstructorProperties({"gameIdentifier", "renter"})
    public GameRentedEvent(String gameIdentifier, String renter) {
        this.gameIdentifier = gameIdentifier;
        this.renter = renter;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }

    public String getRenter() {
        return renter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameRentedEvent that = (GameRentedEvent) o;
        return Objects.equals(gameIdentifier, that.gameIdentifier) &&
                Objects.equals(renter, that.renter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier, renter);
    }

    @Override
    public String toString() {
        return "GameRentedEvent{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", renter='" + renter + '\'' +
                '}';
    }
}
