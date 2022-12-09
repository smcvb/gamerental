package io.axoniq.demo.gamerental.query.reservations;

import io.axoniq.demo.gamerental.coreapi.GameReturnedEvent;
import org.junit.jupiter.api.*;

import static io.axoniq.demo.gamerental.TestUtils.GAME_IDENTIFIER;
import static io.axoniq.demo.gamerental.TestUtils.RENTER;
import static org.mockito.Mockito.*;

class ReservationNotifierTest {

    private ReservationService reservationService;

    private ReservationNotifier testSubject;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);

        testSubject = new ReservationNotifier(reservationService);
    }

    @Test
    void invokesReservationServiceWhenHandlingGameReturnedEvent() {
        testSubject.on(new GameReturnedEvent(GAME_IDENTIFIER, RENTER));

        verify(reservationService).notifyGameAvailability(GAME_IDENTIFIER);
    }
}