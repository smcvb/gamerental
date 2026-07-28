package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.messaging.commandhandling.CommandExecutionException;

public class RentalCommandException extends CommandExecutionException {

    public RentalCommandException(String message, Throwable cause, Object details) {
        super(message, cause, details);
    }
}
