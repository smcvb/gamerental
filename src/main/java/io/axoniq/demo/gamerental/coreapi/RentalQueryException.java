package io.axoniq.demo.gamerental.coreapi;

import org.axonframework.queryhandling.QueryExecutionException;

public class RentalQueryException extends QueryExecutionException {

    public RentalQueryException(String message, Throwable cause, Object details) {
        super(message, cause, details);
    }
}
