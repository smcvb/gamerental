package io.axoniq.demo.gamerental.query.reservations;

import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("reservations")
@Component
@Namespace("reservations")
class ReservationNotifier {

    private final ReservationService reservationService;

    ReservationNotifier(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @EventHandler(eventName = "game-rental.returned")
    public void on(GameReturnedEvent event) {
        reservationService.notifyGameAvailability(event.gameIdentifier());
    }
}
