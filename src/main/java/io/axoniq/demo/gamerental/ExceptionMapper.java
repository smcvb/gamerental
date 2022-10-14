package io.axoniq.demo.gamerental;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.queryhandling.QueryExecutionException;

import java.util.Optional;

public abstract class ExceptionMapper {

    private ExceptionMapper() {
        // Utility class
    }

    public static Throwable mapRemoteException(Throwable exception) {
        if (exception instanceof CommandExecutionException) {
            Optional<Object> details = ((CommandExecutionException) exception).getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalCommandException(statusCode.getDescription(), null, statusCode);
            }
        } else if ((exception instanceof QueryExecutionException)) {
            Optional<Object> details = ((QueryExecutionException) exception).getDetails();
            if (details.isPresent()) {
                ExceptionStatusCode statusCode = (ExceptionStatusCode) details.get();
                return new RentalQueryException(statusCode.getDescription(), null, statusCode);
            }
        }
        return exception;
    }
}
