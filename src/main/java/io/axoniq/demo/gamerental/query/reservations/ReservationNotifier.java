package io.axoniq.demo.gamerental.query.reservations;

import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("reservations")
@Component
class ReservationNotifier {

    private final ReservationService reservationService;

    ReservationNotifier(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @EventHandler
    public void on(GameReturnedEvent event) {
        reservationService.notifyGameAvailability(event.gameIdentifier());
    }
}
