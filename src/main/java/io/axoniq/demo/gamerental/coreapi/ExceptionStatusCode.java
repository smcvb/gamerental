package io.axoniq.demo.gamerental.coreapi;

public enum ExceptionStatusCode {

    INSUFFICIENT("Insufficient items of this game are in stock, please select something else."),
    UNRELEASED("Unfortunately, this game has not been released yet. As such, it cannot be rented out just yet"),
    DIFFERENT_RETURNER("An original renter of the game should return this game."),
    GAME_NOT_FOUND("The game being requested cannot be found in our catalog. Please search for something else."),
    UNKNOWN_EXCEPTION("Something went wrong within the application. Please inform a representative of the Game Rental Application");

    private final String description;

    ExceptionStatusCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
