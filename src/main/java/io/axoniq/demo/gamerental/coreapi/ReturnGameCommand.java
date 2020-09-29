package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class ReturnGameCommand {

    @TargetAggregateIdentifier
    private final String gameIdentifier;
    private final String returner;

    @ConstructorProperties({"gameIdentifier", "returner"})
    public ReturnGameCommand(String gameIdentifier, String returner) {
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
        ReturnGameCommand that = (ReturnGameCommand) o;
        return Objects.equals(gameIdentifier, that.gameIdentifier) &&
                Objects.equals(returner, that.returner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier, returner);
    }

    @Override
    public String toString() {
        return "ReturnGameCommand{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", returner='" + returner + '\'' +
                '}';
    }
}
