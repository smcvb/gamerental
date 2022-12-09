package io.axoniq.demo.gamerental;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.deadletter.DeadLetter;
import org.axonframework.messaging.deadletter.EnqueueDecision;
import org.axonframework.messaging.deadletter.GenericDeadLetter;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RetryConstrainedEnqueuePolicyTest {

    private static final int MAX_RETRIES = 5;
    private static final EventMessage<Object> TEST_EVENT_MESSAGE = GenericEventMessage.asEventMessage("some-event");
    private static final String TEST_SEQUENCE_IDENTIFIER = "seqId";

    private RetryConstrainedEnqueuePolicy testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new RetryConstrainedEnqueuePolicy(MAX_RETRIES);
    }

    @Test
    void decidesToEnqueueWhenThereAreNoRetriesYet() {
        RuntimeException expectedCause = new RuntimeException("some-cause");
        DeadLetter<EventMessage<Object>> testLetter =
                new GenericDeadLetter<>(TEST_SEQUENCE_IDENTIFIER, TEST_EVENT_MESSAGE);

        validateShouldEnqueueDecision(testLetter, expectedCause, 0);
    }

    @Test
    void decidesToEnqueueWhenThereAreLessThenTheConfiguredRetryCount() {
        RuntimeException expectedCause = new RuntimeException("some-cause");

        for (int i = 1; i < MAX_RETRIES - 1; i++) {
            DeadLetter<EventMessage<Object>> testLetter =
                    new GenericDeadLetter<>(TEST_SEQUENCE_IDENTIFIER, TEST_EVENT_MESSAGE)
                            .withDiagnostics(MetaData.with("retries", i));

            validateShouldEnqueueDecision(testLetter, expectedCause, i + 1);
        }
    }

    private void validateShouldEnqueueDecision(DeadLetter<EventMessage<Object>> letter,
                                               RuntimeException expectedCause,
                                               int expectedNumberOfRetries) {
        EnqueueDecision<EventMessage<?>> result = testSubject.decide(letter, expectedCause);

        assertTrue(result.shouldEnqueue());
        Optional<Throwable> resultCause = result.enqueueCause();
        assertTrue(resultCause.isPresent());
        assertEquals(expectedCause, resultCause.get());
        MetaData resultDiagnostics = result.withDiagnostics(letter).diagnostics();
        assertTrue(resultDiagnostics.containsKey("retries"));
        assertEquals(expectedNumberOfRetries, resultDiagnostics.get("retries"));
    }

    @Test
    void decidesToEvictWhenMaximumAmountOfRetriesIsReached() {
        RuntimeException expectedCause = new RuntimeException("some-cause");
        DeadLetter<EventMessage<Object>> testLetter =
                new GenericDeadLetter<>(TEST_SEQUENCE_IDENTIFIER, TEST_EVENT_MESSAGE)
                        .withDiagnostics(MetaData.with("retries", MAX_RETRIES));

        EnqueueDecision<EventMessage<?>> result = testSubject.decide(testLetter, expectedCause);

        assertFalse(result.shouldEnqueue());
        assertFalse(result.enqueueCause().isPresent());
        MetaData resultDiagnostics = result.withDiagnostics(testLetter).diagnostics();
        assertEquals(testLetter.diagnostics(), resultDiagnostics);
    }
}