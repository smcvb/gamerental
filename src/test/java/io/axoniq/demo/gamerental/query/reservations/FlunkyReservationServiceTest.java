package io.axoniq.demo.gamerental.query.reservations;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.axoniq.demo.gamerental.TestUtils.GAME_IDENTIFIER;
import static org.junit.jupiter.api.Assertions.*;

class FlunkyReservationServiceTest {

    @Test
    void notifyingFailsPeriodicallyRegardlessOfGivenIdentifier() {
        ReservationService testSubject = new FlunkyReservationService();

        // As the flunky service does a module 10 over a random integer between 0 and 100, a test set of 1000 should hit both.
        int resultSize = 1000;
        Map<Integer, Boolean> results = new HashMap<>(resultSize);

        for (int i = 0; i < resultSize; i++) {
            boolean result;
            try {
                result = testSubject.notifyGameAvailability(GAME_IDENTIFIER);
            } catch (RuntimeException e) {
                result = false;
            }
            results.put(i, result);
        }

        assertTrue(results.containsValue(true));
        assertTrue(results.containsValue(false));
    }
}