package io.axoniq.demo.gamerental.query.reservations;

interface ReservationService {

    boolean notifyGameAvailability(String gameId);
}
