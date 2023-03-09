package io.axoniq.demo.gamerental;

import io.axoniq.demo.gamerental.coreapi.ExceptionStatusCode;
import io.axoniq.demo.gamerental.coreapi.RentalCommandException;
import io.axoniq.demo.gamerental.coreapi.RentalQueryException;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.queryhandling.QueryExecutionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMapperTest {

    @Test
    void testMapRemoteExceptionReturnsUnknownExceptionsAsIs() {
        Throwable expected = new IllegalArgumentException("some-exception");

        Throwable result = ExceptionMapper.mapRemoteException(expected);

        assertEquals(expected, result);
    }

    @Test
    void testMapRemoteExceptionMapsCommandExecutionExceptionToRentalCommandException() {
        ExceptionStatusCode expectedDetails = ExceptionStatusCode.INSUFFICIENT;

        CommandExecutionException testException = new CommandExecutionException("some-message", null, expectedDetails);

        Throwable result = ExceptionMapper.mapRemoteException(testException);

        assertTrue(result instanceof RentalCommandException);
        assertEquals(expectedDetails.getDescription(), result.getMessage());
        assertTrue(((RentalCommandException) result).getDetails().isPresent());
        assertEquals(expectedDetails, ((RentalCommandException) result).getDetails().get());
    }

    @Test
    void testMapRemoteExceptionMapsQueryExecutionExceptionToRentalQueryException() {
        ExceptionStatusCode expectedDetails = ExceptionStatusCode.GAME_NOT_FOUND;

        QueryExecutionException testException = new QueryExecutionException("some-message", null, expectedDetails);

        Throwable result = ExceptionMapper.mapRemoteException(testException);

        assertTrue(result instanceof RentalQueryException);
        assertEquals(expectedDetails.getDescription(), result.getMessage());
        assertTrue(((RentalQueryException) result).getDetails().isPresent());
        assertEquals(expectedDetails, ((RentalQueryException) result).getDetails().get());
    }
}