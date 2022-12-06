package io.axoniq.demo.gamerental.query.reservations;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;

@Profile("dead-letters")
@Component
class FlunkyReservationService implements ReservationService {

    private final Random random = new Random();

    @Override
    public boolean notifyGameAvailability(String gameId) {
        int randomInt = random.nextInt(100);
        if (randomInt % 10 == 0) {
            throw new RuntimeException("Cannot reach the flunky reservation service...");
        }
        return randomInt % 2 == 0;
    }
}
