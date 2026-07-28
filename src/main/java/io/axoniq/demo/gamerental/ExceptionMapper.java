package io.axoniq.demo.gamerental;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.queryhandling.QueryExecutionException;

import java.util.Optional;

public abstract class ExceptionMapper {

    private ExceptionMapper() {
        // Utility class
    }

    public static Throwable mapRemoteException(Throwable exception) {
        if (exception instanceof CommandExecutionException executionException1) {
            Optional<Object> details = executionException1.getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalCommandException(statusCode.getDescription(), null, statusCode);
            }
        } else if ((exception instanceof QueryExecutionException executionException)) {
            Optional<Object> details = executionException.getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalQueryException(statusCode.getDescription(), null, statusCode);
            }
        }
        return exception;
    }
}
