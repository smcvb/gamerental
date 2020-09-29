package io.axoniq.demo.gamerental.coreapi;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class FindGameQuery {

    private final String gameIdentifier;

    @ConstructorProperties({"gameIdentifier"})
    public FindGameQuery(String gameIdentifier) {
        this.gameIdentifier = gameIdentifier;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FindGameQuery that = (FindGameQuery) o;
        return Objects.equals(gameIdentifier, that.gameIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier);
    }

    @Override
    public String toString() {
        return "FindGameQuery{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                '}';
    }
}
