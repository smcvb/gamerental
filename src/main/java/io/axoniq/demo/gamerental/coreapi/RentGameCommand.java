package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

import java.beans.ConstructorProperties;
import java.util.Objects;

@Command(routingKey = "gameIdentifier")
public class RentGameCommand {

    @TargetEntityId
    private final String gameIdentifier;
    private final String renter;

    @ConstructorProperties({"gameIdentifier", "renter"})
    public RentGameCommand(String gameIdentifier, String renter) {
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
        RentGameCommand that = (RentGameCommand) o;
        return Objects.equals(gameIdentifier, that.gameIdentifier) &&
                Objects.equals(renter, that.renter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameIdentifier, renter);
    }

    @Override
    public String toString() {
        return "RentGameCommand{" +
                "gameIdentifier='" + gameIdentifier + '\'' +
                ", renter='" + renter + '\'' +
                '}';
    }
}
